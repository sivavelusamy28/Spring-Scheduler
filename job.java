List<String> accountNumbers = Arrays.asList("123456789", "987654321", ...);

// Split into two lists
String[] col1Values = new String[accountNumbers.size()];
String[] col2Values = new String[accountNumbers.size()];

for (int i = 0; i < accountNumbers.size(); i++) {
    String acct = accountNumbers.get(i);
    col1Values[i] = acct.substring(0, 3);  // First 3 chars
    col2Values[i] = acct.substring(3, 9);  // Next 6 chars
}

// SQL leveraging the composite index
String sql = "SELECT a.* FROM accounts a " +
             "WHERE a.field1 = ? " +
             "AND a.field2 = ? " +
             "AND a.field3 = ? " +
             "AND (a.col1, a.col2) IN (" +
             "  SELECT c1.COLUMN_VALUE, c2.COLUMN_VALUE " +
             "  FROM (" +
             "    SELECT COLUMN_VALUE, ROWNUM as rn " +
             "    FROM TABLE(CAST(? AS SYS.ODCIVARCHAR2LIST))" +
             "  ) c1, (" +
             "    SELECT COLUMN_VALUE, ROWNUM as rn " +
             "    FROM TABLE(CAST(? AS SYS.ODCIVARCHAR2LIST))" +
             "  ) c2 " +
             "  WHERE c1.rn = c2.rn" +
             ")";

try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
    // Set your 3 other fields
    pstmt.setString(1, field1Value);
    pstmt.setString(2, field2Value);
    pstmt.setString(3, field3Value);
    
    // Create and set the two arrays
    ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
        "SYS.ODCIVARCHAR2LIST", connection);
    
    ARRAY col1Array = new ARRAY(desc, connection, col1Values);
    ARRAY col2Array = new ARRAY(desc, connection, col2Values);
    
    pstmt.setArray(4, col1Array);
    pstmt.setArray(5, col2Array);
    
    ResultSet rs = pstmt.executeQuery();
    // process results
}
