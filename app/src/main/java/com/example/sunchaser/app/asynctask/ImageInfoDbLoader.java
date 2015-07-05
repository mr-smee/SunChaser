package com.example.sunchaser.app.asynctask;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import com.example.sunchaser.app.data.dbcontract.WikiImageEntry;
import com.example.sunchaser.app.data.WikiImageModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by smee on 09/06/15.
 */
public class ImageInfoDbLoader extends AsyncTaskLoader<Set<WikiImageModel>> {

    private static final int MIN_IMAGE_WIDTH = 150;
    private static final int MIN_IMAGE_HEIGHT = 150;

    private static final String[] WIKI_IMAGE_COLUMNS = {
            WikiImageEntry.COLUMN_FILENAME,
            WikiImageEntry.COLUMN_WIDTH,
            WikiImageEntry.COLUMN_HEIGHT,
            WikiImageEntry.COLUMN_THUMBNAIL_URL,
            WikiImageEntry.COLUMN_THUMBNAIL_MIME_TYPE,
    };

    private static final int WIKI_IMAGE_COLUMN_INDEX_FILENAME   = 0;
    private static final int WIKI_IMAGE_COLUMN_INDEX_WIDTH      = 1;
    private static final int WIKI_IMAGE_COLUMN_INDEX_HEIGHT     = 2;
    private static final int WIKI_IMAGE_COLUMN_INDEX_URL        = 3;
    private static final int WIKI_IMAGE_COLUMN_INDEX_MIME_TYPE  = 4;

    private final List<String> imageNames;

    public ImageInfoDbLoader(Context context, Collection<String> imageNames) {
        super(context);
        this.imageNames = new ArrayList<>(imageNames);
    }

    @Override
    public Set<WikiImageModel> loadInBackground() {
        Set<WikiImageModel> results = new HashSet<>();

        StringBuilder whereClauseBuilder = new StringBuilder(WikiImageEntry.COLUMN_FILENAME + " IN (?");
        for (int i = 0; i < imageNames.size() - 1; i++) {
            whereClauseBuilder.append(",?");
        }
        whereClauseBuilder.append(")");

        Cursor cursor = getContext().getContentResolver().query(WikiImageEntry.CONTENT_URI,
                WIKI_IMAGE_COLUMNS,
                whereClauseBuilder.toString(),
                imageNames.toArray(new String[imageNames.size()]),
                null);

        if (!cursor.moveToFirst()) {
            return results;
        }

        do {
            int width = cursor.getInt(WIKI_IMAGE_COLUMN_INDEX_WIDTH);
            int height = cursor.getInt(WIKI_IMAGE_COLUMN_INDEX_HEIGHT);
            if (width < MIN_IMAGE_WIDTH || height < MIN_IMAGE_HEIGHT) {
                continue;
            }

            float MAX_ASPECT_RATIO = 3;

            float aspectRatio = ((float)width / (float)height);
            if (aspectRatio > MAX_ASPECT_RATIO || 1f/aspectRatio > MAX_ASPECT_RATIO) {
                continue;
            }

            String url = cursor.getString(WIKI_IMAGE_COLUMN_INDEX_URL);
            String filename = cursor.getString(WIKI_IMAGE_COLUMN_INDEX_FILENAME);

            WikiImageModel model = new WikiImageModel(filename, url, width, height);
            results.add(model);
        } while (cursor.moveToNext());

        return results;
    }
}
