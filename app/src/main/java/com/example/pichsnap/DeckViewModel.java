// android-app/app/src/main/java/com/pitchsnap/app/ui/DeckViewModel.java
package com.example.pichsnap;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pichsnap.SummaryResponse;
import com.example.pichsnap.DeckRepository;

import java.util.concurrent.Executors;

public class DeckViewModel extends AndroidViewModel {
    private final DeckRepository repo;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> status = new MutableLiveData<>("");
    private final MutableLiveData<SummaryResponse> summary = new MutableLiveData<>();

    public DeckViewModel(@NonNull Application app) {
        super(app);
        repo = new DeckRepository(app);
    }

    public LiveData<Boolean> loading() { return loading; }
    public LiveData<String> status() { return status; }
    public LiveData<SummaryResponse> summary() { return summary; }

    public void upload(Uri uri, String fileName) {
        loading.postValue(true);
        status.postValue("Uploading and summarizing…");
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                SummaryResponse sr = repo.summarize(uri, fileName);
                summary.postValue(sr);
                status.postValue("Done");
            } catch (Exception e) {
                status.postValue("Error: " + e.getMessage());
            } finally {
                loading.postValue(false);
            }
        });
    }
}
