package de.hahn.blog.sqlinclause.model;

import oracle.jbo.server.ViewObjectImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Sun May 05 14:26:07 CEST 2019
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class EmployeesOfDepartmentsViewCriteriaViewImpl extends BaseViewObjectForSqlInClause {
    /**
     * This is the default constructor (do not remove).
     */
    public EmployeesOfDepartmentsViewCriteriaViewImpl() {
    }

    /**
     * Returns the variable value for pListOfValues.
     * @return variable value for pListOfValues
     */
    public String getpListOfValues() {
        return (String) ensureVariableManager().getVariableValue("pListOfValues");
    }

    /**
     * Sets <code>value</code> for variable pListOfValues.
     * @param value value to bind as pListOfValues
     */
    public void setpListOfValues(String value) {
        ensureVariableManager().setVariableValue("pListOfValues", value);
    }
}

