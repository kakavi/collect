package org.odk.collect.android.external.model;

/**
 * Created by victor on 29-Sep-15.
 */
public class TableAttr {
    String column;
    String datatype;
    boolean isPrimaryKey;

    public TableAttr(String column, boolean isPrimaryKey, String datatype) {
        this.column = column;
        this.isPrimaryKey = isPrimaryKey;
        this.datatype = datatype;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    @Override
    public String toString(){
        return column+": "+datatype+": "+isPrimaryKey;
    }

    @Override
    public boolean equals(Object object){
        if(object instanceof TableAttr){
            return ((TableAttr)object).column == column;
        }
        return false;
    }


}
