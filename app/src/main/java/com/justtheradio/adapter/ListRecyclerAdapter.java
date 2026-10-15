package com.justtheradio.adapter;

import static com.justtheradio.utils.constant.Constants.FLAG_PATH_FORMAT_STRING;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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

import java.util.List;

public class ListRecyclerAdapter extends RecyclerView.Adapter<ListRecyclerAdapter.ViewHolder> {
    private final List<String> itemStrings;
    private final OnClickListener onClickListener;
    private final boolean showFlag;

    public interface OnClickListener {
        void onItemClick(String itemString);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView countryNameTextView;
        private final ImageView flagImageView;
        private final CircularProgressIndicator loadingIndicator;

        public ViewHolder(View view) {
            super(view);
            countryNameTextView = view.findViewById(R.id.country_list_name_text_view);
            flagImageView = view.findViewById(R.id.flag_image_view);
            loadingIndicator = view.findViewById(R.id.flag_loading_indicator);
            view.setOnClickListener(this);
        }

        public TextView getCountryNameTextView() {
            return countryNameTextView;
        }

        public ImageView getFlagImageView() {
            return flagImageView;
        }

        public CircularProgressIndicator getLoadingIndicator() {
            return loadingIndicator;
        }

        @Override
        public void onClick(View view) {
            onClickListener.onItemClick(itemStrings.get(getBindingAdapterPosition()));
        }
    }

    public ListRecyclerAdapter(List<String> itemStrings, boolean showFlag,
                               OnClickListener onClickListener) {
        this.itemStrings = itemStrings;
        this.onClickListener = onClickListener;
        this.showFlag = showFlag;
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
        Context context = holder.itemView.getContext();
        holder.getCountryNameTextView().setText(itemStrings.get(position));
        if (showFlag) {
            Glide.with(context)
                    .load(Uri.parse(String.format(FLAG_PATH_FORMAT_STRING,
                            itemStrings.get(position).toLowerCase())))
                    .error(new ColorDrawable(context.getColor(R.color.md_theme_errorContainer)))
                    .into(holder.getFlagImageView());
            holder.getLoadingIndicator().setVisibility(View.INVISIBLE);
            holder.getFlagImageView().setVisibility(View.VISIBLE);
        } else {
            holder.getLoadingIndicator().setVisibility(View.GONE);
            holder.getFlagImageView().setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return itemStrings.size();
    }


}
