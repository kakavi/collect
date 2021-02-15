package org.odk.collect.android.external.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by victor on 20-Jan-16.
 */
public class ExEntity {
    /** Not-null value. */
    private String id;
    /** Not-null value. */
    private String name;
    /** Not-null value. */
    private String tableName;
    private String displayField;
    private String keyField;
//    separated by commas
    private String filterFld;
//    seperated by commas in order of filter flds
    private String filterValues;
//    can be one of and,or
    private String filterJoiner;
//note that filters are not persisted to the db
    private List<PreFillFilter> prefillFilterList = new ArrayList<>();

    private List<String> otherFields = new ArrayList<>();

    public ExEntity() {
    }

    public ExEntity(String id) {
        this.id = id;
    }

    public ExEntity(String id, String name, String tableName, String displayField,
                    String keyField, String filterFld, String filterValues, String filterJoiner,String otherFields) {
        this.id = id;
        this.name = name;
        this.tableName = tableName;
        this.displayField = displayField;
        this.keyField = keyField;
        this.filterFld = filterFld;
        this.filterValues = filterValues;
        this.filterJoiner = filterJoiner;
        if(!otherFields.isEmpty()){
            this.otherFields = new ArrayList<>(Arrays.asList(otherFields.split(",")));
        }
    }

    @Override
    public String toString(){
        return this.name;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDisplayField() {
        return displayField;
    }

    public void setDisplayField(String displayField) {
        this.displayField = displayField;
    }

    public String getKeyField() {
        return keyField;
    }

    public void setKeyField(String keyField) {
        this.keyField = keyField;
    }

    public String getFilterFld() {
        return filterFld;
    }

    public void setFilterFld(String filterFld) {
        this.filterFld = filterFld;
    }

    public String getFilterValues() {
        return filterValues;
    }

    public void setFilterValues(String filterValues) {
        this.filterValues = filterValues;
    }

    public String getFilterJoiner() {
        return filterJoiner;
    }

    public void setFilterJoiner(String filterJoiner) {
        this.filterJoiner = filterJoiner;
    }

    public List<PreFillFilter> getPrefillFilterList() {
        return prefillFilterList;
    }

    public void setPrefillFilterList(List<PreFillFilter> prefillFilterList) {
        this.prefillFilterList = prefillFilterList;
    }

    public List<String> getOtherFields() {
        return otherFields;
    }

    public void setOtherFields(List<String> otherFields) {
        this.otherFields = otherFields;
    }
}
