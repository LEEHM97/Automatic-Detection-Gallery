package io.androidapp.gallerysearch.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import io.androidapp.gallerysearch.R;
import io.androidapp.gallerysearch.model.Photo;
import io.androidapp.gallerysearch.ui.ItemClickListener;

class PhotoListAdapter extends ListAdapter<Photo, PhotoListAdapter.PhotoItemVH> {

    private ItemClickListener<Photo> clickListener;

    public PhotoListAdapter() {
        super(new DiffUtil.ItemCallback<Photo>() {
            @Override
            public boolean areItemsTheSame(@NonNull @NotNull Photo oldItem, @NonNull @NotNull Photo newItem) {
                return oldItem.getPath().equals(newItem.getPath());
            }

            @Override
            public boolean areContentsTheSame(@NonNull @NotNull Photo oldItem, @NonNull @NotNull Photo newItem) {
                return oldItem.getPath().equals(newItem.getPath());
            }
        });
    }

    public void setClickListener(ItemClickListener<Photo> clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @NotNull
    @Override
    public PhotoItemVH onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_photo, parent, false);
        return new PhotoItemVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PhotoListAdapter.PhotoItemVH holder, int position) {
        holder.onBind(getItem(position));
    }

    class PhotoItemVH extends RecyclerView.ViewHolder {
        ImageView imgView;

        public PhotoItemVH(@NonNull @NotNull View itemView) {
            super(itemView);
            imgView = itemView.findViewById(R.id.imageView);
        }

        public void onBind(Photo photo) {
            Glide.with(imgView.getContext())
                    .load(photo.getPath())
                    .centerCrop()
                    .into(imgView);
            itemView.setOnClickListener(view -> {
                if (clickListener != null) clickListener.onClick(photo);
            });
        }
    }
}
