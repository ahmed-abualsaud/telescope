package org.telescope.javel.framework.storage.database;

import org.telescope.javel.framework.helper.BoolUtil;
import org.telescope.javel.framework.helper.JSON;
import org.telescope.javel.framework.helper.NumUtil;
import org.telescope.server.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import javax.sql.DataSource;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.LinkedHashMap;

public class Eloquent {

    private static final Logger LOGGER = LoggerFactory.getLogger(Eloquent.class);
    private final Map<String, List<Integer>> indexMap = new HashMap<>();
    private final Map<String, Map<String, Object>> allProperties = Context.getModelManager().getAllProperties();

    private String query;
    private String select;
    private String joined;
    private String scoped;
    private final String tableName;
    private List<Object> queryParams;
    private List<Object> scopeParams;
    private List<String> jsonColumns;
    private final DataSource dataSource;
    private PreparedStatement statement;
    private Map<String, Object> properties;

    public Eloquent(String tableName, DataSource dataSource) {
        this.query = "";
        this.select = "";
        this.joined = "";
        this.scoped = "";
        this.tableName = tableName;
        this.dataSource = dataSource;
        this.queryParams = new ArrayList<>();
        this.scopeParams = new ArrayList<>();
        this.jsonColumns = new ArrayList<>();
        this.properties = new LinkedHashMap<>();
        this.properties.putAll(getProperties(tableName));
    }

    public Eloquent where(String column, Object value) {
        if (scoped.contains("WHERE")) {
            scoped += " AND " + column + " = ?";
        } else {
            scoped += "WHERE " + column + " = ?";
        }
        scopeParams.add(value);
        return this;
    }
    
    public Eloquent where(String column, String operator, Object value) {
        if (scoped.contains("WHERE")) {
            scoped += " AND " + column + " " + operator + " ?";
        } else {
            scoped += "WHERE " + column + " " + operator + " ?";
        }
        scopeParams.add(value);
        return this;
    }

    public Eloquent whereIn(String column, Object[] values) {
        if (scoped.contains("WHERE")) {
            scoped += " AND " + column + " IN (";
        } else {
            scoped += "WHERE " + column + " IN (";
        }
        for(int i=0; i < values.length; i++) {
            scoped += "?, ";
            scopeParams.add(values[i]);
        }
        scoped = scoped.substring(0, scoped.length() - 2);
        scoped += ")";
        return this;
    }

    public Eloquent select(String... columns) {
        select = "SELECT ";
        String[] col1, col2;
        for(String column : columns) {
            column = column.toLowerCase();
            if (column.contains("->")) {
                col1 = column.split("->");
                column = col1[0] + "->";
                if (col1[1].contains(" as ")) {
                   col2 = col1[1].split("\\s+as\\s+");
                   column += (col2[0].contains("$.") ? col2[0] + " as " : "\"$." + col2[0] + "\" as ") + col2[1];
                } else { column += (col1[1].contains("$.") ? col1[1] : "\"$." + col1[1] + "\"");}
                jsonColumns.add(column);
            }
            select += column + ", ";
        }
        select = select.substring(0, select.length() - 2);
        select += " FROM " + tableName;
        return this;
    }

    public Eloquent join(String secondTable, String firstColumn , String operator, String secondColumn) {
        mergeProperties(secondTable);
        joined += " INNER JOIN " + secondTable + " ON " + firstColumn + operator + secondColumn;
        return this;
    }
    
    public Eloquent leftJoin(String secondTable, String firstColumn , String operator, String secondColumn) {
        mergeProperties(secondTable);
        joined += " LEFT JOIN " + secondTable + " ON " + firstColumn + operator + secondColumn;
        return this;
    }
    
    //==========================================================================

    public Map<String, Object> find(Object id) {
        if (id == null) {return null;}
        if (select.isEmpty()) {select = "SELECT * FROM " + tableName;}
        query = select + " " + joined + " WHERE id=" + id + ";";
        
        Map<String, Object> ret = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            statement = connection.prepareStatement(query);
            LOGGER.info(statement.toString().substring(statement.toString().indexOf(":"), 
                                                       statement.toString().length()));
            ret = executeQueryAndGetResult(false);
            closeStatement();
        } catch (SQLException e) {}
        return ret;
    }
    
    public Map<String, Object> first(boolean all) {
        return getRow(all, 0);
    }
    
    public Map<String, Object> first() {
        return getRow(false, 0);
    }

    public Map<String, Object> last() {
        long count = count();
        if (count == 0) {return null;}
        return getRow(false, (count - 1));
    }

    public long count() {
        query = "SELECT COUNT(*) FROM " + tableName;
        query += " " + joined + " " + scoped + ";";
        
        Map<String, Object> ret = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            prepareQueryAndAssignParams(connection);
            ret = executeQueryAndGetResult(false);
            closeStatement();
        } catch (SQLException e) {}
        return ((Number) ret.get("COUNT(*)")).longValue();
    }

    public List<Map<String, Object>> get() {
        if (select.isEmpty()) {select = "SELECT * FROM " + tableName;}
        query = select + " " + joined + " " + scoped + ";";
        
        List<Map<String, Object>> ret = new LinkedList<>();
        try (Connection connection = dataSource.getConnection()) {
            prepareQueryAndAssignParams(connection);
            ret = executeQueryAndGetResults();
            closeStatement();
        } catch (SQLException e) {}
        return ret;
    }

    public Map<String, Object> create(Map<String, Object> data) {
        if (data == null ) {return null;}
        if (data.isEmpty()) {return data;}
        query = "INSERT INTO " + tableName + "(";
        String values = "VALUES(";
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            query += entry.getKey() + ", ";
            values += "?, ";
            if (properties != null && properties.get("json") != null && entry.getValue() != null &&
                properties.get("json").toString().contains(entry.getKey())) {
                queryParams.add(JSON.encode((Map<String, Object>) entry.getValue()));
            } else {queryParams.add(entry.getValue());}
        }
        query = query.substring(0, query.length() - 2);
        values = values.substring(0, values.length() - 2);
        query += ") " + values + ");";
        
        Map<String, Object> ret = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            prepareQueryAndAssignParams(connection);
            ret = executeUpdateAndGetResult();
            closeStatement();
        } catch (SQLException e) {}
        return ret;
    }
    
    public List<Map<String, Object>> update(Map<String, Object> data) {
        if (data == null) {return null;}
        if (data.isEmpty()) {
            List<Map<String, Object>> ret = new ArrayList<>(); 
            ret.add(new HashMap<>()); return ret;
        }
        String updated = "";
        if (properties != null && (Boolean) properties.get("timestamps"))
        {updated = "updated_at=CURRENT_TIMESTAMP, ";}
        query = "UPDATE " + tableName + " SET " + updated;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            query += entry.getKey() + "= ? " + ", ";
            if (properties != null && properties.get("json") != null && entry.getValue() != null &&
                properties.get("json").toString().contains(entry.getKey())) {
                queryParams.add(JSON.encode((Map<String, Object>) entry.getValue()));
            } else {queryParams.add(entry.getValue());}
        }
        query = query.substring(0, query.length() - 2);
        query += " " + scoped;
        
        List<Map<String, Object>> ret = new LinkedList<>();
        try (Connection connection = dataSource.getConnection()) {
            prepareQueryAndAssignParams(connection);
            ret = executeUpdateAndGetResults(connection);
            closeStatement();
        } catch (SQLException e) {}
        return ret;
    }
    
    public boolean delete() {
        query = "DELETE FROM " + tableName + " " + scoped + ";";
        boolean ret = false;
        try (Connection connection = dataSource.getConnection()) {
            prepareQueryAndAssignParams(connection);
            ret = executeUpdate();
            closeStatement();
        } catch (SQLException e) {}
        return ret;
    }
    
    //==========================================================================
    
    private void prepareQueryAndAssignParams(Connection connection) {
        try {
            statement = connection.prepareStatement(query.trim(), Statement.RETURN_GENERATED_KEYS);
            List<Object> mergedParams = new ArrayList<>();
            mergedParams.addAll(queryParams);
            mergedParams.addAll(scopeParams);
            for (int i = 0; i < mergedParams.size(); i++) {
                setGeneric(i + 1, mergedParams.get(i));
            }
            LOGGER.info(statement.toString().substring(statement.toString().indexOf(":"), 
                                                       statement.toString().length()));
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
            buildResult(all, resultSet, resultMetaData, row);
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
            do {row = new LinkedHashMap<>();
                buildResult(false, resultSet, resultMetaData, row);
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
            buildResult(false, resultSet, resultMetaData, row);
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
            query = "SELECT * FROM " + tableName + " " + scoped + ";";
            statement = connection.prepareStatement(query);
            for (int i = 0; i < scopeParams.size(); i++) {
                setGeneric(i + 1, scopeParams.get(i));
            }
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData resultMetaData = resultSet.getMetaData();
            while (resultSet.next()) {
                row = new LinkedHashMap<>();
                buildResult(false, resultSet, resultMetaData, row);
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

    public Map<String, Object> getRow(boolean all, Object skip) {
        if (select.isEmpty()) {select = "SELECT * FROM " + tableName;}
        query = select + " " + joined + " " + scoped + " LIMIT " + skip + ", 1;";
        
        Map<String, Object> ret = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            prepareQueryAndAssignParams(connection);
            ret = executeQueryAndGetResult(all);
            closeStatement();
        } catch (SQLException e) {}
        return ret;
    }

    private Map<String, Object> getProperties(String table) {
        for (Map.Entry<String, Map<String, Object>> entry : allProperties.entrySet()) {
            if ((entry.getValue().get("table") != null && 
                 entry.getValue().get("table").toString().equals(table)) ||
                (entry.getKey().equalsIgnoreCase(getPropertyKey(table)))) 
                {return entry.getValue();}
        }
        return null;
    }
    
    private void mergeProperties(String table) {
        Map<String, Object> joinedProps = getProperties(table);
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (entry.getKey().equals("json") || entry.getKey().equals("ignored")) {
                properties.put(entry.getKey(), (entry.getValue() == null ? "" : entry.getValue()) + 
                "," + (joinedProps.get(entry.getKey()) == null ? "" : joinedProps.get(entry.getKey())));
            }
        }
    }
    
    private String getPropertyKey(String table) {
        if (table.substring(table.length() - 3, table.length()).equals("ies"))
        {return table.substring(0, table.length() - 3) + "y";}
        else {return table.substring(0, table.length() - 1);}
    }

    private void buildResult(boolean all, ResultSet rs, ResultSetMetaData rmd, Map<String, Object> row) throws SQLException {
        String column; Object value;
        for (int i = 1; i <= rmd.getColumnCount(); i++) {
            column = rmd.getColumnLabel(i);
            value = rs.getObject(i);
            if (properties != null) {
                if (!all && properties.get("ignored") != null &&
                    properties.get("ignored").toString().contains(column)) {continue;}
                if (value != null && value.toString().length() >= 19 && 
                    value.toString().charAt(4) == '-' && value.toString().charAt(7) == '-' &&
                    value.toString().charAt(13) == ':' && value.toString().charAt(16) == ':') {
                    row.put(column, value.toString().substring(0, 19).replace('T', ' '));
                } 
                if (properties.get("json") != null &&
                    properties.get("json").toString().contains(column) && value != null) {
                    if (JSON.valid(value.toString()))
                    {row.put(column, JSON.decode(value.toString()));} 
                }
            }
            if (!row.containsKey(column)) {
                row.put(column, value);
                for (int j = 0; j < jsonColumns.size(); j++) {
                    if (jsonColumns.get(j).contains(column))
                    {row.put(column, BoolUtil.parse(NumUtil.parse(value)));}
                }
            }
        }
    }

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
