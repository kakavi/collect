package org.odk.collect.android.restservices;

import org.odk.collect.android.external.model.EntityData;
import org.odk.collect.android.external.model.ExEntity;
import org.odk.collect.android.external.model.PreFillFilter;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

/**
 * Created by victor on 30-Sep-15.
 */
public interface EntityRestClient {

    @GET("/{server}/odxRest/getPreloadEntities")
    Call<List<ExEntity>> getPreloadEntities(@Path("server") String myPath);

    @GET("/{server}/odxRest/getEntityData")
    Call<List<EntityData>> getEntityData(@Path("server") String myPath, @Query("tableName") String tableName, @Query("keyField") String keyField, @Query("displayField") String displayField);

    @POST("/{server}/odxRest/getFilteredEntityData")
    Call<List<EntityData>> downloadPrefillData(@Path("server") String myPath, @Body ExEntity exEntity);

    @POST("/{server}/odxRest/getFilters")
    Call<PreFillFilter> getPrefillFilters(@Path("server") String myPath, @Body List<PreFillFilter> filters);

    @POST("/{server}/odxRest/getFilteredEntityDataMap")
    Call<List<EntityData>> downloadFilteredEntityDataMap(@Path("server") String path,@Body ExEntity exEntity);
}
