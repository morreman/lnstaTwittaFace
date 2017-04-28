package org.jjam.instatwittaface;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.security.Key;

/**
 * Hjälpklass för att hantera sharedpreferences
 * @author Jerry Pedersen, Jonas Remgård, Anton Nilsson, Mårten Persson
 */
public class PrefUtil {

    private static final String API_ACCESS_TOKEN = "access_token";
    private static final String SHARED = "Instagram_Preferences";
    private static final String API_USERNAME = "username";
    private static final String API_ID = "id";
    private static final String API_NAME = "name";

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    // constructor
    public PrefUtil(Activity activity) {
        if(activity != null) {
            sharedPref = activity.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
            editor = sharedPref.edit();
            editor.apply();
        }
    }

    public void saveAccessToken(String token) {
        editor.putString("token", token);
        editor.apply();
    }

    public String getToken() {
        return sharedPref.getString("token", null);
    }

    public void setInstagramAccessToken(String s) {
        editor.putString("instagramtoken", s);
        editor.apply();
    }
    public String getInstagramToken() {
        //sharedPref = activity.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
        String token = "";
        if(sharedPref != null) {
            token = sharedPref.getString(API_ACCESS_TOKEN, null);
        }
        return token;
    }

    public String getId() {
        String id = "";
        if(sharedPref != null) {
           id =  sharedPref.getString(API_ID, null);
        }
        return id;
    }

    public String getName() {
        String name = "";
        if(sharedPref != null) {
            name = sharedPref.getString(API_NAME, null);
        }
        return name;
    }

    public void setTwitterLoggedIn(boolean loggedIn) {
        editor.putBoolean("twitter_logged_in", loggedIn);
        editor.apply();
    }

    public void storeInstagramToken(String accessToken, String id, String username, String name) {
        editor.putString(API_ID, id);
        editor.putString(API_NAME, name);
        editor.putString(API_ACCESS_TOKEN, accessToken);
        editor.putString(API_USERNAME, username);
        editor.commit();
    }

    public void resetAccessToken() {
        editor.putString(API_ID, null);
        editor.putString(API_NAME, null);
        editor.putString(API_ACCESS_TOKEN, null);
        editor.putString(API_USERNAME, null);
        editor.commit();
    }

    public boolean isLoggedIn() {
        boolean twitterToken = sharedPref.getBoolean("twitter_logged_in", false);
        Object instagramToken = getInstagramToken();
        Object facebookToken = getToken();

        if (twitterToken != false || instagramToken != null || facebookToken != null) {
            return true;
        }

        return false;
    }
}
