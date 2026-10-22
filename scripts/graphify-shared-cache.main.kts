#!/usr/bin/env kotlin
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import kotlinx.serialization.json.*

fun sha(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
    .digest(bytes).joinToString("") { "%02x".format(it) }

fun writeAtomic(path: Path, value: String) {
    val temp = Files.createTempFile(path.parent, ".graphify-", ".tmp")
    try {
        Files.writeString(temp, value)
        Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    } finally {
        Files.deleteIfExists(temp)
    }
}

fun main(args: Array<String>) {
    fun option(name: String): String? = args.indexOf(name).takeIf { it >= 0 }?.let {
        require(it + 1 < args.size) { "Missing value for $name" }
        args[it + 1]
    }
    val root = Path.of(option("--root") ?: ".").toRealPath()
    val key = sha(root.toString().toByteArray()).take(16)
    val cache = Path.of(option("--cache-dir")
        ?: (System.getProperty("user.home") + "/.cache/airscan/graphify/" + key))
        .toAbsolutePath().normalize()
    Files.createDirectories(cache)
    val files = Files.walk(root).use { stream ->
        stream.filter { Files.isRegularFile(it) && it.toString().endsWith(".java") &&
            (it.startsWith(root.resolve("src")) || it.startsWith(root.resolve("worker/src"))) }
            .sorted().toList()
    }
    require(files.isNotEmpty()) { "No Java files" }
    val version = "graphifyy-0.9.92-java-ast-v2"
    val fingerprint = sha(buildString {
        append(version)
        for (file in files) {
            append(root.relativize(file))
            append(sha(Files.readAllBytes(file)))
        }
    }.toByteArray())
    val graph = cache.resolve("graph.json")
    val manifest = cache.resolve("manifest.json")
    FileChannel.open(cache.resolve("index.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE).use { channel ->
        channel.lock().use {
            val previous = if (Files.exists(manifest)) runCatching {
                Json.parseToJsonElement(Files.readString(manifest)).jsonObject
            }.getOrNull() else null
            val hit = previous?.get("fingerprint")?.jsonPrimitive?.content == fingerprint && Files.exists(graph)
            if (!hit) {
                val python = option("--python") ?: "python3"
                val code = """
import json,sys
from pathlib import Path
from graphify.extract import extract
root=Path(sys.argv[1])
files=[root / name for name in sys.argv[2:]]
result=extract(files, root=root, parallel=False)
assert result.get('nodes'), 'Empty graph'
print(json.dumps(result))
""".trimIndent()
                val command = listOf(python, "-c", code, root.toString()) +
                    files.map { root.relativize(it).toString() }
                val process = ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.INHERIT).start()
                val output = process.inputStream.bufferedReader().readText()
                check(process.waitFor() == 0) { "Graphify extraction failed" }
                val result = Json.parseToJsonElement(output).jsonObject
                val nodes = result["nodes"]!!.jsonArray.size
                val edges = result["edges"]!!.jsonArray.size
                check(output.contains("BootstrapResource") && output.contains("BootstrapMarker"))
                // Graphify's extracted edges, not merely text matches, must prove
                // that BootstrapResource.status() calls BootstrapMarker.status().
                val nodeObjects = result["nodes"]!!.jsonArray.associate { it.jsonObject["id"]!!.jsonPrimitive.content to it.jsonObject }
                val labels = result["nodes"]!!.jsonArray.associate {
                    it.jsonObject["id"]!!.jsonPrimitive.content to
                        (it.jsonObject["label"]?.jsonPrimitive?.content ?: "")
                }
                val relationships = result["edges"]!!.jsonArray.map { it.jsonObject }
                val related = relationships.filter { edge ->
                    val source = edge["source"]?.jsonPrimitive?.content ?: ""
                    val target = edge["target"]?.jsonPrimitive?.content ?: ""
                    val sourceLabel = labels[source] ?: source
                    val targetLabel = labels[target] ?: target
                    (sourceLabel.contains("status") || source.contains("status")) &&
                    (targetLabel.contains("status") || target.contains("status")) &&
                    (nodeObjects[source]?.get("source_file")?.toString()?.contains("BootstrapResource.java") == true ||
                        source.contains("BootstrapResource")) &&
                    (nodeObjects[target]?.get("source_file")?.toString()?.contains("BootstrapMarker.java") == true ||
                        target.contains("BootstrapMarker")) &&
                    edge["relation"]?.jsonPrimitive?.content == "calls" &&
                    source != target
                }
                check(related.isNotEmpty()) {
                    "Graphify relationship missing: status() call between bootstrap symbols"
                }
                // Iteration 1: both transport adapters must reach the same
                // application service, without an adapter-to-adapter dependency.
                val applicationEdges = relationships.filter { edge ->
                    val source = edge["source"]?.jsonPrimitive?.content?.lowercase() ?: ""
                    val target = edge["target"]?.jsonPrimitive?.content?.lowercase() ?: ""
                    edge["relation"]?.jsonPrimitive?.content == "calls" &&
                        target.contains("capabilitiesservice") && target.endsWith("_current") &&
                        (source.contains("capabilitiesresource") || source.contains("mcpcapabilitiesadapter"))
                }
                check(applicationEdges.any {
                    it["source"]?.jsonPrimitive?.content?.lowercase()?.contains("capabilitiesresource") == true
                }) { "Graphify: REST adapter -> CapabilitiesService.current call not found" }
                check(applicationEdges.any {
                    it["source"]?.jsonPrimitive?.content?.lowercase()?.contains("mcpcapabilitiesadapter") == true
                }) { "Graphify: MCP adapter -> CapabilitiesService.current call not found" }
                // Iteration 2 graph inspection: persistence symbols and real
                // new-method call edges must be present in the extracted AST graph.
                val persistenceSymbols = listOf(
                    "CentralOperationalStore", "CentralRecordService",
                    "FirestoreCentralOperationalStore", "ObservationArchive",
                    "LocalGzipObservationArchive", "SqliteOutbox")
                for (symbol in persistenceSymbols) {
                    check(output.contains(symbol)) { "Graphify missing persistence symbol: $symbol" }
                }
                val persistenceEdges = relationships.filter { edge ->
                    val source = edge["source"]?.jsonPrimitive?.content?.lowercase() ?: ""
                    edge["relation"]?.jsonPrimitive?.content == "calls" &&
                        (source.contains("centralrecordservice") ||
                            source.contains("sqliteoutbox") ||
                            source.contains("localgzipobservationarchive"))
                }
                check(persistenceEdges.any {
                    it["source"]?.jsonPrimitive?.content?.lowercase()?.contains("centralrecordservice") == true
                }) { "Graphify missing application persistence call edge" }
                check(persistenceEdges.any {
                    it["source"]?.jsonPrimitive?.content?.lowercase()?.contains("sqliteoutbox") == true
                }) { "Graphify missing SQLite outbox call edge" }
                check(persistenceEdges.any {
                    it["source"]?.jsonPrimitive?.content?.lowercase()?.contains("localgzipobservationarchive") == true
                }) { "Graphify missing local archive call edge" }
                // Local POC: inspect extracted call edges for both protocol layers.
                // Preserve the exact graph nodes for independent review, not only file grep.
                val pocSymbols = listOf(
                    "WorkerProtocolResource", "WorkerProtocolService",
                    "WorkerProtocolStore", "FirestoreWorkerProtocolStore",
                    "WorkerPocClient", "LocalTokenWorkerIdentityVerifier")
                for (symbol in pocSymbols) {
                    check(output.contains(symbol)) { "Graphify missing worker POC symbol: $symbol" }
                }
                val pocEdges = relationships.filter { edge ->
                    val source = edge["source"]?.jsonPrimitive?.content?.lowercase() ?: ""
                    edge["relation"]?.jsonPrimitive?.content == "calls" &&
                        (source.contains("workerprotocolresource") ||
                            source.contains("workerprotocolservice") ||
                            source.contains("workerpocclient") ||
                            source.contains("firestoreworkerprotocolstore"))
                }
                for (owner in listOf(
                    "workerprotocolresource", "workerprotocolservice",
                    "workerpocclient", "firestoreworkerprotocolstore")) {
                    check(pocEdges.any { edge ->
                        edge["source"]?.jsonPrimitive?.content?.lowercase()?.contains(owner) == true
                    }) { "Graphify missing calls originating at $owner" }
                }
                val relationshipEvidence = buildJsonObject {
                    put("query", "status() -> status() (distinct Graphify graph nodes)")
                    put("matched_edges", JsonArray(related))
                    put("application_service_edges", JsonArray(applicationEdges))
                    put("persistence_symbols", JsonArray(persistenceSymbols.map { JsonPrimitive(it) }))
                    put("persistence_edges", JsonArray(persistenceEdges))
                    put("poc_symbols", JsonArray(pocSymbols.map { JsonPrimitive(it) }))
                    put("poc_edges", JsonArray(pocEdges))
                    put("pass", true)
                }
                writeAtomic(cache.resolve("relationship.json"), relationshipEvidence.toString())
                writeAtomic(graph, output)
                val metadata = buildJsonObject {
                    put("version", version)
                    put("fingerprint", fingerprint)
                    put("root", root.toString())
                    put("files", files.size)
                    put("nodes", nodes)
                    put("edges", edges)
                    put("update_strategy", "content-hash-skip-or-full-ast-rebuild")
                }
                writeAtomic(manifest, metadata.toString())
            }
            val evidence = option("--output")
            if (evidence != null) {
                val dest = Path.of(evidence)
                Files.createDirectories(dest)
                Files.copy(graph, dest.resolve("graph.json"), StandardCopyOption.REPLACE_EXISTING)
                Files.copy(manifest, dest.resolve("manifest.json"), StandardCopyOption.REPLACE_EXISTING)
                Files.copy(cache.resolve("relationship.json"), dest.resolve("relationship.json"), StandardCopyOption.REPLACE_EXISTING)
            }
            val report = Json.parseToJsonElement(Files.readString(manifest)).jsonObject
            check(Files.exists(cache.resolve("relationship.json"))) { "Relationship evidence missing" }
            println(buildJsonObject {
                put("cache_hit", hit)
                put("cache_dir", cache.toString())
                for ((name, value) in report) put(name, value)
            })
        }
    }
}
main(args)
