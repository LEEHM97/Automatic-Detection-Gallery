package io.androidapp.gallerysearch.ui.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import io.androidapp.gallerysearch.R;
import io.androidapp.gallerysearch.model.Keyword;
import io.androidapp.gallerysearch.ui.ItemClickListener;

public class KeywordListAdapter extends ListAdapter<Keyword, KeywordListAdapter.KeywordItemVH> {

    private ItemClickListener<Keyword> clickListener;

    public KeywordListAdapter() {
        super(new DiffUtil.ItemCallback<Keyword>() {
            @Override
            public boolean areItemsTheSame(@NonNull @NotNull Keyword oldItem, @NonNull @NotNull Keyword newItem) {
                return oldItem.word.equals(newItem.word);
            }

            @Override
            public boolean areContentsTheSame(@NonNull @NotNull Keyword oldItem, @NonNull @NotNull Keyword newItem) {
                return oldItem.getCount().equals(newItem.getCount());
            }
        });
    }

    public void setClickListener(ItemClickListener<Keyword> clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @NotNull
    @Override
    public KeywordListAdapter.KeywordItemVH onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_keyword, parent, false);
        return new KeywordItemVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull KeywordListAdapter.KeywordItemVH holder, int position) {
        holder.onBind(getItem(position));
    }

    class KeywordItemVH extends RecyclerView.ViewHolder {
        TextView tv;
        TextView tv2;

        public KeywordItemVH(@NonNull @NotNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.keyword);
            tv2 = itemView.findViewById(R.id.info);
        }

        public void onBind(Keyword keyword) {
            tv.setText(keyword.word);
            tv2.setText(keyword.getCount() +" photos");
            itemView.setOnClickListener(view -> {
                if (clickListener != null) clickListener.onClick(keyword);
            });
        }
    }
}
