package org.neikius.test.importmatch;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Instant;

public class Import {

  // Set this to true to use the fast COPY method of postgres for import
  private boolean useCopy = true;

  private static final String COPY_SQL = "COPY match (match_id, market_id, outcome_id, specifiers, date_insert) " +
      "FROM '/tmp/data.csv' CSV DELIMITER '|' QUOTE '''';";
  private static final String SELECT_MIN_MAX = "SELECT min(date_insert), max(date_insert) FROM MATCH;";
  private static final String INSERT_QUERY = "INSERT INTO MATCH (match_id, market_id, outcome_id, specifiers, date_insert) " +
      "VALUES (?, ?, ?, ?, ?);";

  public static void main(String[] args) throws SQLException, IOException {
    Import main = new Import();

    Runtime.getRuntime().addShutdownHook(new Thread(){
      @Override
      public void run() {
        super.run();
        try {
          if (!connection.isClosed())
            connection.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    });

    long time = System.nanoTime();
    main.run(args.length > 0 ? args[0] : "fo_random.txt");
    long diff = System.nanoTime() - time;
    System.out.println("Duration in ns:" + diff);
    System.out.println("Duration in ms:" + (diff/1_000_000));
  }

  private void run(String file) throws SQLException, IOException {

    prepareDb();

    // for use with COPY method
    PrintWriter pw;
    if (useCopy) {
      pw = new PrintWriter(Files.newBufferedWriter(Paths.get("/tmp/postgres/data.csv")));
    } else {
      pw = null;
    }

    Parser parser = new Parser(file);
    parser.parseToList().forEach(match -> {
          if (useCopy) {
            String str = "'" + match.getMatchId() + "'|" + match.getMarketId() + "|'" + match.getOutcomeId() + "'|";
            if (match.getSpecifiers() != null) {
              str += "'" + match.getSpecifiers() + "'";
            }
            str += "|'" + createTimestamp() + "'";
            pw.println(str);
          } else {
            try {
              storeMatch(match);
            } catch (SQLException e) {
              e.printStackTrace();

            }
        }});

    if (useCopy) {
      pw.flush();
      pw.close();
      getConnection().prepareStatement(COPY_SQL).execute();
    }
    getConnection().commit();

    ResultSet rs = getConnection().prepareStatement(SELECT_MIN_MAX).executeQuery();
    rs.next();
    System.out.println("date_insert min: " + rs.getString(1) + " max: " + rs.getString(2));
    rs.close();
  }

  private void storeMatch(Match match) throws SQLException {
    PreparedStatement ps = getConnection().prepareStatement(INSERT_QUERY);

    ps.setString(1, match.getMatchId());
    ps.setInt(2, match.getMarketId());
    ps.setString(3, match.getOutcomeId());
    if (match.getSpecifiers()==null) {
      ps.setNull(4, Types.VARCHAR);
    } else {
      ps.setString(4, match.getSpecifiers());
    }
    ps.setTimestamp(5, createTimestamp());

    ps.execute();
  }

  private void prepareDb() throws SQLException {
    String create = "CREATE TABLE IF NOT EXISTS MATCH (" +
        "match_id VARCHAR(255)," +
        "market_id INT," +
        "outcome_id VARCHAR(255)," +
        "specifiers VARCHAR(255)," +
        "date_insert TIMESTAMP DEFAULT NOW()" +
        ");";
    getConnection().prepareStatement(create).execute();
  }

  private static Connection connection;

  private Connection getConnection() {
    if (connection!=null) {
      return connection;
    }
    try {
      connection = DriverManager.getConnection("jdbc:postgresql://localhost:15423/matchimport",
          "matchimport", "matchimport");
      connection.setAutoCommit(false);
      return connection;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Create timestamp
   * @return current {@link Timestamp}
   */
  private Timestamp createTimestamp() {
    Instant now = Instant.now();
    return new Timestamp(now.toEpochMilli());
  }

}
