package org.jjam.instatwittaface;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Hanterar instagram
 * @author Mårten Persson, Anton Nilsson
 */
public class InstagramApp  {

    private PrefUtil prefUtil;
    private InstagramDialog dialog;
    private OAuthAuthenticationListener listener;
    private ProgressDialog progressDialog;
    private String authUrl;
    public static String instagramAccessToken;
    private String clientId;
    private String clientSecret;
    private static int WHAT_FINALIZE = 0;
    private static int WHAT_ERROR = 1;
    private static int WHAT_FETCH_INFO = 2;
    public static String callbackUrl = "";
    private static final String AUTH_URL = "https://api.instagram.com/oauth/authorize/";
    private static final String TOKEN_URL = "https://api.instagram.com/oauth/access_token";
    private static final String API_URL = "https://api.instagram.com/v1";
    private static final String TAG = "InstagramAPI";

    public InstagramApp(Activity activity, String clientId, String clientSecret,
                        String callbackUrl) {

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        prefUtil = new PrefUtil(activity);
        instagramAccessToken = prefUtil.getInstagramToken();
        InstagramApp.callbackUrl = callbackUrl;

        authUrl = AUTH_URL + "?client_id=" + clientId + "&redirect_uri=" + InstagramApp.callbackUrl +
                "&response_type=code&display=touch&scope=likes+comments+relationships";

        InstagramDialog.OAuthDialogListener listener = new InstagramDialog.OAuthDialogListener() {
            @Override
            public void onComplete(String code) {
                getAccessToken(code);
            }
            @Override
            public void onError(String error) {
                InstagramApp.this.listener.onFail("Authorization failed");
            }
        };
        dialog = new InstagramDialog(activity, authUrl, listener);
        progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
    }

    private void getAccessToken(final String code) {
        progressDialog.setMessage("Getting access token ...");
        progressDialog.show();

        new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "Getting access token");
                int what = WHAT_FETCH_INFO;
                try {
                    URL url = new URL(TOKEN_URL);
                    Log.i(TAG, "Opening Token URL " + url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                    writer.write("client_id=" + clientId +
                            "&client_secret=" + clientSecret +
                            "&grant_type=authorization_code" +
                            "&redirect_uri=" + callbackUrl +
                            "&code=" + code);
                    writer.flush();
                    String response = streamToString(urlConnection.getInputStream());
                    Log.i(TAG, "response " + response);
                    JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
                    instagramAccessToken = jsonObj.getString("access_token");
                    Log.i(TAG, "Got access token: " + instagramAccessToken);

                    String id = jsonObj.getJSONObject("user").getString("id");
                    String user = jsonObj.getJSONObject("user").getString("username");
                    String name = jsonObj.getJSONObject("user").getString("full_name");

                    prefUtil.storeInstagramToken(instagramAccessToken, id, user, name);

                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }
                mHandler.sendMessage(mHandler.obtainMessage(what, 1, 0));
            }
        }.start();
    }

    private void fetchUserName() {
        progressDialog.setMessage("Finalizing ...");

        new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "Fetching user info");
                int what = WHAT_FINALIZE;
                try {
                    URL url = new URL(API_URL + "/users/" + prefUtil.getId() + "/?access_token=" + instagramAccessToken);

                    Log.d(TAG, "Opening URL " + url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoInput(true);
                    urlConnection.connect();
                    String response = streamToString(urlConnection.getInputStream());
                    System.out.println(response);
                    JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
                    String name = jsonObj.getJSONObject("data").getString("full_name");
                    String bio = jsonObj.getJSONObject("data").getString("bio");
                    Log.i(TAG, "Got name: " + name + ", bio [" + bio + "]");
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }
                mHandler.sendMessage(mHandler.obtainMessage(what, 2, 0));
            }
        }.start();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_ERROR) {
                progressDialog.dismiss();
                if(msg.arg1 == 1) {
                    listener.onFail("Failed to get access token");
                }
                else if(msg.arg1 == 2) {
                    listener.onFail("Failed to get user information");
                }
            }
            else if(msg.what == WHAT_FETCH_INFO) {
                fetchUserName();
            }
            else {
                progressDialog.dismiss();
                listener.onSuccess();
            }
        }
    };

    public boolean hasAccessToken() {
        return (instagramAccessToken == null) ? false : true;
    }

    public void setListener(OAuthAuthenticationListener listener) {
        this.listener = listener;
    }

    public String getId() {
        return prefUtil.getId();
    }

    public String getName() {
        return prefUtil.getName();
    }

    public void authorize() {
        dialog.show();
    }

    private String streamToString(InputStream is) throws IOException {
        String str = "";

        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
            } finally {
                is.close();
            }
            str = sb.toString();
        }
        return str;
    }

    public void resetAccessToken() {
        if (instagramAccessToken != null) {
            prefUtil.resetAccessToken();
            instagramAccessToken = null;
        }
    }

    public String getInstagramAccessToken() {
        return instagramAccessToken;
    }

    public interface OAuthAuthenticationListener {
        void onSuccess();
        void onFail(String error);
    }
}