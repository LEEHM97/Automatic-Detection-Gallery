package io.androidapp.gallerysearch.ui.main;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.androidapp.gallerysearch.ui.ImageViewActivity;
import io.androidapp.gallerysearch.utils.KtUtils;
import io.androidapp.gallerysearch.utils.RxBus;
import io.androidapp.gallerysearch.ui.CRUD_Activity;


import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import io.androidapp.gallerysearch.R;
import io.androidapp.gallerysearch.Settings;
import io.androidapp.gallerysearch.databinding.MainFragmentBinding;
import io.androidapp.gallerysearch.model.Photo;
import io.androidapp.gallerysearch.ui.ItemOffsetDecoration;
import io.androidapp.gallerysearch.utils.RunWithPermission;

public class MainFragment extends Fragment {

    private MainViewModel viewModel;
    MainFragmentBinding binding;
    PhotoListAdapter listAdapter = new PhotoListAdapter();

    private RunWithPermission withPermission;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = MainFragmentBinding.inflate(inflater, container, false);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.spinner_list_item_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.setAdapter(adapter);
        binding.spinner.setSelection(Settings.getInstance().sortIdx);
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                viewModel.sortTypeChanged(i);
                Settings.getInstance().save(requireContext());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.recyclerView.addItemDecoration(new ItemOffsetDecoration(getResources(), 1));
        binding.recyclerView.setAdapter(listAdapter);

        binding.searchView.setOnClickListener(view -> viewModel.searchClicked());
        binding.searchBtn.setOnClickListener(view -> viewModel.searchClicked());
        binding.taggingBtn.setOnClickListener(view -> {
        });

        RxBus.INSTANCE.receiveDeleteEvent().subscribe(
                path -> {
                    int idx = -1;
                    for (int i = 0; i < viewModel.total.size(); i++) {
                        Photo p = viewModel.total.get(i);
                        if (p.getPath().equals(path)) {
                            idx = i;
                            break;
                        }
                    }
                    if (idx > -1) {
                        viewModel.total.remove(idx);
                        listAdapter.notifyItemRemoved(idx);
                    }
                },
                throwable -> {

                });

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(MainViewModel.class);
        viewModel.photoLiveData.observe(getViewLifecycleOwner(), pair -> {
            List<Photo> list = pair.first;
            boolean ignoreDiffUtil = pair.second;
            if (ignoreDiffUtil)
                listAdapter.notifyDataSetChanged();
            else
                listAdapter.submitList(list);
        });
        viewModel.progressVisible.observe(getViewLifecycleOwner(), visible -> {
            if (visible) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else binding.progressBar.setVisibility(View.GONE);
        });

        viewModel.progress.observe(getViewLifecycleOwner(), s -> binding.progressMsg.setText(s));

        viewModel.searchKeyword.observe(getViewLifecycleOwner(), s -> binding.searchView.setText(s));

        listAdapter.setClickListener(item -> {
            showImg(item);
        });

        withPermission = new RunWithPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        withPermission.setActionWhenGranted(() -> {
            viewModel.loadPhotos();
            return null;
        }).setActionWhenDenied(run -> {
            Snackbar.make(binding.getRoot(), "이미지를 로딩을 위해 권한을 허용해주세요.", Snackbar.LENGTH_INDEFINITE).setAction("OK", v -> {
                run.requestPermission();
            }).show();
            return null;
        }).setActionInsteadPopup(runWithPermission -> {
            runWithPermission.startPermissionIntent();
            return null;
        });

        withPermission.run();
    }

    private void showImg(Photo photo) {
        Intent i = new Intent(requireContext(), ImageViewActivity.class);
        i.putExtra("uri", photo.getUriString());
        i.putExtra("path", photo.getPath());
        i.putExtra("tag", KtUtils.INSTANCE.joinToString(photo.getTags(), ", "));
        i.putExtra("photo",photo);
        startActivity(i);
    }


}