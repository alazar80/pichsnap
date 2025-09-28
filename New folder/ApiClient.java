package com.example.pichsnap;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static volatile ApiService INSTANCE;
    private static final String AUTH_TOKEN = "supersecret_4d9f8a2b"; // 👈 match .env backend

    private ApiClient() {}

    public static ApiService get() {
        if (INSTANCE == null) {
            synchronized (ApiClient.class) {
                if (INSTANCE == null) {
                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(BuildConfig.DEBUG
                            ? HttpLoggingInterceptor.Level.BODY
                            : HttpLoggingInterceptor.Level.NONE);

                    // 🔑 Add Authorization header automatically
                    Interceptor authInterceptor = new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request newRequest = chain.request().newBuilder()
                                    .addHeader("Authorization", "Bearer " + AUTH_TOKEN)
                                    .build();
                            return chain.proceed(newRequest);
                        }
                    };

                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(90, TimeUnit.SECONDS)
                            .addInterceptor(authInterceptor) // 👈 attach token
                            .addInterceptor(log)
                            .build();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BuildConfig.BACKEND_BASE_URL) // from Gradle
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
