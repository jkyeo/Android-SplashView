package com.jkyeo.splashview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by jkyeo on 16/7/7.
 */
public class SplashView extends FrameLayout {

    ImageView splashImageView;
    TextView skipButton;

    private static final String IMG_URL = "splash_img_url";
    private static final String ACT_URL = "splash_act_url";
    private static String IMG_PATH = null;
    private static final String SP_NAME = "splash";
    private static final int skipButtonSizeInDip = 36;
    private static final int skipButtonMarginInDip = 16;
    private Integer duration = 6;
    private static final int delayTime = 1000;   // 每隔1000 毫秒执行一次

    private String imgUrl = null;
    private String actUrl = null;

    private boolean isActionBarShowing = true;

    private Activity mActivity = null;

    private OnSplashViewActionListener mOnSplashViewActionListener = null;

    private Handler handler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (0 == duration) {
                dismissSplashView(false);
                return;
            } else {
                setDuration(--duration);
            }
            handler.postDelayed(timerRunnable, delayTime);
        }
    };

    private void setImage(Bitmap image) {
        splashImageView.setImageBitmap(image);
    }

    public SplashView(Activity context) {
        super(context);
        mActivity = context;
        initComponents();
    }

    public SplashView(Activity context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = context;
        initComponents();
    }

    public SplashView(Activity context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mActivity = context;
        initComponents();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SplashView(Activity context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mActivity = context;
        initComponents();
    }

    private GradientDrawable splashSkipButtonBg = new GradientDrawable();

    void initComponents() {
        splashSkipButtonBg.setShape(GradientDrawable.OVAL);
        splashSkipButtonBg.setColor(Color.parseColor("#66333333"));

        splashImageView = new ImageView(mActivity);
        splashImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        splashImageView.setBackgroundColor(mActivity.getResources().getColor(android.R.color.white));
        LayoutParams imageViewLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(splashImageView, imageViewLayoutParams);
        splashImageView.setClickable(true);

        skipButton = new TextView(mActivity);
        int skipButtonSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, skipButtonSizeInDip, mActivity.getResources().getDisplayMetrics());
        LayoutParams skipButtonLayoutParams = new LayoutParams(skipButtonSize, skipButtonSize);
        skipButtonLayoutParams.gravity = Gravity.TOP|Gravity.RIGHT;
        int skipButtonMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, skipButtonMarginInDip, mActivity.getResources().getDisplayMetrics());
        skipButtonLayoutParams.setMargins(0, skipButtonMargin, skipButtonMargin, 0);
        skipButton.setGravity(Gravity.CENTER);
        skipButton.setTextColor(mActivity.getResources().getColor(android.R.color.white));
        skipButton.setBackgroundDrawable(splashSkipButtonBg);
        skipButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        this.addView(skipButton, skipButtonLayoutParams);

        skipButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissSplashView(true);
            }
        });

        setDuration(duration);
        handler.postDelayed(timerRunnable, delayTime);
    }

    private void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    private void setActUrl(String actUrl) {
        this.actUrl = actUrl;
    }

    private void setDuration(Integer duration) {
        this.duration = duration;
        skipButton.setText(String.format("跳过\n%d s", duration));
    }

    private void setOnSplashImageClickListener(@Nullable final OnSplashViewActionListener listener) {
        if (null == listener) return;
        mOnSplashViewActionListener = listener;
        splashImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSplashImageClick(actUrl);
            }
        });
    }

    /**
     * static method, show splashView on above of the activity
     * you should called after setContentView()
     * @param activity  activity instance
     * @param durationTime  time to countDown
     * @param defaultBitmapRes  if there's no cached bitmap, show this default bitmap;
     *                          if null == defaultBitmapRes, then will not show the splashView
     * @param listener  splash view listener contains onImageClick and onDismiss
     */
    public static void showSplashView(@NonNull Activity activity,
                                      @Nullable Integer durationTime,
                                      @Nullable Integer defaultBitmapRes,
                                      @Nullable OnSplashViewActionListener listener) {

        ViewGroup contentView = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        if (null == contentView || 0 == contentView.getChildCount()) {
            throw new IllegalStateException("You should call showSplashView() after setContentView() in Activity instance");
        }
        IMG_PATH = activity.getFilesDir().getAbsolutePath().toString() + "/splash_img.jpg";
        SplashView splashView = new SplashView(activity);
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        splashView.setOnSplashImageClickListener(listener);
        if (null != durationTime) splashView.setDuration(durationTime);
        Bitmap bitmapToShow = null;

        if (isExistsLocalSplashData(activity)) {
            bitmapToShow = BitmapFactory.decodeFile(IMG_PATH);
            SharedPreferences sp = activity.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            splashView.setImgUrl(sp.getString(IMG_URL, null));
            splashView.setActUrl(sp.getString(ACT_URL, null));
        } else if (null != defaultBitmapRes) {
            bitmapToShow = BitmapFactory.decodeResource(activity.getResources(), defaultBitmapRes);
        }

        if (null == bitmapToShow) return;
        splashView.setImage(bitmapToShow);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (activity instanceof AppCompatActivity) {
            ActionBar supportActionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (null != supportActionBar) {
                supportActionBar.setShowHideAnimationEnabled(false);
                splashView.isActionBarShowing = supportActionBar.isShowing();
                supportActionBar.hide();
            }
        } else if (activity instanceof Activity) {
            android.app.ActionBar actionBar = activity.getActionBar();
            if (null != actionBar) {
                splashView.isActionBarShowing = actionBar.isShowing();
                actionBar.hide();
            }
        }
        contentView.addView(splashView, param);
    }

    /**
     * simple way to show splash view, set all non-able param as non
     * @param activity
     */
    public static void simpleShowSplashView(@NonNull Activity activity) {
        showSplashView(activity, null, null, null);
    }

    private void dismissSplashView(boolean initiativeDismiss) {
        if (null != mOnSplashViewActionListener) mOnSplashViewActionListener.onSplashViewDismiss(initiativeDismiss);


        handler.removeCallbacks(timerRunnable);
        final ViewGroup parent = (ViewGroup) this.getParent();
        if (null != parent) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(SplashView.this, "scale", 0.0f, 0.5f).setDuration(600);
            animator.start();
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float cVal = (Float) animation.getAnimatedValue();
                    SplashView.this.setAlpha(1.0f - 2.0f * cVal);
                    SplashView.this.setScaleX(1.0f + cVal);
                    SplashView.this.setScaleY(1.0f + cVal);
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    parent.removeView(SplashView.this);
                    showSystemUi();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    parent.removeView(SplashView.this);
                    showSystemUi();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
    }

    private void showSystemUi() {
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (mActivity instanceof AppCompatActivity) {
            ActionBar supportActionBar = ((AppCompatActivity) mActivity).getSupportActionBar();
            if (null != supportActionBar) {
                if (isActionBarShowing) supportActionBar.show();
            }
        } else if (mActivity instanceof Activity) {
            android.app.ActionBar actionBar = mActivity.getActionBar();
            if (null != actionBar) {
                if (isActionBarShowing) actionBar.show();
            }
        }
    }

    private static boolean isExistsLocalSplashData(Activity activity) {
        SharedPreferences sp = activity.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String imgUrl = sp.getString(IMG_URL, null);
        return !TextUtils.isEmpty(imgUrl) && isFileExist(IMG_PATH);
    }

    /**
     * static method, update splash view data
     * @param imgUrl - url of image which you want to set as splash image
     * @param actionUrl - related action url, such as webView etc.
     */
    public static void updateSplashData(@NonNull Activity activity, @NonNull String imgUrl, @Nullable String actionUrl) {
        IMG_PATH = activity.getFilesDir().getAbsolutePath().toString() + "/splash_img.jpg";

        SharedPreferences.Editor editor = activity.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(IMG_URL, imgUrl);
        editor.putString(ACT_URL, actionUrl);
        editor.apply();

        getAndSaveNetWorkBitmap(imgUrl);
    }

    public interface OnSplashViewActionListener {
        void onSplashImageClick(String actionUrl);
        void onSplashViewDismiss(boolean initiativeDismiss);
    }

    private static void getAndSaveNetWorkBitmap(final String urlString) {
        Runnable getAndSaveImageRunnable = new Runnable() {
            @Override
            public void run() {
                URL imgUrl = null;
                Bitmap bitmap = null;
                try {
                    imgUrl = new URL(urlString);
                    HttpURLConnection urlConn = (HttpURLConnection) imgUrl.openConnection();
                    urlConn.setDoInput(true);
                    urlConn.connect();
                    InputStream is = urlConn.getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                    saveBitmapFile(bitmap, IMG_PATH);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(getAndSaveImageRunnable).start();
    }

    private static void saveBitmapFile(Bitmap bm, String filePath) throws IOException {
        File myCaptureFile = new File(filePath);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        bos.flush();
        bos.close();
    }

    public static boolean isFileExist(String filePath) {
        if(TextUtils.isEmpty(filePath)) {
            return false;
        } else {
            File file = new File(filePath);
            return file.exists() && file.isFile();
        }
    }
}
