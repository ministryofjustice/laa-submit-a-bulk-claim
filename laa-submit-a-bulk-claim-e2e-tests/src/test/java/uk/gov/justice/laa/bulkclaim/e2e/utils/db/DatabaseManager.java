package uk.gov.justice.laa.bulkclaim.e2e.utils.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Database connection manager for E2E test cleanup and data operations.
 * Handles PostgreSQL connections with SSL retry logic.
 */
public final class DatabaseManager {

  private final String label;
  private final String host;
  private final int port;
  private final String username;
  private final String password;
  private final String database;
  private boolean useSsl;
  private Connection connection;

  public DatabaseManager(String label) {
    this.label = label;
    this.host = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
    this.port = System.getenv("DB_PORT") != null ? Integer.parseInt(System.getenv("DB_PORT")) : 5432;
    this.username = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "";
    this.password = System.getenv("DB_PASS") != null ? System.getenv("DB_PASS") : "";
    this.database = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "";
    this.useSsl =
        System.getenv("DB_SSL") != null
            && System.getenv("DB_SSL").toLowerCase().equals("true");
  }

  public boolean ensureInitialized() {
    if (connection != null) {
      try {
        if (!connection.isClosed()) {
          return true;
        }
      } catch (SQLException e) {
        // Connection is closed, try to reconnect
      }
    }

    try {
      String sslMode = useSsl ? "require" : "disable";
      String url =
          String.format(
              "jdbc:postgresql://%s:%d/%s?sslmode=%s",
              host, port, database, sslMode);
      connection = DriverManager.getConnection(url, username, password);
      System.out.println("✅ [" + label + "] Database connection established"
          + (useSsl ? " (with SSL)" : " (no SSL)"));
      return true;
    } catch (SQLException e) {
      String message = e.getMessage().toLowerCase();

      if (useSsl && (message.contains("ssl") || message.contains("certificate"))) {
        System.out.println(
            "⚠️ [" + label + "] Database does not support SSL. Retrying without SSL.");
        useSsl = false;

        try {
          String sslMode = "disable";
          String url =
              String.format(
                  "jdbc:postgresql://%s:%d/%s?sslmode=%s",
                  host, port, database, sslMode);
          connection = DriverManager.getConnection(url, username, password);
          System.out.println(
              "✅ [" + label + "] Database connection established (without SSL)");
          return true;
        } catch (SQLException innerE) {
          System.out.println("⚠️ [" + label + "] Unable to connect to database: "
              + innerE.getMessage());
          return false;
        }
      }

      System.out.println("⚠️ [" + label + "] Unable to connect to database: " + e.getMessage());
      return false;
    }
  }

  public void destroy() {
    if (connection != null) {
      try {
        connection.close();
        System.out.println("🔒 [" + label + "] Database connection closed");
      } catch (SQLException e) {
        System.out.println("⚠️ [" + label + "] Error closing database connection: "
            + e.getMessage());
      }
    }
  }

  /**
   * Execute a query and return results as a list of string arrays.
   */
  public List<Object[]> query(String sql) throws SQLException {
    if (connection == null || connection.isClosed()) {
      throw new SQLException("Database connection not initialized");
    }

    List<Object[]> results = new ArrayList<>();
    try (Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      int columnCount = rs.getMetaData().getColumnCount();
      while (rs.next()) {
        Object[] row = new Object[columnCount];
        for (int i = 1; i <= columnCount; i++) {
          row[i - 1] = rs.getObject(i);
        }
        results.add(row);
      }
    }
    return results;
  }

  /**
   * Execute an update/delete/insert query.
   */
  public int executeUpdate(String sql) throws SQLException {
    if (connection == null || connection.isClosed()) {
      throw new SQLException("Database connection not initialized");
    }

    try (Statement stmt = connection.createStatement()) {
      return stmt.executeUpdate(sql);
    }
  }

  /**
   * Begin a transaction.
   */
  public void beginTransaction() throws SQLException {
    if (connection != null) {
      connection.setAutoCommit(false);
    }
  }

  /**
   * Commit a transaction.
   */
  public void commit() throws SQLException {
    if (connection != null) {
      connection.commit();
      connection.setAutoCommit(true);
    }
  }

  /**
   * Rollback a transaction.
   */
  public void rollback() throws SQLException {
    if (connection != null) {
      connection.rollback();
      connection.setAutoCommit(true);
    }
  }

  /**
   * Clean up test submissions and their related claims from the database.
   * This is a static utility method for bulk cleanup operations.
   *
   * @param submissionIds Collection of submission IDs to delete
   */
  public static void cleanupSubmissions(Collection<String> submissionIds) {
    if (submissionIds == null || submissionIds.isEmpty()) {
      return;
    }

    DatabaseManager manager = new DatabaseManager("CLEANUP");
    try {
      if (!manager.ensureInitialized()) {
        System.out.println("⚠️ [CLEANUP] Failed to initialize database connection for cleanup");
        return;
      }

      manager.beginTransaction();

      // Build the list of submission IDs for SQL IN clause
      StringBuilder idList = new StringBuilder();
      int count = 0;
      for (String id : submissionIds) {
        if (count > 0) {
          idList.append(", ");
        }
        idList.append("'").append(id.replace("'", "''")).append("'");
        count++;
      }

      // Delete in dependency order (most dependent first)
      // 1. Delete calculated_fee_detail (depends on claim)
      String deleteCalculatedFeeSQL =
          "DELETE FROM claims.calculated_fee_detail WHERE claim_id IN (SELECT id FROM claims.claim WHERE submission_id IN (" + idList.toString() + "))";
      int feeDetailsDeleted = manager.executeUpdate(deleteCalculatedFeeSQL);
      System.out.println("[CLEANUP] Deleted " + feeDetailsDeleted + " calculated fee details");

      // 2. Delete validation_message_log (depends on submission/claim)
      String deleteValidationSQL =
          "DELETE FROM claims.validation_message_log WHERE submission_id IN (" + idList.toString() + ") OR claim_id IN (SELECT id FROM claims.claim WHERE submission_id IN (" + idList.toString() + "))";
      int validationDeleted = manager.executeUpdate(deleteValidationSQL);
      System.out.println("[CLEANUP] Deleted " + validationDeleted + " validation messages");

      // 3. Delete assessment (depends on claim_summary_fee)
      String deleteAssessmentSQL =
          "DELETE FROM claims.assessment WHERE claim_summary_fee_id IN (SELECT id FROM claims.claim_summary_fee WHERE claim_id IN (SELECT id FROM claims.claim WHERE submission_id IN (" + idList.toString() + ")))";
      int assessmentDeleted = manager.executeUpdate(deleteAssessmentSQL);
      System.out.println("[CLEANUP] Deleted " + assessmentDeleted + " assessments");

      // 4. Delete claim_summary_fee (depends on claim)
      String deleteSummaryFeeSQL =
          "DELETE FROM claims.claim_summary_fee WHERE claim_id IN (SELECT id FROM claims.claim WHERE submission_id IN (" + idList.toString() + "))";
      int summaryFeeDeleted = manager.executeUpdate(deleteSummaryFeeSQL);
      System.out.println("[CLEANUP] Deleted " + summaryFeeDeleted + " claim summary fees");

      // 5. Delete claim_case (depends on claim)
      String deleteClaimCaseSQL =
          "DELETE FROM claims.claim_case WHERE claim_id IN (SELECT id FROM claims.claim WHERE submission_id IN (" + idList.toString() + "))";
      int claimCaseDeleted = manager.executeUpdate(deleteClaimCaseSQL);
      System.out.println("[CLEANUP] Deleted " + claimCaseDeleted + " claim cases");

      // 6. Delete client (depends on claim)
      String deleteClientSQL =
          "DELETE FROM claims.client WHERE claim_id IN (SELECT id FROM claims.claim WHERE submission_id IN (" + idList.toString() + "))";
      int clientDeleted = manager.executeUpdate(deleteClientSQL);
      System.out.println("[CLEANUP] Deleted " + clientDeleted + " clients");

      // 7. Delete matter_start (depends on submission)
      String deleteMatterStartSQL =
          "DELETE FROM claims.matter_start WHERE submission_id IN (" + idList.toString() + ")";
      int matterStartDeleted = manager.executeUpdate(deleteMatterStartSQL);
      System.out.println("[CLEANUP] Deleted " + matterStartDeleted + " matter starts");

      // 8. Delete claims (depends on submission)
      String deleteClaimsSQL =
          "DELETE FROM claims.claim WHERE submission_id IN (" + idList.toString() + ")";
      int claimsDeleted = manager.executeUpdate(deleteClaimsSQL);
      System.out.println("[CLEANUP] Deleted " + claimsDeleted + " claims");

      // 9. Finally, delete submissions
      String deleteSubmissionsSQL =
          "DELETE FROM claims.submission WHERE id IN (" + idList.toString() + ")";
      int submissionsDeleted = manager.executeUpdate(deleteSubmissionsSQL);
      System.out.println("[CLEANUP] Deleted " + submissionsDeleted + " submissions");

      manager.commit();
      System.out.println("[CLEANUP] ✅ Database cleanup completed successfully");

    } catch (SQLException e) {
      System.err.println("[CLEANUP] ❌ Error during database cleanup: " + e.getMessage());
      e.printStackTrace();
      try {
        manager.rollback();
      } catch (SQLException rollbackE) {
        System.err.println("[CLEANUP] ❌ Error rolling back transaction: " + rollbackE.getMessage());
      }
    } finally {
      manager.destroy();
    }
  }
}

