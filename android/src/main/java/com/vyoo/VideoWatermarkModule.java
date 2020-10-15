package com.vyoo;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.daasuu.mp4compose.filter.GlFilter;
import com.daasuu.mp4compose.composer.Mp4Composer;
import com.daasuu.mp4compose.filter.GlWatermarkFilter;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.graphics.BitmapFactory;

public class VideoWatermarkModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public VideoWatermarkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "VideoWatermark";
    }

    @ReactMethod
    public void convert(String videoPath, Callback callback) {
        watermarkVideoWithImage(videoPath, callback);
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            // handle exception
        }

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 200, 168, false);

        return resizedBitmap;
    }

    public void watermarkVideoWithImage(String videoPath, final Callback callback) {
        File destFile = new File(this.getReactApplicationContext().getFilesDir(), "converted.mp4");
        if (!destFile.exists()) {
            try {
                destFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        final String destinationPath = destFile.getPath();
        new Mp4Composer(Uri.fromFile(new File(videoPath)), destinationPath, reactContext)
                .filter(new GlWatermarkFilter(getBitmapFromAsset(reactContext, "images/watermark.png"), GlWatermarkFilter.Position.LEFT_BOTTOM))
                .listener(new Mp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {
                        Log.e("Progress", progress + "");
                    }

                    @Override
                    public void onCompleted() {
                        callback.invoke(destinationPath);
                    }

                    @Override
                    public void onCanceled() {

                    }

                    @Override
                    public void onFailed(Exception exception) {
                        exception.printStackTrace();
                        callback.invoke(null);
                    }
                }).start();
    }
}
