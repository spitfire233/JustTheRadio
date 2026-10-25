package com.justtheradio.ui.main.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.justtheradio.R;
import com.justtheradio.adapter.RadioListPagingDataAdapter;
import com.justtheradio.model.Result;
import com.justtheradio.model.RadioStation;
import com.justtheradio.repository.RadioRepository;
import com.justtheradio.ui.viewmodel.RadioViewModel;
import com.justtheradio.ui.viewmodel.RadioViewModelFactory;
import com.justtheradio.utils.source.ServiceLocator;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

public class SearchFragment extends Fragment {

    private RadioViewModel radioViewModel;

    private CircularProgressIndicator circularProgressIndicator;
    private RecyclerView radiosRecyclerView;
    private final List<RadioStation> stations = new ArrayList<>();
    private TextInputEditText editText;
    private boolean scroolOnTop = false;


    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RadioRepository radioRepository = ServiceLocator.getInstance()
                .getRadioRepository(requireActivity().getApplication());
        radioViewModel = new ViewModelProvider(
                requireActivity(),
                new RadioViewModelFactory(radioRepository)
        ).get(RadioViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        circularProgressIndicator = view.findViewById(R.id.search_fragment_circular_progress_indicator);
        radiosRecyclerView = view.findViewById(R.id.search_fragment_recycler_view);
        editText = view.findViewById(R.id.search_edit_text);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RadioListPagingDataAdapter adapter = new RadioListPagingDataAdapter(
                stations -> {
                    // TODO: Open player
                }
        );

        radiosRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        radiosRecyclerView.setAdapter(adapter);

        editText.setOnEditorActionListener((textView, actionID, keyEvent) ->{
            boolean isSearchAction = actionID == EditorInfo.IME_ACTION_SEARCH;

            boolean isEnterPressed =
                    keyEvent != null
                    && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && keyEvent.getAction() == KeyEvent.ACTION_DOWN;

            if (isSearchAction || isEnterPressed) {
                String query = textView.getText() == null ? "" : textView.getText().toString();
                radioViewModel.setQuery(query);
                scroolOnTop = true;
                return true;
            }
            return false;
        });

        adapter.addLoadStateListener(states -> {
            LoadState refresh = states.getRefresh();
            boolean empty = adapter.getItemCount() == 0;

            boolean isLoading = refresh instanceof LoadState.Loading;

            circularProgressIndicator.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
            radiosRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);

            if (isLoading && scroolOnTop) {
                radiosRecyclerView.scrollToPosition(0);
                scroolOnTop = false;
            }
            boolean isError = refresh instanceof LoadState.Error;
            if (isError) {
                Throwable error = ((LoadState.Error) refresh).getError();
                Snackbar.make(requireView(), String.valueOf(error.getMessage()), Snackbar.LENGTH_INDEFINITE)
                        .show();
            }
            return Unit.INSTANCE;
        });

        radioViewModel.getRadioStations().observe(getViewLifecycleOwner(),
                data -> adapter.submitData(getViewLifecycleOwner().getLifecycle(), data));

    }

    private void displayErrorToast(Result.Failure failure) {
        CharSequence message = failure.getMessage();
        Toast toast = Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

}