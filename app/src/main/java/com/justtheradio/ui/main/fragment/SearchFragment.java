package com.justtheradio.ui.main.fragment;

import static com.justtheradio.utils.constant.Constants.CHOSEN_COUNTRY_KEY;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.justtheradio.R;
import com.justtheradio.model.Result;
import com.justtheradio.model.radio.RadioStation;
import com.justtheradio.repository.RadioRepository;
import com.justtheradio.repository.UserRepository;
import com.justtheradio.ui.viewmodel.radio.RadioViewModel;
import com.justtheradio.ui.viewmodel.radio.RadioViewModelFactory;
import com.justtheradio.ui.viewmodel.user.UserViewModel;
import com.justtheradio.ui.viewmodel.user.UserViewModelFactory;
import com.justtheradio.utils.source.ServiceLocator;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SearchFragment extends Fragment {

    private UserViewModel userViewModel;
    private RadioViewModel radioViewModel;


    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserRepository userRepository = ServiceLocator.getInstance()
                .getUserRepository(requireActivity().getApplication());
        userViewModel = new ViewModelProvider(
                requireActivity(),
                new UserViewModelFactory(userRepository)
        ).get(UserViewModel.class);

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel.getUserPreference(CHOSEN_COUNTRY_KEY).observe(getViewLifecycleOwner(),
                result -> {
                    if (result.isSuccess()) {
                        Result.Success<String> success = (Result.Success<String>) result;
                        getRadioStationsWithPaging(success.getData());
                    } else {
                        displayErrorToast((Result.Failure) result);
                    }
        });
    }

    private void getRadioStationsWithPaging(String country) {
        radioViewModel.fetchRadioStations(country).observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                Result.Success<List<RadioStation>> success =
                        (Result.Success<List<RadioStation>>) result;
                updateAdapter(success.getData());
            } else {
                displayErrorToast((Result.Failure) result);
            }
        });
    }

    private void updateAdapter(List<RadioStation> radioStations) {

    }

    private void displayErrorToast(Result.Failure failure) {
        CharSequence message = failure.getMessage();
        Toast toast = Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

}