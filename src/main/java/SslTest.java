import static org.postgresql.PGProperty.SSL_CERT;
import static org.postgresql.PGProperty.SSL_KEY;
import static org.postgresql.PGProperty.SSL_MODE;
import static org.postgresql.PGProperty.SSL_PASSWORD;
import static org.postgresql.PGProperty.SSL_ROOT_CERT;

import java.sql.*;
import java.util.Properties;


public class SslTest {

  public static void main(String[] args) {
    String url = "jdbc:postgresql://localhost/testssl";

    Properties tlsProperties = new Properties();

    tlsProperties.setProperty("user", "test");
    tlsProperties.setProperty("password", "test");
    SSL_MODE.set(tlsProperties, "verify-full");
    SSL_PASSWORD.set(tlsProperties, "");
    SSL_KEY.set(tlsProperties, "/Users/davec/projects/r2dbc/testssl/ssl/client.pk8");
    SSL_CERT.set(tlsProperties,"/Users/davec/projects/r2dbc/testssl/ssl/client.crt");
    SSL_ROOT_CERT.set(tlsProperties, "/Users/davec/projects/r2dbc/testssl/ssl/client_ca.crt");

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
