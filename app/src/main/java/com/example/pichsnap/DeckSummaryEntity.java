// android-app/app/src/main/java/com/pitchsnap/app/db/DeckSummaryEntity.java
package com.example.pichsnap;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "deck_summaries")
public class DeckSummaryEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String fileName;
    public long createdAt;
    public String json; // raw JSON of SummaryResponse
}
