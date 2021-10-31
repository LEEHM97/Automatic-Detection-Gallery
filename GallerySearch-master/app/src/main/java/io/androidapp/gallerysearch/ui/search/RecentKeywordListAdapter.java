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

import java.text.SimpleDateFormat;
import java.util.Date;

import io.androidapp.gallerysearch.R;
import io.androidapp.gallerysearch.model.KeywordRecord;
import io.androidapp.gallerysearch.ui.ItemClickListener;

public class RecentKeywordListAdapter extends ListAdapter<KeywordRecord, RecentKeywordListAdapter.KeywordRecordItemVH> {

    private ItemClickListener<KeywordRecord> clickListener;

    public RecentKeywordListAdapter() {
        super(new DiffUtil.ItemCallback<KeywordRecord>() {
            @Override
            public boolean areItemsTheSame(@NonNull @NotNull KeywordRecord oldItem, @NonNull @NotNull KeywordRecord newItem) {
                return oldItem.word.equals(newItem.word);
            }

            @Override
            public boolean areContentsTheSame(@NonNull @NotNull KeywordRecord oldItem, @NonNull @NotNull KeywordRecord newItem) {
                return oldItem.getDate().equals(newItem.getDate());
            }
        });
    }

    public void setClickListener(ItemClickListener<KeywordRecord> clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @NotNull
    @Override
    public RecentKeywordListAdapter.KeywordRecordItemVH onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_keyword, parent, false);
        return new KeywordRecordItemVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecentKeywordListAdapter.KeywordRecordItemVH holder, int position) {
        holder.onBind(getItem(position));
    }
    //날짜 형식 수정
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd");
    class KeywordRecordItemVH extends RecyclerView.ViewHolder {
        TextView tv;
        TextView tv2;

        public KeywordRecordItemVH(@NonNull @NotNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.keyword);
            tv2 = itemView.findViewById(R.id.info);
        }

        public void onBind(KeywordRecord word) {
            tv.setText(word.word);
            tv2.setText(dateFormat.format(new Date(word.getDate())));
            itemView.setOnClickListener(view -> {
                if (clickListener != null) clickListener.onClick(word);
            });
        }
    }
}
