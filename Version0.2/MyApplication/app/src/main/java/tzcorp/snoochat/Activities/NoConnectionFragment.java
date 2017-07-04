package tzcorp.snoochat.Activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tzcorp.snoochat.R;
import tzcorp.snoochat.Reddit.Authentication;

/**
 * Created by tony on 03/07/17.
 */

public class NoConnectionFragment extends Fragment {
    private Authentication redditAuth;

    private Button mRetryButton;

    public NoConnectionFragment() {
        redditAuth = Authentication.getInstance(this.getActivity());
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_no_connection, container, false);

        mRetryButton = (Button) rootView.findViewById(R.id.retry_connecting);
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redditAuth.tryConnecting();
            }
        });

        return rootView;
    }
}
