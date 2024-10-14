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

    // Create a StringBuilder to dynamically build the list of column names
    StringBuilder columnNamesBuilder = new StringBuilder();
    for (ColumnMetadata column : tableMetadata.getColumns().values()) {
        if (columnNamesBuilder.length() > 0) {
            columnNamesBuilder.append(", ");
        }
        columnNamesBuilder.append(column.getName().asInternal());
    }
    String columnNames = columnNamesBuilder.toString();

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
