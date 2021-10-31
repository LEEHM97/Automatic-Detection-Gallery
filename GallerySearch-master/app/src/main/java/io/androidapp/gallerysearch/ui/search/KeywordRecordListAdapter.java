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

public class KeywordRecordListAdapter extends ListAdapter<KeywordRecord, KeywordRecordListAdapter.KeywordRecordItemVH> {

    private ItemClickListener<KeywordRecord> clickListener;

    public KeywordRecordListAdapter() {
        super(new DiffUtil.ItemCallback<KeywordRecord>() {
            @Override
            public boolean areItemsTheSame(@NonNull @NotNull KeywordRecord oldItem, @NonNull @NotNull KeywordRecord newItem) {
                return oldItem.getDate().equals(newItem.getDate());
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
    public KeywordRecordListAdapter.KeywordRecordItemVH onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_keyword, parent, false);
        return new KeywordRecordItemVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull KeywordRecordListAdapter.KeywordRecordItemVH holder, int position) {
        holder.onBind(getItem(position));
    }


    SimpleDateFormat format = new SimpleDateFormat("MM.dd");

    class KeywordRecordItemVH extends RecyclerView.ViewHolder {
        TextView tv;
        TextView tv2;

        public KeywordRecordItemVH(@NonNull @NotNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.keyword);
            tv2 = itemView.findViewById(R.id.info);
        }

        public void onBind(KeywordRecord record) {
            tv.setText(record.word);
            tv2.setText(format.format(new Date(record.getDate())));
            itemView.setOnClickListener(view -> {
                if (clickListener != null) clickListener.onClick(record);
            });
        }
    }
}
