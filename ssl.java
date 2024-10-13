import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ColumnMetadata;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CassandraDataTransfer {
    public static void main(String[] args) {
        // Connection details for production and non-production clusters
        String prodContactPoint = "prod_cassandra_host"; // Replace with production Cassandra IP
        int prodPort = 9042; // Default Cassandra port
        String prodKeyspace = "prod_keyspace";
        String prodUsername = "prod_username";
        String prodPassword = "prod_password";

        String nonProdContactPoint = "nonprod_cassandra_host"; // Replace with non-production Cassandra IP
        int nonProdPort = 9042;
        String nonProdKeyspace = "nonprod_keyspace";
        String nonProdUsername = "nonprod_username";
        String nonProdPassword = "nonprod_password";
        String tableName = "your_table_name"; // The table to work with

        try (CqlSession prodSession = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(prodContactPoint, prodPort))
                .withLocalDatacenter("datacenter1") // Replace with your data center name
                .withAuthCredentials(prodUsername, prodPassword)
                .build();

             CqlSession nonProdSession = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(nonProdContactPoint, nonProdPort))
                .withLocalDatacenter("datacenter1") // Replace with your data center name
                .withAuthCredentials(nonProdUsername, nonProdPassword)
                .build()) {

            // Get metadata for the table to extract column names
            KeyspaceMetadata keyspaceMetadata = prodSession.getMetadata().getKeyspace(prodKeyspace).orElseThrow();
            TableMetadata tableMetadata = keyspaceMetadata.getTable(tableName).orElseThrow();

            // Get the list of column names from the table metadata
            List<String> columnNames = tableMetadata.getColumns().keySet().stream()
                    .map(ColumnMetadata::getName)
                    .map(Object::toString)
                    .collect(Collectors.toList());

            // Create column names and placeholders for the insert and update queries
            String columnList = String.join(", ", columnNames);
            String placeholders = columnNames.stream().map(name -> "?").collect(Collectors.joining(", "));
            String updateSet = columnNames.stream().map(name -> name + " = ?").collect(Collectors.joining(", "));

            // Query to read data from the production table
            String prodQuery = "SELECT * FROM " + prodKeyspace + "." + tableName + ";";
            ResultSet resultSet = prodSession.execute(prodQuery);

            // Loop through each row in the production table
            for (Row row : resultSet) {
                // Check if the record already exists in the non-production table
                String checkQuery = "SELECT * FROM " + nonProdKeyspace + "." + tableName + " WHERE id = ?;";
                ResultSet checkResult = nonProdSession.execute(
                        SimpleStatement.builder(checkQuery)
                                .addPositionalValue(row.getObject("id")) // Assuming "id" is the primary key
                                .build()
                );

                if (checkResult.one() != null) {
                    // Record exists, perform an update
                    String updateQuery = "UPDATE " + nonProdKeyspace + "." + tableName
                            + " SET " + updateSet + " WHERE id = ?;";
                    Statement<?> updateStatement = SimpleStatement.builder(updateQuery)
                            .addPositionalValues(columnNames.stream().map(row::getObject).collect(Collectors.toList()))
                            .addPositionalValue(row.getObject("id")) // Append the id value for the WHERE clause
                            .build();

                    nonProdSession.execute(updateStatement);
                    System.out.println("Updated record with ID: " + row.getObject("id"));
                } else {
                    // Record does not exist, perform an insert
                    String insertQuery = "INSERT INTO " + nonProdKeyspace + "." + tableName
                            + " (" + columnList + ") VALUES (" + placeholders + ");";
                    Statement<?> insertStatement = SimpleStatement.builder(insertQuery)
                            .addPositionalValues(columnNames.stream().map(row::getObject).collect(Collectors.toList()))
                            .build();

                    nonProdSession.execute(insertStatement);
                    System.out.println("Inserted new record with ID: " + row.getObject("id"));
                }
            }

            System.out.println("Data transfer and update from production to non-production completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred during data transfer.");
        }
    }
}
