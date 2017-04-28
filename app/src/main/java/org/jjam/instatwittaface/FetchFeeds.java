package org.jjam.instatwittaface;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Hämtar de olika flödena från de medier som är inloggade
 * @author Jerry Pedersen, Jonas Remgård, Anton Nilsson, Mårten Persson
 */
public class FetchFeeds {

    private static final String TAG = "FetchFeeds -- ";

    private MainActivity activity;
    private List<Post> postsTwitter, postsFacebook, postsInstagram;
    private PrefUtil prefUtil;

    // Set to public by choice
    public boolean faceDone, twittaDone, instaDone;

    public FetchFeeds(MainActivity activity) {
        this.activity = activity;
        prefUtil = new PrefUtil(activity);
    }

    public void getTwitterFeeds() {

        final List<Post> twitterPosts = new ArrayList<>();
        TwitterSession session = Twitter.getSessionManager().getActiveSession();

        if (session != null) {
            final TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
            twitterApiClient.getStatusesService().homeTimeline(null, null, null, null, null, null, null, new Callback<List<Tweet>>() {

                @Override
                public void success(Result<List<Tweet>> result) {
                    Iterator<Tweet> iter = result.data.iterator();

                    while (iter.hasNext()) {
                        Tweet tweet = iter.next();
                        Log.d(TAG, tweet.text + " Created at: " + tweet.createdAt);
                        try {
                            Post post = new Post("Twitter");
                            String time = tweet.createdAt;
                            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
                            java.util.Date date = format.parse(time);
                            post.setTime(date);
                            post.setText(tweet.text);
                            post.setUserName(tweet.user.name);
                            twitterPosts.add(post);
                            Log.d(TAG, "" + date.toString());
                        } catch (ParseException e) {
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                        Log.d(TAG, ""+twitterPosts.size());
                    }
                    postsTwitter = twitterPosts;
                    twittaDone = true;
                }

                @Override
                public void failure(TwitterException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void getFacebookFeeds(){

        final List<Post> facebookPosts = new ArrayList<>();

        /* make the API call */
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/feed",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        Log.d("FACEBOOK FEED ", "RESPONSE: "+response.toString());
                        try {
                            JSONArray jArray = response.getJSONObject().getJSONArray("data");
                            Log.d("FACEBOOK FEED", jArray.toString());
                            for(int i = 0; i<jArray.length(); i++){
                                JSONObject object = jArray.getJSONObject(i);
                                try {
                                    Post post = new Post("Facebook");
                                    String time = object.getString("created_time");
                                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                                    Date date = format.parse(time);
                                    post.setTime(date);
                                    if(object.has("message") && object.has("story")) {
                                        post.setText(object.getString("message") + " - " + object.getString("story"));
                                    }else if(object.has("message")){
                                        post.setText(object.getString("message"));
                                    }else if(object.has("story")){
                                        post.setText(object.getString("story"));
                                    }
                                    facebookPosts.add(post);
                                    Log.d("FACEBOOK FEED", date.toString());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            postsFacebook = facebookPosts;
                            faceDone = true;
                            Log.d("FACEBOOK FEED", "POSTS SET TO FACEBOOKPOSTS");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }

    public void getInstagramFeeds(){
        final String FEED_URL = "https://api.instagram.com/v1/users/self/feed?access_token=" + prefUtil.getInstagramToken();
        final List<Post> instagramPosts = new ArrayList<>();
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                StringBuilder stringBuilder = new StringBuilder();
                try {
                    String line;
                    URL url = new URL(FEED_URL);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    while ((line = in.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    JSONObject parentData = new JSONObject(stringBuilder.toString());
                    JSONArray parentDataArray = parentData.getJSONArray("data");
                    JSONObject tempObject;
                    JSONObject imagesObject;
                    JSONObject captionObject;
                    JSONObject nameObject;
                    for(int i=0; i<parentDataArray.length();i++) {
                        Post post = new Post(null, "Instagram", null);
                        tempObject = parentDataArray.getJSONObject(i);
                        imagesObject = tempObject.getJSONObject("images").getJSONObject("thumbnail");
                        post.setSmallImage(imagesObject.getString("url"));
                        imagesObject = tempObject.getJSONObject("images").getJSONObject("low_resolution");
                        post.setLargeImage(imagesObject.getString("url"));

                        long time = Long.parseLong(tempObject.getString("created_time"));
                        Date date = new Date(time*1000L);
                        post.setTime(date);

                        if(tempObject.isNull("caption")){
                            post.setText("No description");
                        } else{
                            captionObject = tempObject.getJSONObject("caption");
                            nameObject = tempObject.getJSONObject("user");
                            post.setText(captionObject.getString("text"));
                            post.setUserName(nameObject.getString("username"));
                        }
                        post.setSmallImageBitmap(BitMapCreator(post.getSmallImage()));
                        post.setLargeImageBitmap(BitMapCreator(post.getLargeImage()));
                        instagramPosts.add(post);
                    }
                    postsInstagram = instagramPosts;
                    Log.d("INSTAGRAM", "SETTING instaDone to TRUE!");
                    instaDone = true;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public Bitmap BitMapCreator(String imageurl) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(imageurl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = null;
            input = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public void generatePostList() {
        List<Post> posts = new ArrayList<>();

        //For all posts see witch one is first and put it in the posts list
        if(postsTwitter!= null) {
            for (Post t : postsTwitter) {
                posts.add(t);
            }
        }

        if(postsFacebook != null) {
            for (Post f : postsFacebook) {
                posts.add(f);
            }
        }

        if(postsInstagram != null) {
            for (Post i : postsInstagram) {
                posts.add(i);
            }
        }

        Collections.sort(posts);
        Collections.reverse(posts);

        activity.setPosts(posts);
    }
}
