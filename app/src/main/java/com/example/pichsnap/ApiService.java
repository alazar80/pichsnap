// android-app/app/src/main/java/com/pitchsnap/app/network/ApiService.java
package com.example.pichsnap;

import com.example.pichsnap.SummaryResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("summarize")
    Call<SummaryResponse> summarize(
            @Header("Authorization") String bearerToken,
            @Part MultipartBody.Part filePart
    );
}
