package com.example.sunchaser.app.net;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.util.Log;
import android.view.WindowManager;

import com.example.sunchaser.app.data.WikiImageModel;
import com.example.sunchaser.app.data.dbcontract.WikiImageEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by smee on 07/06/15.
 */
public class WikipediaImageInfoClient extends SunChaserHttpRequestClient<Set<WikiImageModel>> {

    private static final String WIKI_SEARCH_FORMAT = "https://en.wikipedia.org/w/api.php?";

    private static final int MAX_BATCH_SIZE = 50;

    private final List<ContentValues> valuesToStore = new ArrayList<>();

    private final long currentTimestamp = System.currentTimeMillis();

    private final Context context;
    private final List<String> pageTitles;
    private Set<String> fileNameBatch;
    private int currentPageTitleIndex = 0;
    private String continueToken = "";


    public WikipediaImageInfoClient(Context context, Set<String> pageTitles) {
        this.context = context;
        this.pageTitles = new ArrayList<>(pageTitles);
        this.fileNameBatch = getNextFileNameBatch();
    }

    @Override
    protected boolean isRequestNecessary() {
        return true;
    }

    @Override
    protected URL getRequestUrl() throws MalformedURLException {
        String format = "json";
        String action = "query";
        String properties = "imageinfo";
        String imageInfoProperties = "url|mime|thumbmime|size";
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        String imageInfoUrlWidth = Integer.toString(size.x);
        String imageInfoUrlHeight = Integer.toString(size.x);
        String titles = formatImageFilenames(fileNameBatch);

        Uri uri = Uri.parse(WIKI_SEARCH_FORMAT).buildUpon()
                .appendQueryParameter("format", format)
                .appendQueryParameter("action", action)
                .appendQueryParameter("prop", properties)
                .appendQueryParameter("iiprop", properties)
                .appendQueryParameter("iiprop", imageInfoProperties)
                .appendQueryParameter("iiurlwidth", imageInfoUrlWidth)
                .appendQueryParameter("iiurlheight", imageInfoUrlHeight)
                .appendQueryParameter("titles", titles)
                .appendQueryParameter("continue", continueToken)
                .build();

        return new URL(uri.toString());
    }

    private boolean fetchedDataForAllTitles() {
        return currentPageTitleIndex >= pageTitles.size();
    }

    private Set<String> getNextFileNameBatch() {
        if (fetchedDataForAllTitles()) {
            return Collections.EMPTY_SET;
        }
        if (pageTitles.size() <= MAX_BATCH_SIZE) {
            currentPageTitleIndex = pageTitles.size();
            return new HashSet<>(pageTitles);
        }

        Set<String> batch = new HashSet<>(MAX_BATCH_SIZE);
        for (int i = 0; i < MAX_BATCH_SIZE && currentPageTitleIndex < pageTitles.size(); i++) {
            batch.add(pageTitles.get(currentPageTitleIndex));
            currentPageTitleIndex++;
        }

        return batch;
    }

    private String formatImageFilenames(Set<String> titleBatch) {
        StringBuilder builder = new StringBuilder();

        for (String title : titleBatch) {
            if (title == null || title.isEmpty()) {
                continue;
            }
            if (builder.length() != 0) {
                builder.append('|');
            }
            builder.append(title);
        }

        return builder.toString();
    }

    @Override
    protected Set<WikiImageModel> processResponse(String responseString) throws JSONException {
        Log.d(LOG_TAG, "Received data from Wikipedia: " + responseString);

        JSONObject responseObject = new JSONObject(responseString);
        if (!responseObject.has("query")) {
            return Collections.emptySet();
        }
        JSONObject queryObject = responseObject.getJSONObject("query");
        JSONObject pagesObject = queryObject.getJSONObject("pages");

        Iterator<String> keyIterator = pagesObject.keys();
        Set<WikiImageModel> results = new HashSet<>();

        while (keyIterator.hasNext()) {
            String nextKey = keyIterator.next();
            JSONObject page = pagesObject.getJSONObject(nextKey);

            String title = page.getString("title");

            if (page.has("imageinfo")) {
                JSONArray imageInfo = page.getJSONArray("imageinfo");
                JSONObject latestRevision = imageInfo.getJSONObject(0);
                int width = latestRevision.getInt("width");
                int height = latestRevision.getInt("height");
                String url = latestRevision.getString("url");
                String mime = latestRevision.getString("mime");
                String thumbUrl = latestRevision.getString("thumburl");
                String thumbMime = latestRevision.getString("thumbmime");
                // TODO: Probably need to get copyright info as well...

                ContentValues contentValues = new ContentValues();
                contentValues.put(WikiImageEntry.COLUMN_FILENAME, title);
                contentValues.put(WikiImageEntry.COLUMN_WIDTH, width);
                contentValues.put(WikiImageEntry.COLUMN_HEIGHT, height);
                contentValues.put(WikiImageEntry.COLUMN_URL, url);
                contentValues.put(WikiImageEntry.COLUMN_MIME_TYPE, mime);
                contentValues.put(WikiImageEntry.COLUMN_THUMBNAIL_URL, thumbUrl);
                contentValues.put(WikiImageEntry.COLUMN_THUMBNAIL_MIME_TYPE, thumbMime);
                contentValues.put(WikiImageEntry.COLUMN_TIMESTAMP, currentTimestamp);

                valuesToStore.add(contentValues);
                results.add(new WikiImageModel(title, url, width, height));
            }

        }

        ContentValues[] valuesToStoreArray = new ContentValues[valuesToStore.size()];
        valuesToStore.toArray(valuesToStoreArray);
        int recordsInserted = context.getContentResolver().bulkInsert(WikiImageEntry.CONTENT_URI, valuesToStoreArray);

        // Check whether we need to make another call to get more of the response
        String continueObjectParamName = "continue";
        if (responseObject.has(continueObjectParamName)) {
            JSONObject continueObject = responseObject.getJSONObject(continueObjectParamName);

            continueToken = continueObject.getString("continue");
            results.addAll(makeRequest());
            return results;
        }

        if (!fetchedDataForAllTitles()) {
            fileNameBatch = getNextFileNameBatch();
            results.addAll(makeRequest());
            return results;
        }

        Log.d(LOG_TAG, "Inserted " + recordsInserted + " wiki image entries");

        return results;
    }

    @Override
    protected Set<WikiImageModel> getDefaultValue() {
        return Collections.emptySet();
    }
}
