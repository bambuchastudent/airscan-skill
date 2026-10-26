package dev.airscan.worker.aena;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Offline, manually supplied rendered DOM parser. No network access or crawler schedule.
 * Treats unknown DOM structures as incomplete rather than inferring absent routes.
 */
public final class AenaDomCatalogParser {
  private static final Pattern IATA = Pattern.compile("\\(([A-Za-z]{3})\\)");
  private static final Pattern COUNT =
      Pattern.compile(
          "(?i)(?:total\\s+of|un\\s+total\\s+de)\\s+(\\d+)\\s+(?:destinations|destinos)");
  private static final Pattern HEADING =
      Pattern.compile("(?i)^(?:airport destination|aeropuerto destino)$");
  private static final Pattern AIRLINES =
      Pattern.compile("(?i)^(?:airlines|aerol[ií]neas)$");
  private static final String SOURCE = "https://www.aena.es/en/valencia/airlines-destinations/airport-destinations.html";

  public record Route(String routeId, String destination, List<String> listedCarriers) {}

  public record Report(
      String sourceUrl,
      Integer advertisedCount,
      int sourceCandidateRows,
      int duplicateRows,
      int rejectedRows,
      List<Route> routes,
      List<String> diagnostics,
      String coverage) {
    public Report {
      routes = List.copyOf(routes);
      diagnostics = List.copyOf(diagnostics);
    }

    public boolean reconciled() {
      return sourceCandidateRows == routes.size() + duplicateRows + rejectedRows;
    }

    public boolean publishable() {
      return "COMPLETE".equals(coverage);
    }
  }

  public Report parse(String html) {
    if (html == null || html.isBlank()) {
      throw new IllegalArgumentException("Input HTML is empty");
    }
    Document doc = Jsoup.parse(html);
    Element scope = doc.selectFirst("main");
    if (scope == null) {
      scope = doc.body();
    }
    String pageText = scope.text();
    if (!pageText.toLowerCase(Locale.ROOT).contains("valencia")
        || !pageText.toLowerCase(Locale.ROOT).contains("destination")
            && !pageText.toLowerCase(Locale.ROOT).contains("destino")) {
      throw new IllegalArgumentException("Not an identifiable Aena Valencia destinations page");
    }
    Matcher total = COUNT.matcher(pageText);
    Integer advertised = total.find() ? Integer.parseInt(total.group(1)) : null;
    List<String> diagnostics = new ArrayList<>();
    if (advertised == null) {
      diagnostics.add("Source advertised destination count not found");
    }
    List<Element> candidates = new ArrayList<>();
    for (Element table : scope.select("table")) {
      boolean match =
          table.select("th").stream()
              .anyMatch(h -> HEADING.matcher(h.text().strip()).matches());
      if (match) {
        for (Element row : table.select("tr")) {
          if (!row.select("td").isEmpty()) {
            candidates.add(row);
          }
        }
      }
    }
    if (candidates.isEmpty()) {
      // The displayed Aena destination listing can be a card/list instead of a table.
      // Only accept containers with explicit same-card country/airlines fields.
      for (Element list : scope.select("ul,ol")) {
        for (Element li : list.children()) {
          if (!"li".equals(li.normalName())) {
            continue;
          }
          String text = li.text().toLowerCase(Locale.ROOT);
          if (text.contains("country") || text.contains("país")) {
            if (text.contains("airlines") || text.contains("aerolíneas")) {
              candidates.add(li);
            }
          }
        }
      }
    }
    if (candidates.isEmpty()) {
      diagnostics.add("No recognized destination table or complete cards in saved DOM");
    }
    Map<String, TreeSet<String>> routes = new TreeMap<>();
    int rejected = 0;
    int duplicates = 0;
    for (int i = 0; i < candidates.size(); i++) {
      Element row = candidates.get(i);
      Element destinationNode = null;
      if ("tr".equals(row.normalName())) {
        destinationNode = row.selectFirst("td");
      } else {
        destinationNode = row.selectFirst("a,span,strong,h2,h3,h4");
      }
      String destinationName = destinationNode == null ? "" : destinationNode.text();
      List<String> codes = new ArrayList<>();
      Matcher iata = IATA.matcher(destinationName);
      while (iata.find()) {
        codes.add(iata.group(1).toUpperCase(Locale.ROOT));
      }
      if (codes.isEmpty()) {
        diagnostics.add("candidate " + (i + 1) + ": missing destination IATA");
        rejected++;
        continue;
      }
      // Aena occasionally displays an intermediate city code followed by airport code:
      // e.g. TREVISO/S.ANGELO (MIL) (TSF). The rightmost code is the airport identity.
      String airport = codes.get(codes.size() - 1);
      if ("VLC".equals(airport)) {
        diagnostics.add("candidate " + (i + 1) + ": invalid self route");
        rejected++;
        continue;
      }
      if (codes.size() > 1 && !codes.get(0).equals(airport)) {
        diagnostics.add("candidate " + (i + 1) + ": city/airport pair " + codes);
      }
      if (routes.containsKey(airport)) {
        duplicates++;
      }
      TreeSet<String> carriers = routes.computeIfAbsent(airport, key -> new TreeSet<>());
      Element carriersField = null;
      if ("tr".equals(row.normalName())) {
        Elements cells = row.select("td");
        if (cells.size() >= 3) {
          carriersField = cells.get(2);
        }
      } else {
        for (Element child : row.select("div,section,span,p")) {
          if (AIRLINES.matcher(child.ownText().strip()).matches()) {
            carriersField = child.parent();
            break;
          }
        }
      }
      if (carriersField != null) {
        for (Element carrier : carriersField.select("a,li,[data-airline],img[alt]")) {
          String name = carrier.hasAttr("alt") ? carrier.attr("alt") : carrier.text();
          String normalized = name.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
          if (!normalized.isEmpty()) {
            carriers.add(normalized);
          }
        }
        if (carriers.isEmpty() && !carriersField.text().isBlank()) {
          String freeText = carriersField.text().replaceFirst("(?i)^(airlines|aerolíneas)\\s*", "").strip();
          if (!freeText.isEmpty()) {
            diagnostics.add("candidate " + (i + 1) + ": airline names not structurally separable");
          }
        }
      }
    }
    List<Route> normalized = new ArrayList<>();
    for (var entry : routes.entrySet()) {
      var carriers = entry.getValue().isEmpty() ? List.of("UNKNOWN") : List.copyOf(entry.getValue());
      normalized.add(new Route("DIRECT:VLC:" + entry.getKey(), entry.getKey(), carriers));
    }
    int seen = candidates.size();
    if (advertised != null && advertised != normalized.size()) {
      diagnostics.add(
          "Advertised count "
              + advertised
              + " differs from normalized distinct destinations "
              + normalized.size());
    }
    if (seen != normalized.size() + duplicates + rejected) {
      diagnostics.add("Source candidate reconciliation failed");
    }
    // A subset of a page (even valid cards) cannot claim to be the entire advertised network.
    String coverage =
        advertised != null
                && advertised == normalized.size()
                && seen == normalized.size() + duplicates + rejected
                && rejected == 0
                && diagnostics.isEmpty()
            ? "COMPLETE"
            : "PARTIAL";
    return new Report(SOURCE, advertised, seen, duplicates, rejected, normalized, diagnostics, coverage);
  }
}
