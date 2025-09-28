package com.example.pichsnap;

import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;
import androidx.annotation.WorkerThread;
import androidx.room.Room;
import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class DeckRepository {
    private final Application app;
    private final DeckSummaryDao dao;
    private final Gson gson = new Gson();

    public DeckRepository(Application app) {
        this.app = app;
        AppDatabase db = Room.databaseBuilder(app, AppDatabase.class, "pichsnap.db")
                .fallbackToDestructiveMigration()
                .build();
        this.dao = db.deckSummaryDao();
    }

    @WorkerThread
    public SummaryResponse summarize(Uri fileUri, String fileName) throws Exception {
        // read the file into memory (simple & reliable for small PDFs/images)
        byte[] bytes = readAll(app.getContentResolver(), fileUri);
        RequestBody body = RequestBody.create(MediaType.parse("application/pdf"), bytes);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", fileName, body);

        // call backend with token from BuildConfig
        Call<SummaryResponse> call = ApiClient.get().summarize(filePart);


        Response<SummaryResponse> resp = call.execute();
        if (!resp.isSuccessful() || resp.body() == null) {
            throw new IllegalStateException("API error: " + resp.code());
        }

        SummaryResponse sr = resp.body();

        // persist raw JSON for history
        DeckSummaryEntity e = new DeckSummaryEntity();
        e.fileName = fileName;
        e.createdAt = System.currentTimeMillis();
        e.json = gson.toJson(sr);
        dao.insert(e);

        return sr;
    }

    private static byte[] readAll(ContentResolver cr, Uri uri) throws Exception {
        try (InputStream in = cr.openInputStream(uri);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (in == null) throw new IllegalStateException("Cannot open file: " + uri);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) >= 0) out.write(buf, 0, n);
            return out.toByteArray();
        }
    }
}
