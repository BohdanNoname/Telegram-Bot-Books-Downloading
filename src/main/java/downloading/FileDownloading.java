package downloading;

import org.jsoup.helper.HttpConnection;
import pojo.Book;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class FileDownloading implements Runnable{

    private String link;
    private String bookName;

    public FileDownloading(Book book){
        this.link = book.getLink();
        this.bookName = book.getBookName();
    }


    @Override
    public void run() {
    }
}
