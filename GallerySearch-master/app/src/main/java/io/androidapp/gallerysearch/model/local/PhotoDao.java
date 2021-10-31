package io.androidapp.gallerysearch.model.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.ArrayList;
import java.util.List;

import io.androidapp.gallerysearch.model.Photo;

@Dao
public abstract class PhotoDao {
    @Query("SELECT * from Photo ORDER BY date DESC")
    public abstract List<Photo> getAllPhotos();

//    @Query("SELECT * from Photo WHERE tags LIKE '%' || :tag || '%' ORDER BY date DESC")
//    List<Photo> getPhotosContain(String tag);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertPhotos(List<Photo> photos);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertPhoto(Photo photo);

    @Query("delete from Photo where path in (:idList)")
    public abstract void delete(List<String> idList);

    @Query("SELECT * FROM Photo WHERE path = :path")
    public abstract Photo getPhoto(String path);


    //키워드검색 정확도
    @Transaction
    public List<Photo> getPhotosContain(String tag){
        List<Photo> result = new ArrayList<>();
        for (Photo p : getAllPhotos()){
            if(p.getTags().contains(tag)){
                result.add(p);
            }
        }
        return result;
    }
}
