package com.fruits.ntorin.mango.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.fruits.ntorin.mango.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;


public class BitmapFunctions {

    private static final int IO_BUFFER_SIZE = 4 * 1024;

    public static Bitmap getBitmapFromURL(String url) throws IOException {
        Bitmap bitmap = null;
        InputStream in = null;
        BufferedOutputStream out = null;

        try {
            ////Log.d("getBitmapFromURL", "opening connection");
            in = new BufferedInputStream(new URL(url).openStream(), IO_BUFFER_SIZE);
            ////Log.d("getBitmapFromURL", "connection opened");

            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
            copy(in, out);
            out.flush();

            final byte[] data = dataStream.toByteArray();

            ////Log.d("getBitmapFromURL", "starting decode");
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            ////Log.d("getBitmapFromURL", "decode done");
        } catch (IOException e) {
            Log.e("e", "Could not load Bitmap from: " + url);
            //Log.d("stopping", "throwing error");
            throw new IOException();
        } finally {
            closeStream(in);
            closeStream(out);
        }

        return bitmap;
    }

    /**
     * Closes the specified stream.
     *
     * @param stream The stream to close.
     */
    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                android.util.Log.e("e", "Could not close stream", e);
            }
        }
    }

    /**
     * Copy the content of the input stream into the output stream, using a
     * temporary byte array buffer whose size is defined by
     * {@link #IO_BUFFER_SIZE}.
     *
     * @param in The input stream to copy from.
     * @param out The output stream to copy to.
     * @throws IOException If any error occurs during the copy.
     */
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }



    public static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }

    }

    public static boolean cancelPotentialWork(Uri data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Uri bitmapData = bitmapWorkerTask.getData();
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == null || bitmapData != data) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public static void loadBitmap(Uri uri, ImageView imageView, Resources res, LruCache<String, Bitmap> cache,
                                  Set<SoftReference<Bitmap>> softcache, int downscale){
        final String imageKey = uri.getPath();
        final Bitmap bitmap = getBitmapFromMemCache(imageKey, cache);

        if(bitmap != null && !bitmap.isRecycled()){
            //Log.d("BitmapFunctions", "found bitmap in cache: " + uri.getPath());
            imageView.setImageBitmap(bitmap);
        }else if (BitmapFunctions.cancelPotentialWork(uri, imageView)) {
            //Log.d("BitmapFunctions", "bitmap not in cache");
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView, cache, softcache, downscale);
            final BitmapFunctions.AsyncDrawable asyncDrawable =
                    new BitmapFunctions.AsyncDrawable(res, BitmapFactory.decodeResource(res, R.drawable.noimage), task);
            imageView.setImageDrawable(asyncDrawable);
            //AnimationDrawable d = (AnimationDrawable) imageView.getDrawable();
            //d.start();
            task.execute(uri);
        }
    }

    public static void loadBitmap(Uri uri, SubsamplingScaleImageView imageView, Resources res, LruCache<String, Bitmap> cache,
                                  Set<SoftReference<Bitmap>> softcache){
        final String imageKey = uri.getPath();
        final Bitmap bitmap = getBitmapFromMemCache(imageKey, cache);

        if(bitmap != null && !bitmap.isRecycled()){
            //Log.d("BitmapFunctions", "found bitmap in cache: " + uri.getPath());
            imageView.setImage(ImageSource.bitmap(bitmap));
        }else {
            //Log.d("BitmapFunctions", "bitmap not in cache");
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView, cache, softcache);
            //imageView.setImage(ImageSource.resource(R.drawable.loading_animation));
            task.execute(uri);
        }

    }

    public static void addBitmapToMemoryCache(String key, Bitmap bitmap, LruCache<String, Bitmap> cache) {
        if (getBitmapFromMemCache(key, cache) == null) {
            if(bitmap != null) {
                cache.put(key, bitmap);
            }
        }
    }

    public static Bitmap getBitmapFromMemCache(String key, LruCache<String, Bitmap> cache) {
        return cache.get(key);
    }

    public static void addInBitmapOptions(BitmapFactory.Options options,
                                           Set<SoftReference<Bitmap>> cache) {
        // inBitmap only works with mutable bitmaps, so force the decoder to
        // return mutable bitmaps.
        options.inMutable = true;

        if (cache != null) {
            // Try to find a bitmap to use for inBitmap.
            Bitmap inBitmap = getBitmapFromReusableSet(options, cache);

            if (inBitmap != null) {
                // If a suitable bitmap has been found, set it as the value of
                // inBitmap.
                options.inBitmap = inBitmap;
            }
        }
    }

    public static Bitmap getBitmapFromReusableSet(BitmapFactory.Options options, Set<SoftReference<Bitmap>> cache) {
        Bitmap bitmap = null;

        if (cache != null && !cache.isEmpty()) {
            synchronized (cache) {
                final Iterator<SoftReference<Bitmap>> iterator
                        = cache.iterator();
                Bitmap item;

                while (iterator.hasNext()) {
                    item = iterator.next().get();

                    if (null != item && item.isMutable()) {
                        // Check to see it the item can be used for inBitmap.
                        if (canUseForInBitmap(item, options)) {
                            bitmap = item;

                            // Remove from reusable set so it can't be used again.
                            iterator.remove();
                            break;
                        }
                    } else {
                        // Remove from the set if the reference has been cleared.
                        iterator.remove();
                    }
                }
            }
        }
        return bitmap;
    }

    static boolean canUseForInBitmap(
            Bitmap candidate, BitmapFactory.Options targetOptions) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // From Android 4.4 (KitKat) onward we can re-use if the byte size of
            // the new bitmap is smaller than the reusable bitmap candidate
            // allocation byte count.
            int width = targetOptions.outWidth / targetOptions.inSampleSize;
            int height = targetOptions.outHeight / targetOptions.inSampleSize;
            int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
            return byteCount <= candidate.getAllocationByteCount();
        }

        // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
        return candidate.getWidth() == targetOptions.outWidth
                && candidate.getHeight() == targetOptions.outHeight
                && targetOptions.inSampleSize == 1;
    }

    /**
     * A helper function to return the byte usage per pixel of a bitmap based on its configuration.
     */
    static int getBytesPerPixel(Config config) {
        if (config == Config.ARGB_8888) {
            return 4;
        } else if (config == Config.RGB_565) {
            return 2;
        } else if (config == Config.ARGB_4444) {
            return 2;
        } else if (config == Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    public static Bitmap decodeSampledBitmapFromUri(Uri uri,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri.getPath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(uri.getPath(), options);
    }
}
