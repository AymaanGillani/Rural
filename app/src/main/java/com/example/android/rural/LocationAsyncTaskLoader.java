package com.example.android.rural;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

public class LocationAsyncTaskLoader extends AsyncTaskLoader<List<Locations>> {
    public static final String LOG_TAG = LocationAsyncTaskLoader.class.getSimpleName();
    String[] url;

    public LocationAsyncTaskLoader(Context context, String... urls) {
        super(context);
        if (urls.length < 1 || urls[0] == null) {
                Log.e(LOG_TAG,"Url is null");
        }
        url = urls;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Locations> loadInBackground() {
        if (url == null) {
            return null;
        }
        List<Locations> result = QueryUtils.fetchLocationData(url[0]);
        return result;
    }
}
