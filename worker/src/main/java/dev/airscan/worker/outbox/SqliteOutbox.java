package dev.airscan.worker.outbox;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Worker-local durable spool: no cloud credentials, authority or HTTP transport. */
public final class SqliteOutbox implements AutoCloseable {
  private final Connection connection;

  public SqliteOutbox(Path database) throws SQLException {
    Objects.requireNonNull(database);
    connection = DriverManager.getConnection("jdbc:sqlite:" + database.toAbsolutePath());
    try (Statement setup = connection.createStatement()) {
      setup.execute("PRAGMA busy_timeout=5000");
      setup.execute("PRAGMA journal_mode=WAL");
      setup.execute("PRAGMA synchronous=FULL");
      try (var version = setup.executeQuery("PRAGMA user_version")) {
        if (version.next() && version.getInt(1) > 1) {
          throw new SQLException("Unsupported worker outbox schema version");
        }
      }
      setup.execute(
          "CREATE TABLE IF NOT EXISTS worker_outbox ("
              + "batch_id TEXT PRIMARY KEY,"
              + "payload TEXT NOT NULL,"
              + "created_at TEXT NOT NULL,"
              + "state TEXT NOT NULL CHECK(state IN ('PENDING','ACCEPTED')))");
      setup.execute("PRAGMA user_version=1");
    } catch (SQLException error) {
      try {
        connection.close();
      } catch (SQLException closeError) {
        error.addSuppressed(closeError);
      }
      throw error;
    }
  }

  public void enqueue(OutboxEnvelope envelope) throws SQLException {
    Objects.requireNonNull(envelope);
    if (envelope.batchId() == null || envelope.batchId().isBlank()
        || envelope.payload() == null || envelope.createdAt() == null) {
      throw new IllegalArgumentException("Batch identity, payload and timestamp required");
    }
    try (var statement =
        connection.prepareStatement(
            "INSERT OR IGNORE INTO worker_outbox(batch_id,payload,created_at,state)"
                + " VALUES(?,?,?,'PENDING')")) {
      statement.setString(1, envelope.batchId());
      statement.setString(2, envelope.payload());
      statement.setString(3, envelope.createdAt().toString());
      statement.executeUpdate();
    }
    try (var statement =
        connection.prepareStatement(
            "SELECT payload,created_at FROM worker_outbox WHERE batch_id=?")) {
      statement.setString(1, envelope.batchId());
      try (var result = statement.executeQuery()) {
        if (!result.next()
            || !envelope.payload().equals(result.getString(1))
            || !envelope.createdAt().toString().equals(result.getString(2))) {
          throw new IllegalStateException("Retry batch identity collision");
        }
      }
    }
  }

  public List<OutboxEnvelope> pending() throws SQLException {
    var result = new ArrayList<OutboxEnvelope>();
    try (var statement = connection.createStatement();
        var rows =
            statement.executeQuery(
                "SELECT batch_id,payload,created_at FROM worker_outbox"
                    + " WHERE state='PENDING' ORDER BY created_at,batch_id")) {
      while (rows.next()) {
        result.add(
            new OutboxEnvelope(
                rows.getString(1), rows.getString(2), Instant.parse(rows.getString(3))));
      }
    }
    return List.copyOf(result);
  }

  public void markAccepted(String batchId) throws SQLException {
    try (var statement =
        connection.prepareStatement(
            "UPDATE worker_outbox SET state='ACCEPTED' WHERE batch_id=?")) {
      statement.setString(1, batchId);
      if (statement.executeUpdate() != 1) {
        throw new IllegalArgumentException("Unknown batch identity");
      }
    }
  }

  @Override
  public void close() throws SQLException {
    connection.close();
  }
}
