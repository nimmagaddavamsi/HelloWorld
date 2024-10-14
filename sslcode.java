private static String generateInsertQuery(String keyspace, String table, Row row) {
    StringBuilder columns = new StringBuilder();
    StringBuilder values = new StringBuilder();

    row.getColumnDefinitions().forEach(columnDef -> {
        String columnName = columnDef.getName().asInternal();
        Object columnValue = row.getObject(columnName);

        if (columns.length() > 0) {
            columns.append(", ");
            values.append(", ");
        }

        columns.append(columnName);
        values.append("'" + columnValue + "'");
    });

    return String.format("INSERT INTO %s.%s (%s) VALUES (%s)", keyspace, table, columns, values);
}

private static String generateUpdateQuery(String keyspace, String table, Row row, String primaryKeyColumn) {
    StringBuilder setClause = new StringBuilder();
    Object primaryKeyValue = row.getObject(primaryKeyColumn);

    row.getColumnDefinitions().forEach(columnDef -> {
        String columnName = columnDef.getName().asInternal();
        Object columnValue = row.getObject(columnName);

        if (!columnName.equals(primaryKeyColumn)) {
            if (setClause.length() > 0) {
                setClause.append(", ");
            }
            setClause.append(columnName + " = '" + columnValue + "'");
        }
    });

    return String.format("UPDATE %s.%s SET %s WHERE %s = '%s'", keyspace, table, setClause, primaryKeyColumn, primaryKeyValue);
}
