package com.justtheradio.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;

import com.justtheradio.adapter.listener.OnItemClickListener;
import com.justtheradio.model.radio.FavouriteStation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for {@link RadioCardAdapter}.
 * The adapter inflates real layouts and touches Android views, so the tests run on the JVM through
 * {@link org.robolectric.Robolectric Robolectric}.
 * This avoids the need for an emulator or a physical device
 */
@RunWith(RobolectricTestRunner.class)
public class RadioCardAdapterUnitTest {
    /**
     * A themed context: {@code MaterialCardView} crashes on inflation if the context does not use
     * a Material theme.
     */
    private Context context;
    /**
     * Parent view passed to {@link RadioCardAdapter#onCreateViewHolder}
     */
    private FrameLayout parent;
    /**
     * Builds the themed context and the parent view before every test
     */
    @Before
    public void setUp() {
        context = new ContextThemeWrapper(
                ApplicationProvider.getApplicationContext(),
                com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar);
        parent = new FrameLayout(context);
    }
    /**
     * Creates a {@link FavouriteStation} with the given name and favicon URL. The UUID is
     * random, because it is the primary key and must never be null.
     *
     * @param name       The station name
     * @param faviconUrl The URL of the station logo
     * @return The station
     */
    @NonNull
    private FavouriteStation createStation(String name, String faviconUrl) {
        FavouriteStation station = new FavouriteStation();
        station.setUUID(UUID.randomUUID());
        station.setName(name);
        station.setFaviconURL(faviconUrl);
        return station;
    }
    /**
     * Creates an adapter, submits the given stations and
     * returns it.
     *
     * @param listener The click listener, may be null
     * @param stations The stations to display
     * @return The adapter containing the stations
     */
    @NonNull
    private RadioCardAdapter createAdapter(OnItemClickListener<FavouriteStation> listener,
                                           FavouriteStation... stations) {
        RadioCardAdapter adapter = new RadioCardAdapter(listener);
        adapter.submitList(new ArrayList<>(Arrays.asList(stations)));
        return adapter;
    }
    /**
     * Helper that creates a {@link RadioCardAdapter.ViewHolder} and binds it to the given
     * position, as the RecyclerView would do.
     *
     * @param adapter  The adapter under test
     * @param position The position to bind
     * @return The bound view holder
     */
    @NonNull
    private RadioCardAdapter.ViewHolder createAndBind(@NonNull RadioCardAdapter adapter, int position) {
        RadioCardAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, position);
        return holder;
    }
    /**
     * A new adapter, with no list submitted, must contain no items
     */
    @Test
    public void getItemCount_beforeSubmit_returnsZero() {
        RadioCardAdapter adapter = new RadioCardAdapter(null);
        assertEquals(0, adapter.getItemCount());
    }
    /**
     * After submitting a list, the item count must match its size
     */
    @Test
    public void getItemCount_afterSubmit_returnsListSize() {
        RadioCardAdapter adapter = createAdapter(null,
                createStation("Radio One", "http://one/logo.png"),
                createStation("Radio Two", "http://two/logo.png"));
        assertEquals(2, adapter.getItemCount());
    }
    /**
     * Submitting an empty list must produce an adapter with no items
     */
    @Test
    public void getItemCount_emptyList_returnsZero() {
        RadioCardAdapter adapter = new RadioCardAdapter(null);
        adapter.submitList(Collections.emptyList());
        assertEquals(0, adapter.getItemCount());
    }
    /**
     * The view holder must be created with all of its child views resolved from the layout
     */
    @Test
    public void onCreateViewHolder_resolvesAllChildViews() {
        RadioCardAdapter adapter = new RadioCardAdapter(null);
        RadioCardAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        assertNotNull(holder.itemView);
        assertNotNull(holder.getCardView());
        assertNotNull(holder.getRadioLogoImageView());
        assertNotNull(holder.getRadioNameTextView());
    }
    /**
     * The card must not be attached to the parent during inflation: attaching is the
     * RecyclerView's job (attachToRoot = false)
     */
    @Test
    public void onCreateViewHolder_doesNotAttachToParent() {
        RadioCardAdapter adapter = new RadioCardAdapter(null);
        RadioCardAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        assertNull(holder.itemView.getParent());
        assertEquals(0, parent.getChildCount());
    }
    /**
     * The text view must show the name of the station at the bound position
     */
    @Test
    public void onBindViewHolder_setsStationName() {
        RadioCardAdapter adapter = createAdapter(null,
                createStation("Radio One", "http://one/logo.png"),
                createStation("Radio Two", "http://two/logo.png"));

        RadioCardAdapter.ViewHolder holder = createAndBind(adapter, 1);

        assertEquals("Radio Two", holder.getRadioNameTextView().getText().toString());
    }
    /**
     * Rebinding a recycled holder to another position must replace the previous name
     */
    @Test
    public void onBindViewHolder_recycledHolder_updatesName() {
        RadioCardAdapter adapter = createAdapter(null,
                createStation("Radio One", "http://one/logo.png"),
                createStation("Radio Two", "http://two/logo.png"));
        RadioCardAdapter.ViewHolder holder = createAndBind(adapter, 0);

        adapter.onBindViewHolder(holder, 1);

        assertEquals("Radio Two", holder.getRadioNameTextView().getText().toString());
    }
    /**
     * A station without a favicon URL (null) must still bind its name: Glide falls back to the
     * error drawable and the adapter must not crash
     */
    @Test
    public void onBindViewHolder_nullFaviconUrl_stillSetsName() {
        RadioCardAdapter adapter = createAdapter(null, createStation("No Logo FM", null));

        RadioCardAdapter.ViewHolder holder = createAndBind(adapter, 0);

        assertEquals("No Logo FM", holder.getRadioNameTextView().getText().toString());
    }
    /**
     * Binding an out-of-range position is a programming error and must crash
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void onBindViewHolder_invalidPosition_throws() {
        RadioCardAdapter adapter = createAdapter(null,
                createStation("Radio One", "http://one/logo.png"));

        createAndBind(adapter, 1);
    }
    /**
     * Clicking the card must invoke the listener once, with the station bound to that card
     */
    @Test
    public void click_invokesListenerWithBoundStation() {
        FavouriteStation first = createStation("Radio One", "http://one/logo.png");
        FavouriteStation second = createStation("Radio Two", "http://two/logo.png");
        List<FavouriteStation> clicked = new ArrayList<>();
        RadioCardAdapter adapter = createAdapter(clicked::add, first, second);

        RadioCardAdapter.ViewHolder holder = createAndBind(adapter, 1);
        assertTrue(holder.getCardView().performClick());

        assertEquals(1, clicked.size());
        assertSame(second, clicked.get(0));
    }
    /**
     * When a holder is recycled, a click must report the new station and not the old one
     */
    @Test
    public void click_afterRebind_reportsNewStation() {
        FavouriteStation first = createStation("Radio One", "http://one/logo.png");
        FavouriteStation second = createStation("Radio Two", "http://two/logo.png");
        List<FavouriteStation> clicked = new ArrayList<>();
        RadioCardAdapter adapter = createAdapter(clicked::add, first, second);
        RadioCardAdapter.ViewHolder holder = createAndBind(adapter, 0);

        adapter.onBindViewHolder(holder, 1);
        holder.getCardView().performClick();

        assertEquals(1, clicked.size());
        assertSame(second, clicked.get(0));
    }
    /**
     * Every click must be forwarded to the listener
     */
    @Test
    public void click_multipleTimes_invokesListenerEachTime() {
        AtomicInteger count = new AtomicInteger();
        RadioCardAdapter adapter = createAdapter(station -> count.incrementAndGet(),
                createStation("Radio One", "http://one/logo.png"));

        RadioCardAdapter.ViewHolder holder = createAndBind(adapter, 0);
        holder.getCardView().performClick();
        holder.getCardView().performClick();

        assertEquals(2, count.get());
    }
    /**
     * A null listener is allowed: clicking must not throw
     */
    @Test
    public void click_withNullListener_doesNotThrow() {
        RadioCardAdapter adapter = createAdapter(null,
                createStation("Radio One", "http://one/logo.png"));

        RadioCardAdapter.ViewHolder holder = createAndBind(adapter, 0);

        // Succeeds if no exception is thrown
        holder.getCardView().performClick();
    }
}
