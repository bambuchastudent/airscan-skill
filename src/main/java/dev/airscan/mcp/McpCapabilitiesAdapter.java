package dev.airscan.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.airscan.application.CapabilitiesService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * MCP tools/list + tools/call dispatch boundary, without transport or JSON-RPC session handling.
 * Neither REST nor this adapter owns application semantics.
 */
@ApplicationScoped
public class McpCapabilitiesAdapter {
  public static final String TOOL_NAME = "airscan.capabilities";

  private final CapabilitiesService capabilities;
  private final ObjectMapper json;

  @Inject
  public McpCapabilitiesAdapter(CapabilitiesService capabilities, ObjectMapper json) {
    this.capabilities = capabilities;
    this.json = json;
  }

  public List<ToolDefinition> listTools() {
    return List.of(
        new ToolDefinition(
            TOOL_NAME,
            "Read the same system capabilities exposed by the versioned REST API",
            Map.of("type", "object", "properties", Map.of(), "additionalProperties", false)));
  }

  public ToolResult callTool(String name, Map<String, ?> arguments) {
    if (!TOOL_NAME.equals(name) || arguments == null || !arguments.isEmpty()) {
      throw new IllegalArgumentException("Unsupported MCP tool or arguments");
    }
    try {
      return new ToolResult(
          List.of(new TextContent("text", json.writeValueAsString(capabilities.current()))), false);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize application response", e);
    }
  }

  public record ToolDefinition(String name, String description, Map<String, Object> inputSchema) {}

  public record TextContent(String type, String text) {}

  public record ToolResult(List<TextContent> content, boolean isError) {}
}
