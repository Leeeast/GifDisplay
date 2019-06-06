package com.ldl.gifdisplay;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private Bitmap bitmap;
    private GifHandler gifHandler;
    private ImageView imageView;
    private SimpleDraweeView mSdv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageview);
        mSdv = findViewById(R.id.sdb);
        Button btnDisplayGif = findViewById(R.id.btn_display_gif);
        copyAssetAndWrite("demo.gif");
        copyAssetAndWrite("demo2.gif");
        btnDisplayGif.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                File file = new File(getCacheDir(), "demo.gif");
                gifHandler = new GifHandler(file.getAbsolutePath());
                bitmap = Bitmap.createBitmap(gifHandler.getWidth(), gifHandler.getHeight(),
                        Bitmap.Config.ARGB_8888);
                int delay = gifHandler.updateFrame(bitmap);
                mHandler.sendEmptyMessageDelayed(1, delay);
            }
        });
        findViewById(R.id.btn_fresco).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(getCacheDir(), "demo.gif");
                AbstractDraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(Uri.parse("asset:///demo.gif"))
                        .setAutoPlayAnimations(true)
                        .build();
                mSdv.setController(controller);
            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int delay = gifHandler.updateFrame(bitmap);
            imageView.setImageBitmap(bitmap);
            mHandler.sendEmptyMessageDelayed(1, delay);
        }
    };

    /**
     * 将asset文件写入缓存
     */
    private boolean copyAssetAndWrite(String fileName) {
        try {
            File cacheDir = getCacheDir();
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            File outFile = new File(cacheDir, fileName);
            if (!outFile.exists()) {
                boolean res = outFile.createNewFile();
                if (!res) {
                    return false;
                }
            } else {
                if (outFile.length() > 10) {//表示已经写入一次
                    return true;
                }
            }
            InputStream is = getAssets().open(fileName);
            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
