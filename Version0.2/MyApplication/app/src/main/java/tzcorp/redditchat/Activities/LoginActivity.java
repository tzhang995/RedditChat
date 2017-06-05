package tzcorp.redditchat.Activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthHelper;

import tzcorp.redditchat.R;
import tzcorp.redditchat.Reddit.Authentication;
import tzcorp.redditchat.Util.LogUtil;

import static tzcorp.redditchat.Reddit.Authentication.getRedditClient;

/**
 * Created by tony on 05/06/17.
 */

public class LoginActivity extends AppCompatActivity {

    private TextView mTextView;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Activity activity = this;
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        WebView webView = (WebView) findViewById(R.id.web_view);
        mTextView = (TextView) findViewById(R.id.text_view);
        if (Authentication.authentication == null) {
            Authentication.authentication = getPreferences(Context.MODE_PRIVATE);
        }

        RedditClient rc = getRedditClient();

        final OAuthHelper oAuthHelper = rc.getOAuthHelper();
        final Credentials credentials = Credentials.installedApp(Authentication.CLIENT_ID, Authentication.REDIRECT_URL);

        if (Authentication.getRefreshToken(this) != null) {
            new Authentication.TokenRefreshTask(this).execute();
            finish();
            return;
        }

        boolean permanent = true;
        String[] scopes = {"identity"};

        String authorizationUrl = oAuthHelper.
                getAuthorizationUrl(credentials, permanent, scopes).toExternalForm();
        authorizationUrl = authorizationUrl.replace("www.", "i.");
        authorizationUrl = authorizationUrl.replace("%3A%2F%2Fi", "://www");
        LogUtil.d("Auth URL:" + authorizationUrl);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(null);

        webView.loadUrl(authorizationUrl);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                activity.setProgress(newProgress * 1000);
                progressBar.setProgress(newProgress);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.contains("code=")) {
                    LogUtil.d( "WebView URL: " + url);
                    // We've detected the redirect URL
                    new Authentication.UserChallengeTask(getApplicationContext(), oAuthHelper, credentials).execute(url);
                }
            }
        });
    }

}
