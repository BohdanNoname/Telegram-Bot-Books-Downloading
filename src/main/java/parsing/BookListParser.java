package parsing;

import bot.TextData;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.select.Elements;
import pojo.Library;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BookListParser {

    public List<Library> getAllBooksByQuery(String query){
        String formattedQuery = query.replace(" ", "+");
        Elements page = null;
        try {
            HttpConnection htmlPage = (HttpConnection) Jsoup.connect(TextData.LINK_BOOK + formattedQuery);
            page = htmlPage.get().select("div[id=main]").select("ol").select("li");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        assert page != null;
        return setDataToLibrary(page, page.size());
    }

    private List<Library> setDataToLibrary(Elements page, int size){
        List<Library> libraryList = new ArrayList<>();
        for (int position = 0; position < size; position++){
            String author = page.get(position).select("a").last().text();
            String bookName = page.get(position).select("a").first().text();
            String bookLink = page.get(position).select("a").first().attr("href");

            libraryList.add(new Library(String.valueOf(position + 1), author, bookName, bookLink));
        }
        return libraryList;
    }
}
