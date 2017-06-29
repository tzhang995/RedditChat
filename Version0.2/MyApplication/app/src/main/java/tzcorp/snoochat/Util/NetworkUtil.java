package tzcorp.snoochat.Util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by tony on 02/06/17.
 */

public class NetworkUtil extends Activity {

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnected() && activeNetwork.isAvailable();
    }

    public static boolean isInternetAccessible(Context context) {
        if (isConnected(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.facebook.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                boolean retval =urlc.getResponseCode() == 200;
                urlc.disconnect();
                return retval;
            } catch (IOException e) {
                LogUtil.d("Couldn't check internet connection");
            }
        } else {
            LogUtil.d("Internet not available!");
        }
        return false;
    }
}
