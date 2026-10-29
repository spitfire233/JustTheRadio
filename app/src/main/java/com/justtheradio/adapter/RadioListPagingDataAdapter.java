package com.justtheradio.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.justtheradio.R;
import com.justtheradio.adapter.diff.RadioStationDiff;
import com.justtheradio.adapter.listener.OnItemClickListener;
import com.justtheradio.model.radio.RadioStation;

/**
 * {@link PagingDataAdapter} implementation that binds a paged list of radio stations to a list
 * layout. Each list item displays the name and logo of the radio.
 */
public class RadioListPagingDataAdapter
        extends PagingDataAdapter<RadioStation, RadioListPagingDataAdapter.ViewHolder> {
    /**
     * Listener to handle user interaction with each list item
     */
    private final OnItemClickListener<RadioStation> onItemClickListener;
    /**
     * Constructor for the RadioListPagingAdapter class
     * @param onItemClickListener A listener to handle user interaction with each list item
     */
    public RadioListPagingDataAdapter(OnItemClickListener<RadioStation> onItemClickListener) {
        super(RadioStationDiff.getDiffCallback());
        this.onItemClickListener = onItemClickListener;
    }
    /**
     * {@link RecyclerView.ViewHolder} implementation that holds references to the UI components
     * of a single list item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * The text view that displays the radio station's name
         */
        private final TextView radioNameTextView;
        /**
         * The image view that displays the radio station's logo
         */
        private final ImageView faviconImageView;
        /**
         * A progress indicator to visualize a loading state for the radio station's logo image view
         */
        private final CircularProgressIndicator radioLogoProgressIndicator;
        /**
         * Constructor for the {@link ViewHolder} class
         * @param itemView The view for the individual list item
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            radioNameTextView = itemView.findViewById(R.id.name_text_view);
            faviconImageView = itemView.findViewById(R.id.radio_image_view);
            radioLogoProgressIndicator = itemView.findViewById(R.id.image_view_loading_indicator);
        }
        /**
         * Getter method for the radio station's name {@link TextView}
         * @return The radio station's name {@link TextView}
         */
        public TextView getRadioNameTextView() {
            return radioNameTextView;
        }
        /**
         * Getter method for the radio station's logo {@link ImageView}
         * @return The radio station's logo {@link ImageView}
         */
        public ImageView getFaviconImageView() {
            return faviconImageView;
        }
        /**
         * Getter method for the {@link CircularProgressIndicator}
         * related to {@link faviconImageView}
         * @return The {@link CircularProgressIndicator}
         */
        public CircularProgressIndicator getRadioLogoProgressIndicator() {
            return radioLogoProgressIndicator;
        }
    }
    /**
     * Inflates the list item layout and returns a new {@link ViewHolder}
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new {@link ViewHolder} for the list item
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }
    /**
     * Binds the radio station to the corresponding list view in the {@link ViewHolder}
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the current radio station
        RadioStation radioStation = getItem(position);
        if (radioStation != null) {
            // Set the text view to display the station's name and enable marquis elliptization
            holder.getRadioNameTextView().setText(radioStation.getName());
            holder.getRadioNameTextView().setSelected(true);
            // Load the station's logo with glide
            Glide.with(holder.itemView.getContext())
                    .load(radioStation.getFaviconURL())
                    .placeholder(R.drawable.justtheradio_logo)
                    .error(R.drawable.justtheradio_logo)
                    .into(holder.getFaviconImageView());
            // Hide the circular progress indicator and show the logo image view
            holder.getRadioLogoProgressIndicator().setVisibility(View.GONE);
            holder.getFaviconImageView().setVisibility(View.VISIBLE);

            // Set the onClickListener to utilize the provided implementation
            holder.itemView.setOnClickListener(view -> {
                if (onItemClickListener != null)
                    onItemClickListener.onItemClick(radioStation);
            });
        }
    }
}
