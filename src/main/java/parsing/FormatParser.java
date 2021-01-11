package parsing;

import bot.TextData;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FormatParser {
    static List<String> mimeList;
    private final String link;

    public FormatParser(String link){
        this.link = TextData.MAIN_LINK + link;
    }

    public List<String> getFormats(){
        return parsingOfFormats(getAvailableFormatInHTML(), getAvailableFormatInHTML().size());
    }

    private List<String> parsingOfFormats(Elements elements, int size) {
        mimeList = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            String mime = elements.get(index).text();

            if (mime.equals(TextData.EPUB_MIME_TYPE) || mime.equals(TextData.FB2_MIME_TYPE) ||
                    mime.equals(TextData.MOBI_MIME_TYPE) || mime.equals(TextData.PDF_MIME_TYPE) ||
                    mime.equals(TextData.READ_ONLINE)) {
                mimeList.add(mime);
            }
        } return mimeList;
    }

    private Elements getAvailableFormatInHTML(){
        Elements elements = null;
        try {
            HttpConnection page = (HttpConnection) Jsoup.connect(link);
            elements = page.get().select("div[id=main]").select("h1[class=title]").nextAll().select("div").select("a");
        } catch (IOException exception) {
            exception.printStackTrace();
        } return elements;
    }
}
