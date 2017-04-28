package org.jjam.instatwittaface;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Visar instagraminlägg i full skala
 * @author Mårten Persson
 */
public class DetailActivity extends Activity implements View.OnTouchListener {

    private GestureDetector gestureDetector;
    private View.OnTouchListener gestureListener;
    private ImageView imageView;
    private TextView textView, tvUsername;

    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);
        initComponenets();
        setListener();
    }

    private void initComponenets() {
        imageView = (ImageView) findViewById(R.id.ivInstaDetail);
        textView = (TextView) findViewById(R.id.tvInstaDescription);
        tvUsername = (TextView)findViewById(R.id.tvUsername);
        String image_url = getIntent().getStringExtra("LARGE_IMAGE");
        String description = getIntent().getStringExtra("CONTENT");
        textView.setText(description);
        String name = getIntent().getStringExtra("USERNAME");
        tvUsername.setText(name);
        new LoadBigImage(imageView).execute(image_url);
        gestureDetector = new GestureDetector(this, new GestureListener());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
    }

    public void setListener(){
        imageView.setOnTouchListener(DetailActivity.this);
        imageView.setOnTouchListener(gestureListener);
        textView.setOnTouchListener(DetailActivity.this);
        textView.setOnTouchListener(gestureListener);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                    result = true;
                }
                result = true;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }

        private void onSwipeLeft() {
            onBackPressed();
        }

        private void onSwipeRight() {
            onBackPressed();
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }
}