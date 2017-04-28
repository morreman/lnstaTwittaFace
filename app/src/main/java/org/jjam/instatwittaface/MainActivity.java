package org.jjam.instatwittaface;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import com.facebook.FacebookSdk;
import android.widget.ProgressBar;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import io.fabric.sdk.android.Fabric;

/**
 * Visar lista med flöden. Fungerar som en kontroller för att hantera flödena.
 * @author Jerry Pedersen, Jonas Remgård, Anton Nilsson, Mårten Persson
 */
public class MainActivity extends AppCompatActivity {

    private List<Post> posts;
    private FeedArrayAdapter adapter;
    private ListView feedListView;
    private FetchFeeds ff;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private PrefUtil prefUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.twitter_key), getString(R.string.twitter_secret));
        Fabric.with(this, new Twitter(authConfig));

        FacebookSdk.sdkInitialize(getApplicationContext());

        initComponents();

        getFeeds();
    }

    private void initComponents() {
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, getString(R.string.post_not_implementet_yet), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        posts = new ArrayList<>();
        feedListView = (ListView) findViewById(R.id.feed_list_view);
        ff = new FetchFeeds(this);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.activity_main_swipe_refresh_layout);
        swipeRefreshLayout.setRefreshing(true);
        adapter = new FeedArrayAdapter(this, R.layout.row_view, posts);
        feedListView = (ListView) findViewById(R.id.feed_list_view);
        feedListView.setAdapter(adapter);
        feedListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            private Post post;

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                post = (Post) feedListView.getItemAtPosition(i);
                if(post.getCompany()== "Instagram") {
                    Intent detailActivity = new Intent(getApplicationContext(), DetailActivity.class);
                    detailActivity.putExtra("LARGE_IMAGE", post.getLargeImage());
                    detailActivity.putExtra("CONTENT", post.getText());
                    detailActivity.putExtra("USERNAME", post.getUserName());
                    startActivity(detailActivity);
                }
                return true;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFeeds();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_feed) {
            Intent intent = new Intent(this, AddFeedActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public void updatePostsList() {
        adapter = new FeedArrayAdapter(this, R.layout.row_view, posts);
        feedListView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    public void getFeeds() {
        prefUtil = new PrefUtil(this);
        if (!prefUtil.isLoggedIn()) {
            Intent intent = new Intent(this, AddFeedActivity.class);
            startActivity(intent);
        } else {
            ff.twittaDone = false;
            ff.instaDone = false;
            ff.faceDone = false;
            ff.getInstagramFeeds();
            ff.getTwitterFeeds();
            ff.getFacebookFeeds();
            new GetFeedAsync().execute("");
        }
    }

    private class GetFeedAsync extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            while (!ff.instaDone && !ff.twittaDone && !ff.faceDone) {}
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            ff.generatePostList();
            updatePostsList();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
