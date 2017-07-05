package tzcorp.snoochat.Reddit;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.LoggingMode;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;
import net.dean.jraw.models.Subreddit;

import java.util.ArrayList;
import java.util.UUID;

import tzcorp.snoochat.Activities.LoginActivity;
import tzcorp.snoochat.Dialogs.BasicDialogFragment;
import tzcorp.snoochat.R;
import tzcorp.snoochat.Util.LogUtil;
import tzcorp.snoochat.Util.NetworkUtil;

/**
 * Created by tony on 05/06/17.
 */

public class Authentication {
    public static final String CLIENT_ID    = "74OFIHVJEzI7Gw";
    public static final String REDIRECT_URL = "http://www.example.com/my_redirect";

    private RedditClient reddit;
    private RedditClient noAuthReddit;

    private int loginStatus;
    public static int LOGGEDOUT = 0;
    public static int LOGGEDIN = 1;
    public static int CONNECTING = 2;
    public static int NO_CONNECTION = 3;

    private static Authentication instance;
    private ArrayList<RedditAuthInterface> authListener;

    //This is where I will keep the refreshToken using lastToken as the id name
    private SharedPreferences authentication;
    public static Authentication getInstance(Context context) {
        if (instance == null) {
            instance = new Authentication(context);
        }
        return instance;
    }


    private Authentication(final Context context) {
        if (reddit == null) {
            reddit = new RedditClient(UserAgent.of("RedditChat"));
            reddit.setLoggingMode(LoggingMode.ALWAYS);
        }
        if (noAuthReddit == null) {
            noAuthReddit = new RedditClient(UserAgent.of("RedditChat"));
            noAuthReddit.setLoggingMode(LoggingMode.ALWAYS);
        }
        authListener = new ArrayList<>();
        tryConnecting();
    }

    public SharedPreferences getSharePref(Context context) {
        if (authentication == null) {
            authentication = context.getSharedPreferences(context.getString(R.string.shared_preferences_key),Context.MODE_PRIVATE);
        }
        return authentication;
    }

    public RedditClient getRedditClient() {
        return reddit;
    }

    @Nullable
    public String getRefreshToken(Context context) {
        return getSharePref(context).getString(context.getString(R.string.reddit_refresh_token), null);
    }

    public void startExistingLogin(Context context){
        if (NetworkUtil.isConnected(context)) {
            new TokenRefreshTask(context).execute();
        } else {
            //TODO
        }
    }

    private final class TokenRefreshTask extends AsyncTask<String, Void, OAuthData> {
        private Context mContext;

        public TokenRefreshTask(Context context) {
            mContext = context;
            LogUtil.d("Starting TokenRefreshTask");
        }

        @Override
        protected OAuthData doInBackground(String... params) {
            final Credentials credentials = Credentials.installedApp(Authentication.CLIENT_ID, Authentication.REDIRECT_URL);
            OAuthHelper oAuthHelper = reddit.getOAuthHelper();
            String refresh_token = getRefreshToken(mContext);
            loginStatus = CONNECTING;
            oAuthHelper.setRefreshToken(refresh_token);

            try {
                OAuthData finalData = oAuthHelper.refreshToken(credentials);
                RedditClient rc = getRedditClient();
                rc.authenticate(finalData);

                if (rc.isAuthenticated()) {
                    LogUtil.d("Authenticated");
                    LogUtil.d("Username: " + reddit.getAuthenticatedUser());

                }
            } catch (OAuthException | NetworkException | IllegalStateException e) {
                e.printStackTrace();
                LogUtil.d(e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(OAuthData oAuthData) {
            if(getRedditClient().isAuthenticated()) {
                loginStatus = LOGGEDIN;
                if(mContext instanceof LoginActivity) {
                    LoginActivity mc = (LoginActivity) mContext;
                    mc.signinSuccessful();
                }
            } else {
                loginStatus = LOGGEDOUT;
            }
            notifyListeners();
        }
    }

    public void startNewLogin(LoginActivity context, OAuthHelper oAuthHelper, Credentials credentials, String URL) {
        if (NetworkUtil.isConnected(context)) {
            new UserChallengeTask(context, oAuthHelper, credentials).execute(URL);
        } else {
            //TODO
        }
    }


    private final class UserChallengeTask extends AsyncTask<String, Void, OAuthData> {
        private OAuthHelper mOAuthHelper;
        private Credentials mCredentials;
        private LoginActivity mContext;

        public UserChallengeTask(LoginActivity context, OAuthHelper oAuthHelper, Credentials credentials) {
            mContext = context;
            mOAuthHelper = oAuthHelper;
            mCredentials = credentials;
            LogUtil.d("Starting UserChallengeTask");
        }

        @Override
        protected OAuthData doInBackground(String... params) {
            loginStatus = CONNECTING;
            LogUtil.d("params[0]: " + params[0]);
            try {
                OAuthData oAuthData = mOAuthHelper.onUserChallenge(params[0], mCredentials);
                if (oAuthData != null) {
                    RedditClient rc = getRedditClient();

                    rc.authenticate(oAuthData);
                } else {
                    LogUtil.d("OAuthData is null");
                }
                return oAuthData;
            } catch (OAuthException | IllegalStateException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(OAuthData oAuthData) {
            LogUtil.d("onPostExecute()");

            if (oAuthData != null) {
                loginStatus = LOGGEDIN;
                RedditClient rc = getRedditClient();

                String refreshToken = rc.getOAuthData().getRefreshToken();
                getSharePref(mContext).edit().putString(mContext.getString(R.string.reddit_refresh_token),refreshToken);
                getSharePref(mContext).edit().commit();
                LogUtil.d("Refresh Token: " + refreshToken);
                LogUtil.d("Username: " + rc.getAuthenticatedUser());
                mContext.signinSuccessful();

            } else {
                loginStatus = LOGGEDOUT;
                LogUtil.d("OAuthData was null");
                mContext.signinFailed();
            }
            notifyListeners();
        }
    }

    public int getLoginStatus() {
        return loginStatus;
    }

    public void signOut(Context context) {
        LogUtil.d("Signing out");
        SharedPreferences.Editor sharedPreferences = getSharePref(context).edit();
        sharedPreferences.remove(context.getString(R.string.reddit_access_token));
        sharedPreferences.remove(context.getString(R.string.reddit_refresh_token));
        sharedPreferences.apply();
        loginStatus = LOGGEDOUT;
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                reddit.getOAuthHelper().revokeAccessToken(Credentials.installedApp(CLIENT_ID, REDIRECT_URL));
            }
        });
        notifyListeners();
    }

    public void addAuthListener(@NonNull RedditAuthInterface rai){
        if (authListener == null) {
            authListener = new ArrayList<>();
        }
        if (!authListener.contains(rai)) {
            authListener.add(rai);
        }
    }

    public void removeAuthListener(@NonNull RedditAuthInterface rai){
        if (authListener.contains(rai)) {
            authListener.remove(rai);
        }
    }


    public interface RedditAuthInterface {
        void authChanged();
    }

    private void notifyListeners() {
        for(RedditAuthInterface redditAuthInterface : authListener) {
            redditAuthInterface.authChanged();
        }
    }

    public void tryConnecting() {
        ConnectToRedditAsyncTask  connectToReddit = new ConnectToRedditAsyncTask();
        connectToReddit.execute();
    }

    public class ConnectToRedditAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
                openAccess();
            } catch (Exception e) {
                e.printStackTrace();
                loginStatus = NO_CONNECTION;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            notifyListeners();
        }
    }


    public void openAccess() {
        try {
            loginStatus = CONNECTING;
            Credentials cred = Credentials.userlessApp(CLIENT_ID, UUID.randomUUID());
            OAuthHelper oAuthHelper = noAuthReddit.getOAuthHelper();
            OAuthData authData = oAuthHelper.easyAuth(cred);
            noAuthReddit.authenticate(authData);
            loginStatus = LOGGEDOUT;
        } catch (OAuthException | IllegalStateException e) {
            e.printStackTrace();
            loginStatus = NO_CONNECTION;
        }
    }

    @Nullable
    public Subreddit doesSubredditExist(@NonNull String subreddit) {
        try {
            Subreddit subreddit1 = noAuthReddit.getSubreddit(subreddit);
            LogUtil.d(subreddit1 != null ? "Found subreddit" : subreddit + " does not exist");
            return subreddit1;
        } catch (NetworkException e) {
            LogUtil.d(subreddit + " not found");
            e.printStackTrace();
            return null;
        }
    }
}
