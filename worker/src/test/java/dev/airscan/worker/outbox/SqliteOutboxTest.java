package dev.airscan.worker.outbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqliteOutboxTest {
  @TempDir Path directory;

  @Test
  void pendingIdentitySurvivesCloseReopenAndAcceptance() throws Exception {
    Path db = directory.resolve("worker.sqlite");
    var item = new OutboxEnvelope("batch-42", "{\"value\":1}", Instant.parse("2026-10-18T00:00:00Z"));
    try (var processA = new SqliteOutbox(db)) {
      processA.enqueue(item);
      processA.enqueue(item);
      assertEquals(1, processA.pending().size());
      assertThrows(
          IllegalStateException.class,
          () -> processA.enqueue(new OutboxEnvelope(item.batchId(), "modified", item.createdAt())));
    }
    try (var processB = new SqliteOutbox(db)) {
      assertEquals(item, processB.pending().getFirst());
      processB.markAccepted(item.batchId());
      assertTrue(processB.pending().isEmpty());
    }
    try (var processC = new SqliteOutbox(db)) {
      assertTrue(processC.pending().isEmpty());
      processC.markAccepted(item.batchId());
      assertThrows(IllegalArgumentException.class, () -> processC.markAccepted("unknown"));
    }
    try (var check = DriverManager.getConnection("jdbc:sqlite:" + db);
        var pragma = check.createStatement();
        var mode = pragma.executeQuery("PRAGMA journal_mode")) {
      assertEquals("wal", mode.getString(1));
    }
  }
}
