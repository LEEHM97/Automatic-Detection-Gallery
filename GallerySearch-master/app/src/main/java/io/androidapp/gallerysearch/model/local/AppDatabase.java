package io.androidapp.gallerysearch.model.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import io.androidapp.gallerysearch.model.Keyword;
import io.androidapp.gallerysearch.model.KeywordRecord;
import io.androidapp.gallerysearch.model.Photo;

@Database(entities = {Keyword.class, Photo.class, KeywordRecord.class}, version = 1)
@TypeConverters(io.androidapp.gallerysearch.model.local.TypeConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract KeywordDao keywordDao();
    public abstract PhotoDao photoDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = buildDatabase(context);
        }
        return INSTANCE;
    }

    private static AppDatabase buildDatabase(Context context) {
        return Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "MyApp.db").build();
    }
}
