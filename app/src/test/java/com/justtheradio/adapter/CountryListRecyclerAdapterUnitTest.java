package com.justtheradio.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.FrameLayout;

import androidx.test.core.app.ApplicationProvider;

import com.justtheradio.adapter.listener.OnItemClickListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for {@link CountryListRecyclerAdapter}.
 * The adapter inflates real layouts and touches Android views, so the tests run on the JVM through
 * {@link org.robolectric.Robolectric Robolectric}.
 * This avoids the need for an emulator or a physical device
 */
@RunWith(RobolectricTestRunner.class)
public class CountryListRecyclerAdapterUnitTest {
    /**
     * Sample data used by most of the tests
     */
    private static final List<String> COUNTRIES = Arrays.asList("IT", "FR", "DE");

    /**
     * A themed context: Material components such as {@code CircularProgressIndicator} crash on
     * inflation if the context does not use a Material theme.
     */
    private Context context;
    /**
     * Parent view passed to {@link CountryListRecyclerAdapter#onCreateViewHolder}
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
     * Helper that creates a {@link CountryListRecyclerAdapter.ViewHolder} and binds it to the
     * given position.
     *
     * @param adapter  The adapter under test
     * @param position The position to bind
     * @return The bound view holder
     */
    private CountryListRecyclerAdapter.ViewHolder createAndBind(
            CountryListRecyclerAdapter adapter, int position) {
        CountryListRecyclerAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, position);
        return holder;
    }
    /**
     * The item count must match the size of the list given to the constructor
     */
    @Test
    public void getItemCount_returnsListSize() {
        CountryListRecyclerAdapter adapter = new CountryListRecyclerAdapter(COUNTRIES, null);

        assertEquals(COUNTRIES.size(), adapter.getItemCount());
    }
    /**
     * An empty list must produce an adapter with no items
     */
    @Test
    public void getItemCount_emptyList_returnsZero() {
        CountryListRecyclerAdapter adapter =
                new CountryListRecyclerAdapter(Collections.emptyList(), null);

        assertEquals(0, adapter.getItemCount());
    }
    /**
     * The view holder must be created with all of its child views resolved from the layout
     */
    @Test
    public void onCreateViewHolder_resolvesAllChildViews() {
        CountryListRecyclerAdapter adapter = new CountryListRecyclerAdapter(COUNTRIES, null);
        CountryListRecyclerAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        assertNotNull(holder.itemView);
        assertNotNull(holder.getCountryCodeTextView());
        assertNotNull(holder.getFlagImageView());
        assertNotNull(holder.getImageProgressIndicator());
    }
    /**
     * The item view must not be attached to the parent during inflation: attaching is the
     * RecyclerView's job (attachToRoot = false)
     */
    @Test
    public void onCreateViewHolder_doesNotAttachToParent() {
        CountryListRecyclerAdapter adapter = new CountryListRecyclerAdapter(COUNTRIES, null);
        CountryListRecyclerAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        assertNull(holder.itemView.getParent());
        assertEquals(0, parent.getChildCount());
    }
    /**
     * The text view must show the country code found at the bound position
     */
    @Test
    public void onBindViewHolder_setsCountryCodeText() {
        CountryListRecyclerAdapter adapter = new CountryListRecyclerAdapter(COUNTRIES, null);

        CountryListRecyclerAdapter.ViewHolder holder = createAndBind(adapter, 1);

        assertEquals("FR", holder.getCountryCodeTextView().getText().toString());
    }
    /**
     * Rebinding a recycled holder to another position must replace the previous text
     */
    @Test
    public void onBindViewHolder_recycledHolder_updatesText() {
        CountryListRecyclerAdapter adapter = new CountryListRecyclerAdapter(COUNTRIES, null);
        CountryListRecyclerAdapter.ViewHolder holder = createAndBind(adapter, 0);

        adapter.onBindViewHolder(holder, 2);

        assertEquals("DE", holder.getCountryCodeTextView().getText().toString());
    }
    /**
     * After binding, the loading indicator must be hidden and the flag must be visible
     */
    @Test
    public void onBindViewHolder_hidesProgressAndShowsFlag() {
        CountryListRecyclerAdapter adapter = new CountryListRecyclerAdapter(COUNTRIES, null);
        CountryListRecyclerAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        // Force the opposite state so the test proves that the bind changes it
        holder.getImageProgressIndicator().setVisibility(View.VISIBLE);
        holder.getFlagImageView().setVisibility(View.INVISIBLE);

        adapter.onBindViewHolder(holder, 0);

        assertEquals(View.GONE, holder.getImageProgressIndicator().getVisibility());
        assertEquals(View.VISIBLE, holder.getFlagImageView().getVisibility());
    }
    /**
     * Binding an out-of-range position is a programming error and must crash
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void onBindViewHolder_invalidPosition_throws() {
        CountryListRecyclerAdapter adapter = new CountryListRecyclerAdapter(COUNTRIES, null);

        createAndBind(adapter, COUNTRIES.size());
    }
    /**
     * Clicking an item must invoke the listener once, with the country bound to that item
     */
    @Test
    public void click_invokesListenerWithBoundCountry() {
        List<String> clicked = new ArrayList<>();
        OnItemClickListener<String> listener = clicked::add;
        CountryListRecyclerAdapter adapter = new CountryListRecyclerAdapter(COUNTRIES, listener);

        CountryListRecyclerAdapter.ViewHolder holder = createAndBind(adapter, 2);
        assertTrue(holder.itemView.performClick());

        assertEquals(Collections.singletonList("DE"), clicked);
    }
    /**
     * When a holder is recycled, a click must report the new country and not the old one
     */
    @Test
    public void click_afterRebind_reportsNewCountry() {
        List<String> clicked = new ArrayList<>();
        OnItemClickListener<String> listener = clicked::add;
        CountryListRecyclerAdapter adapter = new CountryListRecyclerAdapter(COUNTRIES, listener);
        CountryListRecyclerAdapter.ViewHolder holder = createAndBind(adapter, 0);

        adapter.onBindViewHolder(holder, 1);
        holder.itemView.performClick();

        assertEquals(Collections.singletonList("FR"), clicked);
    }
    /**
     * Every click must be forwarded to the listener
     */
    @Test
    public void click_multipleTimes_invokesListenerEachTime() {
        AtomicInteger count = new AtomicInteger();
        OnItemClickListener<String> listener = country -> count.incrementAndGet();
        CountryListRecyclerAdapter adapter = new CountryListRecyclerAdapter(COUNTRIES, listener);

        CountryListRecyclerAdapter.ViewHolder holder = createAndBind(adapter, 0);
        holder.itemView.performClick();
        holder.itemView.performClick();

        assertEquals(2, count.get());
    }
    /**
     * A null listener is allowed: clicking must not throw
     */
    @Test
    public void click_withNullListener_doesNotThrow() {
        CountryListRecyclerAdapter adapter = new CountryListRecyclerAdapter(COUNTRIES, null);

        CountryListRecyclerAdapter.ViewHolder holder = createAndBind(adapter, 0);

        // Succeeds if no exception is thrown
        holder.itemView.performClick();
    }
}
