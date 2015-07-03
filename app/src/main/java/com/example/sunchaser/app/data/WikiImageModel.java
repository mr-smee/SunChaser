package com.example.sunchaser.app.data;

/**
 * Created by smee on 09/06/15.
 */
public class WikiImageModel implements ImageModel {

    private final String filename;
    private final String url;
    private final int width;
    private final int height;

    public WikiImageModel(String filename, String url, int width, int height) {
        this.filename = filename;
        this.url = url;
        this.width = width;
        this.height = height;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getUrl(Integer maxWidth, Integer maxHeight) {
        return url;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WikiImageModel that = (WikiImageModel) o;

        return filename.equals(that.filename);

    }

    @Override
    public int hashCode() {
        return filename.hashCode();
    }
}
