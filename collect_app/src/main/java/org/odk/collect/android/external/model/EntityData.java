package org.odk.collect.android.external.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import timber.log.Timber;

import java.util.*;

/**
 * Created by victor on 01-Oct-15.
 */
public class EntityData implements Parcelable {
    public static String DISPLAY_FIELD_SEPERATORE = ",";
    String keyField;
    Map<String,String> otherFields = new LinkedHashMap<>();
    String displayField;

    public EntityData() {
    }

    public EntityData(String keyField,Map<String,String> otherFields) {
        this.keyField = keyField;
        this.otherFields = otherFields;
        this.displayField = displayField;
    }

    public String getKeyField() {
        return keyField;
    }

    public void setKeyField(String keyField) {
        this.keyField = keyField;
    }

    public Map<String,String>  getOtherFields() {
        return otherFields;
    }

    public void setOtherFields(Map<String,String> otherFields) {
        this.otherFields = otherFields;
    }

    public String getDisplayField() {
        return displayField;
    }

    public void setDisplayField(String displayField) {
        this.displayField = displayField;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(keyField);
        parcel.writeMap(otherFields);
        parcel.writeString(displayField);
    }

    public static final Creator<EntityData> CREATOR = new Creator<EntityData>() {
        @Override
        public EntityData createFromParcel(Parcel parcel) {
            EntityData entityData = new EntityData();
            entityData.keyField = parcel.readString();
            parcel.readMap(entityData.otherFields,String.class.getClassLoader());
            entityData.displayField = parcel.readString();
            return entityData;
        }

        @Override
        public EntityData[] newArray(int size) {
            return new EntityData[size];
        }
    };

    public Map<String,String> getDisplayFields(ExEntity exEntity){
        List<String> displayFlds = Arrays.asList(exEntity.getDisplayField().split(EntityData.DISPLAY_FIELD_SEPERATORE));
        Log.e("EntityData",displayFlds.toString());
        Map<String,String> resultFlds = new LinkedHashMap<>();

        //temporary
  /*      String sss = otherFields.get(displayFlds.get(0));

        String[] list = sss.split(",");

        for (int i=0; i<displayFlds.size();i++){
                resultFlds.put(displayFlds.get(i), list[i]);
        }*/

        for(String str:displayFlds){
            resultFlds.put(str,otherFields.get(str));
        }
        return resultFlds;
    }

}
