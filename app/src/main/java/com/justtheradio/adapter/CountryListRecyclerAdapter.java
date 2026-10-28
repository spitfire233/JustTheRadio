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

/**
 *
 * {@link RecyclerView.Adapter} implementation that binds each country code returned by RadioBrowser
 * to a list element. Each list item displays the flag of the country and the related ISO 3166
 * country code.
 */
public class CountryListRecyclerAdapter
        extends RecyclerView.Adapter<CountryListRecyclerAdapter.ViewHolder> {
    /**
     * The list of cuntries to bind
     */
    private final List<String> countries;
    /**
     * Listener to handle user interaction with the elements of the list
     */
    private final OnItemClickListener<String> clickListener;
    /**
     * Constructor for the CountryListRecyclerAdapter class
     * @param countries The list of countries to bind
     * @param clickListener A listener to handle user interaction with the elements of the
     *                            list
     */
    public CountryListRecyclerAdapter(List<String> countries,
                                      OnItemClickListener<String> clickListener) {
        this.countries = countries;
        this.clickListener = clickListener;
    }
    /**
     * {@link RecyclerView.ViewHolder} implementation that holds references to the UI components of
     * a single list item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * The text view that contains the country name
         */
        private final TextView countryCodeTextView;
        /**
         * The image view that contains the flag of the country
         */
        private final ImageView flagImageView;
        /**
         * A progress indicator to visualize a loading state for the country flag image view
         */
        private final CircularProgressIndicator imageProgressIndicator;
        /**
         * Constructor for the {@link ViewHolder} class
         * @param itemView The view for the individual list item
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            countryCodeTextView = itemView.findViewById(R.id.name_text_view);
            flagImageView = itemView.findViewById(R.id.radio_image_view);
            imageProgressIndicator = itemView.findViewById(R.id.image_view_loading_indicator);
        }
        /**
         * Getter method for the country name {@link TextView}
         * @return The country name {@link TextView}
         */
        public TextView getCountryCodeTextView() {
            return countryCodeTextView;
        }
        /**
         * Getter method for the country flag {@link ImageView}
         * @return The country flag {@link ImageView}
         */
        public ImageView getFlagImageView() {
            return flagImageView;
        }
        /**
         * Getter method for the {@link CircularProgressIndicator} for the flag image view
         * @return The {@link CircularProgressIndicator}
         */
        public CircularProgressIndicator getImageProgressIndicator() {
            return imageProgressIndicator;
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
     * Binds the country code to the corresponding list item view in the ViewHolder
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.flagImageView.getContext(); // Get the current context
        String country = countries.get(position); // Get the country at this position in the list
        // Display the country code in the text view
        holder.getCountryCodeTextView().setText(country);

        // Set a listener for when the list item is clicked/tapped
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null)
                // Call the implemented onClickListener
                clickListener.onItemClick(country);
        });

        // Prepare an error drawable in case Glide fails to load a flag into the image view
        ColorDrawable errorDrawable = new ColorDrawable(
                holder.getFlagImageView().getContext().getColor(R.color.md_theme_errorContainer)
        );

        // Load the country flag with Glide
        Glide.with(context)
                .load(String.format(FLAG_PATH_FORMAT_STRING, country.toLowerCase()))
                .error(errorDrawable)
                .into(holder.getFlagImageView());

        // Hide the progress indicator and show the flag image view
        holder.getImageProgressIndicator().setVisibility(View.GONE);
        holder.getFlagImageView().setVisibility(View.VISIBLE);
    }

    /**
     * Returns the current size of the {@link countries} list
     * @return The size of {@link countries}
     */
    @Override
    public int getItemCount() {
        return countries.size();
    }
}
