package org.odk.collect.android.external.model;

/**
 * Created by kakavi on 5/9/2016.
 */
public class PrefillData {
    String value;
    String parentId;

    public PrefillData() {
//
    }

    public PrefillData(String value) {
        this.value = value;
    }

    @Override
    public String toString(){return value;}
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
