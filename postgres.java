import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExcelDataTransfer {
    private static final String SOURCE_DB_URL = "jdbc:postgresql://source-db-host:5432/source_database";
    private static final String TARGET_DB_URL = "jdbc:postgresql://target-db-host:5432/target_database";
    private static final String USERNAME = "your_username";
    private static final String PASSWORD = "your_password";
    private static final String EXCEL_FILE_PATH = "path_to_your_excel_file.xlsx"; // Path to your Excel file
    private static final String SOURCE_TABLE_NAME = "your_source_table"; // Replace with your source table name
    private static final int BATCH_SIZE = 100; // Number of records to process at a time

    public static void main(String[] args) {
        try (Connection sourceConn = DriverManager.getConnection(SOURCE_DB_URL, USERNAME, PASSWORD);
             Connection targetConn = DriverManager.getConnection(TARGET_DB_URL, USERNAME, PASSWORD)) {

            List<String> primaryKeyValues = readPrimaryKeyValuesFromExcel(EXCEL_FILE_PATH, 0); // Assuming first column for primary key
            List<String> columnNames = getColumnNames(sourceConn, SOURCE_TABLE_NAME);
            String primaryKeyColumn = columnNames.get(0); // Assuming the first column is the primary key

            // Process in batches
            for (int i = 0; i < primaryKeyValues.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, primaryKeyValues.size());
                List<String> batchKeys = primaryKeyValues.subList(i, end);
                processBatch(sourceConn, targetConn, batchKeys, primaryKeyColumn, columnNames);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static List<String> getColumnNames(Connection conn, String tableName) throws SQLException {
        List<String> columnNames = new ArrayList<>();
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                columnNames.add(rs.getString("COLUMN_NAME"));
            }
        }
        return columnNames;
    }

    private static void processBatch(Connection sourceConn, Connection targetConn, List<String> primaryKeyValues, String primaryKeyColumn, List<String> columnNames) throws SQLException {
        String selectQuery = createSelectQuery(SOURCE_TABLE_NAME, primaryKeyColumn, primaryKeyValues.size());
        
        try (PreparedStatement selectStmt = sourceConn.prepareStatement(selectQuery)) {
            for (int index = 0; index < primaryKeyValues.size(); index++) {
                selectStmt.setString(index + 1, primaryKeyValues.get(index));
            }
            try (ResultSet rs = selectStmt.executeQuery()) {
                while (rs.next()) {
                    // Prepare insert/update statement
                    insertOrUpdateData(targetConn, rs, columnNames);
                }
            }
        }
    }

    private static String createSelectQuery(String tableName, String primaryKeyColumn, int numberOfKeys) {
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + tableName + " WHERE " + primaryKeyColumn + " IN (");
        for (int i = 0; i < numberOfKeys; i++) {
            queryBuilder.append("?");
            if (i < numberOfKeys - 1) {
                queryBuilder.append(", ");
            }
        }
        queryBuilder.append(")");
        return queryBuilder.toString();
    }

    private static void insertOrUpdateData(Connection targetConn, ResultSet rs, List<String> columnNames) throws SQLException {
        StringBuilder insertQuery = new StringBuilder("INSERT INTO your_target_table ("); // Replace with your target table name
        StringBuilder valuesPlaceholder = new StringBuilder(" VALUES (");
        StringBuilder updateSet = new StringBuilder();

        for (int i = 0; i < columnNames.size(); i++) {
            String columnName = columnNames.get(i);
            insertQuery.append(columnName);
            valuesPlaceholder.append("?");
            updateSet.append(columnName).append(" = EXCLUDED.").append(columnName);

            if (i < columnNames.size() - 1) {
                insertQuery.append(", ");
                valuesPlaceholder.append(", ");
                updateSet.append(", ");
            }
        }
        
        insertQuery.append(")").append(valuesPlaceholder.append(")").toString());
        insertQuery.append(" ON CONFLICT (").append(columnNames.get(0)).append(") DO UPDATE SET ").append(updateSet);

        try (PreparedStatement pstmt = targetConn.prepareStatement(insertQuery.toString())) {
            for (int i = 0; i < columnNames.size(); i++) {
                pstmt.setObject(i + 1, rs.getObject(columnNames.get(i))); // Use column name for getting value
            }
            pstmt.executeUpdate();
        }
    }

    private static List<String> readPrimaryKeyValuesFromExcel(String excelFilePath, int columnIndex) {
        List<String> values = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
             
            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet
            for (Row row : sheet) {
                Cell cell = row.getCell(columnIndex);
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    values.add(cell.getStringCellValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return values;
    }
}
