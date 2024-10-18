import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlloyDBDataTransfer {
    private static final String SOURCE_DB_URL = "jdbc:postgresql://source-alloydb-host:5432/source_database";
    private static final String TARGET_DB_URL = "jdbc:postgresql://target-alloydb-host:5432/target_database";
    private static final String USERNAME = "your_username";
    private static final String PASSWORD = "your_password";

    public static void main(String[] args) {
        try (Connection sourceConn = DriverManager.getConnection(SOURCE_DB_URL, USERNAME, PASSWORD);
             Connection targetConn = DriverManager.getConnection(TARGET_DB_URL, USERNAME, PASSWORD)) {

            // Define the tables you want to transfer
            List<String> tableNames = List.of("table1", "table2", "table3");

            for (String tableName : tableNames) {
                transferData(sourceConn, targetConn, tableName);
            }

            System.out.println("Data transfer completed successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void transferData(Connection sourceConn, Connection targetConn, String tableName) throws SQLException {
        // Step 1: Get the column names and primary key from the source table using metadata
        String selectQuery = "SELECT * FROM " + tableName + " LIMIT 1"; // Get the column metadata
        try (Statement statement = sourceConn.createStatement();
             ResultSet resultSet = statement.executeQuery(selectQuery)) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<String> columnNames = new ArrayList<>();
            String primaryKey = metaData.getColumnName(1); // Assuming the first column is the primary key

            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }

            // Step 2: Fetch the data from the source table
            String dynamicSelectQuery = "SELECT * FROM " + tableName;
            try (ResultSet dataResultSet = statement.executeQuery(dynamicSelectQuery)) {

                // Step 3: Prepare the update and insert queries dynamically
                StringBuilder updateQuery = new StringBuilder("UPDATE " + tableName + " SET ");
                StringBuilder insertQuery = new StringBuilder("INSERT INTO " + tableName + " (");
                for (String column : columnNames) {
                    if (!column.equals(primaryKey)) { // Exclude the primary key from the SET clause
                        updateQuery.append(column).append(" = ?, ");
                        insertQuery.append(column).append(", ");
                    }
                }
                // Remove trailing commas
                updateQuery.setLength(updateQuery.length() - 2);
                updateQuery.append(" WHERE ").append(primaryKey).append(" = ?;");
                insertQuery.setLength(insertQuery.length() - 2);
                insertQuery.append(") VALUES (");
                insertQuery.append("?, ".repeat(columnCount - 1)).append("?)");
                insertQuery.append(";");

                try (PreparedStatement updateStatement = targetConn.prepareStatement(updateQuery.toString());
                     PreparedStatement insertStatement = targetConn.prepareStatement(insertQuery.toString())) {

                    while (dataResultSet.next()) {
                        Object primaryKeyValue = dataResultSet.getObject(primaryKey);
                        boolean exists = checkIfExists(targetConn, tableName, primaryKey, primaryKeyValue);

                        if (exists) {
                            // Update existing record
                            for (int i = 1; i <= columnCount; i++) {
                                if (!metaData.getColumnName(i).equals(primaryKey)) {
                                    updateStatement.setObject(i, dataResultSet.getObject(i));
                                }
                            }
                            updateStatement.setObject(columnCount, primaryKeyValue); // Set the primary key value
                            updateStatement.executeUpdate(); // Perform the update
                        } else {
                            // Insert new record
                            for (int i = 1; i <= columnCount; i++) {
                                insertStatement.setObject(i, dataResultSet.getObject(i));
                            }
                            insertStatement.executeUpdate(); // Perform the insert
                        }
                    }
                }
            }
        }
    }

    private static boolean checkIfExists(Connection conn, String tableName, String primaryKey, Object primaryKeyValue) throws SQLException {
        String checkQuery = "SELECT 1 FROM " + tableName + " WHERE " + primaryKey + " = ?";
        try (PreparedStatement checkStatement = conn.prepareStatement(checkQuery)) {
            checkStatement.setObject(1, primaryKeyValue);
            try (ResultSet resultSet = checkStatement.executeQuery()) {
                return resultSet.next(); // If a record exists, this will return true
            }
        }
    }
}
