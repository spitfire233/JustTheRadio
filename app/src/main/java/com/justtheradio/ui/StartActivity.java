package com.justtheradio.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.lifecycle.ViewModelProvider;

import com.justtheradio.repository.RadioRepository;
import com.justtheradio.ui.intro.IntroActivity;
import com.justtheradio.ui.main.MainActivity;
import com.justtheradio.ui.viewmodel.radio.RadioViewModel;
import com.justtheradio.ui.viewmodel.radio.RadioViewModelFactory;
import com.justtheradio.utils.source.ServiceLocator;

/**
 * Entry point of the app: shows the splash screen and decides which screen to open next.
 * The splash screen stays visible until the saved country preference has been read. If a
 * country has been saved, the intro was already completed and {@link MainActivity} is
 * started; otherwise (first launch) {@link IntroActivity} is started so that the user can
 * choose the country.
 */
public class StartActivity extends AppCompatActivity {
    /** {@code true} once the country preference has been read;
     * the splash screen is kept until then.
     */
    private boolean checked = false;

    /**
     * Executed on the creation of the activity.
     * Installs the splash screen, reads the country preference and opens the next activity.
     *
     * @param savedInstanceState The previously saved state of the activity, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Install the splash screen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        // Keep the splash screen visible until the preference has been checked
        splashScreen.setKeepOnScreenCondition(() -> !checked);

        // Initialize the radio view model to check the existence of the country in cache
        RadioRepository radioRepository = ServiceLocator.getInstance().getRadioRepository(
                getApplication()
        );
        RadioViewModel radioViewModel = new ViewModelProvider(
                this,
                new RadioViewModelFactory(radioRepository)
        ).get(RadioViewModel.class);

        // Check if the country string is present in cache.
        radioViewModel.getCountryPreference().observe(this, result -> {
            checked = true;
            if (result.isSuccess()) // Is present, start the main activity
                startActivity(new Intent(this, MainActivity.class));
            else // Is not present, start the intro activity
                startActivity(new Intent(this, IntroActivity.class));
        });
    }
}