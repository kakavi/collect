package org.odk.collect.android.events;


import org.odk.collect.android.external.model.EntityData;
import org.odk.collect.android.external.model.ExEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by victor on 14-Oct-15.
 */
public class DbChangeEvent {

    List<EntityData> entityDataList = new ArrayList<EntityData>();
    private ExEntity entity;

    public DbChangeEvent(List<EntityData> entityDatas, ExEntity entity) {
        this.entityDataList = entityDatas;
        this.entity = entity;
    }

    public List<EntityData> getEntityDataList() {
        return entityDataList;
    }

    public ExEntity getEntity() {
        return entity;
    }
}
