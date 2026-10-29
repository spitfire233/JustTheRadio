package com.justtheradio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.justtheradio.R;
import com.justtheradio.adapter.diff.RadioStationDiff;
import com.justtheradio.adapter.listener.OnItemClickListener;
import com.justtheradio.model.radio.FavouriteStation;

/**
 * {@link ListAdapter} implementation that binds each favourite radio station to a card layout. Each
 * card displays the logo and the name of the radio
 */
public class RadioCardAdapter extends ListAdapter<FavouriteStation, RadioCardAdapter.ViewHolder> {
    /**
     * Listener to handle user interaction with each card
     */
    private final OnItemClickListener<FavouriteStation> clickListener;
    /**
     * Constructor for the RadioCardAdapter class
     * @param clickListener A listener to handle user interaction with each card
     */
    public RadioCardAdapter(OnItemClickListener<FavouriteStation> clickListener) {
        super(RadioStationDiff.getDiffCallback());
        this.clickListener = clickListener;
    }
    /**
     * {@link RecyclerView.ViewHolder} implementation that holds references to the UI components of
     * a single station card
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * The entire card view of the card
         */
        private final MaterialCardView cardView;
        /**
         * The image view that contains the radio logo
         */
        private final ImageView radioLogoImageView;
        /**
         * The text view that displays the radio name
         */
        private final TextView radioNameTextView;
        /**
         * Constructor for the {@link ViewHolder} class
         * @param view The view for the card
         */
        public ViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.favourite_card_view);
            radioLogoImageView = view.findViewById(R.id.favourite_image_view);
            radioNameTextView = view.findViewById(R.id.favourite_name_text_view);
        }
        /**
         * Getter method for the radio logo {@link ImageView}
         * @return The radio logo {@link ImageView}
         */
        public ImageView getRadioLogoImageView() {
            return radioLogoImageView;
        }
        /**
         * Getter method for the radio name {@link TextView}
         * @return The radio name {@link TextView}
         */
        public TextView getRadioNameTextView() {
            return radioNameTextView;
        }
        /**
         * Getter method for the card {@link MaterialCardView}
         * @return The card {@link MaterialCardView}
         */
        public MaterialCardView getCardView() {
            return cardView;
        }
    }
    /**
     * Inflates the station card and returns a new {@link ViewHolder}
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new {@link ViewHolder} for the card
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.favourite_radio_card, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the radio station to the corresponding card view in the {@link ViewHolder}
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.radioLogoImageView.getContext(); // Get the current context
        FavouriteStation station = getItem(position); // Get the current item
        if (station != null) {
            // Load the radio logo into the image view with Glide
            Glide.with(context)
                    .load(getItem(position).getFaviconURL())
                    .placeholder(R.drawable.justtheradio_logo)
                    .error(R.drawable.justtheradio_logo)
                    .into(holder.getRadioLogoImageView());
            // Set the text in the text view to the radio name
            holder.getRadioNameTextView().setText(getItem(position).getName());

            // Set the onClickListener to utilize the provided implementation
            holder.getCardView().setOnClickListener(view -> {
                if (clickListener != null)
                    clickListener.onItemClick(getItem(position));
            });
        }
    }
}
