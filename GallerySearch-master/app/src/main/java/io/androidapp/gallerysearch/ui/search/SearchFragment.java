package io.androidapp.gallerysearch.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;

import io.androidapp.gallerysearch.databinding.FragmentSearchBinding;
import io.androidapp.gallerysearch.ui.main.MainViewModel;

public class SearchFragment extends Fragment {

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }


    private MainViewModel mainViewModel;
    private SearchViewModel searchViewModel;
    private FragmentSearchBinding binding;

    private KeywordListAdapter recommendAdapter = new KeywordListAdapter();
    private RecentKeywordListAdapter recentAdapter = new RecentKeywordListAdapter();
    private KeywordListAdapter resultAdapter = new KeywordListAdapter();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        binding.searchView.requestFocus();
        showKeyboard();

        binding.rvRecommend.setAdapter(recommendAdapter);
        binding.rvRecent.setAdapter(recentAdapter);
        binding.searchResultView.setAdapter(resultAdapter);
        binding.searchView.setOnEditorActionListener((textView, i, keyEvent) -> {
            // 텍스트 내용을 가져온다.
            String searchData = textView.getText().toString();

            // 텍스트 내용이 비어있다면...
            if (searchData.isEmpty()) {
                return true;
            }

            mainViewModel.keywordClicked(searchData);
            return true;
        });
        binding.searchWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchData = binding.searchView.getText().toString();
                mainViewModel.keywordClicked(searchData);
            }
        });

        recommendAdapter.setClickListener(item -> mainViewModel.keywordClicked(item.word));
        recentAdapter.setClickListener(item -> mainViewModel.keywordClicked(item.word));
        resultAdapter.setClickListener(item -> mainViewModel.keywordClicked(item.word));

        binding.searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchViewModel.doQuery(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return binding.getRoot();
    }

    @Override
    public void onPause() {
        hideKeyboard();
        super.onPause();
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void hideKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(binding.searchView.getWindowToken(), 0);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        searchViewModel.load();
        searchViewModel.recommendKeyword.observe(getViewLifecycleOwner(), recommendAdapter::submitList);
        searchViewModel.recentKeyword.observe(getViewLifecycleOwner(), recentAdapter::submitList);
        searchViewModel.searchResult.observe(getViewLifecycleOwner() , list -> {
            if (list.isEmpty()) {
                binding.recommendLayout.setVisibility(View.VISIBLE);
                binding.searchResultView.setVisibility(View.GONE);
            } else {
                binding.recommendLayout.setVisibility(View.GONE);
                binding.searchResultView.setVisibility(View.VISIBLE);
                resultAdapter.submitList(list);
            }
        });
    }
}