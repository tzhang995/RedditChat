package tzcorp.redditchat.Reddit;

import android.content.Context;
import android.util.Log;

import net.dean.jraw.RedditClient;
import net.dean.jraw.android.BuildConfig;
import net.dean.jraw.http.OkHttpAdapter;
import net.dean.jraw.http.UserAgent;

import okhttp3.OkHttpClient;
import tzcorp.redditchat.Reddit.Reddit;
import tzcorp.redditchat.Util.NetworkUtil;

/**
 * Created by tony on 01/06/17.
 */

public class Authentication {
    private static final String TAG = "Authentication";
    private static final String CLIENT_ID    = "adjzxa_AatUq6A";
    private static final String REDIRECT_URL = "http://www.example.com/my_redirect";

    public static RedditClient reddit;
    private static OkHttpAdapter httpAdapter;
    public Authentication(Context context){

        if (NetworkUtil.isConnected(context)) {
            reddit = new RedditClient(UserAgent.of(context.getPackageName() + BuildConfig.VERSION_NAME));
            reddit.setRetryLimit(2);
        } else {
            Log.d(TAG, "No internet");
        }
    }
}
