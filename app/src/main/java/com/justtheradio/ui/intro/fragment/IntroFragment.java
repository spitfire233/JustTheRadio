package com.justtheradio.ui.intro.fragment;

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
import com.justtheradio.utils.constant.Constants;

/**
 * First screen of the intro, shown when the app is opened for the first time.
 */
public class IntroFragment extends Fragment {
    /**
     * Executed when the fragment is created.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    /**
     * Called when the fragment is created.
     * Inflates the layout of the fragment and sets up its link and buttons.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The root view of the inflated layout
     */
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

        repositoryButton.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.MAIN_REPOSITORY))));

        // Set on click listener to navigate to next fragment
        MaterialButton startConfigButton = view.findViewById(R.id.startConfigButton);
        startConfigButton.setOnClickListener(v -> Navigation.findNavController(v)
                .navigate(R.id.action_introFragment_to_chooseCountryFragment));
        return view;
    }
}