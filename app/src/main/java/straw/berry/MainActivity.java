package straw.berry;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ViewGroup container;
    private TextView counterText;
    private int clickCount = 0;
    private final int STRAWBERRY_COUNT = 15;
    private final int[] strawberryImages = {
            R.drawable.strawberry1,
            R.drawable.strawberry2,
            R.drawable.strawberry3,
            R.drawable.strawberry4,
            R.drawable.strawberry5,
            R.drawable.strawberry6
    };
    private Random random = new Random();

    // Sound-related variables
    private SoundPool soundPool;
    private int soundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        container = findViewById(R.id.container);
        counterText = findViewById(R.id.counter_text);
        updateCounter();

        // Initialize sound
        initializeSound();

        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                startStrawberryRain();
            }
        });
    }

    private void initializeSound() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        soundId = soundPool.load(this, R.raw.munch2, 1);
    }

    private void startStrawberryRain() {
        for (int i = 0; i < 20; i++) {
            addStrawberry();
        }
    }

    private void addStrawberry() {
        ImageView strawberry = new ImageView(this);
        int imageResource = strawberryImages[random.nextInt(strawberryImages.length)];
        strawberry.setImageResource(imageResource);

        int size = dpToPx(40 + random.nextInt(40)); // Random size between 40dp and 80dp
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(size, size);
        strawberry.setLayoutParams(params);

        container.addView(strawberry);

        strawberry.setRotation(random.nextFloat() * 360);
        strawberry.setY(-size);

        strawberry.post(() -> {
            int maxX = container.getWidth() - strawberry.getWidth();
            if (maxX > 0) {
                strawberry.setX(random.nextInt(maxX));
            }
        });

        strawberry.setOnClickListener(v -> {
            clickCount++;
            updateCounter();
            container.removeView(v);
            playClickSound();
            if (clickCount == STRAWBERRY_COUNT) {
                triggerCrazyEvent();
            } else {
                addStrawberry();
            }
        });

        ObjectAnimator animator = ObjectAnimator.ofFloat(strawberry, "translationY", container.getHeight() + size);
        animator.setDuration(3000 + random.nextInt(3000));
        animator.setInterpolator(new AccelerateInterpolator(1.2f));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                container.removeView(strawberry);
                addStrawberry();
            }
        });
        animator.start();
    }
    // dGhlIHNlZWQgaGFzIGJlZW4gcGxhbnRlZCA=
    private void playClickSound() {
        soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    private void updateCounter() {
        counterText.setText(clickCount + "/" + STRAWBERRY_COUNT);
    }

    private void triggerCrazyEvent() {
        ImageView giantStrawberry = new ImageView(this);
        giantStrawberry.setImageResource(R.drawable.strawberry_giant);
        giantStrawberry.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        container.addView(giantStrawberry);

        giantStrawberry.setScaleX(0);
        giantStrawberry.setScaleY(0);
        giantStrawberry.animate()
                .scaleX(1)
                .scaleY(1)
                .setDuration(1000)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        resetGame();
                    }
                })
                .start();
    }

    private void resetGame() {
        container.removeAllViews();
        clickCount = 0;
        updateCounter();
        startStrawberryRain();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}