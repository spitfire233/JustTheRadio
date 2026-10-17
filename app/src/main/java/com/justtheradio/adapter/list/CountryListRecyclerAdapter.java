package com.justtheradio.adapter.list;

import static com.justtheradio.utils.constant.Constants.FLAG_PATH_FORMAT_STRING;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.justtheradio.R;

import java.util.List;

public class CountryListRecyclerAdapter
        extends BaseListRecyclerAdapter<String, BaseListRecyclerAdapter.BaseViewHolder> {

    public CountryListRecyclerAdapter(List<String> items, OnItemClickListener<String> onItemClickListener) {
        super(items, onItemClickListener);
    }

    public static class CountryViewHolder extends BaseListRecyclerAdapter.BaseViewHolder {
        public CountryViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new CountryViewHolder(view);
    }

    @Override
    protected String getName(String country) {
        return country;
    }

    @Override
    protected void onBindItem(@NonNull BaseViewHolder holder, String country) {
        Context context = holder.itemView.getContext();
        Glide.with(context)
                .load(Uri.parse(String.format(FLAG_PATH_FORMAT_STRING, country.toLowerCase())))
                .error(new ColorDrawable(context.getColor(R.color.md_theme_errorContainer)))
                .into(holder.getImageView());
        holder.getCircularProgressIndicator().setVisibility(View.GONE);
        holder.getImageView().setVisibility(View.VISIBLE);
    }
}
