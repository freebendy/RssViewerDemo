package com.roadmap.activity;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import com.roadmap.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class ImageDownloader {
    
    private static final String LOG_TAG = "roadmap.ImageDownloader";
    
    private Context mContext;
    
    public ImageDownloader(Context aContext ) {
        mContext = aContext;
    }

    /**
     * Download the specified image from the Internet and binds it to the provided ImageView. The
     * binding is immediate if the image is found in the cache and will be done asynchronously
     * otherwise. A null bitmap will be associated to the ImageView if an error occurs.
     *
     * @param aUrl The URL of the image to download.
     * @param aImageView The ImageView to bind the downloaded image to.
     */
    public void download(String aUrl, ImageView aImageView) {
        Log.v(LOG_TAG, "download: Url = " + aUrl );
        Bitmap bitmap = getBitmapFromCache(aUrl);

        if (bitmap == null) {
            forceDownload(aUrl, aImageView);
        } else {
            cancelPotentialDownload(aUrl, aImageView);
            aImageView.setImageBitmap(bitmap);
        }
    }

    /**
     * Same as download but the image is always downloaded and the cache is not used.
     * Kept private at the moment as its interest is not clear.
     */
    private void forceDownload(String aUrl, ImageView aImageView) {
        Log.v(LOG_TAG, "forceDownload" );
        // State sanity: url is guaranteed to never be null in DownloadedDrawable and cache keys.
        if (aUrl == null) {
            aImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.default_image));
            return;
        }

        if (cancelPotentialDownload(aUrl, aImageView)) {

            BitmapDownloaderTask task = new BitmapDownloaderTask(aImageView);
            DownloadedDrawable downloadedDrawable = new DownloadedDrawable(mContext, task);
            aImageView.setImageDrawable(downloadedDrawable);
            task.execute(aUrl);
        }
    }
    
    /**
     * Returns true if the current download has been canceled or if there was no download in
     * progress on this image view.
     * Returns false if the download in progress deals with the same url. The download is not
     * stopped in that case.
     */
    private static boolean cancelPotentialDownload(String aUrl, ImageView aImageView) {
        Log.v(LOG_TAG, "cancelPotentialDownload" );
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(aImageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.mUrl;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(aUrl))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }
    
    /**
     * @param aImageView Any imageView
     * @return Retrieve the currently active download task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView aImageView) {
        Log.v(LOG_TAG, "getBitmapDownloaderTask" );
        if (aImageView != null) {
            Drawable drawable = aImageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }
    
    Bitmap downloadBitmap(String aUrl) {
        Log.v(LOG_TAG, "downloadBitmap" );
        // AndroidHttpClient is not allowed to be used from the main thread
        final HttpClient client = AndroidHttpClient.newInstance("Android");
        final HttpGet getRequest = new HttpGet(aUrl);

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.w("ImageDownloader", "Error " + statusCode +
                        " while retrieving bitmap from " + aUrl);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    // return BitmapFactory.decodeStream(inputStream);
                    // Bug on slow connections, fixed in future release.
                    return BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (IOException e) {
            getRequest.abort();
            Log.w(LOG_TAG, "I/O error while retrieving bitmap from " + aUrl, e);
        } catch (IllegalStateException e) {
            getRequest.abort();
            Log.w(LOG_TAG, "Incorrect URL: " + aUrl);
        } catch (Exception e) {
            getRequest.abort();
            Log.w(LOG_TAG, "Error while retrieving bitmap from " + aUrl, e);
        } finally {
            if ((client instanceof AndroidHttpClient)) {
                ((AndroidHttpClient) client).close();
            }
        }
        return null;
    }
    
    /*
     * An InputStream that skips the exact number of bytes provided, unless it reaches EOF.
     */
    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream aInputStream) {
            super(aInputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
    
    /**
     * The actual AsyncTask that will asynchronously download the image.
     */
    class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private String mUrl;
        private final WeakReference<ImageView> mImageViewReference;

        public BitmapDownloaderTask(ImageView aImageView) {
            mImageViewReference = new WeakReference<ImageView>(aImageView);
        }

        /**
         * Actual download method.
         */
        @Override
        protected Bitmap doInBackground(String... aParams) {
            Log.v(LOG_TAG, "doInBackground: Url = " + mUrl );
            mUrl = aParams[0];
            return downloadBitmap(mUrl);
        }

        /**
         * Once the image is downloaded, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap aBitmap) {
            Log.v(LOG_TAG, "onPostExecute: Url = " + mUrl );
            if (isCancelled()) {
                aBitmap = null;
            }

            addBitmapToCache(mUrl, aBitmap);

            if (mImageViewReference != null) {
                ImageView imageView = mImageViewReference.get();
                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                
                // Change bitmap only if this process is still associated with it
                if (this == bitmapDownloaderTask) {
                    imageView.setImageBitmap(aBitmap);
                }
            }
        }
    }
    
    /**
     * A fake Drawable that will be attached to the imageView while the download is in progress.
     *
     * <p>Contains a reference to the actual download task, so that a download task can be stopped
     * if a new binding is required, and makes sure that only the last started download process can
     * bind its result, independently of the download finish order.</p>
     */
    static class DownloadedDrawable extends BitmapDrawable {
        private final WeakReference<BitmapDownloaderTask> mBitmapDownloaderTaskReference;

        public DownloadedDrawable(Context aContext, BitmapDownloaderTask bitmapDownloaderTask) {
            super(BitmapFactory.decodeResource(aContext.getResources(), R.drawable.default_image));
            mBitmapDownloaderTaskReference =
                new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return mBitmapDownloaderTaskReference.get();
        }
    }
    
    /*
     * Cache-related fields and methods.
     * 
     * We use a hard and a soft cache. A soft reference cache is too aggressively cleared by the
     * Garbage Collector.
     */
    
    private static final int HARD_CACHE_CAPACITY = 20;
//    private static final int DELAY_BEFORE_PURGE = HARD_CACHE_CAPACITY * 3 * 1000; // in milliseconds

    // Hard cache, with a fixed maximum capacity and a life duration
    private final HashMap<String, Bitmap> mHardBitmapCache =
        new LinkedHashMap<String, Bitmap>(HARD_CACHE_CAPACITY / 2, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest) {
            if (size() > HARD_CACHE_CAPACITY) {
                // Entries push-out of hard reference cache are transferred to soft reference cache
                mSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
                return true;
            } else
                return false;
        }
    };

    // Soft cache for bitmaps kicked out of hard cache
    private final static ConcurrentHashMap<String, SoftReference<Bitmap>> mSoftBitmapCache =
        new ConcurrentHashMap<String, SoftReference<Bitmap>>(HARD_CACHE_CAPACITY / 2);

//    private final Handler mPurgeHandler = new Handler();
//
//    private final Runnable mPurger = new Runnable() {
//        public void run() {
//            clearCache();
//        }
//    };

    /**
     * Adds this bitmap to the cache.
     * @param bitmap The newly downloaded bitmap.
     */
    private void addBitmapToCache(String aUrl, Bitmap aBitmap) {
        Log.v(LOG_TAG, "addBitmapToCache: Url = " + aUrl );
        if (aBitmap != null) {
            synchronized (mHardBitmapCache) {
                mHardBitmapCache.put(aUrl, aBitmap);
            }
        }
    }

    /**
     * @param url The URL of the image that will be retrieved from the cache.
     * @return The cached bitmap or null if it was not found.
     */
    private Bitmap getBitmapFromCache(String aUrl) {
        Log.v(LOG_TAG, "getBitmapFromCache: Url = " + aUrl );
        // First try the hard reference cache
        synchronized (mHardBitmapCache) {
            final Bitmap bitmap = mHardBitmapCache.get(aUrl);
            if (bitmap != null) {
                // Bitmap found in hard cache
                // Move element to first position, so that it is removed last
                mHardBitmapCache.remove(aUrl);
                mHardBitmapCache.put(aUrl, bitmap);
                return bitmap;
            }
        }

        // Then try the soft reference cache
        SoftReference<Bitmap> bitmapReference = mSoftBitmapCache.get(aUrl);
        if (bitmapReference != null) {
            final Bitmap bitmap = bitmapReference.get();
            if (bitmap != null) {
                // Bitmap found in soft cache
                return bitmap;
            } else {
                // Soft reference has been Garbage Collected
                mSoftBitmapCache.remove(aUrl);
            }
        }

        return null;
    }
 
    /**
     * Clears the image cache used internally to improve performance. Note that for memory
     * efficiency reasons, the cache will automatically be cleared after a certain inactivity delay.
     */
//    public void clearCache() {
//        mHardBitmapCache.clear();
//        mSoftBitmapCache.clear();
//    }

    /**
     * Allow a new delay before the automatic cache clear is done.
     */
//    private void resetPurgeTimer() {
//        mPurgeHandler.removeCallbacks(mPurger);
//        mPurgeHandler.postDelayed(mPurger, DELAY_BEFORE_PURGE);
//    }

}
