package org.odk.collect.android.restservices;

import android.net.TrafficStats;
import android.util.Base64;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by victor on 05-Jan-16.
 */
public class ServiceGenerator {
    public static String API_BASE_URL = "http://192.168.10.9:8090/snv-mis/";
    public static String PATH = "snv-mis";

    private static HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();

    private static okhttp3.OkHttpClient.Builder httpClientBuilder = new okhttp3.OkHttpClient.Builder();

    private static final Gson customDeserializer = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create();

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create(customDeserializer))
            .build();

    public static <S> S createAnonymousService(Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }

    public static <S> S createService(Class<S> serviceClass,String username, String password) {
        TrafficStats.setThreadStatsTag((int) Thread.currentThread().getId());
        if (username != null && password != null ) {
            String credentials = username + ":" + password;
            final String basic = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);

            okhttp3.OkHttpClient okHttpClient = httpClientBuilder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();

                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", basic)
                            .header("Accept", "applicaton/json")
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();

                    return chain.proceed(request);
                }
            }).connectTimeout(120, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS).addInterceptor(httpLoggingInterceptor).build();


            //fix for IllegalArgumentException: baseUrl must end in "/"
            String lastCharacter = API_BASE_URL.substring(API_BASE_URL.length() - 1);
            if(!lastCharacter.equals("/")){
                API_BASE_URL = API_BASE_URL + "/";
            }

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create(customDeserializer))
                    .build();

            return retrofit.create(serviceClass);

        }else{
            return retrofit.create(serviceClass);
        }

    }
}
