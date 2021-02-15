package org.odk.collect.android.external.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kakavi on 5/6/2016.
 */
public class PreFillFilter {
    String field;
    String value;
    int filterNumber;
    String tableName;
    List<PrefillData> dataList = new ArrayList<>();

    public PreFillFilter() {
//
    }

    public PreFillFilter(String field, String value, int filterNumber, List<PrefillData> dataList, String tableName) {
        this.field = field;
        this.value = value;
        this.filterNumber = filterNumber;
        this.dataList = dataList;
        this.tableName = tableName;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<PrefillData> getDataList() {
        return dataList;
    }

    public int getFilterNumber() {
        return filterNumber;
    }

    public void setFilterNumber(int filterNumber) {
        this.filterNumber = filterNumber;
    }

    public void setDataList(List<PrefillData> dataList) {
        this.dataList = dataList;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
