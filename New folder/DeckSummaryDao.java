// android-app/app/src/main/java/com/pitchsnap/app/db/DeckSummaryDao.java
package com.example.pichsnap;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DeckSummaryDao {
    @Insert
    long insert(DeckSummaryEntity entity);

    @Query("SELECT * FROM deck_summaries ORDER BY createdAt DESC")
    List<DeckSummaryEntity> list();
}
