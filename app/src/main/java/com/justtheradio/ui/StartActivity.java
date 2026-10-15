package com.justtheradio.ui;

import static com.justtheradio.utils.constant.Constants.CHOSEN_COUNTRY_KEY;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.justtheradio.R;
import com.justtheradio.repository.UserRepository;
import com.justtheradio.ui.intro.IntroActivity;
import com.justtheradio.ui.main.MainActivity;
import com.justtheradio.ui.viewmodel.user.UserViewModel;
import com.justtheradio.ui.viewmodel.user.UserViewModelFactory;
import com.justtheradio.utils.source.ServiceLocator;

public class StartActivity extends AppCompatActivity {

    private boolean checked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        splashScreen.setKeepOnScreenCondition(() -> !checked);

        UserRepository userRepository = ServiceLocator.getInstance()
                .getUserRepository(getApplication());

        UserViewModel userViewModel = new ViewModelProvider(
                this,
                new UserViewModelFactory(userRepository)
        ).get(UserViewModel.class);

        userViewModel.getUserPreference(CHOSEN_COUNTRY_KEY).observe(this, result -> {
            checked = true;
            if (result.isSuccess()) // Resource exists
                startActivity(new Intent(this, MainActivity.class));
            else
                startActivity(new Intent(this, IntroActivity.class));
            finish();
        });

    }
}