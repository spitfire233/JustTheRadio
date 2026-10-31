package com.justtheradio.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.paging.PagingData;
import androidx.test.core.app.ApplicationProvider;

import com.justtheradio.adapter.listener.OnItemClickListener;
import com.justtheradio.model.radio.RadioStation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for {@link RadioListPagingDataAdapter}.
 * The adapter inflates real layouts and touches Android views, so the tests run on the JVM through
 * {@link org.robolectric.Robolectric Robolectric}.
 * This avoids the need for an emulator or a physical device
 */
@RunWith(RobolectricTestRunner.class)
public class RadioListPagingDataAdapterUnitTest {
    /**
     * Maximum time, in milliseconds, to wait for the adapter to receive the submitted data
     */
    private static final long LOAD_TIMEOUT_MS = 5000;
    /**
     * Minimal {@link LifecycleOwner}: {@code submitData} needs a lifecycle to scope its coroutines
     */
    private static class TestLifecycleOwner implements LifecycleOwner {
        /**
         * The registry that holds the lifecycle state
         */
        private final LifecycleRegistry registry = new LifecycleRegistry(this);

        /**
         * Moves the lifecycle to the STARTED state, so data collection can begin
         */
        TestLifecycleOwner() {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
            registry.handleLifecycleEvent(Lifecycle.Event.ON_START);
        }
        /**
         * Moves the lifecycle to the DESTROYED state, cancelling the running coroutines
         */
        void destroy() {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
            registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
        }
        @NonNull
        @Override
        public Lifecycle getLifecycle() {
            return registry;
        }
    }
    /**
     * The lifecycle owner used to submit data
     */
    private TestLifecycleOwner lifecycleOwner;
    /**
     * Parent view passed to {@link RadioListPagingDataAdapter#onCreateViewHolder}
     */
    private FrameLayout parent;
    /**
     * Builds the lifecycle, the themed context and the parent view before every test
     */
    @Before
    public void setUp() {
        lifecycleOwner = new TestLifecycleOwner();
        Context context = new ContextThemeWrapper(
                ApplicationProvider.getApplicationContext(),
                com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar);
        parent = new FrameLayout(context);
    }
    /**
     * Destroys the lifecycle after every test, so no coroutine keeps running in the background
     */
    @After
    public void tearDown() {
        lifecycleOwner.destroy();
    }
    /**
     * Creates a {@link RadioStation} with the given name and favicon URL. The UUID is random,
     * because it is the primary key and must never be null.
     *
     * @param name       The station name
     * @param faviconUrl The URL of the station logo
     * @return The station
     */
    private RadioStation createStation(String name, String faviconUrl) {
        RadioStation station = new RadioStation();
        station.setUUID(UUID.randomUUID());
        station.setName(name);
        station.setURL("http://example.com/" + name.replace(' ', '-'));
        station.setFaviconURL(faviconUrl);
        return station;
    }
    /**
     * Submits the stations to the adapter as a static {@link PagingData} and blocks until the
     * adapter reports the expected item count.
     * The data is delivered through the main looper and a background dispatcher, so the loop
     * alternates between running the pending main looper tasks and a short sleep, to give the
     * background work time to complete.
     *
     * @param adapter  The adapter under test
     * @param stations The stations to load
     */
    private void loadStations(RadioListPagingDataAdapter adapter, RadioStation... stations) {
        adapter.submitData(lifecycleOwner.getLifecycle(),
                PagingData.from(new ArrayList<>(Arrays.asList(stations))));

        long deadline = System.currentTimeMillis() + LOAD_TIMEOUT_MS;
        while (adapter.getItemCount() != stations.length) {
            if (System.currentTimeMillis() > deadline) {
                fail("The adapter did not receive " + stations.length + " items in time");
            }
            shadowOf(Looper.getMainLooper()).idle();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Interrupted while waiting for the data");
            }
        }
    }
    /**
     * Helper that creates a {@link RadioListPagingDataAdapter.ViewHolder} and binds it to the
     * given position, as the RecyclerView would do.
     *
     * @param adapter  The adapter under test
     * @param position The position to bind
     * @return The bound view holder
     */
    private RadioListPagingDataAdapter.ViewHolder createAndBind(
            RadioListPagingDataAdapter adapter, int position) {
        RadioListPagingDataAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, position);
        return holder;
    }
    /**
     * A new adapter, with no data submitted, must contain no items
     */
    @Test
    public void getItemCount_beforeSubmit_returnsZero() {
        RadioListPagingDataAdapter adapter = new RadioListPagingDataAdapter(null);

        assertEquals(0, adapter.getItemCount());
    }
    /**
     * After the data is loaded, the item count must match the number of stations
     */
    @Test
    public void getItemCount_afterLoad_returnsStationCount() {
        RadioListPagingDataAdapter adapter = new RadioListPagingDataAdapter(null);

        loadStations(adapter,
                createStation("Radio One", "http://one/logo.png"),
                createStation("Radio Two", "http://two/logo.png"));

        assertEquals(2, adapter.getItemCount());
    }
    /**
     * The view holder must be created with all of its child views resolved from the layout
     */
    @Test
    public void onCreateViewHolder_resolvesAllChildViews() {
        RadioListPagingDataAdapter adapter = new RadioListPagingDataAdapter(null);

        RadioListPagingDataAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        assertNotNull(holder.itemView);
        assertNotNull(holder.getRadioNameTextView());
        assertNotNull(holder.getFaviconImageView());
        assertNotNull(holder.getRadioLogoProgressIndicator());
    }
    /**
     * The item view must not be attached to the parent during inflation: attaching is the
     * RecyclerView's job (attachToRoot = false)
     */
    @Test
    public void onCreateViewHolder_doesNotAttachToParent() {
        RadioListPagingDataAdapter adapter = new RadioListPagingDataAdapter(null);

        RadioListPagingDataAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        assertNull(holder.itemView.getParent());
        assertEquals(0, parent.getChildCount());
    }
    /**
     * The text view must show the name of the station at the bound position
     */
    @Test
    public void onBindViewHolder_setsStationName() {
        RadioListPagingDataAdapter adapter = new RadioListPagingDataAdapter(null);
        loadStations(adapter,
                createStation("Radio One", "http://one/logo.png"),
                createStation("Radio Two", "http://two/logo.png"));

        RadioListPagingDataAdapter.ViewHolder holder = createAndBind(adapter, 1);

        assertEquals("Radio Two", holder.getRadioNameTextView().getText().toString());
    }
    /**
     * The name text view must be selected, which is what starts the marquee (scrolling ellipsis)
     * for long station names
     */
    @Test
    public void onBindViewHolder_selectsNameForMarquee() {
        RadioListPagingDataAdapter adapter = new RadioListPagingDataAdapter(null);
        loadStations(adapter, createStation("Radio One", "http://one/logo.png"));

        RadioListPagingDataAdapter.ViewHolder holder = createAndBind(adapter, 0);

        assertTrue(holder.getRadioNameTextView().isSelected());
    }
    /**
     * Rebinding a recycled holder to another position must replace the previous name
     */
    @Test
    public void onBindViewHolder_recycledHolder_updatesName() {
        RadioListPagingDataAdapter adapter = new RadioListPagingDataAdapter(null);
        loadStations(adapter,
                createStation("Radio One", "http://one/logo.png"),
                createStation("Radio Two", "http://two/logo.png"));
        RadioListPagingDataAdapter.ViewHolder holder = createAndBind(adapter, 0);

        adapter.onBindViewHolder(holder, 1);

        assertEquals("Radio Two", holder.getRadioNameTextView().getText().toString());
    }
    /**
     * A station without a favicon URL (null) must still bind its name: Glide falls back to the
     * error drawable and the adapter must not crash
     */
    @Test
    public void onBindViewHolder_nullFaviconUrl_stillSetsName() {
        RadioListPagingDataAdapter adapter = new RadioListPagingDataAdapter(null);
        loadStations(adapter, createStation("No Logo FM", null));

        RadioListPagingDataAdapter.ViewHolder holder = createAndBind(adapter, 0);

        assertEquals("No Logo FM", holder.getRadioNameTextView().getText().toString());
    }
    /**
     * After binding, the loading indicator must be hidden and the logo must be visible
     */
    @Test
    public void onBindViewHolder_hidesProgressAndShowsLogo() {
        RadioListPagingDataAdapter adapter = new RadioListPagingDataAdapter(null);
        loadStations(adapter, createStation("Radio One", "http://one/logo.png"));
        RadioListPagingDataAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        // Force the opposite state so the test proves that the bind changes it
        holder.getRadioLogoProgressIndicator().setVisibility(View.VISIBLE);
        holder.getFaviconImageView().setVisibility(View.INVISIBLE);

        adapter.onBindViewHolder(holder, 0);

        assertEquals(View.GONE, holder.getRadioLogoProgressIndicator().getVisibility());
        assertEquals(View.VISIBLE, holder.getFaviconImageView().getVisibility());
    }
    /**
     * Binding an out-of-range position is a programming error and must crash
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void onBindViewHolder_invalidPosition_throws() {
        RadioListPagingDataAdapter adapter = new RadioListPagingDataAdapter(null);
        loadStations(adapter, createStation("Radio One", "http://one/logo.png"));

        createAndBind(adapter, 1);
    }
    /**
     * Clicking an item must invoke the listener once, with the station bound to that item.
     * Stations are compared by UUID, which is their identity.
     */
    @Test
    public void click_invokesListenerWithBoundStation() {
        RadioStation first = createStation("Radio One", "http://one/logo.png");
        RadioStation second = createStation("Radio Two", "http://two/logo.png");
        List<RadioStation> clicked = new ArrayList<>();
        OnItemClickListener<RadioStation> listener = clicked::add;
        RadioListPagingDataAdapter adapter = new RadioListPagingDataAdapter(listener);
        loadStations(adapter, first, second);

        RadioListPagingDataAdapter.ViewHolder holder = createAndBind(adapter, 1);
        assertTrue(holder.itemView.performClick());

        assertEquals(1, clicked.size());
        assertEquals(second.getUUID(), clicked.get(0).getUUID());
    }
    /**
     * When a holder is recycled, a click must report the new station and not the old one
     */
    @Test
    public void click_afterRebind_reportsNewStation() {
        RadioStation first = createStation("Radio One", "http://one/logo.png");
        RadioStation second = createStation("Radio Two", "http://two/logo.png");
        List<RadioStation> clicked = new ArrayList<>();
        OnItemClickListener<RadioStation> listener = clicked::add;
        RadioListPagingDataAdapter adapter = new RadioListPagingDataAdapter(listener);
        loadStations(adapter, first, second);
        RadioListPagingDataAdapter.ViewHolder holder = createAndBind(adapter, 0);

        adapter.onBindViewHolder(holder, 1);
        holder.itemView.performClick();

        assertEquals(1, clicked.size());
        assertEquals(second.getUUID(), clicked.get(0).getUUID());
    }
    /**
     * Every click must be forwarded to the listener
     */
    @Test
    public void click_multipleTimes_invokesListenerEachTime() {
        AtomicInteger count = new AtomicInteger();
        OnItemClickListener<RadioStation> listener = station -> count.incrementAndGet();
        RadioListPagingDataAdapter adapter = new RadioListPagingDataAdapter(listener);
        loadStations(adapter, createStation("Radio One", "http://one/logo.png"));

        RadioListPagingDataAdapter.ViewHolder holder = createAndBind(adapter, 0);
        holder.itemView.performClick();
        holder.itemView.performClick();

        assertEquals(2, count.get());
    }
    /**
     * A null listener is allowed: clicking must not throw
     */
    @Test
    public void click_withNullListener_doesNotThrow() {
        RadioListPagingDataAdapter adapter = new RadioListPagingDataAdapter(null);
        loadStations(adapter, createStation("Radio One", "http://one/logo.png"));

        RadioListPagingDataAdapter.ViewHolder holder = createAndBind(adapter, 0);

        // Succeeds if no exception is thrown
        holder.itemView.performClick();
    }
}
