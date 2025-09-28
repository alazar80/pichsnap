// android-app/app/src/main/java/com/pitchsnap/app/db/AppDatabase.java
package com.example.pichsnap;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DeckSummaryEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DeckSummaryDao deckSummaryDao();
}
