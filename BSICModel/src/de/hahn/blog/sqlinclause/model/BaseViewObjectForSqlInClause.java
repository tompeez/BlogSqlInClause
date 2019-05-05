package de.hahn.blog.sqlinclause.model;

import java.util.ArrayList;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.Variable;
import oracle.jbo.ViewCriteriaItem;
import oracle.jbo.ViewCriteriaItemValue;
import oracle.jbo.server.ViewDefImpl;
import oracle.jbo.server.ViewObjectImpl;

public class BaseViewObjectForSqlInClause extends ViewObjectImpl {
    private static ADFLogger _logger = ADFLogger.createADFLogger(BaseViewObjectForSqlInClause.class);

    // comma-separated list of custom operators. Each custom operator muast have a ',' at the end!
    private static final String CUSTOM_OPERATORS = "IN,";

    public BaseViewObjectForSqlInClause(String string, ViewDefImpl viewDefImpl) {
        super(string, viewDefImpl);
    }

    public BaseViewObjectForSqlInClause() {
        super();
    }

    /**
     * Check if a given criteria item tries to use an 'IN' operator using a bind parameter (comma seperated list of strings).
     * Create special SQL clause for 'IN' operator
     * @param aVCI Criteria item
     * @return where clause part for the criteria item
     */
    @Override
    public String getCriteriaItemClause(ViewCriteriaItem aVCI) {
        // we only handle the SQL 'IN' operator
        String sqloperator = aVCI.getOperator();
        // an den sqloperator noch ein Komma hängen, um ISBLANK von ISBANKIFTRUE zu unterscheiden
        boolean customOp = CUSTOM_OPERATORS.indexOf(sqloperator.concat(",")) >= 0;
        customOp |= sqloperator.indexOf("NVL") >= 0;
        if (customOp) {
            ArrayList<ViewCriteriaItemValue> lArrayList = aVCI.getValues();
            if (lArrayList != null && !lArrayList.isEmpty()) {
                // check if the criteria item has bind parameters (only the first if of interest here as the IN clause onlyallows one parameter)
                ViewCriteriaItemValue itemValue = (ViewCriteriaItemValue) lArrayList.get(0);
                if (itemValue.getIsBindVar()) {
                    // get variable and check if null values should be ignored for bind parameters
                    Variable lBindVariable = itemValue.getBindVariable();
                    Object obj = ensureVariableManager().getVariableValue(lBindVariable.getName());
                    boolean b = aVCI.isGenerateIsNullClauseForBindVariables();
                    if (b && obj == null) {
                        //if null values for bind variables should be ignored, use the default getCriteriaItemClause
                        return super.getCriteriaItemClause(aVCI);
                    }

                    try {
                        // we only handle strings data types for bind variables
                        String val = (String) obj;
                    } catch (Exception e) {
                        // The bind variabel has hte wrong type! Only Strings are allowed
                        _logger.warning("Bind variabel for SQL " + sqloperator +
                                        " clause is not of type String! -> No custom SQL clause created! (Class: " +
                                        obj.getClass() + ", Content: " + obj + ", Variable: " +
                                        lBindVariable.getName() + ", View: " + this.getName() + ")");
                        String s = ":" + lBindVariable.getName() + " = :" + lBindVariable.getName();
                        return s;
                    }

                    // only handle queries send to the db
                    if (aVCI.getViewCriteria()
                            .getRootViewCriteria()
                            .isCriteriaForQuery()) {
                        String sql_clause = null;
                        switch (sqloperator) {
                        case "IN":
                            sql_clause = createINClause(aVCI, lBindVariable);
                            break;
                        default:
                            _logger.severe("Unknown custom operator '" + sqloperator + "' found! -> do nothing!");
                            break;
                        }

                        return sql_clause;

                    } else {
                        // bind variable not set or
                        // for in memory we don't need to anything so just return '1=1'
                        return "1=1";
                    }
                }

            }

        }

        return super.getCriteriaItemClause(aVCI);
    }

    private String createINClause(ViewCriteriaItem aVCI, Variable lBindVariable) {
        // start build the sql 'IN' where clause (COLUMN is the name of the column, bindParam the name of the bind variable):
        // COLUMN IN (SELECT regexp_substr(:bindParam,'[^,]+',1,level) FROM dual CONNECT BY regexp_substr(:bindParam,'[^,]+',1,level) IS NOT NULL
        // get flagg to create an sql where clause which ignores the case of the bind parameter
        boolean upper = aVCI.isUpperColumns();
        String sql_in_clause = null;
        StringBuilder sql = new StringBuilder();
        if (upper) {
            sql.append("UPPER(");
        }
        sql.append(aVCI.getColumnNameForQuery());
        if (upper) {
            sql.append(")");
        }
        sql.append(" ").append(aVCI.getOperator());
        sql.append(" (select regexp_substr(");
        if (upper) {
            sql.append("UPPER(");
        }
        sql.append(":");
        sql.append(lBindVariable.getName());
        if (upper) {
            sql.append(")");
        }
        sql.append(",'[^,]+', 1, level) from dual connect by regexp_substr(");
        if (upper) {
            sql.append("UPPER(");
        }
        sql.append(":").append(lBindVariable.getName());
        if (upper) {
            sql.append(")");
        }
        sql.append(", '[^,]+', 1, level) is not null)");
        sql_in_clause = sql.toString();

        _logger.finest("generated SQL-IN clause: " + sql_in_clause);

        return sql_in_clause;
    }

}
