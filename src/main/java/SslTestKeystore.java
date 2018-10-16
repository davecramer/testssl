
import static org.postgresql.PGProperty.SSL_FACTORY;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class SslTestKeystore {

  public static void main(String[] args) {
    String url = "jdbc:postgresql://localhost/testssl";

    Properties tlsProperties = new Properties();

    tlsProperties.setProperty("user", "test");
    tlsProperties.setProperty("password", "test");
    SSL_FACTORY.set(tlsProperties,"org.postgresql.ssl.DefaultJavaSSLFactory");
    try (Connection db = DriverManager.getConnection(url, tlsProperties)) {
      System.out.println("Java JDBC PostgreSQL Example");
      System.out.println("Connected to PostgreSQL database with TLS!");
      ResultSet resultSet = db.createStatement().executeQuery("SELECT now() as CurrDttm");
      while (resultSet.next()) {
        System.out.printf("Current Date/Time %s\n", resultSet.getString("CurrDttm"));
      }
      try {
        db.close();
        System.out.println("Disconnected from PostgreSQL database");
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } catch (SQLException e) {
      System.out.println("Connection failure.");
      e.printStackTrace();
    }
  }

}
