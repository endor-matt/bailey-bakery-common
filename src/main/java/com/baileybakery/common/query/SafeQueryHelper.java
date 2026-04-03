package com.baileybakery.common.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Parameterized query builder for user-facing search operations.
 * Uses prepared statement parameters to prevent SQL injection.
 */
public class SafeQueryHelper {

    private static final Logger log = LoggerFactory.getLogger(SafeQueryHelper.class);

    private final String baseTable;
    private final List<String> conditions = new ArrayList<>();
    private final List<Object> parameters = new ArrayList<>();
    private String orderBy;
    private Integer limit;

    public SafeQueryHelper(String table) {
        this.baseTable = table;
    }

    /**
     * Adds a parameterized WHERE condition.
     */
    public SafeQueryHelper where(String field, String operator, Object value) {
        conditions.add(field + " " + operator + " ?");
        parameters.add(value);
        return this;
    }

    /**
     * Adds a parameterized text search condition.
     */
    public SafeQueryHelper search(String field, String searchTerm) {
        conditions.add(field + " LIKE ?");
        parameters.add("%" + searchTerm + "%");
        return this;
    }

    public SafeQueryHelper orderBy(String field, String direction) {
        if (direction.equalsIgnoreCase("ASC") || direction.equalsIgnoreCase("DESC")) {
            this.orderBy = field + " " + direction;
        }
        return this;
    }

    public SafeQueryHelper limit(int limit) {
        this.limit = limit;
        return this;
    }

    public String build() {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + baseTable);
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        if (orderBy != null) sql.append(" ORDER BY ").append(orderBy);
        if (limit != null) sql.append(" LIMIT ").append(limit);
        return sql.toString();
    }

    public List<Object> getParameters() {
        return List.copyOf(parameters);
    }
}
