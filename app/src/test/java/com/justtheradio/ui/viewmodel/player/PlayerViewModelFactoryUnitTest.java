package com.justtheradio.ui.viewmodel.player;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link PlayerViewModelFactory}.
 */
public class PlayerViewModelFactoryUnitTest {
    /**
     * A view model of a different type, used to check what the factory does when it is asked for
     * a class it does not know how to build
     */
    public static class UnrelatedViewModel extends ViewModel { }
    /**
     * The factory under test
     */
    private PlayerViewModelFactory factory;
    /**
     * A store used by the tests that go through a {@link ViewModelProvider}
     */
    private ViewModelStore store;
    /**
     * Creates a fresh factory and store before every test
     */
    @Before
    public void setUp() {
        factory = new PlayerViewModelFactory();
        store = new ViewModelStore();
    }
    /**
     * Clears the store, which calls {@code onCleared()} on every view model it holds
     */
    @After
    public void tearDown() {
        store.clear();
    }
    /**
     * The created view model must start in its initial state: no station selected
     */
    @Test
    public void create_returnsViewModelWithNoSelectedStation() {
        PlayerViewModel viewModel = factory.create(PlayerViewModel.class);

        assertNull(viewModel.getSelectedRadioStation().getValue());
    }
    /**
     * The factory does not cache: every call must produce a new instance. Caching is the job of
     * the {@link ViewModelStore}.
     */
    @Test
    public void create_calledTwice_returnsDifferentInstances() {
        PlayerViewModel first = factory.create(PlayerViewModel.class);
        PlayerViewModel second = factory.create(PlayerViewModel.class);

        assertNotSame(first, second);
    }
    /**
     * The factory builds a {@link PlayerViewModel} even if the requested class is a supertype
     * (the class argument is not inspected)
     */
    @Test
    public void create_supertypeRequested_returnsPlayerViewModel() {
        ViewModel viewModel = factory.create(ViewModel.class);

        assertTrue(viewModel instanceof PlayerViewModel);
    }
    /**
     * The factory does not validate the requested class. Asking for an unrelated view model type
     * still builds a {@link PlayerViewModel}, so the unchecked cast fails with a
     * {@link ClassCastException} at the call site.
     */
    @Test(expected = ClassCastException.class)
    public void create_unrelatedClassRequested_failsWithClassCastException() {
        UnrelatedViewModel viewModel = factory.create(UnrelatedViewModel.class);

        // Reading the variable forces the cast; the exception is thrown before this line
        assertNotNull(viewModel);
    }
    /**
     * Used through a {@link ViewModelProvider}, the factory must provide a {@link PlayerViewModel}
     */
    @Test
    public void viewModelProvider_getsPlayerViewModel() {
        ViewModelProvider provider = new ViewModelProvider(store, factory);

        PlayerViewModel viewModel = provider.get(PlayerViewModel.class);

        assertNotNull(viewModel);
    }
    /**
     * With the same store, the provider must return the same instance on every request. This is
     * what lets the activity and the fragments share one selected station.
     */
    @Test
    public void viewModelProvider_sameStore_returnsSameInstance() {
        ViewModelProvider provider = new ViewModelProvider(store, factory);

        PlayerViewModel first = provider.get(PlayerViewModel.class);
        PlayerViewModel second = provider.get(PlayerViewModel.class);

        assertSame(first, second);
    }
    /**
     * Two different stores must get two independent instances
     */
    @Test
    public void viewModelProvider_differentStores_returnDifferentInstances() {
        ViewModelStore otherStore = new ViewModelStore();
        try {
            PlayerViewModel first =
                    new ViewModelProvider(store, factory).get(PlayerViewModel.class);
            PlayerViewModel second =
                    new ViewModelProvider(otherStore, factory).get(PlayerViewModel.class);

            assertNotSame(first, second);
        } finally {
            otherStore.clear();
        }
    }
}
