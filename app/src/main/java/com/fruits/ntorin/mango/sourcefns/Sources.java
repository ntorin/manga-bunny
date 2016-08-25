package com.fruits.ntorin.mango.sourcefns;

/**
 * Created by Ntori on 6/27/2016.
 */
public class Sources {
    public static final int BATOTO = 0;
    public static final int GOODMANGA = 1;
    public static final int KISSMANGA = 2;
    public static final int MANGAEDEN = 3;
    public static final int MANGAFOX = 4;
    public static final int MANGAGO = 5;
    public static final int MANGAHERE = 6;
    public static final int MANGAINN = 7;
    public static final int MANGAPANDA = 8;
    public static final int MANGAREADER = 9;
    public static final int MANGATOWN = 10;
    private static int selectedSource = MANGAFOX;

    public static int getSelectedSource() {
        return selectedSource;
    }

    public static void setSelectedSource(int selectedSource) {
        Sources.selectedSource = selectedSource;
    }

    public static String getSourceString(int source) {
        switch (source){
            case MANGAHERE:
                return "MangaHere";
            case GOODMANGA:
                return "GoodManga";
            case MANGAFOX:
                return "MangaFox";
            case MANGAINN:
                return "MangaInn";
            case MANGAPANDA:
                return "MangaPanda";
            case MANGAREADER:
                return "MangaReader";

        }
        return null;
    }
}
