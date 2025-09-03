package com.example.pichsnap;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static volatile ApiService INSTANCE;
    private ApiClient() {}
    public static ApiService get() {
        if (INSTANCE == null) {
            synchronized (ApiClient.class) {
                if (INSTANCE == null) {
                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(BuildConfig.DEBUG
                            ? HttpLoggingInterceptor.Level.BODY
                            : HttpLoggingInterceptor.Level.NONE);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(90, TimeUnit.SECONDS)
                            .addInterceptor(log)
                            .build();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BuildConfig.BACKEND_BASE_URL) // <- from Gradle
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    INSTANCE = retrofit.create(ApiService.class);
                }
            }
        }
        return INSTANCE;
    }
}
