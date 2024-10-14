import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.stream.Collectors;

public class CassandraDataTransfer {

    private static final String SOURCE_KEYSPACE = "source_keyspace";
    private static final String DESTINATION_KEYSPACE = "destination_keyspace";
    private static final String TABLE_NAME = "example_table";

    public static void main(String[] args) {
        // Connect to the source (production) and destination (non-production) clusters
        try (CqlSession sourceSession = connectToCluster("127.0.0.1", "datacenter1");  // Replace with source IP and datacenter
             CqlSession destinationSession = connectToCluster("192.168.1.10", "datacenter2")) { // Replace with destination IP and datacenter

            System.out.println("Connected to both clusters successfully!");

            transferData(sourceSession, destinationSession, SOURCE_KEYSPACE, DESTINATION_KEYSPACE, TABLE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static CqlSession connectToCluster(String ipAddress, String datacenter) {
        return CqlSession.builder()
                .addContactPoint(new InetSocketAddress(ipAddress, 9042))
                .withLocalDatacenter(datacenter)
                .build();
    }

    private static void transferData(CqlSession sourceSession, CqlSession destinationSession,
                                     String sourceKeyspace, String destinationKeyspace, String tableName) {
        // Retrieve table metadata from the source cluster
        TableMetadata tableMetadata = sourceSession.getMetadata()
                .getKeyspace(sourceKeyspace)
                .flatMap(ks -> ks.getTable(tableName))
                .orElse(null);

        if (tableMetadata == null) {
            System.out.println("Table metadata not found for table: " + tableName);
            return;
        }

        // Get column names for generating dynamic queries
        String columnNames = tableMetadata.getColumns().keySet().stream()
                .collect(Collectors.joining(", "));

        // Query to fetch all rows from the source table
        String selectQuery = String.format("SELECT %s FROM %s.%s", columnNames, sourceKeyspace, tableName);
        ResultSet resultSet = sourceSession.execute(selectQuery);

        // Process each row and insert or update in the destination cluster
        for (Row row : resultSet) {
            String primaryKeyColumn = "id"; // Assume the primary key column is named 'id'; adjust as necessary
            Object primaryKeyValue = row.getObject(primaryKeyColumn);

            // Check if the row already exists in the destination table
            String checkQuery = String.format("SELECT %s FROM %s.%s WHERE %s = ?", primaryKeyColumn, destinationKeyspace, tableName, primaryKeyColumn);
            ResultSet checkResult = destinationSession.execute(SimpleStatement.newInstance(checkQuery, primaryKeyValue));

            if (checkResult.one() != null) {
                // If the row exists, perform an update
                String updateQuery = generateUpdateQuery(destinationKeyspace, tableName, row, primaryKeyColumn);
                destinationSession.execute(updateQuery);
                System.out.println("Updated row with primary key: " + primaryKeyValue);
            } else {
                // If the row does not exist, perform an insert
                String insertQuery = generateInsertQuery(destinationKeyspace, tableName, row);
                destinationSession.execute(insertQuery);
                System.out.println("Inserted new row with primary key: " + primaryKeyValue);
            }
        }
    }

    private static String generateInsertQuery(String keyspace, String table, Row row) {
        String columns = row.getColumnDefinitions().asList().stream()
                .map(column -> column.getName().asInternal())
                .collect(Collectors.joining(", "));

        String values = row.getColumnDefinitions().asList().stream()
                .map(column -> "'" + row.getObject(column.getName().asInternal()) + "'")
                .collect(Collectors.joining(", "));

        return String.format("INSERT INTO %s.%s (%s) VALUES (%s)", keyspace, table, columns, values);
    }

    private static String generateUpdateQuery(String keyspace, String table, Row row, String primaryKeyColumn) {
        String setClause = row.getColumnDefinitions().asList().stream()
                .filter(column -> !column.getName().asInternal().equals(primaryKeyColumn))
                .map(column -> column.getName().asInternal() + " = '" + row.getObject(column.getName().asInternal()) + "'")
                .collect(Collectors.joining(", "));

        Object primaryKeyValue = row.getObject(primaryKeyColumn);

        return String.format("UPDATE %s.%s SET %s WHERE %s = '%s'", keyspace, table, setClause, primaryKeyColumn, primaryKeyValue);
    }
}
