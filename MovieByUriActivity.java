package com.osell;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.MediaController;
import android.widget.VideoView;

import com.osell.view.TopRedView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by inred on 2015/4/23.
 */
public class MovieByUriActivity extends OChatBaseActivity {


    private VideoView videoView;
    private String uriPath;
    private String progressStr, title;
    private String path = Environment.getExternalStorageDirectory().getPath() + "/Video";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_uri_activity);

        Intent it = getIntent();
        uriPath = it.getStringExtra("uriPath");
        if (TextUtils.isEmpty(uriPath)) {
            finish();
            return;
        }
        progressStr = getResources().getString(R.string.please_wait_video_down);
        title = getResources().getString(R.string.video_play);

        downFileThread(path, uriPath);
        initView();

    }

    private void initView() {
        TopRedView topView = new TopRedView(this, title);
        topView.showBackBtn();
        topView.hideSend();

        videoView = (VideoView) findViewById(R.id.uri_videoview);
//        videoView.setVideoURI(Uri.parse(uriPath));
        videoView.setMediaController(new MediaController(this));
//        videoView.requestFocus();
//        videoView.start();

    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            hideProgressDialog();
            videoView.setVideoPath(path + "/uri.mp4");
            videoView.start();
        }
    };

    public void downFileThread(final String path, final String url) {
        showCanCloseProgressDialog(progressStr);
        new Thread(new Runnable() {
            @Override
            public void run() {
                downFile(path, url);
            }
        }).start();
    }

    public void downFile(String path, String url) {
        File file = new File(path);
        if (!file.exists() || (file.exists() && file.isFile()))
            file.mkdirs();
        try {
            URL ur = new URL(url);
            URLConnection conn = ur.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            int size = conn.getContentLength();
            FileOutputStream fos = new FileOutputStream(path + "/uri.mp4");
            byte buf[] = new byte[1024];
            do {
                int numread = is.read(buf);
                if (numread == -1) {
                    break;
                }
                fos.write(buf, 0, numread);
            } while (true);
            handler.sendEmptyMessage(0);
            is.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected boolean isLoginRequired() {
        return true;
    }
}
