// android-app/app/src/main/java/com/pitchsnap/app/ui/MainActivity.java
package com.example.pichsnap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.example.pichsnap.R;
import com.example.pichsnap.SummaryResponse;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DeckViewModel vm;
    private TextView tvSummary, tvScore, tvRisks, tvQuestions, tvStatus;
    private ProgressBar progress;
    private SummaryResponse last;

    private final ActivityResultLauncher<String[]> docPicker =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onPicked);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vm = new ViewModelProvider(this).get(DeckViewModel.class);

        tvSummary = findViewById(R.id.tvSummary);
        tvScore = findViewById(R.id.tvScore);
        tvRisks = findViewById(R.id.tvRisks);
        tvQuestions = findViewById(R.id.tvQuestions);
        tvStatus = findViewById(R.id.tvStatus);
        progress = findViewById(R.id.progress);

        MaterialButton btnUpload = findViewById(R.id.btnUpload);
        MaterialButton btnDraftEmail = findViewById(R.id.btnDraftEmail);
        MaterialButton btnShare = findViewById(R.id.btnShare);

        btnUpload.setOnClickListener(v ->
                docPicker.launch(new String[]{"application/pdf"}));

        btnDraftEmail.setOnClickListener(v -> {
            if (last != null && last.drafts != null) {
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("message/rfc822");
                send.putExtra(Intent.EXTRA_SUBJECT, "Quick thoughts on your deck");
                send.putExtra(Intent.EXTRA_TEXT, last.drafts.email);
                startActivity(Intent.createChooser(send, "Send email"));
            }
        });

        btnShare.setOnClickListener(v -> {
            if (last != null) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, composeShareText(last));
                startActivity(Intent.createChooser(share, "Share via"));
            }
        });

        vm.loading().observe(this, isLoading -> progress.setVisibility(isLoading ? View.VISIBLE : View.GONE));
        vm.status().observe(this, s -> tvStatus.setText(s));
        vm.summary().observe(this, this::render);
    }

    private void onPicked(Uri uri) {
        if (uri == null) return;
        String name = queryDisplayName(uri);
        if (name == null) name = "deck.pdf";
        vm.upload(uri, name);
    }

    private String queryDisplayName(Uri uri) {
        try (var c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return c.getString(idx);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void render(SummaryResponse sr) {
        last = sr;

        // Bullets
        StringBuilder sb = new StringBuilder();
        if (sr.bullets != null) {
            for (String b : sr.bullets) sb.append("• ").append(b).append("\n");
        }
        tvSummary.setText(sb.toString().trim());

        // Scorecard
        if (sr.scorecard != null) {
            tvScore.setText("Team: " + sr.scorecard.team +
                    "  |  Market: " + sr.scorecard.market +
                    "  |  Traction: " + sr.scorecard.traction +
                    "  |  Clarity: " + sr.scorecard.clarity);
        } else tvScore.setText("-");

        // Risks & Questions
        tvRisks.setText(joinLines(sr.risks));
        tvQuestions.setText(joinLines(sr.questions));
    }

    private String joinLines(List<String> items) {
        if (items == null) return "-";
        List<String> dots = new ArrayList<>();
        for (String s : items) dots.add("• " + s);
        return String.join("\n", dots);
    }

    private String composeShareText(SummaryResponse s) {
        StringBuilder out = new StringBuilder();
        out.append("PitchSnap Summary");
        if (s.title != null && !s.title.isEmpty()) out.append(" — ").append(s.title);
        out.append("\n\n");

        out.append("Summary:\n").append(joinLines(s.bullets)).append("\n\n");
        if (s.scorecard != null) {
            out.append("Scorecard (0–10): Team ")
               .append(s.scorecard.team).append(", Market ").append(s.scorecard.market)
               .append(", Traction ").append(s.scorecard.traction).append(", Clarity ").append(s.scorecard.clarity)
               .append("\n\n");
        }
        out.append("Risks:\n").append(joinLines(s.risks)).append("\n\n");
        out.append("Questions:\n").append(joinLines(s.questions)).append("\n");
        return out.toString();
    }
}
