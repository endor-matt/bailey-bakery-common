package com.baileybakery.common.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Dynamic query builder for generating reporting and analytics queries.
 * Supports flexible WHERE clause construction for the admin dashboard's
 * custom report builder feature.
 */
public class QueryHelper {

    private static final Logger log = LoggerFactory.getLogger(QueryHelper.class);

    /** Allowlist of SQL comparison operators accepted from the report builder UI. */
    private static final Set<String> ALLOWED_OPERATORS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("=", "!=", "<>", "<", ">", "<=", ">=")));

    /** Allowlist of ORDER BY directions. */
    private static final Set<String> ALLOWED_DIRECTIONS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("ASC", "DESC")));

    private final String baseTable;
    private final List<String> conditions = new ArrayList<>();
    private final List<Object> parameters = new ArrayList<>();
    private String orderBy;
    private Integer limit;

    public QueryHelper(String table) {
        this.baseTable = table;
    }

    /**
     * Adds a WHERE condition using the specified field, operator, and value.
     * Supports dynamic filtering for the report builder UI where users
     * can select fields, comparisons, and enter filter values.
     * Values are stored as bind parameters to prevent SQL injection.
     *
     * @param field the column name (must not be user-controlled)
     * @param operator the comparison operator (validated against an allowlist)
     * @param value the filter value (bound as a parameter, not concatenated)
     * @return this builder for chaining
     */
    public QueryHelper where(String field, String operator, Object value) {
        if (!ALLOWED_OPERATORS.contains(operator)) {
            throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
        conditions.add(field + " " + operator + " ?");
        parameters.add(value);
        return this;
    }

    /**
     * Adds a text search condition. Used for recipe name/description search
     * and customer lookup by name or email.
     * The search term is bound as a parameter to prevent SQL injection.
     *
     * @param field the column to search (must not be user-controlled)
     * @param searchTerm the search text (bound as a parameter)
     * @return this builder for chaining
     */
    public QueryHelper search(String field, String searchTerm) {
        conditions.add(field + " LIKE ?");
        parameters.add("%" + searchTerm + "%");
        return this;
    }

    /**
     * Adds an IN clause for multi-value filtering (e.g., order status dropdown).
     * Values are bound as parameters to prevent SQL injection.
     *
     * @param field the column name (must not be user-controlled)
     * @param values the list of values (each bound as a parameter)
     * @return this builder for chaining
     */
    public QueryHelper whereIn(String field, List<String> values) {
        StringJoiner placeholders = new StringJoiner(",", "(", ")");
        values.forEach(v -> {
            placeholders.add("?");
            parameters.add(v);
        });
        conditions.add(field + " IN " + placeholders);
        return this;
    }

    /**
     * Sets the ORDER BY clause.
     * Both field and direction are validated against allowlists to prevent injection.
     */
    public QueryHelper orderBy(String field, String direction) {
        String normalizedDirection = direction.toUpperCase();
        if (!ALLOWED_DIRECTIONS.contains(normalizedDirection)) {
            throw new IllegalArgumentException("Unsupported sort direction: " + direction);
        }
        this.orderBy = field + " " + normalizedDirection;
        return this;
    }

    /**
     * Returns the ordered list of bind parameter values corresponding to the
     * placeholders in the query produced by {@link #build()}.
     * Callers must bind these values to a PreparedStatement in order.
     *
     * @return unmodifiable list of parameter values
     */
    public List<Object> getParameters() {
        return Collections.unmodifiableList(parameters);
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
            sql.append(String.join(" AND ", conditions));
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
