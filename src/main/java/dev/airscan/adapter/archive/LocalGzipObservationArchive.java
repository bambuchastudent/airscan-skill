package dev.airscan.adapter.archive;

import dev.airscan.application.port.ArchiveReference;
import dev.airscan.application.port.ObservationArchive;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** Local deterministic test adapter; GCS implementation is intentionally deferred. */
public final class LocalGzipObservationArchive implements ObservationArchive {
  private final Path root;

  public LocalGzipObservationArchive(Path root) {
    this.root = Objects.requireNonNull(root).toAbsolutePath().normalize();
  }

  @Override
  public ArchiveReference archive(String sourceId, String batchId, byte[] sanitizedPayload) {
    if (sourceId == null || sourceId.isBlank() || batchId == null || batchId.isBlank()) {
      throw new IllegalArgumentException("Source and batch identity are required");
    }
    Objects.requireNonNull(sanitizedPayload);
    if (sanitizedPayload.length > 64 * 1024) {
      throw new IllegalArgumentException("Synthetic archive payload exceeds 64 KiB");
    }
    String hash = sha256(sanitizedPayload);
    String key =
        "synthetic/"
            + sha256(sourceId.getBytes(StandardCharsets.UTF_8))
            + "/"
            + sha256(batchId.getBytes(StandardCharsets.UTF_8))
            + "/"
            + hash
            + ".json.gz";
    Path destination = root.resolve(key);
    try {
      Files.createDirectories(destination.getParent());
      byte[] compressed = gzip(sanitizedPayload);
      try {
        Files.write(destination, compressed, StandardOpenOption.CREATE_NEW);
      } catch (FileAlreadyExistsException existing) {
        if (!Arrays.equals(readFile(destination), sanitizedPayload)) {
          throw new IllegalStateException("Immutable archive object differs", existing);
        }
      }
      return new ArchiveReference(
          sourceId, batchId, hash, key, "gzip", sanitizedPayload.length, Files.size(destination));
    } catch (IOException e) {
      throw new IllegalStateException("Cannot store immutable archive object", e);
    }
  }

  public byte[] read(ArchiveReference reference) {
    if (!reference.objectKey().startsWith("synthetic/")
        || !reference.objectKey().endsWith("/" + reference.sha256() + ".json.gz")) {
      throw new IllegalArgumentException("Unrecognized archive reference");
    }
    Path path = root.resolve(reference.objectKey()).normalize();
    if (!path.startsWith(root)) {
      throw new IllegalArgumentException("Archive key escapes root");
    }
    try {
      byte[] data = readFile(path);
      if (!sha256(data).equals(reference.sha256())) {
        throw new IllegalStateException("Archive content hash mismatch");
      }
      return data;
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read archive object", e);
    }
  }

  private static byte[] readFile(Path path) throws IOException {
    try (var in = new GZIPInputStream(Files.newInputStream(path))) {
      return in.readAllBytes();
    }
  }

  private static byte[] gzip(byte[] value) throws IOException {
    var bytes = new ByteArrayOutputStream();
    try (var out = new GZIPOutputStream(bytes)) {
      out.write(value);
    }
    return bytes.toByteArray();
  }

  private static String sha256(byte[] value) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
