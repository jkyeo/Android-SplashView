# Android-SplashView

闪屏页或者广告页 (SplashView) 在众多 App 里是比较常见的。一般来说 SplashView 有以下职责：

- 在合适的时机显示 SplashView - 可控性
- 下载、缓存、更新图片
- 回调响应图片点击事件
- 倒计时 Dismiss View，主动跳过 Dissmiss View
- 本地没有缓存时，显示默认图片或者不显示 SplashView

<!-- more -- > 

好了，先看看效果：

![Default SplashView Demo](http://ww4.sinaimg.cn/large/006tNc79gw1f5p3fx3mlwg315o0nfgzr.gif)

![Normal SplashView Demo](http://ww2.sinaimg.cn/large/006tNbRwgw1f5p2r46gzug315o0nfkjm.gif)

在 Activity 上显示 SplashView， 需要注意的是要在 Activity setContentView(int viewId) 之后调用:

```java
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        // call after setContentView(R.layout.activity_sample);
        SplashView.simpleShowSplashView(this);
}
```

也可以自定义超时时间、默认图片、回调方法等：

```java
/**
     * static method, show splashView on above of the activity
     * you should called after setContentView()
     * @param activity  activity instance
     * @param durationTime  time to countDown
     * @param defaultBitmapRes  if there's no cached bitmap, show this default bitmap;
     *                          if null == defaultBitmapRes, then will not show the splashView
     * @param listener  splash view listener contains onImageClick and onDismiss
     */
SplashView.showSplashView(this, 3, R.drawable.default_img, new SplashView.OnSplashViewActionListener() {
            @Override
            public void onSplashImageClick(String actionUrl) {
                Log.d("SplashView", "img clicked. actionUrl: " + actionUrl);
                Toast.makeText(SampleActivity.this, "img clicked.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSplashViewDismiss(boolean initiativeDismiss) {
                Log.d("SplashView", "dismissed, initiativeDismiss: " + initiativeDismiss);
            }
        });
```

更新 SplashView 数据方法 updateSplashData 可以在任何时候任何地方调用：

```java
// call this method anywhere to update splash view data
SplashView.updateSplashData(this, "http://ww2.sinaimg.cn/large/72f96cbagw1f5mxjtl6htj20g00sg0vn.jpg", "http://jkyeo.com");
```

说说原理，其实很简单。如图选中 FrameLayout 下唯一的 ChildView 是我们自定义的通过 setContentView 设置的 View。那么我们只需要讲 SplashView 作为这个 FrameLayout 的 ChildView 覆盖在最上层即可。合适的时候 remove 实现 Dismiss。

![](http://ww1.sinaimg.cn/large/006tNc79gw1f5p4jjodf5j31i610kk3c.jpg)

核心代码：

```java
ViewGroup contentView = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
...
contentView.addView(splashView, param);
```

剩下的就是处理缓存、定时器、Dismiss 动画、显示隐藏状态栏，ActionBar 的问题了。

详情见博文：[SplashView-一行代码解决闪屏页-广告页-Android-篇](http://jkyeo.com/2016/07/10/SplashView-一行代码解决闪屏页-广告页-Android-篇/)

对应 iOS(Swift) 版本在这里: [SplashView-一行代码解决闪屏页-广告页-iOS-Swift-篇](http://jkyeo.com/2016/07/10/SplashView-一行代码解决闪屏页-广告页-iOS-Swift-篇/)

