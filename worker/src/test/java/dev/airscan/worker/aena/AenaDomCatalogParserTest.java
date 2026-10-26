package dev.airscan.worker.aena;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AenaDomCatalogParserTest {
  @TempDir Path temp;

  private String fixture() throws Exception {
    try (var input =
        getClass().getResourceAsStream("/aena/vlc-browser-table-structural-example.html")) {
      if (input == null) {
        throw new IllegalStateException("Missing HTML example");
      }
      return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  @Test
  void parsesVisibleDestinationRowsAndSeparatesListedCarrierEvidence() throws Exception {
    var parser = new AenaDomCatalogParser();
    var result = parser.parse(fixture());
    assertEquals(4, result.advertisedCount());
    assertEquals(4, result.sourceCandidateRows());
    assertEquals(4, result.routes().size());
    assertEquals(0, result.rejectedRows());
    assertEquals(0, result.duplicateRows());
    assertEquals("COMPLETE", result.coverage());
    assertTrue(result.reconciled());
    assertEquals(result, parser.parse(fixture()));
    assertEquals("DIRECT:VLC:TSF", result.routes().get(3).routeId());
    assertEquals("UNKNOWN", result.routes().get(2).listedCarriers().get(0));
    assertEquals(2, result.routes().get(0).listedCarriers().size());
  }

  @Test
  void damagedAndDuplicateRowsNeverBecomeACompleteCatalog() throws Exception {
    String html =
        fixture()
            .replace(
                "<tr><td>BILBAO (BIO)</td>",
                "<tr><td>NO AIRPORT CODE</td>")
            .replace(
                "</tbody>",
                "<tr><td>AMSTERDAM /SCHIPHOL (AMS)</td><td>HOLANDA</td><td><a>VOLOTEA</a></td></tr></tbody>");
    var result = new AenaDomCatalogParser().parse(html);
    assertEquals(5, result.sourceCandidateRows());
    assertEquals(3, result.routes().size());
    assertEquals(1, result.duplicateRows());
    assertEquals(1, result.rejectedRows());
    assertTrue(result.reconciled());
    assertFalse(result.publishable());
    assertEquals("PARTIAL", result.coverage());
  }

  @Test
  void unknownMarkupFailsClosed() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new AenaDomCatalogParser().parse("<html><body>Hello</body></html>"));
    var changed =
        new AenaDomCatalogParser()
            .parse(
                "<main><h1>Valencia Airport destinations</h1>"
                    + "<p>Valencia Airport has a total of 4 destinations served by 3 airlines</p>"
                    + "<section>New structure without cards</section></main>");
    assertFalse(changed.publishable());
    assertEquals(0, changed.sourceCandidateRows());
  }

  @Test
  void cliPublishesImmutableHistoryAndNeverReplacesGoodLatestWithBadSnapshot()
      throws Exception {
    Path input = temp.resolve("saved.html");
    Path output = temp.resolve("catalog");
    Files.writeString(input, fixture());
    var first =
        AenaCatalogImportCli.importFile(input, Instant.parse("2026-10-26T12:00:00Z"));
    Path immutable = AenaCatalogImportCli.publish(output, first);
    assertTrue(Files.exists(immutable));
    byte[] previous = Files.readAllBytes(output.resolve("latest.json"));
    assertEquals("COMPLETE", first.report().coverage());
    assertEquals(
        immutable,
        AenaCatalogImportCli.publish(output, first));
    assertTrue(java.util.Arrays.equals(previous, Files.readAllBytes(output.resolve("latest.json"))));
    Files.writeString(
        input,
        fixture().replace("total of 4 destinations", "total of 5 destinations"));
    var second =
        AenaCatalogImportCli.importFile(input, Instant.parse("2026-10-27T12:00:00Z"));
    Path partial = AenaCatalogImportCli.publish(output, second);
    assertTrue(Files.exists(partial));
    assertEquals("PARTIAL", second.report().coverage());
    assertTrue(java.util.Arrays.equals(previous, Files.readAllBytes(output.resolve("latest.json"))));
  }
  @Test
  void parsesSourceLabelledCardListAndExcludesDestinationPicker() {
    String html =
        "<main><h1>Valencia Airport destinations</h1>"
            + "<p>Valencia Airport has a total of 2 destinations served by 2 airlines</p>"
            + "<div><select><option>AMSTERDAM (AMS)</option></select></div>"
            + "<ul><li><span>Airport destination</span></li>"
            + "<li><a>AMSTERDAM (AMS)</a>"
            + "<div><span>Country</span> NETHERLANDS</div>"
            + "<div><span>Airlines</span><a>TRANSAVIA</a><a>KLM</a></div></li>"
            + "<li><a>BILBAO (BIO)</a>"
            + "<div><span>Country</span> SPAIN</div>"
            + "<div><span>Airlines</span></div></li></ul>"
            + "</main>";
    var result = new AenaDomCatalogParser().parse(html);
    assertEquals(2, result.sourceCandidateRows());
    assertTrue(result.publishable());
    assertEquals(2, result.routes().size());
    assertEquals("DIRECT:VLC:AMS", result.routes().get(0).routeId());
    assertEquals("UNKNOWN", result.routes().get(1).listedCarriers().get(0));
  }

  @Test
  void endToEndMainInvocationSavesStableAcceptedJson() throws Exception {
    Path input = temp.resolve("saved-from-browser.html");
    Path output = temp.resolve("manual-catalog");
    Files.writeString(input, fixture());
    AenaCatalogImportCli.main(
        new String[] {
          "--input", input.toString(),
          "--output", output.toString(),
          "--captured-at", "2026-10-26T13:00:00Z"
        });
    String saved = Files.readString(output.resolve("latest.json"));
    assertTrue(saved.contains("DIRECT:VLC:AMS"));
    assertTrue(saved.contains("aena-manual-dom-v1"));
    assertTrue(saved.contains("sourceSha256"));
    assertTrue(saved.contains("2026-10-26T13:00:00Z"));
  }

}
