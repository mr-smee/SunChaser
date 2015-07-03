package com.example.sunchaser.app.net;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.sunchaser.app.data.WikiArticle;
import com.example.sunchaser.app.data.dbcontract.WikiArticleEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by smee on 07/06/15.
 */
public class WikipediaArticleClient extends SunChaserHttpRequestClient<Map<String, WikiArticle>> {

    private static final String WIKI_SEARCH_FORMAT = "https://en.wikipedia.org/w/api.php?";

    private final Map<Integer, ContentValues> articlesToStore = new HashMap<>();

    private final long currentTimestamp = System.currentTimeMillis();

    private Context context;
    private List<String> pageTitles;
    private Map<String, String> normalisedTitles;
    private String continueToken = "";
    private String extractContinueToken = "";
    private String imageContinueToken = "";


    public WikipediaArticleClient(Context context, List<String> pageTitles) {
        this.context = context;
        this.pageTitles = pageTitles;
    }

    @Override
    protected boolean isRequestNecessary() {
        return true;
    }

    @Override
    protected URL getRequestUrl() throws MalformedURLException {
        String format = "json";
        String action = "query";
        String properties = "extracts|images";
        String exIntro = "";
        String explainText = "true";
        String titles = formatPageTitles();

        // TODO: 'exvariant' convert content into this language variant

        Uri uri = Uri.parse(WIKI_SEARCH_FORMAT).buildUpon()
                .appendQueryParameter("format", format)
                .appendQueryParameter("action", action)
                .appendQueryParameter("prop", properties)
                .appendQueryParameter("redirects", "")
                .appendQueryParameter("exintro", exIntro)
                .appendQueryParameter("explaintext", explainText)
                .appendQueryParameter("titles", titles)
                .appendQueryParameter("continue", continueToken)
                .build();

        if (!extractContinueToken.isEmpty()) {
            uri = uri.buildUpon().appendQueryParameter("excontinue", extractContinueToken).build();
        }
        if (!imageContinueToken.isEmpty()) {
            uri = uri.buildUpon().appendQueryParameter("imcontinue", imageContinueToken).build();
        }

        return new URL(uri.toString());
    }

    private String formatPageTitles() {
        StringBuilder builder = new StringBuilder();

        for (String title : pageTitles) {
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
    protected Map<String, WikiArticle> processResponse(String responseString) throws JSONException {
        Log.d(LOG_TAG, "Received data from Wikipedia: " + responseString);

        JSONObject responseObject = new JSONObject(responseString);
        JSONObject queryObject = responseObject.getJSONObject("query");
        JSONObject pagesObject = queryObject.getJSONObject("pages");

        if (normalisedTitles == null) {
            normalisedTitles = new HashMap<>();
            if (queryObject.has("normalized")) {
                JSONArray normalisedArray = queryObject.getJSONArray("normalized");
                for (int i = 0; i < normalisedArray.length(); i++) {
                    JSONObject normalised = normalisedArray.getJSONObject(i);
                    normalisedTitles.put(normalised.getString("to"), normalised.getString("from"));
                }
            }
        }

        Iterator<String> keyIterator = pagesObject.keys();
        while (keyIterator.hasNext()) {
            String nextKey = keyIterator.next();
            JSONObject page = pagesObject.getJSONObject(nextKey);

            if (!page.has("pageid") || page.has("missing")) {
                continue;
            }
            int pageId = page.getInt("pageid");

            ContentValues contentValues = articlesToStore.get(pageId);
            if (contentValues == null) {
                contentValues = new ContentValues();
                contentValues.put(WikiArticleEntry.COLUMN_ARTICLE_ID, pageId);
                contentValues.put(WikiArticleEntry.COLUMN_TIMESTAMP, currentTimestamp);
                articlesToStore.put(pageId, contentValues);
            }

            if (page.has("title")) {
                String title = page.getString("title");
                String denormalisedTitle = normalisedTitles.get(title);
                if (denormalisedTitle != null) {
                    title = denormalisedTitle;
                }
                contentValues.put(WikiArticleEntry.COLUMN_TITLE, title);
            }

            if (page.has("extract")) {
                String extract = page.getString("extract");
                if (extract != null) {
                    extract = extract.replaceAll("\n", "\n\n");
                }
                contentValues.put(WikiArticleEntry.COLUMN_EXTRACT, extract);
            }

            if (page.has("images")) {
                JSONArray imagesArray = page.getJSONArray("images");

                String imageNames = contentValues.getAsString(WikiArticleEntry.COLUMN_IMAGES);
                StringBuilder imageBuilder = (imageNames == null) ? new StringBuilder() : new StringBuilder(imageNames);

                for (int i = 0; i < imagesArray.length(); i++) {
                    JSONObject imageObject = imagesArray.getJSONObject(i);
                    String imageTitle = imageObject.getString("title");
                    // TODO: Check if there are any others we want to cull here
                    if (imageTitle.endsWith(".svg")) {
                        continue;
                    }
                    if (imageBuilder.length() != 0) {
                        imageBuilder.append('|');
                    }
                    imageBuilder.append(imageTitle);
                }

                contentValues.put(WikiArticleEntry.COLUMN_IMAGES, imageBuilder.toString());
            }
        }

        // Check whether we need to make another call to get more of the response
        String continueObjectParamName = "continue";
        if (responseObject.has(continueObjectParamName)) {
            JSONObject continueObject = responseObject.getJSONObject(continueObjectParamName);

            if (continueObject.has("continue")) {
                continueToken = continueObject.getString("continue");
            } else {
                continueToken = "";
            }
            if (continueObject.has("excontinue")) {
                extractContinueToken = continueObject.getString("excontinue");
            } else {
                extractContinueToken = "";
            }
            if (continueObject.has("imcontinue")) {
                imageContinueToken = continueObject.getString("imcontinue");
            } else {
                imageContinueToken = "";
            }
            return makeRequest();
        }

        ContentValues[] valuesToStoreArray = new ContentValues[articlesToStore.size()];
        articlesToStore.values().toArray(valuesToStoreArray);
        int recordsInserted = context.getContentResolver().bulkInsert(WikiArticleEntry.CONTENT_URI, valuesToStoreArray);
        Log.d(LOG_TAG, "Inserted " + recordsInserted + " wiki article entries");
// TODO: Tidy this up a lot!
        Map<String, WikiArticle> results = new HashMap<>(valuesToStoreArray.length);
        for (ContentValues cv : valuesToStoreArray) {
            results.put(cv.getAsString(WikiArticleEntry.COLUMN_TITLE),
                    new WikiArticle(cv.getAsInteger(WikiArticleEntry.COLUMN_ARTICLE_ID),
                            cv.getAsString(WikiArticleEntry.COLUMN_TITLE),
                            cv.getAsString(WikiArticleEntry.COLUMN_EXTRACT),
                            cv.getAsString(WikiArticleEntry.COLUMN_IMAGES).split("\\|")));
        }

        return results;
    }

    @Override
    protected Map<String, WikiArticle> getDefaultValue() {
        return Collections.emptyMap();
    }
}
