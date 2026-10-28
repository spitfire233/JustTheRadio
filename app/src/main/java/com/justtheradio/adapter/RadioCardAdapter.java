package com.justtheradio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.justtheradio.R;
import com.justtheradio.adapter.listener.OnItemClickListener;
import com.justtheradio.model.radio.BaseRadioStation;
import com.justtheradio.model.radio.FavouriteStation;

import java.util.List;

public class RadioCardAdapter extends ListAdapter<FavouriteStation, RadioCardAdapter.ViewHolder> {

    private final OnItemClickListener<BaseRadioStation> clickListener;

    public RadioCardAdapter(OnItemClickListener<BaseRadioStation> clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView imageView;
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.favourite_card_view);
            imageView = view.findViewById(R.id.favourite_image_view);
            textView = view.findViewById(R.id.favourite_name_text_view);
        }

        public ImageView getImageView() {
            return imageView;
        }

        public TextView getTextView() {
            return textView;
        }

        public MaterialCardView getCardView() {
            return cardView;
        }
    }

    private static final DiffUtil.ItemCallback<FavouriteStation> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull FavouriteStation oldItem, @NonNull FavouriteStation newItem) {
            return oldItem.getUUID().equals(newItem.getUUID());
        }

        @Override
        public boolean areContentsTheSame(@NonNull FavouriteStation oldItem, @NonNull FavouriteStation newItem) {
            return oldItem.getURL().equals(newItem.getURL())
                    && oldItem.getName().equals(newItem.getName());
        }
    };


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.favourite_radio_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.imageView.getContext();
        Glide.with(context)
                .load(getItem(position).getFaviconURL())
                .placeholder(R.drawable.justtheradio_logo)
                .error(R.drawable.justtheradio_logo)
                .into(holder.getImageView());
        holder.getTextView().setText(getItem(position).getName());

        holder.getCardView().setOnClickListener(view -> {
            if (clickListener != null)
                clickListener.onItemClick(getItem(position));
        });
    }
}
