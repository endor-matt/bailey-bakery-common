package com.baileybakery.common.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Dynamic query builder for generating reporting and analytics queries.
 * Supports flexible WHERE clause construction for the admin dashboard's
 * custom report builder feature.
 *
 * All user-supplied values are bound as positional parameters (?) to prevent
 * SQL injection. Call {@link #getParameters()} to retrieve the binding list
 * for use with a PreparedStatement.
 */
public class QueryHelper {

    private static final Logger log = LoggerFactory.getLogger(QueryHelper.class);

    /** Operators accepted in WHERE conditions. */
    private static final Set<String> ALLOWED_OPERATORS = new HashSet<>(Arrays.asList(
            "=", "!=", "<>", ">", "<", ">=", "<=", "LIKE"
    ));

    /** Sort directions accepted by ORDER BY. */
    private static final Set<String> ALLOWED_DIRECTIONS = new HashSet<>(Arrays.asList(
            "ASC", "DESC"
    ));

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
     *
     * The value is stored as a positional parameter and never interpolated
     * into the SQL string, preventing SQL injection.
     *
     * @param field    the column name (must be an application-controlled identifier)
     * @param operator the comparison operator; must be one of =, !=, <>, >, <, >=, <=, LIKE
     * @param value    the filter value, bound as a prepared-statement parameter
     * @return this builder for chaining
     * @throws IllegalArgumentException if operator is not in the allowed set
     */
    public QueryHelper where(String field, String operator, String value) {
        if (!ALLOWED_OPERATORS.contains(operator.toUpperCase())) {
            throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
        conditions.add(field + " " + operator.toUpperCase() + " ?");
        parameters.add(value);
        return this;
    }

    /**
     * Adds a text search condition. Used for recipe name/description search
     * and customer lookup by name or email.
     *
     * The searchTerm is wrapped in % wildcards and bound as a parameter.
     *
     * @param field      the column to search
     * @param searchTerm the search text, bound as a prepared-statement parameter
     * @return this builder for chaining
     */
    public QueryHelper search(String field, String searchTerm) {
        conditions.add(field + " LIKE ?");
        parameters.add("%" + searchTerm + "%");
        return this;
    }

    /**
     * Adds an IN clause for multi-value filtering (e.g., order status dropdown).
     *
     * Each element of values is bound as a separate positional parameter.
     *
     * @param field  the column name
     * @param values the list of values, each bound as a prepared-statement parameter
     * @return this builder for chaining
     */
    public QueryHelper whereIn(String field, List<String> values) {
        StringJoiner placeholders = new StringJoiner(",", "(", ")");
        for (String v : values) {
            placeholders.add("?");
            parameters.add(v);
        }
        conditions.add(field + " IN " + placeholders);
        return this;
    }

    /**
     * Sets the ORDER BY clause.
     *
     * The direction is validated against an allowlist (ASC/DESC) to prevent
     * ORDER BY clause injection.
     *
     * @param field     the column name (must be an application-controlled identifier)
     * @param direction sort direction; must be ASC or DESC (case-insensitive)
     * @return this builder for chaining
     * @throws IllegalArgumentException if direction is not ASC or DESC
     */
    public QueryHelper orderBy(String field, String direction) {
        if (!ALLOWED_DIRECTIONS.contains(direction.toUpperCase())) {
            throw new IllegalArgumentException("Sort direction must be ASC or DESC, got: " + direction);
        }
        this.orderBy = field + " " + direction.toUpperCase();
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
     * Builds the final SQL query string with positional ? placeholders.
     * Bind the values returned by {@link #getParameters()} to a PreparedStatement
     * in order before executing this query.
     *
     * @return the complete SQL query with ? placeholders
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

        log.debug("Built query template: {}", sql);
        return sql.toString();
    }

    /**
     * Returns the ordered list of parameter values to bind to the PreparedStatement
     * produced by {@link #build()}.
     *
     * @return immutable copy of the parameter list
     */
    public List<Object> getParameters() {
        return List.copyOf(parameters);
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
