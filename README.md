# PostgreSQL JDBC SSL Example

Note: this is just for testing and is a very simple example.

## Setting up the server

create a server cert and key the common name is 'localhost', but can be anything usually the host name

`openssl req -new -x509 -days 365 -nodes -text -out server.crt -keyout server.key -subj "/CN=localhost"`

`chmod og-rwx server.key`

create a client cert and key with the same common name

`openssl req -new -x509 -days 365 -nodes -text -out client.crt -keyout client.key -subj "/CN=localhost"`

`chmod og-rwx client.key`

move server.key and server.crt into the postgresql data directory.

copy server.crt to server_ca.crt, this will be used by the client to verify the server certs.

copy client.crt to client_ca.crt and move to the postgresql data directory

in postgresql.conf


```
ssl_cert_file = 'server.crt'
ssl_key_file = 'server.key'
ssl_ca_file = 'client_ca.crt'
```


in pg_hba.conf

`hostssl testssl         test            127.0.0.1/32            md5 clientcert=1`

create a database called testssl and a user named test

confirm that you can connect with psql and get an ssl connection

psql uses the following environment variables

```
PGSSLROOTCERT=/Users/davec/projects/r2dbc/testssl/ssl/client_ca.crt
PGSSLCERT=/Users/davec/projects/r2dbc/testssl/ssl/client.crt
PGSSLKEY=/Users/davec/projects/r2dbc/testssl/ssl/client.key
PGSSLMODE=verify-ca
```

```
/usr/local/pgsql/10/bin/psql testssl -U test -h 127.0.0.1
Password for user test:
psql (10.5)
SSL connection (protocol: TLSv1.2, cipher: ECDHE-RSA-CHACHA20-POLY1305, bits: 256, compression: off)
```

you may have to make pg_hba.conf more restrictive to ensure that only an ssl connection is possible to that database/user/host combination.

in java.

*first* we have to change the client key into a format java understands.

`openssl pkcs8 -topk8 -in client.key -out client.pk8 -outform DER -v1 PBE-MD5-DES`

This will ask for an encryption password. For this example there will be no password, however it is important that you provide the same password in the java code `SSL_PASSWORD`

```java
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

```
