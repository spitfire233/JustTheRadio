package com.justtheradio.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.justtheradio.R;
import com.justtheradio.adapter.listener.OnItemClickListener;
import com.justtheradio.model.RadioStation;

public class RadioListPagingDataAdapter extends PagingDataAdapter<RadioStation, RadioListPagingDataAdapter.RadioViewHolder> {

    private final OnItemClickListener<RadioStation> onItemClickListener;

    public RadioListPagingDataAdapter(OnItemClickListener<RadioStation> onItemClickListener) {
        super(DIFF_CALLBACK);
        this.onItemClickListener = onItemClickListener;
    }

    public static class RadioViewHolder extends RecyclerView.ViewHolder {
        private final TextView radioNameTextView;
        private final ImageView faviconImageView;
        private final CircularProgressIndicator circularProgressIndicator;
        public RadioViewHolder(@NonNull View itemView) {
            super(itemView);
            radioNameTextView = itemView.findViewById(R.id.name_text_view);
            faviconImageView = itemView.findViewById(R.id.image_view);
            circularProgressIndicator = itemView.findViewById(R.id.image_view_loading_indicator);
        }

        public TextView getRadioNameTextView() {
            return radioNameTextView;
        }

        public ImageView getFaviconImageView() {
            return faviconImageView;
        }

        public CircularProgressIndicator getCircularProgressIndicator() {
            return circularProgressIndicator;
        }
    }

    private static final DiffUtil.ItemCallback<RadioStation> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull RadioStation oldItem, @NonNull RadioStation newItem) {
            return oldItem.getUUID().equals(newItem.getUUID());
        }

        @Override
        public boolean areContentsTheSame(@NonNull RadioStation oldItem, @NonNull RadioStation newItem) {
            return oldItem.isFavourite() == newItem.isFavourite() &&
                    oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getURL().equals(newItem.getURL()) &&
                    oldItem.getFaviconURL().equals(newItem.getFaviconURL());
        }
    };


    @NonNull
    @Override
    public RadioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new RadioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RadioViewHolder holder, int position) {
        RadioStation radioStation = getItem(position);
        if (radioStation != null) {
            holder.getRadioNameTextView().setText(radioStation.getName());
            holder.getRadioNameTextView().setSelected(true);
            Glide.with(holder.itemView.getContext())
                    .load(radioStation.getFaviconURL())
                    .placeholder(R.drawable.justtheradio_logo)
                    .error(R.drawable.justtheradio_logo)
                    .into(holder.getFaviconImageView());
            holder.getCircularProgressIndicator().setVisibility(View.GONE);
            holder.getFaviconImageView().setVisibility(View.VISIBLE);

            holder.itemView.setOnClickListener(view -> {
                if (onItemClickListener != null)
                    onItemClickListener.onItemClick(radioStation);
            });
        }
    }
}
