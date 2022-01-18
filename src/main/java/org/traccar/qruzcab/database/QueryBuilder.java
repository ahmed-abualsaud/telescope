package org.traccar.qruzcab.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryBuilder.class);
    private final Map<String, List<Integer>> indexMap = new HashMap<>();
    
    private String query;
    private String whereString;
    private final String tableName;
    private final DataSource dataSource;
    private Map<String, Object> queryParams;
    
    private Connection connection;
    private PreparedStatement statement;
    
    public QueryBuilder(String tableName, DataSource dataSource) {
        this.query = "";
        this.whereString = "";
        this.tableName = tableName;
        this.dataSource = dataSource;
        this.queryParams = new HashMap<>();
    }
    
    public QueryBuilder where(String column, Object value) {
        if (whereString.contains("WHERE")) {
            whereString += " AND " + column + " = :" + column;
        } else {
            whereString += "WHERE " + column + " = :" + column;
        }
        queryParams.put(column, value);
        return this;
    }
    
    public void update(Map<String, Object> data) {
        query = "UPDATE " + tableName + " SET ";
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            query += entry.getKey() + " = :" + entry.getKey() + ", ";
            queryParams.put(entry.getKey(), entry.getValue());
        }
        query = query.substring(0, query.length() - 2);
        query += " " + whereString + ";";
        
        prepareQuery();
        assignQueryParams();
        executeQuery();
    }
    
    private void prepareQuery() {
        String parsedQuery = parse(query.trim(), indexMap);
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(parsedQuery);
        } catch (SQLException e) {
            LOGGER.error("Qruz Cab database connection error: ", e);
            closeConnection(true);
        }
    }
    
    private void assignQueryParams() {
        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            setGeneric(entry.getKey(), entry.getValue());
        }
    }
    
    public void executeQuery() {
        if (query != null) {
            try {
                statement.execute();
            } catch (SQLException e) {
                LOGGER.error("Qruz Cab execute query error: ", e);
            }
        }
        closeConnection(false);
    }
    
    public void setGeneric(String name, Object value) {
        for (int i : indexes(name)) {
            try {
                if (value == null) {
                    statement.setNull(i, Types.OTHER);
                } else {
                    statement.setObject(i, value);
                }
            } catch (SQLException e) {
                LOGGER.error("Qruz Cab prepare statement error: ", e);
                closeConnection(false);
            }
        }
    }
    
    public void closeConnection(boolean withoutConnection) {
        try {
            if (withoutConnection) {statement.close();}
            else {statement.close(); connection.close();}
        } catch (SQLException e) {
            LOGGER.error("Qruz Cab close conection error: ", e);
        }
    }
    
    private List<Integer> indexes(String name) {
        name = name.toLowerCase();
        List<Integer> result = indexMap.get(name);
        if (result == null) {
            result = new LinkedList<>();
        }
        return result;
    }
    
    private static String parse(String query, Map<String, List<Integer>> paramMap) {

        int length = query.length();
        StringBuilder parsedQuery = new StringBuilder(length);
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int index = 1;

        for (int i = 0; i < length; i++) {

            char c = query.charAt(i);

            // String end
            if (inSingleQuote) {
                if (c == '\'') {
                    inSingleQuote = false;
                }
            } else if (inDoubleQuote) {
                if (c == '"') {
                    inDoubleQuote = false;
                }
            } else {

                // String begin
                if (c == '\'') {
                    inSingleQuote = true;
                } else if (c == '"') {
                    inDoubleQuote = true;
                } else if (c == ':' && i + 1 < length
                        && Character.isJavaIdentifierStart(query.charAt(i + 1))) {

                    // Identifier name
                    int j = i + 2;
                    while (j < length && Character.isJavaIdentifierPart(query.charAt(j))) {
                        j++;
                    }

                    String name = query.substring(i + 1, j);
                    c = '?';
                    i += name.length();
                    name = name.toLowerCase();

                    // Add to list
                    List<Integer> indexList = paramMap.computeIfAbsent(name, k -> new LinkedList<>());
                    indexList.add(index);

                    index++;
                }
            }

            parsedQuery.append(c);
        }

        return parsedQuery.toString();
    }
}
