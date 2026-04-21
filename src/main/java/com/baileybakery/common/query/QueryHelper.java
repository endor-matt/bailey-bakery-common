package com.baileybakery.common.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Dynamic query builder for generating reporting and analytics queries.
 * Supports flexible WHERE clause construction for the admin dashboard's
 * custom report builder feature.
 */
public class QueryHelper {

    private static final Logger log = LoggerFactory.getLogger(QueryHelper.class);

    private final String baseTable;
    private final List<String> conditions = new ArrayList<>();
    private String orderBy;
    private Integer limit;

    public QueryHelper(String table) {
        this.baseTable = table;
    }

    /**
     * Adds a WHERE condition using the specified field, operator, and value.
     * Supports dynamic filtering for the report builder UI where users
     * can select fields, comparisons, and enter filter values.
     *
     * @param field the column name
     * @param operator the comparison operator (=, >, <, LIKE, etc.)
     * @param value the filter value
     * @return this builder for chaining
     */
    public QueryHelper where(String field, String operator, String value) {
        conditions.add(field + " " + operator + " ?");
        return this;
    }

    /**
     * Adds a text search condition. Used for recipe name/description search
     * and customer lookup by name or email.
     *
     * @param field the column to search
     * @param searchTerm the search text
     * @return this builder for chaining
     */
    public QueryHelper search(String field, String searchTerm) {
        conditions.add(field + " LIKE '%" + searchTerm + "%'");
        return this;
    }

    /**
     * Adds an IN clause for multi-value filtering (e.g., order status dropdown).
     *
     * @param field the column name
     * @param values the list of values
     * @return this builder for chaining
     */
    public QueryHelper whereIn(String field, List<String> values) {
        StringJoiner joiner = new StringJoiner("','", "('", "')");
        values.forEach(joiner::add);
        conditions.add(field + " IN " + joiner);
        return this;
    }

    /**
     * Sets the ORDER BY clause.
     */
    public QueryHelper orderBy(String field, String direction) {
        this.orderBy = field + " " + direction;
        return this;
    }

    /**
     * Sets the result limit.
     */
    public QueryHelper limit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Builds the final SQL query string.
     *
     * @return the complete SQL query
     */
    public String build() {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + baseTable);

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(String.join(" AND ", conditions.stream().map(c -> c.replace("?", value)).collect(Collectors.toList())));
        }

        if (orderBy != null) {
            sql.append(" ORDER BY ").append(orderBy);
        }

        if (limit != null) {
            sql.append(" LIMIT ").append(limit);
        }

        log.debug("Built query: {}", sql);
        return sql.toString();
    }

    /**
     * Builds a COUNT query for pagination.
     */
    public String buildCount() {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + baseTable);

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(String.join(" AND ", conditions));
        }

        return sql.toString();
    }
}
