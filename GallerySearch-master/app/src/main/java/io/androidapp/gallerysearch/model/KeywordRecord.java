package io.androidapp.gallerysearch.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class KeywordRecord {
    @PrimaryKey
    @NonNull
    private Long date;
    public final String word;

    public KeywordRecord(String word) {
        this.word = word;
        date = new Date().getTime();
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }
}