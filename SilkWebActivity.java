package com.osell.activity.silk.mission;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.osell.R;
import com.osell.activity.web.ShowByGetUrlAndNameActivity;
import com.osell.activity.web.SwipeBackWebActivity;
import com.osell.global.OSellCommon;
import com.osell.net.OSellInfo;

public class SilkWebActivity extends SwipeBackWebActivity {

    private String url;
    private boolean isshowtitle;
    private LinearLayout TitleLayout;
    private ImageView bacImage;
    private LinearLayout searchLayout;
    private String currentUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.newo2owebview);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Intent it = getIntent();
        url = it.getStringExtra("url");
        isshowtitle = it.getBooleanExtra("isshowtitle", false);
        if (TextUtils.isEmpty(url)) {
            finish();
            return;
        }
        if (it.getBooleanExtra("isLandscape",false)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (it.getBooleanExtra("iscapture", false)) {
            int samplecode = getIntent().getIntExtra("samplecode", 0);
            if (samplecode != 0) {
                vCodeRecord(samplecode + "");
            } else
                vCodeRecord(url);
        }


        searchLayout = (LinearLayout) findViewById(R.id.search_layout);
        searchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SilkWebActivity.this, com.osell.activity.web.ShowByGetUrlActivity.class);
                intent.putExtra("url", OSellCommon.getOSellInfo().SERVER_PREFIX + "search/topicindex");
                startActivity(intent);
            }
        });

        TitleLayout = (LinearLayout) findViewById(R.id.nav_header_layout);
        bacImage = (ImageView) findViewById(R.id.qr_code);
        bacImage.setBackgroundResource(R.drawable.red_header_press_btn);
        bacImage.setImageResource(R.drawable.icon_return);
        bacImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mWebView.canGoBack() && !mIsErr) {
                    mWebView.goBack();
                } else {
                    finish();
                }
            }
        });
        setNavRightBtnVisibility(View.VISIBLE);
        setNavRightBtnImageRes(R.drawable.icon_cplus);
        super.onCreate(savedInstanceState);
        if (isShowTitleUrl(url)) {
            setLoadingtitleVisable(false);
        } else
            TitleLayout.setVisibility(View.GONE);

        setIsNoScroll(true);
        WebSettings settings = mWebView.getSettings();
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(false);
        settings.setDisplayZoomControls(false);
        mWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);

    }

    @Override
    protected void onNavRightBtnClick() {
        super.onNavRightBtnClick();

    }


    @Override
    protected void addJavascriptInterface() {
//        mWebView.addJavascriptInterface(new GetCamera_OverView(), "getCamera");

        //2015年1月29号web界面二维码扫描
        mWebView.addJavascriptInterface(new OverviewApi(),
                "overviewApi"); //注册首页 api接口 刘宇 20150129
        mWebView.addJavascriptInterface(new GetCamera_OrderWeb(), "getCamera");
        mWebView.addJavascriptInterface(new sendImage(), "sendImage");
        mWebView.addJavascriptInterface(new sendVideo(), "sendVideo");
    }

    @Override
    protected void initWebView() {
        mWebView = (WebView) findViewById(R.id.MyWebView);
//        mProgressBar = (ProgressBar) findViewById(R.id.myProgressBar);
    }


    @Override
    protected boolean webViewShouldOverrideUrlLoading(WebView view, String url) {
        currentUrl = url;
        if (!isOsellUrl(url) || isJumpUrl(url) || isPay(url)) {
            Intent it = new Intent(this, ShowByGetUrlAndNameActivity.class);
            it.putExtra("url", url);
            it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // 20150205 刘宇 所有从网页点击打开app activity 的地方 全部加上这个flag
            startActivity(it);
            return true;
        }
        if (url.toLowerCase().startsWith(OSellInfo.O2OSERVER_PREFIX + "businesscircle/circledetail/"))
            isHideProgressBar = true;
        else
            isHideProgressBar = false;

        if (isShowTitleUrl(url)) {
            TitleLayout.setVisibility(View.VISIBLE);
        } else {
            TitleLayout.setVisibility(View.GONE);
        }
        return super.webViewShouldOverrideUrlLoading(view, url);
    }

    @Override
    protected void webViewOnPageStarted(WebView view, String url, Bitmap favicon) {
        if (isShowTitleUrl(url)) {
            setLoadingtitleVisable(false);
            TitleLayout.setVisibility(View.VISIBLE);
        } else {
            setLoadingtitleVisable(true);
            TitleLayout.setVisibility(View.GONE);
        }
        super.webViewOnPageStarted(view, url, favicon);
    }

    @Override
    protected String getUrl() {
        return url;
    }

    public boolean isShowTitleUrl(String url) {
        return url.toLowerCase().startsWith(OSellInfo.O2OSERVER_PREFIX + "businesscircle/myreviewlist") ||
                url.toLowerCase().startsWith(OSellInfo.O2OSERVER_PREFIX + "dynamic/detail")
                || url.toLowerCase().startsWith(OSellInfo.O2OSERVER_PREFIX + "user/userpage");
    }

    public void vCodeRecord(final String osellurl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    OSellCommon.getOSellInfo().vCodeValue(getLoginInfo().userID, osellurl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected boolean isStatusBar() {
        return false;
    }

    protected boolean isJumpUrl(String url) {
        return url.toLowerCase().startsWith(OSellInfo.O2OSERVER_PREFIX + "order/selproduct")
                || url.toLowerCase().startsWith(OSellInfo.O2OSERVER_PREFIX + "order/handorder")
                || url.toLowerCase().startsWith(OSellInfo.O2OSERVER_PREFIX + "order/list");
    }

}
