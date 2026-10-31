package com.justtheradio.ui.viewmodel.radio;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.justtheradio.repository.RadioRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Unit tests for {@link RadioViewModelFactory}.
 * The {@link RadioRepository}
 * is mocked to prevent testing a different class
 */
@RunWith(RobolectricTestRunner.class)
public class RadioViewModelFactoryUnitTest {
    /**
     * A view model of a different type, used to check what the factory does when it is asked for
     * a class it does not know how to build
     */
    public static class UnrelatedViewModel extends ViewModel { }
    /**
     * The mocked repository given to the factory
     */
    private RadioRepository repository;
    /**
     * The factory under test
     */
    private RadioViewModelFactory factory;
    /**
     * A store used by the tests that go through a {@link ViewModelProvider}
     */
    private ViewModelStore store;
    /**
     * Mocks the repository and creates the factory and the store before every test.
     * The streams read by the {@link RadioViewModel} constructor are stubbed, so that building
     * a view model does not fail.
     */
    @Before
    public void setUp() {
        repository = mock(RadioRepository.class);
        stubFavouriteStreams(repository);
        factory = new RadioViewModelFactory(repository);
        store = new ViewModelStore();
    }
    /**
     * Clears the store, which releases the view models (and cancels their coroutine scope)
     */
    @After
    public void tearDown() {
        store.clear();
    }
    /**
     * Stubs the streams that the {@link RadioViewModel} constructor reads from the repository
     *
     * @param mockRepository The repository mock to stub
     */
    private void stubFavouriteStreams(RadioRepository mockRepository) {
        when(mockRepository.observeFavouriteUUIDs()).thenReturn(Flowable.never());
        when(mockRepository.observeFavourites()).thenReturn(Flowable.never());
    }
    /**
     * Asking for a {@link RadioViewModel} must return a usable, non-null instance
     */
    @Test
    public void create_radioViewModelClass_returnsRadioViewModel() {
        RadioViewModel viewModel = factory.create(RadioViewModel.class);

        assertNotNull(viewModel);
        assertTrue(viewModel instanceof RadioViewModel);
    }
    /**
     * The view model must be built with the repository given to the factory: its constructor
     * reads both favourite streams from it
     */
    @Test
    public void create_usesTheFactoryRepository() {
        factory.create(RadioViewModel.class);

        verify(repository).observeFavouriteUUIDs();
        verify(repository).observeFavourites();
    }
    /**
     * Two factories with different repositories must not mix them up: only the repository of the
     * factory that built the view model is used
     */
    @Test
    public void create_doesNotTouchOtherRepositories() {
        RadioRepository otherRepository = mock(RadioRepository.class);
        stubFavouriteStreams(otherRepository);

        factory.create(RadioViewModel.class);

        verify(otherRepository, never()).observeFavouriteUUIDs();
        verify(otherRepository, never()).observeFavourites();
    }
    /**
     * The factory does not cache: every call must produce a new instance. Caching is the job of
     * the {@link ViewModelStore}.
     */
    @Test
    public void create_calledTwice_returnsDifferentInstances() {
        RadioViewModel first = factory.create(RadioViewModel.class);
        RadioViewModel second = factory.create(RadioViewModel.class);

        assertNotSame(first, second);
    }
    /**
     * The factory builds a {@link RadioViewModel} even if the requested class is a supertype
     * (the class argument is not inspected)
     */
    @Test
    public void create_supertypeRequested_returnsRadioViewModel() {
        ViewModel viewModel = factory.create(ViewModel.class);

        assertTrue(viewModel instanceof RadioViewModel);
    }
    /**
     * The factory does not validate the requested class. Asking for an unrelated view model type
     * still builds a {@link RadioViewModel}, so the unchecked cast fails with a
     * {@link ClassCastException} at the call site.
     */
    @Test(expected = ClassCastException.class)
    public void create_unrelatedClassRequested_failsWithClassCastException() {
        UnrelatedViewModel viewModel = factory.create(UnrelatedViewModel.class);

        // Reading the variable forces the cast; the exception is thrown before this line
        assertNotNull(viewModel);
    }
    /**
     * Used through a {@link ViewModelProvider}, the factory must provide a {@link RadioViewModel}
     */
    @Test
    public void viewModelProvider_getsRadioViewModel() {
        ViewModelProvider provider = new ViewModelProvider(store, factory);

        RadioViewModel viewModel = provider.get(RadioViewModel.class);

        assertNotNull(viewModel);
    }
    /**
     * With the same store, the provider must return the same instance on every request, and the
     * factory must be used only once.
     */
    @Test
    public void viewModelProvider_sameStore_returnsSameInstanceAndBuildsOnce() {
        ViewModelProvider provider = new ViewModelProvider(store, factory);

        RadioViewModel first = provider.get(RadioViewModel.class);
        RadioViewModel second = provider.get(RadioViewModel.class);

        assertSame(first, second);
        verify(repository, times(1)).observeFavouriteUUIDs();
    }
    /**
     * Two different stores must get two independent instances
     */
    @Test
    public void viewModelProvider_differentStores_returnDifferentInstances() {
        ViewModelStore otherStore = new ViewModelStore();
        try {
            RadioViewModel first =
                    new ViewModelProvider(store, factory).get(RadioViewModel.class);
            RadioViewModel second =
                    new ViewModelProvider(otherStore, factory).get(RadioViewModel.class);

            assertNotSame(first, second);
        } finally {
            otherStore.clear();
        }
    }
}
