package com.justtheradio.adapter.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.justtheradio.R;
import com.justtheradio.model.RadioStation;

import java.util.List;

public class RadioListRecyclerAdapter
        extends BaseListRecyclerAdapter<RadioStation, RadioListRecyclerAdapter.RadioViewHolder> {

    public interface OnFavouriteChangedListener {
        void onFavouriteStatusChanged(RadioStation radioStation, boolean isFavourite);
    }

    private final OnFavouriteChangedListener onFavouriteChangedListener;

    public RadioListRecyclerAdapter(List<RadioStation> items,
                                       OnItemClickListener<RadioStation> onItemClickListener,
                                       OnFavouriteChangedListener onFavouriteChangedListener) {
        super(items, onItemClickListener);
        this.onFavouriteChangedListener = onFavouriteChangedListener;
    }

    public static class RadioViewHolder extends BaseListRecyclerAdapter.BaseViewHolder {
        private final MaterialCheckBox favouriteCheckBox;
        public RadioViewHolder(@NonNull View itemView) {
            super(itemView);
            favouriteCheckBox = itemView.findViewById(R.id.favourite_checkbox);
        }

        public MaterialCheckBox getFavouriteCheckBox() {
            return this.favouriteCheckBox;
        }

    }

    @NonNull
    @Override
    public RadioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new RadioViewHolder(view);
    }

    @Override
    protected String getName(RadioStation station) {
        return station.getName();
    }

    @Override
    protected void onBindItem(@NonNull RadioViewHolder holder, RadioStation station) {
        holder.getFavouriteCheckBox().setVisibility(View.VISIBLE);
        Context context = holder.itemView.getContext();
        Glide.with(context)
                .load(station.getFaviconURL())
                .error(R.drawable.justtheradio_logo)
                .into(holder.getImageView());

        holder.getCircularProgressIndicator().setVisibility(View.GONE);
        holder.getImageView().setVisibility(View.VISIBLE);

        // Remove to prevent duplicate calls on rebinding
        holder.getFavouriteCheckBox().setOnCheckedChangeListener(null);
        holder.getFavouriteCheckBox().setChecked(station.isFavourite());
        holder.getFavouriteCheckBox()
                .setOnCheckedChangeListener(((button, checked) -> {
                    if (onFavouriteChangedListener != null)
                        onFavouriteChangedListener.onFavouriteStatusChanged(station, checked);
                }));
    }






}
