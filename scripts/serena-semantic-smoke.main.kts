#!/usr/bin/env kotlin
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

// MCP stdio framing uses newline-delimited JSON-RPC; tool results are checked,
// not substituted with lexical grep. Requires Python mcp + Serena installation.
val root = Path.of(".").toAbsolutePath().normalize()
val evidence = root.resolve("serena-evidence")
Files.createDirectories(evidence)
val py = """
import asyncio,json,os
from pathlib import Path
from mcp import ClientSession, StdioServerParameters
from mcp.client.stdio import stdio_client

async def main():
    root=str(Path.cwd())
    params=StdioServerParameters(command="serena",args=[
        "start-mcp-server","--project",root,"--agent-interface","tools",
        "--transport","stdio"])
    async with stdio_client(params) as (reader,writer):
        async with ClientSession(reader,writer) as session:
            await session.initialize()
            available={t.name for t in (await session.list_tools()).tools}
            required={"find_symbol","find_referencing_symbols"}
            assert required <= available, f"Missing semantic tools: {required-available}"
            async def invoke(name,arguments):
                response=await session.call_tool(name,arguments)
                text="\n".join(getattr(c,"text","") for c in response.content)
                assert not response.is_error, f"{name}: {text}"
                return text
            symbols=await invoke("find_symbol",{
                "name_path_pattern":"BootstrapResource","relative_path":"src",
                "include_body":True,"substring_matching":True})
            assert "BootstrapResource" in symbols and "status" in symbols,symbols
            marker=await invoke("find_symbol",{
                "name_path_pattern":"BootstrapMarker/status","include_body":True,
                "substring_matching":True})
            assert "status" in marker,marker
            references=await invoke("find_referencing_symbols",{
                "name_path":"BootstrapMarker/status",
                "relative_path":"src/main/java/dev/airscan/bootstrap/BootstrapMarker.java"})
            assert "BootstrapResource" in references,references
            application_symbol=await invoke("find_symbol",{
                "name_path_pattern":"CapabilitiesService/current",
                "relative_path":"src","include_body":True,
                "substring_matching":True})
            assert "current" in application_symbol,application_symbol
            application_refs=await invoke("find_referencing_symbols",{
                "name_path":"CapabilitiesService/current",
                "relative_path":"src/main/java/dev/airscan/application/CapabilitiesService.java"})
            assert "CapabilitiesResource" in application_refs,application_refs
            assert "McpCapabilitiesAdapter" in application_refs,application_refs
            persistence_symbols={}
            for name in ("CentralOperationalStore","CentralRecordService",
                         "FirestoreCentralOperationalStore","ObservationArchive"):
                result=await invoke("find_symbol",{
                    "name_path_pattern":name,"relative_path":"src",
                    "include_body":False,"substring_matching":True})
                assert name in result,result
                persistence_symbols[name]=result
            persistence_references=await invoke("find_referencing_symbols",{
                "name_path":"CentralRecordService/createSynthetic",
                "relative_path":"src/main/java/dev/airscan/application/CentralRecordService.java"})
            assert "CentralRecordServiceTest" in persistence_references,persistence_references
            assert "FirestoreCentralOperationalStoreIT" in persistence_references,persistence_references
            Path("serena-evidence/semantic.json").write_text(json.dumps({
                "tools":sorted(required),"resource":symbols,
                "marker_status":marker,"references":references,
                "application_service":application_symbol,
                "application_references":application_refs,
                "persistence_symbols":persistence_symbols,
                "persistence_references":persistence_references},indent=2))
            print("Serena Java semantic lookup PASS; Iteration 1 adapters and Iteration 2 persistence symbols/references PASS")
asyncio.run(main())
""".trimIndent()
val tmp=Files.createTempFile("airscan-serena-", ".py")
try {
    Files.writeString(tmp,py)
    val process=ProcessBuilder("python3",tmp.toString()).directory(root.toFile())
        .inheritIO().start()
    check(process.waitFor(240,TimeUnit.SECONDS)) { "Serena timed out" }
    check(process.exitValue()==0) { "Serena semantic smoke failed" }
} finally { Files.deleteIfExists(tmp) }
