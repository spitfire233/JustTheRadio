package com.justtheradio.ui.intro;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.justtheradio.R;

/**
 * Activity that hosts the intro of the app, i.e. the screens shown the first time the app
 * It provides the container for the fragments and draws the content edge to edge.
 */
public class IntroActivity extends AppCompatActivity {
    /**
     * Executed when creating the activity.
     * Sets the layout of the activity and handles the window insets.
     *
     * @param savedInstanceState The previously saved state of the activity, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intro);
        // Apply the size of the system bars as padding of the root view
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}