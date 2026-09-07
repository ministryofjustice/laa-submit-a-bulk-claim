package uk.gov.justice.laa.payments.submit.e2e.base;

import java.io.File;
import javax.sql.DataSource;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.BeforeEach;
import org.postgresql.ds.PGSimpleDataSource;
import uk.gov.justice.laa.payments.submit.e2e.config.EnvConfig;
import uk.gov.justice.laa.payments.submit.e2e.persistence.DatabaseQueryExecutor;

public abstract class DbUnitBaseTest extends BaseTest {

  private static final String SCHEMA = "claims";

  /** e.g. "datasets/submission_details.xml". */
  protected abstract String getDataSetPath();

  @Override
  @BeforeEach
  public void setup() {
    try {
      seedDataSet();
    } catch (Exception e) {
      throw new RuntimeException("Failed to seed database via DBUnit", e);
    }

    page = BrowserSession.getContext().newPage();
    page.navigate(EnvConfig.baseUrl());
  }

  private void seedDataSet() throws Exception {
    try (var databaseQueryExecutor = new DatabaseQueryExecutor()) {
      databaseQueryExecutor.cleanAll();
    }

    try (var connection = getDataSource().getConnection()) {
      IDatabaseConnection dbUnitConnection = new DatabaseConnection(connection, SCHEMA);
      dbUnitConnection
          .getConfig()
          .setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());

      // Makes test data a little more lenient
      dbUnitConnection.getConfig().setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true);

      DatabaseOperation.CLEAN_INSERT.execute(dbUnitConnection, getDataSet());
    }
  }

  protected DataSource getDataSource() {
    var dataSource = new PGSimpleDataSource();
    dataSource.setUrl(EnvConfig.dbConnectionUrl());
    dataSource.setUser(EnvConfig.dbUser());
    dataSource.setPassword(EnvConfig.dbPassword());
    return dataSource;
  }

  protected IDataSet getDataSet() throws Exception {
    var resource = getClass().getClassLoader().getResource(getDataSetPath());
    if (resource == null) {
      throw new IllegalStateException(
          "DBUnit dataset not found on classpath: " + getDataSetPath());
    }
    // Column sensing scans every row of a table (not just the first) to build its full column
    // set, so rows are free to include different/extra columns without a DTD.
    var builder = new FlatXmlDataSetBuilder();
    builder.setColumnSensing(true);
    return builder.build(new File(resource.toURI()));
  }
}
