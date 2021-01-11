package pojo;

public class BookOptions {
    private String position;
    private String author;
    private String name;
    private String link;

    public BookOptions(String position, String author, String name, String link) {
        this.position = position;
        this.author = author;
        this.name = name;
        this.link = link;
    }

    public String getAuthor() {
        return author;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return '\n' +  position + ". " +
                "Автор - " + author + ", " +
                "Название книги - " + '"' + name + '"';
    }
}
