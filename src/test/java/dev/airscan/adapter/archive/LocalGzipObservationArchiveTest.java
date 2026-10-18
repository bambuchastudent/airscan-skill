package dev.airscan.adapter.archive;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.airscan.application.port.ArchiveReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalGzipObservationArchiveTest {
  @TempDir Path directory;

  @Test
  void immutableCompressedSyntheticBatchRetainsProvenance() throws Exception {
    var archive = new LocalGzipObservationArchive(directory);
    byte[] original = "{\"value\":\"sanitized\"}".getBytes(StandardCharsets.UTF_8);
    var first = archive.archive("synthetic-source", "batch-1", original);
    byte[] compressed = Files.readAllBytes(directory.resolve(first.objectKey()));
    assertTrue(Files.isRegularFile(directory.resolve(first.objectKey())));
    assertEquals(
        HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(original)),
        first.sha256());
    var same = archive.archive("synthetic-source", "batch-1", original);
    assertEquals(first, same);
    assertArrayEquals(compressed, Files.readAllBytes(directory.resolve(same.objectKey())));
    assertArrayEquals(original, archive.read(first));
    assertEquals("synthetic-source", first.sourceId());
    assertEquals("batch-1", first.batchId());
    assertEquals("gzip", first.compression());
    assertEquals(original.length, first.originalBytes());
    assertTrue(first.compressedBytes() > 0);
    assertEquals(64, first.sha256().length());

    var changed =
        archive.archive(
            "synthetic-source", "batch-1", "{\"value\":2}".getBytes(StandardCharsets.UTF_8));
    assertNotEquals(first.objectKey(), changed.objectKey());
    assertArrayEquals(original, archive.read(first));
    assertThrows(IllegalArgumentException.class, () -> archive.archive("", "batch-1", original));
    assertNoStagingFiles();
  }

  @Test
  void corruptFinalObjectFailsClosedAndCleansStagingWithoutOverwriting() throws Exception {
    var archive = new LocalGzipObservationArchive(directory);
    byte[] payload = "{\"value\":\"test\"}".getBytes(StandardCharsets.UTF_8);
    var reference = archive.archive("source-corrupt", "batch-corrupt", payload);
    Path destination = directory.resolve(reference.objectKey());
    Files.delete(destination);
    byte[] truncated = new byte[] {0x1f, (byte) 0x8b, 0x08, 0x00};
    Files.write(destination, truncated);

    assertThrows(
        IllegalStateException.class,
        () -> archive.archive("source-corrupt", "batch-corrupt", payload));
    assertArrayEquals(truncated, Files.readAllBytes(destination));
    assertNoStagingFiles();
  }

  @Test
  void simultaneousSameContentWritersCannotOverwriteCompletedObject() throws Exception {
    var archive = new LocalGzipObservationArchive(directory);
    byte[] payload = "{\"synthetic\":true}".getBytes(StandardCharsets.UTF_8);
    var start = new CountDownLatch(1);
    var executor = Executors.newFixedThreadPool(4);
    try {
      var writers = new ArrayList<Future<ArchiveReference>>();
      for (int i = 0; i < 4; i++) {
        writers.add(
            executor.submit(
                () -> {
                  start.await();
                  return archive.archive("same-source", "same-batch", payload);
                }));
      }
      start.countDown();
      var expected = writers.getFirst().get();
      for (var writer : writers) {
        assertEquals(expected, writer.get());
      }
      assertArrayEquals(payload, archive.read(expected));
      assertNoStagingFiles();
    } finally {
      executor.shutdownNow();
    }
  }

  private void assertNoStagingFiles() throws Exception {
    try (var paths = Files.walk(directory)) {
      assertEquals(
          0,
          paths.filter(p -> p.getFileName().toString().endsWith(".tmp")).count(),
          "Temporary archive staging files must be cleaned on success and failure");
    }
  }
}
