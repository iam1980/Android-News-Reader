package com.suredigit.naftemporikihd;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This helper class download images from the Internet and binds those with the provided ImageView.
 *
 * <p>It requires the INTERNET permission, which should be added to your application's manifest
 * file.</p>
 *
 * A local cache of downloaded images is maintained internally to improve performance.
 */
public class TextDownloader {
    private static final String LOG_TAG = "TextDownloader";

    public enum Mode { NO_ASYNC_TASK, NO_DOWNLOADED_DRAWABLE, CORRECT }
    private Mode mode = Mode.CORRECT;
    
    /**
     * Download the specified image from the Internet and binds it to the provided ImageView. The
     * binding is immediate if the image is found in the cache and will be done asynchronously
     * otherwise. A null bitmap will be associated to the ImageView if an error occurs.
     *
     * @param url The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    public void download(String url, String text) {
        resetPurgeTimer();
        String tcache = getStringFromCache(url);

        if (tcache == null) {
            forceDownload(url, text);
        } else {
            cancelPotentialDownload(url, text);
            text = (tcache);
        }
    }

    /*
     * Same as download but the image is always downloaded and the cache is not used.
     * Kept private at the moment as its interest is not clear.
       private void forceDownload(String url, ImageView view) {
          forceDownload(url, view, null);
       }
     */

    /**
     * Same as download but the image is always downloaded and the cache is not used.
     * Kept private at the moment as its interest is not clear.
     */
    private void forceDownload(String url, String text) {
        // State sanity: url is guaranteed to never be null in DownloadedDrawable and cache keys.
        if (url == null) {
            text = null;
            return;
        }

        if (cancelPotentialDownload(url, text)) {
            switch (mode) {
                case NO_ASYNC_TASK:
                    String myText = downloadString(url);
                    addStringToCache(url, myText);
                    text =  (myText);
                    break;

                case NO_DOWNLOADED_DRAWABLE:
                    //text.setMinimumHeight(156);
                    StringDownloaderTask task = new StringDownloaderTask(text);
                    task.execute(url);
                    break;

                case CORRECT:
                    task = new StringDownloaderTask(text);
                    DownloadedString downloadedDrawable = new DownloadedString(task);
                    text = "";
                    //text.setMinimumHeight(156);
                    task.execute(url);
                    break;
            }
        }
    }

    /**
     * Returns true if the current download has been canceled or if there was no download in
     * progress on this image view.
     * Returns false if the download in progress deals with the same url. The download is not
     * stopped in that case.
     */
    private static boolean cancelPotentialDownload(String url, String text) {
        StringDownloaderTask bitmapDownloaderTask = getStringDownloaderTask(text);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    /**
     * @param text Any imageView
     * @return Retrieve the currently active download task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static StringDownloaderTask getStringDownloaderTask(String text) {
        if (text != null) {
            String myText = text;
            if (myText instanceof String) {
                DownloadedString downloadedDrawable = new DownloadedString(myText);
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    String downloadString(String url) {
        final int IO_BUFFER_SIZE = 4 * 1024;

        // AndroidHttpClient is not allowed to be used from the main thread
        final HttpClient client = (mode == Mode.NO_ASYNC_TASK) ? new DefaultHttpClient() :
            AndroidHttpClient.newInstance("Android");
        final HttpGet getRequest = new HttpGet(url);

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.w("ImageDownloader", "Error " + statusCode +
                        " while retrieving bitmap from " + url);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    // return BitmapFactory.decodeStream(inputStream);
                    // Bug on slow connections, fixed in future release.
                    return MainActivity.convertStreamToString(inputStream); 
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (IOException e) {
            getRequest.abort();
            Log.w(LOG_TAG, "I/O error while retrieving bitmap from " + url, e);
        } catch (IllegalStateException e) {
            getRequest.abort();
            Log.w(LOG_TAG, "Incorrect URL: " + url);
        } catch (Exception e) {
            getRequest.abort();
            Log.w(LOG_TAG, "Error while retrieving bitmap from " + url, e);
        } finally {
            if ((client instanceof AndroidHttpClient)) {
                ((AndroidHttpClient) client).close();
            }
        }
        return null;
    }

   

    /**
     * The actual AsyncTask that will asynchronously download the image.
     */
    class StringDownloaderTask extends AsyncTask<String, Void, String> {
        private String url;
        private final WeakReference<String> stringReference;

        public StringDownloaderTask(String string) {
            stringReference = new WeakReference<String>(string);
        }

        /**
         * Actual download method.
         */
        @Override
        protected String doInBackground(String... params) {
            url = params[0];
            return downloadString(url);
        }

        /**
         * Once the image is downloaded, associates it to the imageView
         */
        @Override
        protected void onPostExecute(String string) {
            if (isCancelled()) {
            	string = null;
            }

            addStringToCache(url, string);

            if (stringReference != null) {
                String myString = stringReference.get();
                StringDownloaderTask bitmapDownloaderTask = getStringDownloaderTask(myString);
                // Change bitmap only if this process is still associated with it
                // Or if we don't use any bitmap to task association (NO_DOWNLOADED_DRAWABLE mode)
                if ((this == bitmapDownloaderTask) || (mode != Mode.CORRECT)) {
                    myString = (string);
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
    static class DownloadedString{
        private final WeakReference<StringDownloaderTask> stringDownloaderTaskReference;
        private String mString = "";

        public DownloadedString(String string){
        	stringDownloaderTaskReference = null;
        	mString = string;
        }
        
        public DownloadedString(StringDownloaderTask bitmapDownloaderTask) {
            //super(Color.BLACK);
        	mString = "";
            stringDownloaderTaskReference =
                new WeakReference<StringDownloaderTask>(bitmapDownloaderTask);
        }

        public StringDownloaderTask getBitmapDownloaderTask() {
            return stringDownloaderTaskReference.get();
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        clearCache();
    }

    
    /*
     * Cache-related fields and methods.
     * 
     * We use a hard and a soft cache. A soft reference cache is too aggressively cleared by the
     * Garbage Collector.
     */
    
    private static final int HARD_CACHE_CAPACITY = 10;
    private static final int DELAY_BEFORE_PURGE = 10 * 1000; // in milliseconds

    // Hard cache, with a fixed maximum capacity and a life duration
    private final HashMap<String, String> sHardTextCache =
        new LinkedHashMap<String, String>(HARD_CACHE_CAPACITY / 2, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(LinkedHashMap.Entry<String, String> eldest) {
            if (size() > HARD_CACHE_CAPACITY) {
                // Entries push-out of hard reference cache are transferred to soft reference cache
                sSoftTextCache.put(eldest.getKey(), new SoftReference<String>(eldest.getValue()));
                return true;
            } else
                return false;
        }
    };

    // Soft cache for bitmaps kicked out of hard cache
    private final static ConcurrentHashMap<String, SoftReference<String>> sSoftTextCache =
        new ConcurrentHashMap<String, SoftReference<String>>(HARD_CACHE_CAPACITY / 2);

    private final Handler purgeHandler = new Handler();

    private final Runnable purger = new Runnable() {
        public void run() {
            clearCache();
        }
    };

    /**
     * Adds this bitmap to the cache.
     * @param bitmap The newly downloaded bitmap.
     */
    private void addStringToCache(String url, String text) {
        if (text != null) {
            synchronized (sHardTextCache) {
                sHardTextCache.put(url, text);
            }
        }
    }

    /**
     * @param url The URL of the image that will be retrieved from the cache.
     * @return The cached bitmap or null if it was not found.
     */
    private String getStringFromCache(String url) {
        // First try the hard reference cache
        synchronized (sHardTextCache) {
            final String text = sHardTextCache.get(url);
            if (text != null) {
                // Bitmap found in hard cache
                // Move element to first position, so that it is removed last
                sHardTextCache.remove(url);
                sHardTextCache.put(url, text);
                return text;
            }
        }

        // Then try the soft reference cache
        SoftReference<String> stringReference = sSoftTextCache.get(url);
        if (stringReference != null) {
            final String string = stringReference.get();
            if (string != null) {
                // Bitmap found in soft cache
                return string;
            } else {
                // Soft reference has been Garbage Collected
                sSoftTextCache.remove(url);
            }
        }

        return null;
    }
 
    /**
     * Clears the image cache used internally to improve performance. Note that for memory
     * efficiency reasons, the cache will automatically be cleared after a certain inactivity delay.
     */
    public void clearCache() {
        sHardTextCache.clear();
        sSoftTextCache.clear();
    }

    /**
     * Allow a new delay before the automatic cache clear is done.
     */
    private void resetPurgeTimer() {
        purgeHandler.removeCallbacks(purger);
        purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
    }
}