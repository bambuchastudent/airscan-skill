package dev.airscan.worker.aena;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

/** Manual CLI import: no requests, timers, worker HTTP protocol, or Firestore writes. */
public final class AenaCatalogImportCli {
  private static final String SCHEMA = "aena-manual-dom-v1";

  public record Snapshot(
      String schemaVersion,
      String sourceId,
      String sourceUrl,
      String capturedAt,
      String sourceSha256,
      AenaDomCatalogParser.Report report) {}

  private AenaCatalogImportCli() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 6
        || !"--input".equals(args[0])
        || !"--output".equals(args[2])
        || !"--captured-at".equals(args[4])) {
      throw new IllegalArgumentException(
          "Usage: --input saved-aena.html --output directory --captured-at 2026-10-26T12:00:00Z");
    }
    Snapshot snapshot = importFile(Path.of(args[1]), Instant.parse(args[5]));
    Path file = publish(Path.of(args[3]), snapshot);
    System.out.println("Snapshot: " + file);
    System.out.println(
        "Routes: "
            + snapshot.report().routes().size()
            + ", coverage: "
            + snapshot.report().coverage());
    if (!snapshot.report().publishable()) {
      throw new IllegalStateException("Incomplete snapshot: latest.json was NOT replaced");
    }
  }

  public static Snapshot importFile(Path input, Instant capturedAt) throws IOException {
    byte[] bytes = Files.readAllBytes(input);
    if (bytes.length == 0 || bytes.length > 10_000_000) {
      throw new IllegalArgumentException("HTML input empty or exceeds 10 MB");
    }
    String html = new String(bytes, StandardCharsets.UTF_8);
    var report = new AenaDomCatalogParser().parse(html);
    return new Snapshot(
        SCHEMA,
        "aena-vlc",
        report.sourceUrl(),
        capturedAt.toString(),
        sha256(bytes),
        report);
  }

  /** Immutable per-capture file; publish latest.json only after a complete, validated parse. */
  public static Path publish(Path directory, Snapshot snapshot) throws IOException {
    Files.createDirectories(directory);
    String id = snapshot.capturedAt().replace(':', '-');
    String suffix = snapshot.sourceSha256().substring(0, 12);
    Path permanent = directory.resolve("vlc-" + id + "-" + suffix + ".json");
    var mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    byte[] json = mapper.writeValueAsBytes(snapshot);
    if (Files.exists(permanent)) {
      if (!java.util.Arrays.equals(json, Files.readAllBytes(permanent))) {
        throw new IOException("Existing immutable snapshot identity collision");
      }
    } else {
      Files.write(permanent, json);
    }
    if (!snapshot.report().publishable()) {
      return permanent;
    }
    Path temp = Files.createTempFile(directory, ".vlc-latest-", ".tmp");
    try {
      Files.write(temp, json);
      try {
        Files.move(
            temp,
            directory.resolve("latest.json"),
            StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException error) {
        throw new IOException("Filesystem cannot atomically publish latest.json", error);
      }
    } finally {
      Files.deleteIfExists(temp);
    }
    return permanent;
  }

  private static String sha256(byte[] bytes) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (NoSuchAlgorithmException error) {
      throw new IllegalStateException("SHA-256 unavailable", error);
    }
  }
}
