package io.androidapp.gallerysearch.model.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;
import java.util.Map;

import io.androidapp.gallerysearch.model.Keyword;
import io.androidapp.gallerysearch.model.KeywordRecord;

@Dao
public abstract class KeywordDao {

    @Query("SELECT * from KeywordRecord ORDER BY date DESC LIMIT 20")
    public abstract List<KeywordRecord> getRecord();

    @Query("SELECT * from Keyword ORDER BY count DESC")
    public abstract List<Keyword> getKeywordsPopular();

    @Query("SELECT * from Keyword WHERE word LIKE '%' || :tag || '%' ORDER BY count DESC")
    public abstract List<Keyword> getKeywordsContain(String tag);

    @Query("SELECT * from Keyword WHERE word = :tag")
    public abstract Keyword getKeyword(String tag);

    @Query("UPDATE Keyword SET count = count+:c WHERE word = :tag")
    public abstract void addCount(String tag, int c);

    @Query("UPDATE Keyword SET count = count - :c WHERE word = :tag")
    public abstract void decreaseCount(String tag, int c);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(Keyword keyword);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(KeywordRecord keyword);

    @Query("DELETE FROM Keyword WHERE count = 0")
    public abstract void deleteZeroCount();

    //최근검색어 중복제거
    @Query("SELECT * FROM KeywordRecord WHERE word = :tag")
    public abstract KeywordRecord getKeywordRecord(String tag);

    @Query("DELETE FROM KeywordRecord WHERE word = :tag")
    public abstract void deleteKeywordRecord(String tag);

    @Transaction
    public void insertKeywordRecord(KeywordRecord keyword){
        if(getKeywordRecord(keyword.word) != null){
            deleteKeywordRecord(keyword.word);
        }
        insert(keyword);
    }

    @Transaction
    public void decreaseCountTransaction(Map<String, Integer> targets) {
        for (String key : targets.keySet()) {
            int c = targets.get(key);
            decreaseCount(key, c);
        }
        deleteZeroCount();

    }

}
