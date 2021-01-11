package pojo;

import java.util.List;

public class Book {
    private List<String> fileFormat;
    private String bookName;
    private String author;
    private String link;

    public Book(List<String> fileFormat, String bookName, String author, String link) {
        this.fileFormat = fileFormat;
        this.bookName = bookName;
        this.author = author;
        this.link = link;
    }

    public List<String> getFileFormat() {
        return fileFormat;
    }

    public String getBookName() {
        return bookName;
    }

    public String getAuthor() {
        return author;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return "Book{" +
                "fileFormat=" + fileFormat +
                ", bookName='" + bookName + '\'' +
                ", author='" + author + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
