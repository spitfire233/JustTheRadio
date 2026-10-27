package com.justtheradio.adapter;

import static com.justtheradio.utils.constant.Constants.FLAG_PATH_FORMAT_STRING;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.justtheradio.R;
import com.justtheradio.adapter.listener.OnItemClickListener;

import java.util.List;

public class CountryListRecyclerAdapter
        extends RecyclerView.Adapter<CountryListRecyclerAdapter.ViewHolder> {

    private final List<String> countries;
    private final OnItemClickListener<String> onItemClickListener;

    public CountryListRecyclerAdapter(List<String> countries, OnItemClickListener<String> onItemClickListener) {
        this.countries = countries;
        this.onItemClickListener = onItemClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView imageView;
        private final CircularProgressIndicator circularProgressIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.name_text_view);
            imageView = itemView.findViewById(R.id.radio_image_view);
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.imageView.getContext();
        String country = countries.get(position);
        holder.getTextView().setText(country);
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null)
                onItemClickListener.onItemClick(country);
        });
        Glide.with(context)
                .load(String.format(FLAG_PATH_FORMAT_STRING, country.toLowerCase()))
                .error(new ColorDrawable(holder.getImageView().getContext().getColor(R.color.md_theme_errorContainer)))
                .into(holder.getImageView());
        holder.getCircularProgressIndicator().setVisibility(View.GONE);
        holder.getImageView().setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return countries.size();
    }


}
