// android-app/app/src/main/java/com/pitchsnap/app/repo/DeckRepository.java
package com.example.pichsnap;

import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.WorkerThread;
import androidx.room.Room;

import com.google.gson.Gson;
import com.example.pichsnap.BuildConfig;
import com.example.pichsnap.SummaryResponse;
import com.example.pichsnap.AppDatabase;
import com.example.pichsnap.DeckSummaryDao;
import com.example.pichsnap.DeckSummaryEntity;
import com.example.pichsnap.ApiClient;
import com.example.pichsnap.ApiService;

import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class DeckRepository {
    private final Application app;
    private final ApiService api;
    private final DeckSummaryDao dao;
    private final Gson gson = new Gson();

    public DeckRepository(Application app) {
        this.app = app;
        this.api = ApiClient.get();
        AppDatabase db = Room.databaseBuilder(app, AppDatabase.class, "pitchsnap.db").build();
        this.dao = db.deckSummaryDao();
    }

    @WorkerThread
    public SummaryResponse summarize(Uri uri, String fileName) throws Exception {
        ContentResolver cr = app.getContentResolver();
        InputStream is = cr.openInputStream(uri);
        byte[] bytes = is.readAllBytes();

        RequestBody body = RequestBody.create(bytes, MediaType.parse("application/pdf"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", fileName, body);

        Response<SummaryResponse> resp = api.summarize("Bearer " + BuildConfig.BACKEND_AUTH_TOKEN, part).execute();
        if (!resp.isSuccessful() || resp.body() == null) {
            throw new IllegalStateException("API error: " + resp.code());
        }
        SummaryResponse sr = resp.body();

        DeckSummaryEntity e = new DeckSummaryEntity();
        e.fileName = fileName;
        e.createdAt = System.currentTimeMillis();
        e.json = gson.toJson(sr);
        dao.insert(e);

        return sr;
    }
}
