package io.androidapp.gallerysearch.ui.main;

import android.app.Application;
import android.content.ContentResolver;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.androidapp.gallerysearch.R;
import io.androidapp.gallerysearch.Settings;
import io.androidapp.gallerysearch.model.Keyword;
import io.androidapp.gallerysearch.model.KeywordRecord;
import io.androidapp.gallerysearch.model.Photo;
import io.androidapp.gallerysearch.model.local.AppDatabase;
import io.androidapp.gallerysearch.yolov4.TFHelper;
import io.androidapp.gallerysearch.utils.RxBus;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class MainViewModel extends AndroidViewModel {
    public MainViewModel(@NonNull @NotNull Application application) {
        super(application);
        contentResolver = application.getContentResolver();
        db = AppDatabase.getInstance(application.getApplicationContext());
    }

    private ContentResolver contentResolver;
    private AppDatabase db;
    public MutableLiveData<Pair<List<Photo>, Boolean>> photoLiveData = new MutableLiveData();
    public List<Photo> total = new ArrayList<>();

    //키워드번역
    public JSONObject labelmap = new JSONObject();

    public MutableLiveData<Boolean> startSearch = new MutableLiveData<>(false);
    public MutableLiveData<String> searchKeyword = new MutableLiveData<>();

    public MutableLiveData<Boolean> progressVisible = new MutableLiveData<>(false);
    public MutableLiveData<String> progress = new MutableLiveData<>();

    public void searchClicked() {
        startSearch.setValue(true);
    }

    public void keywordClicked(String word) {
        searchKeyword.setValue(word);
        Single.just(0).subscribeOn(Schedulers.io()).subscribe((integer, throwable) -> {
            if (throwable == null) {
                boolean ignoreDiffUtil = false;
                List<Photo> temp = db.photoDao().getPhotosContain(word);
                //최근검색어 중복제거
                db.keywordDao().insertKeywordRecord(new KeywordRecord(word));
                if (temp.size() >= 500 || total.size() > 500) {
                    //이미지가 너무 많아서 DiffUtil 의 처리시간이 너무 길어지는 것을 고려하여 유틸 하용하지 않고 처리
                    ignoreDiffUtil = true;
                    total.clear();
                    total.addAll(temp);
                } else total = temp;
                applyFilter(ignoreDiffUtil);
            }
        });
    }

    public void loadPhotos() {
        Uri allImagesuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.SIZE
        };


        Cursor cursor;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bundle bundle = new Bundle();
            bundle.putInt(ContentResolver.QUERY_ARG_LIMIT, Settings.getInstance().maxLoadCount);
            bundle.putString(ContentResolver.QUERY_ARG_SORT_COLUMNS, MediaStore.Images.Media.DATE_ADDED);
            bundle.putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING);
            cursor = contentResolver.query(allImagesuri, projection, bundle, null);
        } else {
            String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT " + Settings.getInstance().maxLoadCount;
            cursor = contentResolver.query(allImagesuri, projection, null, null, sortOrder);
        }
        try {
            if (cursor != null) cursor.moveToFirst();
            total.clear();

            int idIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int nameIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            int dateIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
            int sizeIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);

            do {
                long id = cursor.getLong(idIdx);
                String name = cursor.getString(nameIdx);
                long date = cursor.getLong(dateIdx);
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                int size = cursor.getInt(sizeIdx);
                Uri imgUri = Uri.withAppendedPath(allImagesuri, Long.toString(id));
                Photo p = new Photo(path);
                p.setDisplayName(name);
                p.setDate(date);
                p.setSize(size);
                p.setUriString(imgUri.toString());
                total.add(p);

            } while (cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (searchKeyword.getValue() == null || searchKeyword.getValue().isEmpty()){
            applyFilter(false);
            taggingOnyNewPhoto();
        } else {
            applyFilter(true);
            searchKeyword.postValue("");
        }
    }

    private final Settings settings = Settings.getInstance();

    public void sortTypeChanged(int idx) {
        Timber.i("%s", idx);
        if (idx != settings.sortIdx) {
            settings.sortIdx = idx;
            justApplyFilter();
        }
    }

    private void justApplyFilter() {
        boolean useNotify = true;
        if (total.size() <= 500) {
            total = new ArrayList<>(total);
            useNotify = false;
        }
        sortWithFilter();
        photoLiveData.postValue(new Pair(total, useNotify));
        Timber.i(total.size() + "개의 데이터 정렬됨, SortIdx: " + settings.sortIdx);
    }

    private void applyFilter(boolean ignoreDiffUtil) {
        sortWithFilter();
        photoLiveData.postValue(new Pair(total, ignoreDiffUtil));
        Timber.i(total.size() + "개의 데이터 정렬됨, SortIdx: " + settings.sortIdx);
    }

    private void sortWithFilter() {
        switch (settings.sortIdx) {
            case 0:
                Collections.sort(total, (photo, t1) -> -(photo.getDate().compareTo(t1.getDate())));
                break;

            case 1:
                Collections.sort(total, (photo, t1) -> -(photo.getDate().compareTo(t1.getDate())));
                Collections.reverse(total);
                break;

            case 2:
                Collections.sort(total, (photo, t1) -> -(photo.getDisplayName().compareTo(t1.getDisplayName())));
                break;

            case 3:
                Collections.sort(total, (photo, t1) -> -(photo.getDisplayName().compareTo(t1.getDisplayName())));
                Collections.reverse(total);
                break;

            case 4:
                Collections.sort(total, (photo, t1) -> -(photo.getSize().compareTo(t1.getSize())));
                break;

            case 5:
                Collections.sort(total, (photo, t1) -> -(photo.getSize().compareTo(t1.getSize())));
                Collections.reverse(total);
                break;

        }
    }

    //키워드번역
     public void json_load() {
        String json = "";

        try {
            InputStream is = getApplication().getAssets().open("label.json");
            int fileSize = is.available();

            byte[] buffer = new byte[fileSize];
            is.read(buffer);
            is.close();

            json = new String(buffer, "UTF-8");
            labelmap = new JSONObject(json);

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> translate(List<String> tags){
        if (labelmap.length() == 0){
            json_load();
        }
        ArrayList<String> ntags = new ArrayList<>();
        for (String t : tags){
            try{
                JSONArray label = labelmap.getJSONArray(t);
                for(int i = 0; i<label.length(); i++){
                    ntags.add(label.get(i).toString());
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ntags;
    }

    Disposable disposable;

    public void taggingOnyNewPhoto() {
        TFHelper helper = new TFHelper(getApplication().getApplicationContext());
        DecimalFormat form = new DecimalFormat("#.#");
        disposable = Observable.just(0).subscribeOn(Schedulers.io())
                .flatMap(integer -> {
                    HashMap<String, Photo> totalMap = new HashMap<>();
                    for (Photo p : total) {
                        totalMap.put(p.getPath(), p);
                    }
                    List<Photo> photos = db.photoDao().getAllPhotos();
                    List<Photo> inExistPhotos = new ArrayList<>();
                    for (Photo p : photos) {
                        if (totalMap.remove(p.getPath()) == null) inExistPhotos.add(p);
                    }
                    ArrayList<Pair<Photo, String>> res = new ArrayList<>();
                    int i = 0;
                    for (Photo p : totalMap.values()) {
                        i++;
                        float per = (i * 100f / totalMap.size());
                        res.add(new Pair<>(p, form.format(per)));
                    }
                    progress.postValue(i+"개의 이미지 태깅 준비중");
                    Timber.i("[%s]개의 이미지 태깅 준비", i);

                    deleteUnavailablePhotos(inExistPhotos);

                    return Observable.fromIterable(res);
                }).map(pair -> {
                    progressVisible.postValue(true);
                    Photo p = pair.first;
                    String per = pair.second;
                    List<String> tags = helper.tagging(p.getPath());
                    //키워드번역
                    p.getTags().addAll(translate(tags));
                    progress.postValue(per + "%");
//                    Timber.i("%s%%완료", per);
                    return p;
                })
                .buffer(10)
                .subscribe(
                        photos -> { // onNext: Consumer
                            db.photoDao().insertPhotos(photos);
                            HashMap<String, Integer> tagMap = new HashMap<>();
                            for (Photo p : photos) {
                                for (String tag : p.getTags()) {
                                    if (tagMap.get(tag) == null) tagMap.put(tag, 1);
                                    else tagMap.put(tag, tagMap.get(tag) + 1);
                                }
                            }
                            for (String tag : tagMap.keySet()) {
                                int count = tagMap.get(tag);
                                Keyword keyword = db.keywordDao().getKeyword(tag);
                                if (keyword == null) {
                                    Keyword newKey = new Keyword(tag);
                                    newKey.setCount(count);
                                    db.keywordDao().insert(newKey);
                                } else {
                                    keyword.setCount(keyword.getCount() + count);
                                    db.keywordDao().insert(keyword);
                                }
                            }
                            Timber.i("키워드 %s 개 추가/수정 완료", tagMap.size());
                        },

                        throwable -> { // onError: throwable
                            Timber.e(throwable);
                        },

                        () -> { // onComplete: Unit
                            progressVisible.postValue(false);
                            progress.postValue(getApplication().getString(R.string.msg_tagging_completed));
                            Timber.i("태깅작업 완료");
                        }

                );


    }

    /**
     * 삭제되었지만 디비에 남아있는 이미지 제거
     */
    private void deleteUnavailablePhotos(List<Photo> inExistPhotos) {
        HashMap<String, Integer> removeTagTarget = new HashMap<>();
        ArrayList<String> idList = new ArrayList<>();
        for (Photo p : inExistPhotos) {
            idList.add(p.getPath());
            for (String tag : p.getTags()) {
                if (removeTagTarget.containsKey(tag)) {
                    removeTagTarget.put(tag, removeTagTarget.get(tag) + 1);
                } else {
                    removeTagTarget.put(tag, 1);
                }
            }
        }
        db.photoDao().delete(idList);
        db.keywordDao().decreaseCountTransaction(removeTagTarget);
//
    }

    public void deletePhoto(String path) {
        Single.just(path).subscribeOn(Schedulers.io()).subscribe((s, throwable) -> {
            ArrayList<Photo> photos = new ArrayList<>();
            photos.add(new Photo(s));
            deleteUnavailablePhotos(photos);
            RxBus.INSTANCE.sendDeleteEvent(s);
        });
    }
    @Override
    protected void onCleared() {
        if (disposable != null) {
            disposable.dispose();
        }
        super.onCleared();
    }
}