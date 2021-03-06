package tzcorp.snoochat.Firebase;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import tzcorp.snoochat.Activities.BasicMessage;
import tzcorp.snoochat.Util.LogUtil;

/**
 * Created by tony on 06/06/17.
 */

public class FBase {
    private static FBase instance;

    public FirebaseDatabase db;
    public DatabaseReference reference;
    public FirebaseStorage storage;
    public ChildEventListener childListener;
    private ArrayList<FBaseListener> listeners;
    public boolean loggedIn;
    public FirebaseAuth auth;
    public FirebaseAuth.AuthStateListener stateListener;

    public DatabaseReference presenceReference;
    public ChildEventListener presenceListener;

    private FBase(){
        db = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        listeners = new ArrayList<>();
        changeChannel("ALL");
        loggedIn = false;

        auth = FirebaseAuth.getInstance();
        stateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    loggedIn = true;
                    LogUtil.d("Logged in");
                } else {
                    loggedIn = false;
                    LogUtil.d("Not Logged in");
                }
            }
        };
        auth.addAuthStateListener(stateListener);

        presenceReference = db.getReference("all/connections");
        final DatabaseReference connectedRef = db.getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    DatabaseReference con = presenceReference.push();

                    // when this device disconnects, remove it
                    con.onDisconnect().removeValue();

                    // add this device to my connections list
                    // this value could contain info about the device or a timestamp too
                    con.setValue(Boolean.TRUE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Listener was cancelled at .info/connected");
            }
        });

        presenceListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                presenceReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(FBaseListener listener: listeners) {
                            listener.presenceChange(dataSnapshot.getChildrenCount());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                presenceReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(FBaseListener listener: listeners) {
                            listener.presenceChange(dataSnapshot.getChildrenCount());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        presenceReference.addChildEventListener(presenceListener);
    }


    public static FBase getInstance() {
        if (instance == null) {
            instance = new FBase();
        }
        return instance;
    }



    public void signinAnon() {
        auth.signInAnonymously()
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            LogUtil.d( "signInAnonymously:success");
                            FirebaseUser user = auth.getCurrentUser();
                            loggedIn = true;
                        } else {
                            // If sign in fails, display a message to the user.
                            LogUtil.d( "signInAnonymously:failure" + task.getException());
                            loggedIn = false;
                        }

                        // ...
                    }
                });
    }

    public void addMessage(@NonNull BasicMessage message) {
        reference.push().setValue(message);
    }


    public void addFBaseListeners(@NonNull FBaseListener fBaseInterface) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        LogUtil.d(fBaseInterface.getClass().getName());
        if (!listeners.contains(fBaseInterface)) {
            listeners.add(fBaseInterface);
        }
    }

    public void removeFBaseListener(@Nullable FBaseListener fbl){
        if (listeners!= null) {
            listeners.remove(fbl);
        }
    }

    private void notifyListeners(){
        //TODO
    }

    public interface FBaseListener{
        void authchanged();
        void newMessage(BasicMessage message);
        void presenceChange(long people);
    }

    public void changeChannel(@NonNull String subreddit) {
        if (childListener != null && reference != null) {
            reference.removeEventListener(childListener);
        }
        reference = FirebaseDatabase.getInstance().getReference().child(subreddit);
        childListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                LogUtil.d("New Message Received");
                BasicMessage basicMessage = dataSnapshot.getValue(BasicMessage.class);
                for(FBaseListener listener: listeners) {
                    listener.newMessage(basicMessage);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        reference.addChildEventListener(childListener);
    }
}
