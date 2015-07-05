package com.example.sunchaser.app.data;

import java.util.Collection;

/**
 * Created by smee on 07/06/15.
 */
public class WikiArticle {

    private final int id;
    private final String title;
    private final String extract;
    private final Collection<String> imageTitles;

    public WikiArticle(int id, String title, String extract, Collection<String> imageTitles) {
        this.id = id;
        this.title = title;
        this.extract = extract;
        this.imageTitles = imageTitles;
    }

    public Collection<String> getImageTitles() {
        return imageTitles;
    }

}
