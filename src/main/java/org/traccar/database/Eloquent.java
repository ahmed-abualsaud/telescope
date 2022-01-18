package org.traccar.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Eloquent {

    private static final Logger LOGGER = LoggerFactory.getLogger(Eloquent.class);
    private final Map<String, List<Integer>> indexMap = new HashMap<>();
    
    private String query;
    private String scope;
    private final String tableName;
    private List<Object> queryParams;
    private List<Object> scopeParams;
    private final DataSource dataSource;
    
    private Connection connection;
    private PreparedStatement statement;
    
    public Eloquent(String tableName, DataSource dataSource) {
        this.query = "";
        this.scope = "";
        this.tableName = tableName;
        this.dataSource = dataSource;
        this.queryParams = new ArrayList<>();
        this.scopeParams = new ArrayList<>();
    }
    
    public Eloquent where(String column, Object value) {
        if (scope.contains("WHERE")) {
            scope += " AND " + column + " = ?";
        } else {
            scope += "WHERE " + column + " = ?";
        }
        scopeParams.add(value);
        return this;
    }
    
    public Eloquent whereIn(String column, Object[] values) {
        if (scope.contains("WHERE")) {
            scope += " AND " + column + " IN (";
        } else {
            scope += "WHERE " + column + " IN (";
        }
        for(int i=0; i < values.length; i++) {
            scope += "?, ";
            scopeParams.add(values[i]);
        }
        scope = scope.substring(0, scope.length() - 2);
        scope += ")";
        return this;
    }
    
    public Eloquent select(String... columns) {
        query = "SELECT ";
        for(String column : columns){query += column + ", ";}
        query = query.substring(0, query.length() - 2);
        query += " FROM " + tableName;
        return this;
    }
    
    //==========================================================================

    public Map<String, Object> find(long id) {
        if (!query.contains("SELECT")) {
            query = "SELECT * FROM " + tableName;
        }
        query += " WHERE id=" + id + ";";
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(query);
            LOGGER.info(statement.toString());
        } catch (SQLException e) {
            LOGGER.error("Telescope database connection error: ", e);
            closeConnection(true);
        }
        return executeQueryAndGetResult(false);
    }
    
    public List<Map<String, Object>> get() {
        if (!query.contains("SELECT")) {
            query = "SELECT * FROM " + tableName;
        }
        query += " " + scope + ";";
        
        prepareQueryAndAssignParams();
        return executeQueryAndGetResults();
    }
    
    public Map<String, Object> first(boolean all) {
        if (!query.contains("SELECT")) {
            query = "SELECT * FROM " + tableName;
        }
        query += " " + scope + ";";
        
        prepareQueryAndAssignParams();
        return executeQueryAndGetResult(all);
    }
    
    public Map<String, Object> first() {
        if (!query.contains("SELECT")) {
            query = "SELECT * FROM " + tableName;
        }
        query += " " + scope + ";";
        
        prepareQueryAndAssignParams();
        return executeQueryAndGetResult(false);
    }
    
    public Map<String, Object> create(Map<String, Object> data) {
        query = "INSERT INTO " + tableName + "(";
        String values = "VALUES(";
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            query += entry.getKey() + ", ";
            values += "?, ";
            queryParams.add(entry.getValue());
        }
        query = query.substring(0, query.length() - 2);
        values = values.substring(0, values.length() - 2);
        query += ") " + values + ");";
        
        prepareQueryAndAssignParams();
        return executeUpdateAndGetResult();
    }
    
    public List<Map<String, Object>> update(Map<String, Object> data) {
        query = "UPDATE " + tableName + " SET ";
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            query += entry.getKey() + "= ? " + ", ";
            queryParams.add(entry.getValue());
        }
        query = query.substring(0, query.length() - 2);
        query += " " + scope + ";";
        
        prepareQueryAndAssignParams();
        return executeUpdateAndGetResults();
    }
    
    public boolean delete() {
        query = "DELETE FROM " + tableName + " " + scope + ";";
        
        prepareQueryAndAssignParams();
        return executeUpdate();
    }
    
    //==========================================================================
    
    private void prepareQueryAndAssignParams() {
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(query.trim(), Statement.RETURN_GENERATED_KEYS);
            queryParams.addAll(scopeParams);
            for (int i = 0; i < queryParams.size(); i++) {
                setGeneric(i + 1, queryParams.get(i));
            }
            LOGGER.info(statement.toString());
        } catch (SQLException e) {
            LOGGER.error("Telescope database connection error: ", e);
            closeConnection(true);
        }
    }
    
    private Map<String, Object> executeQueryAndGetResult(boolean all) {
        Map<String, Object> row = new LinkedHashMap<>();
        try {
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData resultMetaData = resultSet.getMetaData();
            if (!resultSet.next()) {return null;}
            String column;
            for (int i = 1; i <= resultMetaData.getColumnCount(); i++) {
                column = resultMetaData.getColumnLabel(i);
                if (all) {row.put(column, resultSet.getObject(i));}
                else {
                    if (!(column.equals("password") || column.equals("token") || column.equals("salt"))) {
                        row.put(column, resultSet.getObject(i));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Telescope execute query error: ", e);
        }
        closeConnection(false);
        if (row.isEmpty()) {return null;}
        return row;
    }
    
    private List<Map<String, Object>> executeQueryAndGetResults() {
        List<Map<String, Object>> rows = new LinkedList<>();
        Map<String, Object> row;
        try {
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData resultMetaData = resultSet.getMetaData();
            if(!resultSet.next()) {return null;}
            String column;
            do {
                row = new LinkedHashMap<>();
                for (int i = 1; i <= resultMetaData.getColumnCount(); i++) {
                    column = resultMetaData.getColumnLabel(i);
                    if (!(column.equals("password") || column.equals("token") || column.equals("salt"))) {
                        row.put(column, resultSet.getObject(i));
                    }
                }
                rows.add(row);
            } while (resultSet.next());
        } catch (SQLException e) {
            LOGGER.error("Telescope execute query error: ", e);
        }
        closeConnection(false);
        if (rows.isEmpty()) {return null;}
        return rows;
    }
    
    private Map<String, Object> executeUpdateAndGetResult() {
        Map<String, Object> row = new LinkedHashMap<>();
        try {
            if(statement.executeUpdate() == 0) {return null;}
            ResultSet resultSet = statement.getGeneratedKeys();
            resultSet.next();
            resultSet = statement.executeQuery("SELECT * FROM " + tableName + " WHERE id=" + resultSet.getLong(1) + ";");
            ResultSetMetaData resultMetaData = resultSet.getMetaData();
            resultSet.next();
            String column;
            for (int i = 1; i <= resultMetaData.getColumnCount(); i++) {
                column = resultMetaData.getColumnLabel(i);
                if (!(column.equals("password") || column.equals("token") || column.equals("salt"))) {
                    row.put(column, resultSet.getObject(i));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Telescope execute update error: ", e);
        }
        closeConnection(false);
        if (row.isEmpty()) {return null;}
        return row;
    }
    
    private List<Map<String, Object>> executeUpdateAndGetResults() {
        List<Map<String, Object>> rows = new LinkedList<>();
        Map<String, Object> row;
        try {
            if(statement.executeUpdate() == 0) {return null;}
            query = "SELECT * FROM " + tableName + " " + scope + ";";
            statement = connection.prepareStatement(query);
            for (int i = 0; i < scopeParams.size(); i++) {
                setGeneric(i + 1, scopeParams.get(i));
            }
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData resultMetaData = resultSet.getMetaData();
            String column;
            while (resultSet.next()) {
                row = new LinkedHashMap<>();
                for (int i = 1; i <= resultMetaData.getColumnCount(); i++) {
                    column = resultMetaData.getColumnLabel(i);
                    if (!(column.equals("password") || column.equals("token") || column.equals("salt"))) {
                        row.put(column, resultSet.getObject(i));
                    }
                }
                rows.add(row);
            }
        } catch (SQLException e) {
            LOGGER.error("Telescope execute update error: ", e);
        }
        closeConnection(false);
        if (rows.isEmpty()) {return null;}
        return rows;
    }
    
    private boolean executeUpdate() {
        try {
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Telescope execute update error: ", e);
            return false;
        }
        closeConnection(false);
        return true;
    }
    
    //==========================================================================
    
    private void setGeneric(int i, Object value) {
        try {
            if (value == null) {
                statement.setNull(i, Types.OTHER);
            } else {
                statement.setObject(i, value);
            }
        } catch (SQLException e) {
            LOGGER.error("Telescope prepare statement error: ", e);
            closeConnection(false);
        }
    }
    
    private void closeConnection(boolean withoutConnection) {
        try {
            if (withoutConnection) {statement.close();}
            else {statement.close(); connection.close();}
        } catch (SQLException e) {
            LOGGER.error("Telescope close conection error: ", e);
        }
    }
}
