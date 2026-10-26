package dev.airscan.worker.aena;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a manually sanitized text extract of publicly displayed Aena destination facts.
 * NOT a live HTML/DOM adapter: RT-2 must separately verify the actual DOM and legally
 * permitted acquisition before using this normalization stage for ingestion.
 */
public final class AenaSanitizedExcerptParser {
  private static final Pattern DESTINATION =
      Pattern.compile("^Destination: .+?\\(([A-Za-z]{3})\\)(?: \\([A-Za-z]{3}\\))?$");
  private static final String DESTINATION_PREFIX = "Destination:";
  private static final String CARRIERS_PREFIX = "Carriers:";
  private static final String UNKNOWN = "UNKNOWN";

  public record ParsedRoute(String routeKey, List<String> carrierNames) {}

  public record ParseReport(
      List<ParsedRoute> routes,
      int sourceRows,
      int duplicateDestinationRows,
      int rejectedDestinationRows,
      List<String> omissions) {
    public ParseReport {
      routes = List.copyOf(routes);
      omissions = List.copyOf(omissions);
    }

    public boolean hasGaps() {
      return !omissions.isEmpty();
    }

    /** Reconciles only candidate lines recognized by this synthetic excerpt grammar. */
    public boolean candidateRowsReconciled() {
      return sourceRows == routes.size() + duplicateDestinationRows + rejectedDestinationRows;
    }

    /** Cannot assert full source coverage when a changed/noncandidate line was skipped. */
    public boolean coverageIsKnown() {
      return candidateRowsReconciled() && !hasGaps();
    }
  }

  public ParseReport parse(String text, String origin) {
    if (text == null || !text.startsWith("# AENA_VLC_SANITIZED_EXCERPT_V1\n")) {
      throw new IllegalArgumentException("Unsupported or changed extract structure");
    }
    if (!"VLC".equalsIgnoreCase(origin)) {
      throw new IllegalArgumentException("Only the VLC RT-1 extract is supported");
    }
    Map<String, TreeSet<String>> canonical = new TreeMap<>();
    List<String> omissions = new ArrayList<>();
    String current = null;
    int rows = 0;
    int duplicates = 0;
    int rejected = 0;
    int lineNo = 0;
    for (String raw : text.split("\\R")) {
      lineNo++;
      String line = raw.strip();
      if (line.isBlank() || line.startsWith("#")) {
        continue;
      }
      if (line.startsWith(DESTINATION_PREFIX)) {
        rows++;
        Matcher matcher = DESTINATION.matcher(line);
        if (!matcher.matches()) {
          omissions.add("line " + lineNo + ": invalid destination identity");
          rejected++;
          current = null;
          continue;
        }
        String destination = matcher.group(1).toUpperCase(Locale.ROOT);
        if ("VLC".equals(destination)) {
          omissions.add("line " + lineNo + ": self route");
          rejected++;
          current = null;
          continue;
        }
        current = "DIRECT:VLC:" + destination;
        if (canonical.containsKey(current)) {
          duplicates++;
        }
        canonical.computeIfAbsent(current, ignored -> new TreeSet<>());
        continue;
      }
      if (line.startsWith(CARRIERS_PREFIX)) {
        if (current == null) {
          omissions.add("line " + lineNo + ": orphan carrier data");
          continue;
        }
        String names = line.substring(CARRIERS_PREFIX.length()).strip();
        if (names.isEmpty()) {
          canonical.get(current).add(UNKNOWN);
        } else {
          for (String name : names.split("\\|")) {
            String normalized = name.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
            if (!normalized.isEmpty()) {
              canonical.get(current).add(normalized);
            }
          }
        }
        continue;
      }
      omissions.add("line " + lineNo + ": changed extract structure");
    }
    List<ParsedRoute> routes = new ArrayList<>();
    for (var entry : canonical.entrySet()) {
      if (entry.getValue().isEmpty()) {
        entry.getValue().add(UNKNOWN);
      }
      if (entry.getValue().size() > 1) {
        entry.getValue().remove(UNKNOWN);
      }
      routes.add(new ParsedRoute(entry.getKey(), List.copyOf(entry.getValue())));
    }
    if (rows == 0) {
      omissions.add("No destination rows recognized");
    }
    return new ParseReport(routes, rows, duplicates, rejected, omissions);
  }
}
