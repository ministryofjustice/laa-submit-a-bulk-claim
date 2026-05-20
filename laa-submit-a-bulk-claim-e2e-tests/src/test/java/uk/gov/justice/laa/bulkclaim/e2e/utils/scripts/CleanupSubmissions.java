package uk.gov.justice.laa.bulkclaim.e2e.utils.scripts;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import uk.gov.justice.laa.bulkclaim.e2e.utils.db.DatabaseManager;

/**
 * Cleanup utility for removing test submissions and related data from the database.
 * Equivalent to TS cleanup-submissions.ts
 */
public final class CleanupSubmissions {

  private static final int DAYS_BACK = 30;

  /**
   * Clean submission data and all related records from the database.
   *
   * @param manager DatabaseManager instance
   * @param submissionIds List of submission IDs to delete
   * @throws SQLException if database operations fail
   */
  public static void cleanSubmissionData(
      DatabaseManager manager, List<String> submissionIds)
      throws SQLException {

    if (submissionIds.isEmpty()) {
      System.out.println("⚠️ No submissions to clean.");
      return;
    }

    String submissionIdList = String.join(", ", submissionIds);
    System.out.println("🚀 Beginning cleanup for submission IDs: " + submissionIdList);

    manager.beginTransaction();

    try {
      // Get bulk submission IDs
      String bulkSubmissionQuery =
          "SELECT DISTINCT bulk_submission_id::text FROM claims.submission"
              + " WHERE id = ANY(ARRAY["
              + formatIdListForSQL(submissionIds)
              + "])"
              + " AND bulk_submission_id IS NOT NULL";

      List<Object[]> bulkRows = manager.query(bulkSubmissionQuery);
      List<String> bulkSubmissionIds = new ArrayList<>();
      for (Object[] row : bulkRows) {
        if (row[0] != null) {
          bulkSubmissionIds.add(row[0].toString());
        }
      }

      // Delete in dependency order
      deleteFromTable(
          manager,
          "claims.calculated_fee_detail",
          "DELETE FROM claims.calculated_fee_detail WHERE claim_id IN"
              + " (SELECT id FROM claims.claim WHERE submission_id = ANY(ARRAY["
              + formatIdListForSQL(submissionIds)
              + "]))");

      deleteFromTable(
          manager,
          "claims.validation_message_log",
          "DELETE FROM claims.validation_message_log WHERE submission_id = ANY(ARRAY["
              + formatIdListForSQL(submissionIds)
              + "]) OR claim_id IN"
              + " (SELECT id FROM claims.claim WHERE submission_id = ANY(ARRAY["
              + formatIdListForSQL(submissionIds)
              + "]))");

      deleteFromTable(
          manager,
          "claims.assessment",
          "DELETE FROM claims.assessment WHERE claim_summary_fee_id IN"
              + " (SELECT id FROM claims.claim_summary_fee WHERE claim_id IN"
              + " (SELECT id FROM claims.claim WHERE submission_id = ANY(ARRAY["
              + formatIdListForSQL(submissionIds)
              + "])))");

      deleteFromTable(
          manager,
          "claims.claim_summary_fee",
          "DELETE FROM claims.claim_summary_fee WHERE claim_id IN"
              + " (SELECT id FROM claims.claim WHERE submission_id = ANY(ARRAY["
              + formatIdListForSQL(submissionIds)
              + "]))");

      deleteFromTable(
          manager,
          "claims.claim_case",
          "DELETE FROM claims.claim_case WHERE claim_id IN"
              + " (SELECT id FROM claims.claim WHERE submission_id = ANY(ARRAY["
              + formatIdListForSQL(submissionIds)
              + "]))");

      deleteFromTable(
          manager,
          "claims.client",
          "DELETE FROM claims.client WHERE claim_id IN"
              + " (SELECT id FROM claims.claim WHERE submission_id = ANY(ARRAY["
              + formatIdListForSQL(submissionIds)
              + "]))");

      deleteFromTable(
          manager,
          "claims.matter_start",
          "DELETE FROM claims.matter_start WHERE submission_id = ANY(ARRAY["
              + formatIdListForSQL(submissionIds)
              + "])");

      deleteFromTable(
          manager,
          "claims.claim",
          "DELETE FROM claims.claim WHERE submission_id = ANY(ARRAY["
              + formatIdListForSQL(submissionIds)
              + "])");

      deleteFromTable(
          manager,
          "claims.submission",
          "DELETE FROM claims.submission WHERE id = ANY(ARRAY["
              + formatIdListForSQL(submissionIds)
              + "])");

      if (!bulkSubmissionIds.isEmpty()) {
        deleteFromTable(
            manager,
            "claims.bulk_submission",
            "DELETE FROM claims.bulk_submission WHERE id = ANY(ARRAY["
                + formatIdListForSQL(bulkSubmissionIds)
                + "])");
      }

      manager.commit();
      System.out.println(
          "🎉 Cleanup completed successfully for submission IDs: " + submissionIdList);

    } catch (SQLException e) {
      manager.rollback();
      System.out.println("❌ Cleanup failed: " + e.getMessage());
      throw e;
    }
  }

  /**
   * Get submission IDs from the past N days.
   *
   * @param manager DatabaseManager instance
   * @param daysBack Number of days to look back
   * @return List of submission IDs
   * @throws SQLException if database query fails
   */
  public static List<String> getRecentSubmissionIds(
      DatabaseManager manager, int daysBack)
      throws SQLException {

    String query =
        "SELECT id::text FROM claims.submission WHERE created_on >= (CURRENT_DATE - INTERVAL '"
            + daysBack
            + " days')";

    List<Object[]> results = manager.query(query);
    List<String> ids = new ArrayList<>();

    for (Object[] row : results) {
      if (row[0] != null) {
        ids.add(row[0].toString());
      }
    }

    return ids;
  }

  /**
   * Delete from a table and log result.
   */
  private static void deleteFromTable(
      DatabaseManager manager, String tableName, String deleteQuery)
      throws SQLException {

    int rowsDeleted = manager.executeUpdate(deleteQuery);
    System.out.println("✅ Deleted " + rowsDeleted + " row(s) from " + tableName);
  }

  /**
   * Format list of IDs for SQL IN clause.
   */
  private static String formatIdListForSQL(List<String> ids) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ids.size(); i++) {
      sb.append("'").append(ids.get(i)).append("'::uuid");
      if (i < ids.size() - 1) {
        sb.append(",");
      }
    }
    return sb.toString();
  }

  /**
   * Main entry point for cleanup.
   */
  public static void main(String[] args) {
    DatabaseManager manager = new DatabaseManager("E2E Cleanup");

    try {
      if (!manager.ensureInitialized()) {
        System.out.println("❌ Failed to initialize database connection");
        System.exit(1);
      }

      List<String> submissionIds = getRecentSubmissionIds(manager, DAYS_BACK);

      if (submissionIds.isEmpty()) {
        System.out.println(
            "⚠️ No submissions found in the past " + DAYS_BACK + " days.");
      } else {
        System.out.println(
            "🧾 Found " + submissionIds.size() + " submission(s) in the past "
                + DAYS_BACK + " days.");
        cleanSubmissionData(manager, submissionIds);
      }
    } catch (SQLException e) {
      System.err.println("❌ Cleanup failed: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    } finally {
      manager.destroy();
    }
  }
}

