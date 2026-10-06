package com.justtheradio.ui.start.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.justtheradio.R;
import com.justtheradio.utils.Strings;

public class IntroFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro, container, false);

        // Setup hyperlink to RadioBrowserAPI website
        TextView hyperLinkTextView = view.findViewById(R.id.hyperlink_textview);
        hyperLinkTextView.setMovementMethod(LinkMovementMethod.getInstance());

        // Setup button to open the browser on the main repository page
        // (for now GitHub, but it will be CodeBerg in the future)
        MaterialButton repositoryButton = view.findViewById(R.id.repositoryButton);

        repositoryButton.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Strings.main_repository)));
        });

        // Set on click listener to navigate to next fragment
        MaterialButton startConfigButton = view.findViewById(R.id.startConfigButton);
        startConfigButton.setOnClickListener(v -> Navigation.findNavController(v)
                .navigate(R.id.action_introFragment_to_chooseCountryFragment));
        return view;
    }
}