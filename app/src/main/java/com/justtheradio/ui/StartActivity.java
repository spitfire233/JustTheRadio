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

public class StartActivity extends AppCompatActivity {

    private boolean checked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        splashScreen.setKeepOnScreenCondition(() -> !checked);


        RadioRepository radioRepository = ServiceLocator.getInstance().getRadioRepository(
                getApplication()
        );

        RadioViewModel radioViewModel = new ViewModelProvider(
                this,
                new RadioViewModelFactory(radioRepository)
        ).get(RadioViewModel.class);

        radioViewModel.getCountryPreference().observe(this, result -> {
            checked = true;
            if (result.isSuccess())
                startActivity(new Intent(this, MainActivity.class));
            else
                startActivity(new Intent(this, IntroActivity.class));
        });

    }
}