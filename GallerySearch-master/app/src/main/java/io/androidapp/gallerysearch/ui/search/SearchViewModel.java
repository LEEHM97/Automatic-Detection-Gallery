package io.androidapp.gallerysearch.ui.search;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.androidapp.gallerysearch.model.Keyword;
import io.androidapp.gallerysearch.model.KeywordRecord;
import io.androidapp.gallerysearch.model.local.AppDatabase;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SearchViewModel extends AndroidViewModel {
    public SearchViewModel(@NonNull @NotNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application.getApplicationContext());
    }

    private AppDatabase db;

    public MutableLiveData<List<Keyword>> searchResult = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<List<Keyword>> recommendKeyword = new MutableLiveData<>();
    public MutableLiveData<List<KeywordRecord>> recentKeyword = new MutableLiveData<>();
    final public MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public void load() {

        isLoading.setValue(true);
        Single.just(0).subscribeOn(Schedulers.io())
                .subscribe((i, t) -> {
                    if (t == null) {
                        recommendKeyword.postValue(db.keywordDao().getKeywordsPopular());
                        recentKeyword.postValue(db.keywordDao().getRecord());
                    }
                    isLoading.postValue(false);
                });

    }

    Disposable disposable;

    public void doQuery(CharSequence query) {
        disposable = Single.just(query).subscribeOn(Schedulers.io())
                .subscribe((q, throwable) -> {
                    if (throwable == null) {
                        searchResult.postValue(db.keywordDao().getKeywordsContain(q.toString()));
                    }
                });
    }

    @Override
    protected void onCleared() {
        if (disposable != null) disposable.dispose();
        super.onCleared();
    }
}