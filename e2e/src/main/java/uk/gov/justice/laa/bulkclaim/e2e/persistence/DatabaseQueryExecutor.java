package uk.gov.justice.laa.bulkclaim.e2e.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import uk.gov.justice.laa.bulkclaim.e2e.config.EnvConfig;
import uk.gov.justice.laa.bulkclaim.e2e.models.Insert;
import uk.gov.justice.laa.bulkclaim.e2e.models.SqlStatement;

public class DatabaseQueryExecutor implements AutoCloseable {

  private final Connection connection;

  public DatabaseQueryExecutor() throws SQLException {
    String url = EnvConfig.dbConnectionUrl();
    this.connection = DriverManager.getConnection(url, EnvConfig.dbUser(), EnvConfig.dbPassword());
  }

  public ResultSet executeQuery(SqlStatement sql) {
    try (var preparedStatement = prepareStatement(sql)) {
      return preparedStatement.executeQuery();
    } catch (Exception e) {
      throw new RuntimeException("Failed to execute SQL query against DB", e);
    }
  }

  public void executeUpdate(SqlStatement sql) {
    try (var preparedStatement = prepareStatement(sql)) {
      preparedStatement.executeUpdate();
    } catch (Exception e) {
      throw new RuntimeException("Failed to execute SQL update against DB", e);
    }
  }

  private PreparedStatement prepareStatement(SqlStatement sql) throws SQLException {
    var ps = connection.prepareStatement(sql.sql());
    for (var entry : sql.getParameters().entrySet()) {
      ps.setObject(entry.getKey(), entry.getValue());
    }
    return ps;
  }

  public void seed(List<Insert> inserts) {
    inserts.stream().map(SqlStatement::fromFile).forEach(this::executeUpdate);
  }

  public void cleanAll() {
    deleteAll("validation_message_log");
    deleteAll("assessment");
    deleteAll("calculated_fee_detail");
    deleteAll("claim_summary_fee");
    deleteAll("client");
    deleteAll("claim_case");
    deleteAll("claim");
    deleteAll("matter_start");
    deleteAll("submission");
    deleteAll("bulk_submission");
  }

  public void deleteAll(String table) {
    String sql = String.format("DELETE FROM claims.%s", table);
    executeUpdate(SqlStatement.fromRaw(sql));
  }

  public ResultSet select(String table, String id) {
    String sql = String.format("SELECT * FROM claims.%s WHERE id = ?::uuid", table);
    return executeQuery(SqlStatement.fromRaw(sql, List.of(id)));
  }

  @Override
  public void close() throws Exception {
    connection.close();
  }
}
