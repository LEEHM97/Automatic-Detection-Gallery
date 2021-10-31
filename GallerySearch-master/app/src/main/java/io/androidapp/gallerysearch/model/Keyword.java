package io.androidapp.gallerysearch.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Keyword {
    @PrimaryKey
    @NonNull
    public final String word;
    private Integer count;

    public Keyword(@NonNull String word) {
        this.word = word;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}