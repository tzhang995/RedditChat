package tzcorp.redditchat.Reddit;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.LoggingMode;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;

import tzcorp.redditchat.R;
import tzcorp.redditchat.Util.LogUtil;

/**
 * Created by tony on 05/06/17.
 */

public class Authentication {
    public static final String CLIENT_ID    = "adjzxa_AatUq6A";
    public static final String REDIRECT_URL = "http://www.example.com/my_redirect";

    public static RedditClient reddit;

    //This is where I will keep the refreshToken using lastToken as the id name
    public static SharedPreferences authentication;

    public static RedditClient getRedditClient() {
        if (reddit == null) {
            reddit = new RedditClient(UserAgent.of("RedditChat"));
            reddit.setLoggingMode(LoggingMode.ALWAYS);
        }
        return reddit;
    }

    @Nullable
    public static String getRefreshToken(Context context) {
        return authentication.getString(context.getString(R.string.refresh_token), null);
    }

    public static final class TokenRefreshTask extends AsyncTask<String, Void, OAuthData> {
        private Context mContext;

        public TokenRefreshTask(Context context) {
            mContext = context;
        }

        @Override
        protected OAuthData doInBackground(String... params) {
            final Credentials credentials = Credentials.installedApp(Authentication.CLIENT_ID, Authentication.REDIRECT_URL);
            OAuthHelper oAuthHelper = reddit.getOAuthHelper();
            String refresh_token = getRefreshToken(mContext);

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
    }


    public static final class UserChallengeTask extends AsyncTask<String, Void, OAuthData> {
        private OAuthHelper mOAuthHelper;
        private Credentials mCredentials;
        private Context mContext;

        public UserChallengeTask(Context context, OAuthHelper oAuthHelper, Credentials credentials) {
            mContext = context;
            mOAuthHelper = oAuthHelper;
            mCredentials = credentials;
        }

        @Override
        protected OAuthData doInBackground(String... params) {
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
            } catch (OAuthException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(OAuthData oAuthData) {
            LogUtil.d("onPostExecute()");

            if (oAuthData != null) {
                RedditClient rc = getRedditClient();

                String refreshToken = rc.getOAuthData().getRefreshToken();
                SharedPreferences.Editor editor = authentication.edit();
                editor.putString(mContext.getString(R.string.refresh_token),refreshToken);
                editor.commit();
                LogUtil.d("Refresh Token: " + refreshToken);
                LogUtil.d("Username: " + rc.getAuthenticatedUser());
            } else {
                LogUtil.d("OAuthData was null");
            }
        }
    }
}
