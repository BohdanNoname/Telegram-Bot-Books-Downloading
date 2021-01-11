package parsing;

import bot.TextData;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.select.Elements;
import pojo.BookOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BookListParser {

    private final String query;

    public BookListParser(String query){
        this.query = query;
    }

    public List<BookOptions> getAllOptionsOfBooks(){
        String formattedQuery = query.replace(" ", "+");
        Elements elements = null;
        try {
            HttpConnection htmlPage = (HttpConnection) Jsoup.connect(TextData.LINK_BOOK + formattedQuery);
            elements = htmlPage.get().select("div[id=main]").select("ol").select("li");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        assert elements != null;
        return setParserData(elements, elements.size());
    }

    private List<BookOptions> setParserData(Elements elements, int size){
        List<BookOptions> bookOptionsList = new ArrayList<>();
        for (int position = 0; position < size; position++){
            String author = elements.get(position).select("a").last().text();
            String bookName = elements.get(position).select("a").first().text();
            String bookLink = elements.get(position).select("a").first().attr("href");

            bookOptionsList.add(new BookOptions(String.valueOf(position + 1), author, bookName, bookLink));
        }
        return bookOptionsList;
    }
}
