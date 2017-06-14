package tzcorp.redditchat.Activities;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;

import tzcorp.redditchat.Firebase.FBase;
import tzcorp.redditchat.R;
import tzcorp.redditchat.Reddit.Authentication;
import tzcorp.redditchat.Util.LogUtil;

/**
 * Created by tony on 05/06/17.
 */

public class ChatActivity extends AppCompatActivity implements Authentication.RedditAuthInterface {
    public static final int REQUEST_CODE_SIGN_IN = 8001;

    private Authentication redditAuth = Authentication.getInstance(this);

    //Layout stuff
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private ChatFragment chatFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.chat_drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                myToolbar,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_signin:
                        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                        startActivityForResult(intent,REQUEST_CODE_SIGN_IN);
                        break;
                    case R.id.navigation_signout:
                        redditAuth.signOut(getApplicationContext());
                        break;
                    case R.id.navigation_subreddit_schoolidolfestval:
                        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                            @Override
                            public void run() {
                                String subreddit = getString(R.string.subreddit_schoolidolfestval);
                                if (redditAuth.doesSubredditExist(subreddit)){
                                    chatFragment.changeChannel(subreddit);
                                }
                            }
                        });
                        break;
                    case R.id.navigation_subreddt_all:
                        break;
                }
                mDrawerLayout.closeDrawers();
                return false;
            }
        });

        chatFragment = new ChatFragment();

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().add(R.id.content_frame, chatFragment).commit();

        redditAuth.addAuthListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                LogUtil.d("Logged in");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (redditAuth.getLoginStatus() == Authentication.LOGGEDOUT) {
            if (redditAuth.getRefreshToken(this) != null) {
                redditAuth.startExistingLogin(this);
            }
            mNavigationView.getMenu().clear();
            mNavigationView.inflateMenu(R.menu.signin_menu);
        } else if (redditAuth.getLoginStatus() == Authentication.LOGGEDIN) {
            mNavigationView.getMenu().clear();
            mNavigationView.inflateMenu(R.menu.signout_menu);
        }
    }

    @Override
    public void authChanged() {
        if (redditAuth.getLoginStatus() == Authentication.LOGGEDOUT) {
            mNavigationView.getMenu().clear();
            mNavigationView.inflateMenu(R.menu.signin_menu);
        } else if (redditAuth.getLoginStatus() == Authentication.LOGGEDIN) {
            mNavigationView.getMenu().clear();
            mNavigationView.inflateMenu(R.menu.signout_menu);
        }
    }
}