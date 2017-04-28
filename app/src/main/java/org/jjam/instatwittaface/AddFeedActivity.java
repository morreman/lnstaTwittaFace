package org.jjam.instatwittaface;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

/**
 * Klass för att lägga till ett flöde ( logga in )
 * @author Jerry Pedersen, Jonas Remgård, Anton Nilsson, Mårten Persson
 */
public class AddFeedActivity extends AppCompatActivity {

    private PrefUtil prefUtil;
    private TwitterLoginButton twitterLoginButton;
    private LoginButton facebookLoginButton;
    private CallbackManager callbackManager;
    private InstagramApp instagramApp;
    private Button instagramLoginButton;
    private ImageView ivAppLogo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_feed);

        prefUtil = new PrefUtil(this);

        ivAppLogo = (ImageView) findViewById(R.id.appLogo);
        ivAppLogo.setBackground(getDrawable(R.drawable.applogo));

        initTwitterButton();
        initFacebookButton();
        initInstagramButton();

    }

    private void initTwitterButton() {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.twitter_key), getString(R.string.twitter_secret));
        Fabric.with(this, new Twitter(authConfig));

        final LinearLayout addFeedLayout = (LinearLayout) findViewById(R.id.add_feed_layout);
        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        final Button twitterLogoutButton = (Button) findViewById(R.id.twitter_logout_button);

        if (null != TwitterCore.getInstance().getSessionManager().getActiveSession()) {
            Log.d("TwitterButton", "Twitter logged in");
            addFeedLayout.removeView(twitterLoginButton);
            twitterLogoutButton.setText(getString(R.string.log_out));

            twitterLogoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Twitter.getInstance();
                    Twitter.logOut();
                    prefUtil.setTwitterLoggedIn(false);
                    addFeedLayout.removeView(twitterLogoutButton);
                    addFeedLayout.addView(twitterLoginButton, 1);
                    setTwitterButtonCallback();
                }
            });
        } else {
            addFeedLayout.removeView(twitterLogoutButton);
            setTwitterButtonCallback();
        }
    }

    private void setTwitterButtonCallback() {
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterSession session = result.data;

                if (session != null) {
                    String msg = "@" + session.getUserName() + " logged in! (#" + session.getUserId() + ")";
                    Snackbar.make(findViewById(R.id.add_feed_layout), "Twitter logged in as: " + session.getUserName(), Snackbar.LENGTH_LONG).show();
                    Log.d("TWITTER", "This is the profile: " + session.getUserName());
                }
                prefUtil.setTwitterLoggedIn(true);
                AddFeedActivity.this.recreate();
            }

            @Override
            public void failure(TwitterException exception) {
                Log.e("TwitterKit", "Login with Twitter failure", exception);
            }
        });
    }

    private void initFacebookButton() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        facebookLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        facebookLoginButton.setReadPermissions("user_posts");
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Profile profile = Profile.getCurrentProfile();
                String userId = loginResult.getAccessToken().getUserId();
                String accessToken = loginResult.getAccessToken().getToken();

                //stores the accesstoken
                AccessToken.setCurrentAccessToken(loginResult.getAccessToken());

                // save accessToken to SharedPreference
                prefUtil.saveAccessToken(accessToken);
                if (profile != null) {
                    Snackbar.make(findViewById(R.id.add_feed_layout), "Facebook logged in to: " + profile.getName(), Snackbar.LENGTH_LONG).show();
                    Log.d("FACEBOOK", "This is the profile: " + userId);
                } else Log.e("FacebookLogin", "profile is null");
            }

            @Override
            public void onCancel() {
                Log.d("FACEBOOK", "onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();
                Log.d("FACEBOOK", "onError");
            }
        });
    }

    private void initInstagramButton() {
        instagramApp = new InstagramApp(AddFeedActivity.this, getString(R.string.instagram_id),
                getString(R.string.instagram_secret), getString(R.string.instagram_callback));
        instagramApp.setListener(listener);

        instagramLoginButton = (Button) findViewById(R.id.btnConnectInsta);
        instagramLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (instagramApp.hasAccessToken()) {
                    prefUtil.setInstagramAccessToken(instagramApp.getInstagramAccessToken());
                    final AlertDialog.Builder builder = new AlertDialog.Builder(
                            AddFeedActivity.this);
                    builder.setMessage("Disconnect from Instagram?")
                            .setCancelable(false)
                            .setPositiveButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            instagramApp.resetAccessToken();
                                            instagramLoginButton.setText(getString(R.string.instagram_log_in));
                                        }
                                    })
                            .setNegativeButton("No",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    final AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    instagramApp.authorize();
                    prefUtil.setInstagramAccessToken(instagramApp.getInstagramAccessToken());
                }
            }
        });

        if (instagramApp.hasAccessToken()) {
            instagramLoginButton.setText("Disconnect");
            prefUtil.setInstagramAccessToken(instagramApp.getInstagramAccessToken());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    InstagramApp.OAuthAuthenticationListener listener = new InstagramApp.OAuthAuthenticationListener() {

        @Override
        public void onSuccess() {
            instagramLoginButton.setText(getString(R.string.log_out));
        }

        @Override
        public void onFail(String error) {
            Toast.makeText(AddFeedActivity.this, error, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
