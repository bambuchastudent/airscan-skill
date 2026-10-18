package dev.airscan.adapter.archive;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
  }
}
