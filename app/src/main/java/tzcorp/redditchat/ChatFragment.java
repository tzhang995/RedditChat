package tzcorp.redditchat;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tony on 21/05/17.
 */

public class ChatFragment extends Fragment {
    public static final String TAG = "Chat_Fragment";
    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    //Firebase stuff
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ChildEventListener mChildEventListener;

    //Layout Stuff
    private ListView mMessageListView;
    private EditText mMessageEditText;
    private Button mMessageSendButton;
    private ProgressBar mProgressbar;
    private ImageButton mPhotoPickerButton;
    private String mUsername;
    private MessageAdapter mMessageAdapter;

    public ChatFragment() {
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
        mPhotoPickerButton = (ImageButton) rootView.findViewById(R.id.messagePhotoPickerButton);

        List<BasicMessage> basicMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(getActivity(), R.layout.item_message, basicMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        setupFirebase();

        addPageListeners();

        return rootView;
    }

    private void setupFirebase() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("messages");
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                Log.d(TAG, "OnAuthStateChanged");
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "Logged in");
                    onSignedInInitialize(user.getDisplayName());
                } else {
                    Log.d(TAG, "Not Logged in");
                    signoutCleanUp();
                }
            }
        };
    }


    private void onSignedInInitialize(String username) {
        attachDatabaseReadListener();
    }

    private void signoutCleanUp() {
        detachDatabaseReadListener();
    }


    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    BasicMessage basicMessage = dataSnapshot.getValue(BasicMessage.class);
                    mMessageAdapter.add(basicMessage);
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                public void onCancelled(DatabaseError databaseError) {}
            };
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void addPageListeners() {
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
                BasicMessage basicMessage = new BasicMessage(mMessageEditText.getText().toString(), mUsername, null);
                mMessagesDatabaseReference.push().setValue(basicMessage);

                // Clear input box
                mMessageEditText.setText("");
            }
        });
    }

}
