package dev.airscan.mcp;

import static io.restassured.RestAssured.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.airscan.application.CapabilitiesService;
import dev.airscan.application.CapabilitiesSnapshot;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
class McpCapabilitiesAdapterTest {
  @InjectMock CapabilitiesService capabilities;
  @Inject McpCapabilitiesAdapter adapter;
  @Inject ObjectMapper json;

  @Test
  void restAndMcpDispatchBothUseSameMockedApplicationSemantics() throws Exception {
    var sentinel = new CapabilitiesSnapshot("v1", List.of("synthetic.test-only-operation"));
    when(capabilities.current()).thenReturn(sentinel);

    var rest = get("/api/v1/capabilities").then().statusCode(200).extract().asString();
    var mcp = adapter.callTool(McpCapabilitiesAdapter.TOOL_NAME, Map.of());

    assertEquals(json.readTree(rest), json.readTree(mcp.content().getFirst().text()));
    assertEquals(json.valueToTree(sentinel), json.readTree(rest));
    assertFalse(mcp.isError());
    assertEquals("text", mcp.content().getFirst().type());
    verify(capabilities, times(2)).current();
  }

  @Test
  void toolContractRejectsUnadvertisedCalls() {
    var tool = adapter.listTools().getFirst();
    assertEquals(McpCapabilitiesAdapter.TOOL_NAME, tool.name());
    assertEquals("object", tool.inputSchema().get("type"));
    assertEquals(false, tool.inputSchema().get("additionalProperties"));
    assertThrows(IllegalArgumentException.class, () -> adapter.callTool("airscan.fares", Map.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> adapter.callTool(McpCapabilitiesAdapter.TOOL_NAME, Map.of("route", "VLC")));
  }
}
