package com.example.sunchaser.app.data;

/**
 * Created by smee on 07/06/15.
 */
public class WikiArticle {

    private final int id;
    private final String title;
    private final String extract;
    private final String[] imageTitles;

    public WikiArticle(int id, String title, String extract, String... imageTitles) {
        this.id = id;
        this.title = title;
        this.extract = extract;
        this.imageTitles = imageTitles;
    }

    public String[] getImageTitles() {
        return imageTitles;
    }
}
