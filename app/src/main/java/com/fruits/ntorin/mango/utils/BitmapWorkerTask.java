package com.fruits.ntorin.mango.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Set;

public class BitmapWorkerTask extends AsyncTask<Uri, Void, Bitmap> {
    private WeakReference<ImageView> imageViewReference = null;
    private WeakReference<SubsamplingScaleImageView> subsamplingScaleImageViewWeakReference = null;
    private LruCache<String, Bitmap> cache;
    private Set<SoftReference<Bitmap>> softcache;
    private int scale = 0;

    public Uri getData() {
        return data;
    }

    public void setData(Uri data) {
        this.data = data;
    }

    private Uri data = null;

    public BitmapWorkerTask(SubsamplingScaleImageView imageView, LruCache<String, Bitmap> cache,
                            Set<SoftReference<Bitmap>> softcache) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        subsamplingScaleImageViewWeakReference =
                new WeakReference<SubsamplingScaleImageView>(imageView);
        this.cache = cache;
        this.softcache = softcache;
    }

    public BitmapWorkerTask(ImageView imageView, LruCache<String, Bitmap> cache,
                            Set<SoftReference<Bitmap>> softcache, int scale) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
        this.cache = cache;
        this.softcache = softcache;
        this.scale = scale;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Uri... params) {
        data = params[0];
        BitmapFactory.Options downscale = new BitmapFactory.Options();
        if(imageViewReference != null) {
            downscale.inSampleSize = scale;
        }
        //Bitmap bitmap = BitmapFunctions.decodeSampledBitmapFromUri(data);
        Bitmap bitmap = BitmapFactory.decodeFile(data.getPath(), downscale);
        BitmapFactory.Options options = new BitmapFactory.Options();
        BitmapFunctions.addInBitmapOptions(options, softcache);
        BitmapFunctions.addBitmapToMemoryCache(data.getPath(), bitmap, cache);
        return bitmap;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if (subsamplingScaleImageViewWeakReference != null && bitmap != null) {
            final SubsamplingScaleImageView imageView = subsamplingScaleImageViewWeakReference.get();
            if (imageView != null) {
                imageView.setImage(ImageSource.bitmap(bitmap));
            }
        }

        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask =
                    BitmapFunctions.getBitmapWorkerTask(imageView);
            if (this == bitmapWorkerTask && imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
