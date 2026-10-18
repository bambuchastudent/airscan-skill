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
    val version = "graphifyy-0.1.14-java-ast-v1"
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
result=extract(files)
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
            }
            val report = Json.parseToJsonElement(Files.readString(manifest)).jsonObject
            println(buildJsonObject {
                put("cache_hit", hit)
                put("cache_dir", cache.toString())
                for ((name, value) in report) put(name, value)
            })
        }
    }
}
main(args)
