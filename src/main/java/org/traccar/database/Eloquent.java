package org.traccar.database;

import org.traccar.Main;
import org.traccar.api.modelconf.ModelConf;

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

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Properties;
import java.util.LinkedHashMap;

public class Eloquent {

    private static final Logger LOGGER = LoggerFactory.getLogger(Eloquent.class);
    private final Map<String, List<Integer>> indexMap = new HashMap<>();
    private final Map<String, Properties> modelConf = Main.getInjector().getInstance(ModelConf.class).getModelConf();
    private Properties properties = null;
    
    private String query;
    private String scope;
    private final String tableName;
    private List<Object> queryParams;
    private List<Object> scopeParams;
    private final DataSource dataSource;
    private PreparedStatement statement;
    
    public Eloquent(String tableName, DataSource dataSource) {
        this.query = "";
        this.scope = "";
        this.tableName = tableName;
        this.dataSource = dataSource;
        this.queryParams = new ArrayList<>();
        this.scopeParams = new ArrayList<>();
        for (Map.Entry<String, Properties> entry : modelConf.entrySet()) {
            if (entry.getKey().equals(tableName.substring(0, tableName.length() - 1).toLowerCase())) {
                this.properties = entry.getValue();
            }
        }
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
    
    public Eloquent join(String secondTable, String firstColumn , String operator, String secondColumn) {
        if (!query.contains("SELECT")) {
            query = "SELECT * FROM " + tableName;
        }
        query += " INNER JOIN " + secondTable + " ON " + firstColumn + operator + secondColumn;
        return this;
    }
    
    //==========================================================================

    public Map<String, Object> find(Object id) {
        if (!query.contains("SELECT")) {
            query = "SELECT * FROM " + tableName;
        }
        query += " WHERE id=" + id + ";";
        
        Map<String, Object> ret = new LinkedHashMap<>();
        try{try (Connection connection = dataSource.getConnection()) {
            statement = connection.prepareStatement(query);
            LOGGER.info(statement.toString());
            ret = executeQueryAndGetResult(false);
            closeStatement();
        }} catch (SQLException e) {}
        return ret;
    }
    
    public List<Map<String, Object>> get() {
        if (!query.contains("SELECT")) {
            query = "SELECT * FROM " + tableName;
        }
        query += " " + scope + ";";
        
        List<Map<String, Object>> ret = new LinkedList<>();
        try{try (Connection connection = dataSource.getConnection()) {
            prepareQueryAndAssignParams(connection);
            ret = executeQueryAndGetResults();
            closeStatement();
        }} catch (SQLException e) {}
        return ret;
    }
    
    public Map<String, Object> first(boolean all) {
        if (!query.contains("SELECT")) {
            query = "SELECT * FROM " + tableName;
        }
        query += " " + scope + ";";
        
        Map<String, Object> ret = new LinkedHashMap<>();
        try{try (Connection connection = dataSource.getConnection()) {
            prepareQueryAndAssignParams(connection);
            ret = executeQueryAndGetResult(all);
            closeStatement();
        }} catch (SQLException e) {}
        return ret;
    }
    
    public Map<String, Object> first() {
        if (!query.contains("SELECT")) {
            query = "SELECT * FROM " + tableName;
        }
        query += " " + scope + ";";
        
        Map<String, Object> ret = new LinkedHashMap<>();
        try{try (Connection connection = dataSource.getConnection()) {
            prepareQueryAndAssignParams(connection);
            ret = executeQueryAndGetResult(false);
            closeStatement();
        }} catch (SQLException e) {}
        return ret;
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
        
        Map<String, Object> ret = new LinkedHashMap<>();
        try{try (Connection connection = dataSource.getConnection()) {
            prepareQueryAndAssignParams(connection);
            ret = executeUpdateAndGetResult();
            closeStatement();
        }} catch (SQLException e) {}
        return ret;
    }
    
    public List<Map<String, Object>> update(Map<String, Object> data) {
        String updated = "";
        for (Map.Entry<String, Properties> entry : modelConf.entrySet()) {
            if (entry.getKey().equals(tableName.substring(0, tableName.length() - 1).toLowerCase()) &&
                entry.getValue().getProperty("timestamps") != null && 
                entry.getValue().getProperty("timestamps").equals("true")
            ) {updated = "updated_at=CURRENT_TIMESTAMP, ";}
        }
        query = "UPDATE " + tableName + " SET " + updated;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            query += entry.getKey() + "= ? " + ", ";
            queryParams.add(entry.getValue());
        }
        query = query.substring(0, query.length() - 2);
        query += " " + scope;
        
        List<Map<String, Object>> ret = new LinkedList<>();
        try{try (Connection connection = dataSource.getConnection()) {
            prepareQueryAndAssignParams(connection);
            ret = executeUpdateAndGetResults(connection);
            closeStatement();
        }} catch (SQLException e) {}
        return ret;
    }
    
    public boolean delete() {
        query = "DELETE FROM " + tableName + " " + scope + ";";
        boolean ret = false;
        try{try (Connection connection = dataSource.getConnection()) {
            prepareQueryAndAssignParams(connection);
            ret = executeUpdate();
            closeStatement();
        }} catch (SQLException e) {}
        return ret;
    }
    
    //==========================================================================
    
    private void prepareQueryAndAssignParams(Connection connection) {
        try {
            statement = connection.prepareStatement(query.trim(), Statement.RETURN_GENERATED_KEYS);
            queryParams.addAll(scopeParams);
            for (int i = 0; i < queryParams.size(); i++) {
                setGeneric(i + 1, queryParams.get(i));
            }
            LOGGER.info(statement.toString());
        } catch (SQLException e) {
            LOGGER.error("Database connection error: ", e);
            closeStatement();
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
                if (!all && properties != null && properties.getProperty("ignored") != null &&
                    properties.getProperty("ignored").contains(column)) {continue;} 
                row.put(column, resultSet.getObject(i));
            }
        } catch (SQLException e) {
            LOGGER.error("Execute query error: ", e);
            closeStatement();
        }
        if (row.isEmpty()) {return null;}
        return row;
    }
    
    private List<Map<String, Object>> executeQueryAndGetResults() {
        List<Map<String, Object>> rows = new LinkedList<>();
        try {
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData resultMetaData = resultSet.getMetaData();
            if(!resultSet.next()) {return null;}
            Map<String, Object> row;
            String column;
            do {row = new LinkedHashMap<>();
                for (int i = 1; i <= resultMetaData.getColumnCount(); i++) {
                    column = resultMetaData.getColumnLabel(i);
                    if (properties != null && properties.getProperty("ignored") != null &&
                        properties.getProperty("ignored").contains(column)) {continue;} 
                    row.put(column, resultSet.getObject(i));
                }
                rows.add(row);
            } while (resultSet.next());
        } catch (SQLException e) {
            LOGGER.error("Execute query error: ", e);
            closeStatement();
        }
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
                if (properties != null && properties.getProperty("ignored") != null &&
                    properties.getProperty("ignored").contains(column)) {continue;} 
                row.put(column, resultSet.getObject(i));
            }
        } catch (SQLException e) {
            LOGGER.error("Execute update error: ", e);
            closeStatement();
        }
        if (row.isEmpty()) {return null;}
        return row;
    }
    
    private List<Map<String, Object>> executeUpdateAndGetResults(Connection connection) {
        List<Map<String, Object>> rows = new LinkedList<>();
        try {
            if(statement.executeUpdate() == 0) {return null;}
            Map<String, Object> row;
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
                    if (properties != null && properties.getProperty("ignored") != null &&
                        properties.getProperty("ignored").contains(column)) {continue;} 
                    row.put(column, resultSet.getObject(i));
                }
                rows.add(row);
            }
        } catch (SQLException e) {
            LOGGER.error("Execute update error: ", e);
            closeStatement();
        }
        if (rows.isEmpty()) {return null;}
        return rows;
    }
    
    private boolean executeUpdate() {
        try {
            if (statement.executeUpdate() > 0) {return true;}
        } catch (SQLException e) {
            LOGGER.error("Execute update error: ", e);
            closeStatement();
        }
        return false;
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
            LOGGER.error("Assign values error: ", e);
            closeStatement();
        }
    }
    
    private void closeStatement() {
        try {
            statement.close(); 
        } catch (SQLException e) {
            LOGGER.error("Close conection error: ", e);
        }
    }
}
