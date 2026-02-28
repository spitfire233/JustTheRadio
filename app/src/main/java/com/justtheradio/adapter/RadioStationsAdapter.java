package com.justtheradio.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.carousel.MaskableFrameLayout;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.justtheradio.R;

import java.util.List;

import de.sfuhrm.radiobrowser4j.Station;

public class RadioStationsAdapter extends RecyclerView.Adapter<RadioStationsAdapter.ViewHolder>{

    public interface OnClickListener {
        public void onFavouriteButtonPressed(int position);
        public void onRadioItemPressed(Station station);
    }

    private final int layout; // Layout ID for the carousel to use
    private List<Station> stations; // Stations to adapt
    private final OnClickListener onClickListener;
    private Context context;
    private final boolean favouriteButtonEnabled;

    public RadioStationsAdapter(int layout, List<Station> stations,
                                OnClickListener onClickListener,
                                boolean favouriteButtonEnabled) {
        this.layout = layout;
        this.stations = stations;
        this.onClickListener = onClickListener;
        this.favouriteButtonEnabled = favouriteButtonEnabled;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final MaskableFrameLayout carouselMaskableFrameLayout;
        private final MaterialCardView materialCardView;
        private final ImageView radioImageView;
        private final MaterialCheckBox favouriteButtonView;
        private final TextView radioNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.carouselMaskableFrameLayout = itemView.findViewById(R.id.carousel_item_container);
            this.materialCardView = itemView.findViewById(R.id.radio_card);
            this.radioImageView = materialCardView.findViewById(R.id.radio_logo);
            this.favouriteButtonView = materialCardView.findViewById(R.id.favourite_button);
            this.radioNameTextView = materialCardView.findViewById(R.id.radio_name);
        }

        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.favourite_button)
                onClickListener.onFavouriteButtonPressed(getBindingAdapterPosition());
            else if(view.getId() == R.id.radio_card)
                onClickListener.onRadioItemPressed(stations.get(getBindingAdapterPosition()));
        }

        public MaskableFrameLayout getCarouselMaskableFrameLayout() {
            return carouselMaskableFrameLayout;
        }

        public MaterialCardView getMaterialCardView() {
            return materialCardView;
        }

        public MaterialCheckBox getFavouriteButtonView() {
            return favouriteButtonView;
        }

        public ImageView getRadioImageView() {
            return radioImageView;
        }

        public TextView getRadioNameTextView() {
            return radioNameTextView;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(this.context == null)
            this.context = parent.getContext();
        View view = LayoutInflater.from(this.context).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Station station = stations.get(position);
        holder.getRadioNameTextView().setText(station.getName());
        holder.getRadioNameTextView().setSelected(true);
        holder.getRadioImageView().setImageURI(Uri.parse(station.getFavicon()));

        Glide.with(holder.getRadioImageView().getContext())
                .load(station.getFavicon())
                .into(holder.getRadioImageView());
        if (favouriteButtonEnabled) {
            // TODO: Put here logic to handle favourites; it might need a custom model
        }
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }
}
