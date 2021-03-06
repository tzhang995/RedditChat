package tzcorp.snoochat.Activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;

import net.dean.jraw.RedditClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import tzcorp.snoochat.Firebase.FBase;
import tzcorp.snoochat.R;
import tzcorp.snoochat.Reddit.Authentication;
import tzcorp.snoochat.Util.LogUtil;

/**
 * Created by tony on 05/06/17.
 */

public class ChatFragment extends Fragment implements FBase.FBaseListener, Authentication.RedditAuthInterface,
        MessageAdapter.MessageAdapterListeners{
    public static final String TAG = "Chat_Fragment";
    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 145;

    private Authentication redditAuth;

    //Firebase stuff
    private FBase fBase = FBase.getInstance();
    //Layout Stuff
    private ListView mMessageListView;
    private EditText mMessageEditText;
    private Button mMessageSendButton;
    private ProgressBar mProgressbar;
//    private ImageButton mPhotoPickerButton;
    private String mUsername;
    private MessageAdapter mMessageAdapter;

    public ChatFragment() {
        redditAuth = Authentication.getInstance(this.getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_chat_frame, container, false);

        mUsername = ANONYMOUS;
        mMessageListView = (ListView) rootView.findViewById(R.id.messageListView);
        mMessageEditText = (EditText) rootView.findViewById(R.id.messageEditText);
        mMessageSendButton = (Button) rootView.findViewById(R.id.messageSendButton);
        mProgressbar = (ProgressBar) rootView.findViewById(R.id.messageProgressBar);
        mProgressbar.setVisibility(View.INVISIBLE);
//        mPhotoPickerButton = (ImageButton) rootView.findViewById(R.id.messagePhotoPickerButton);

        List<BasicMessage> basicMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(getActivity(), R.layout.item_message, basicMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        addPageListeners();

        return rootView;
    }
    public void setUsername(@NonNull final String username) {
        mUsername = username;
    }

    private void addPageListeners() {
        //Add to firebase
        fBase.addFBaseListeners(this);

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mMessageSendButton.setEnabled(true);
                } else {
                    mMessageSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mMessageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fBase.loggedIn == true) {
                    RedditClient rc = redditAuth.getRedditClient();
                    if (rc.isAuthenticated()) {
                        mUsername = rc.getAuthenticatedUser();
                    }

                    SimpleDateFormat df = new SimpleDateFormat("E, d MMM y H:m:s z");
                    df.setTimeZone(TimeZone.getTimeZone("GMT"));
                    String gmtTime = df.format(new Date());
                    BasicMessage basicMessage = new BasicMessage(mMessageEditText.getText().toString(), mUsername, gmtTime);
                    fBase.addMessage(basicMessage);
                    // Clear input box
                    mMessageEditText.setText("");
                }
            }
        });

        mMessageAdapter.addListeners(this);

        redditAuth.addAuthListener(this);
    }

    @Override
    public void authchanged() {

    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseAuth firebaseAuth = fBase.auth;
        if (firebaseAuth.getCurrentUser() == null) {
            fBase.signinAnon();
        }
        fBase.addFBaseListeners(this);
    }

    @Override
    public void newMessage(BasicMessage message) {
        LogUtil.d(message.getText());
        mMessageAdapter.add(message);
    }

    @Override
    public void presenceChange(long people) {

    }

    @Override
    public void onPause() {
        super.onPause();
        fBase.removeFBaseListener(this);
    }

    @Override
    public void authChanged() {
        if (redditAuth.getLoginStatus() != Authentication.LOGGEDIN) {
            mUsername = ANONYMOUS;
        }
    }

    public void changeChannel(@NonNull String subreddit) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessageAdapter.clear();
            }
        });
        fBase.changeChannel(subreddit);
    }

    @Override
    public void messagesChanged() {
        if (mMessageAdapter.getCount() == 0 && redditAuth.getLoginStatus() == Authentication.CONNECTING) {
            mProgressbar.setVisibility(View.VISIBLE);
        } else {
            mProgressbar.setVisibility(View.INVISIBLE);
        }
    }
}