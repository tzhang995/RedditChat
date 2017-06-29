package tzcorp.snoochat.Activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthHelper;

import tzcorp.snoochat.R;
import tzcorp.snoochat.Reddit.Authentication;
import tzcorp.snoochat.Util.LogUtil;

/**
 * Created by tony on 05/06/17.
 */

public class LoginActivity extends AppCompatActivity {
    private Authentication authentication;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authentication = Authentication.getInstance(this);
        setContentView(R.layout.activity_login);

        final Activity activity = this;
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        WebView webView = (WebView) findViewById(R.id.web_view);

        RedditClient rc = authentication.getRedditClient();

        final OAuthHelper oAuthHelper = rc.getOAuthHelper();
        final Credentials credentials = Credentials.installedApp(Authentication.CLIENT_ID, Authentication.REDIRECT_URL);

        if (authentication.getRefreshToken(this) != null) {
            authentication.startExistingLogin(this);
            signinSuccessful();
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
                    authentication.startNewLogin(LoginActivity.this, oAuthHelper, credentials,url);
                }
            }
        });
        webView.getSettings().setDomStorageEnabled(true);
    }

    public void signinSuccessful() {
        setResult(RESULT_OK);
        finish();
    }

    public void signinFailed() {
        setResult(RESULT_CANCELED);
        finish();
    }

}
