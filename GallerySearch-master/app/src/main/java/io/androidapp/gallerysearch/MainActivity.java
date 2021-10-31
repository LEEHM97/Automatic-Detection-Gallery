package io.androidapp.gallerysearch;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import io.androidapp.gallerysearch.ui.main.MainFragment;
import io.androidapp.gallerysearch.ui.main.MainViewModel;
import io.androidapp.gallerysearch.ui.search.SearchFragment;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private SearchFragment searchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(MainViewModel.class);


        viewModel.searchKeyword.observe(this, s -> {
            if (searchFragment != null) finishSearchFragment();
        });
        viewModel.startSearch.observe(this, show -> {
            if (show) {
                startSearchFragment();
            }
        });

        if (savedInstanceState == null) {
            startMainFragment();
        }
    }

    private void startMainFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow();
    }

    private void startSearchFragment() {
        searchFragment = SearchFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, searchFragment)
                .commitNow();
    }

    private void finishSearchFragment() {
        getSupportFragmentManager().beginTransaction()
                .remove(searchFragment)
                .commitNow();
        searchFragment = null;
    }

    @Override
    public void onBackPressed() {
        if (searchFragment != null) {
            finishSearchFragment();
        } else {
            String currentQuery = viewModel.searchKeyword.getValue();
            if (currentQuery == null || currentQuery.isEmpty())
                super.onBackPressed();
            else
                viewModel.loadPhotos();
        }
    }

}