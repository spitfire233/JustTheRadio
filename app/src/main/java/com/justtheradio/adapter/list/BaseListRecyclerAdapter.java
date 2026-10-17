package com.justtheradio.adapter.list;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.justtheradio.R;

import java.util.List;

public abstract class BaseListRecyclerAdapter<T, VH extends BaseListRecyclerAdapter.BaseViewHolder>
    extends RecyclerView.Adapter<VH>{

    public interface OnItemClickListener<T> {
        void onItemClick(T item);
    }

    protected final List<T> items;
    private final OnItemClickListener<T> onItemClickListener;

    public static abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView imageView;
        private final CircularProgressIndicator circularProgressIndicator;
        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.name_text_view);
            imageView = itemView.findViewById(R.id.image_view);
            circularProgressIndicator = itemView.findViewById(R.id.image_view_loading_indicator);
        }
        public TextView getTextView() {
            return textView;
        }
        public ImageView getImageView() {
            return imageView;
        }
        public CircularProgressIndicator getCircularProgressIndicator() {
            return circularProgressIndicator;
        }
    }

    protected BaseListRecyclerAdapter(List<T> items, OnItemClickListener<T> onItemClickListener) {
        this.items = items;
        this.onItemClickListener = onItemClickListener;
    }

    protected abstract String getName(T item);

    protected abstract void onBindItem(@NonNull VH ViewHolder, T item);

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        T item = items.get(position);
        holder.getTextView().setText(getName(item));
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null)
                onItemClickListener.onItemClick(item);
        });
        onBindItem(holder, item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
