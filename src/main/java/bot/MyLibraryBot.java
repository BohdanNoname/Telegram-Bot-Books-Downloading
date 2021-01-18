package bot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import parsing.BookListParser;
import parsing.FormatParser;
import pojo.Book;
import pojo.Library;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MyLibraryBot extends TelegramLongPollingBot {

    private Message message;
    private static SendMessage messageSettings;

    private List<Library> library;
    private Book book;

    private int levelOfProgram = 0;

    /*___________________________________________________________________________*/

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try{
            telegramBotsApi.registerBot(new MyLibraryBot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace(); }

        messageSettings = new SendMessage();
        setButtonsRow(addingRowToKeyboard());
    }

    public String getBotUsername() {
        return TextData.USERNAME_TELEGRAM_BOT;
    }

    public String getBotToken() {
        return TextData.TOKEN_TELEGRAM_BOT;
    }

    /*___________________________________________________________________________*/

    public void onUpdateReceived(Update update) {
        message = update.getMessage();
        String trigger = message.getText();

        switch (trigger){
            case TextData.MAIN_START:
                sendMessage(TextData.WELCOMING_OF_NEW_USER_PROMPT);
                break;
            case TextData.CANCEL_OF_SEARCHING_PROMPT:
                setButtonsRow(addingRowToKeyboard());
                sendMessage(TextData.CANCEL_TRUE_PROMPT + '\n'
                        + TextData.STARTING_OF_NEW_SEARCHING_PROMPT);
                levelOfProgram = 0;
                break;
            default:
                getListOfBooks(trigger);
                settingPositionOfBookByUser(trigger);
                sendingBookToUser(trigger);
                break;
        }
    }

    /*______________________________Algorithm of program_____________________________________________*/

    private void getListOfBooks(String query){
        if (levelOfProgram == 0) {
            BookListParser bookListParser = new BookListParser();
            library = bookListParser.getAllBooksByQuery(query);
            if (library.size() != 0) {
                sendMessage(library.toString()
                        .replace("[", "").replace("]", ""));
                levelOfProgram = 1;
            } else {
                sendMessage(TextData.ERROR_EMPTY_PROMPT);
            }
        }
    }

    private void settingPositionOfBookByUser(String position){
        if (library.size() != 0 && isNumeric(position)
                && Integer.parseInt(position) <= library.size() && levelOfProgram == 1){
            book = setBookData(library, (Integer.parseInt(position) - 1));
            messageSettings.setReplyMarkup(setButtonsRow(addingRowToKeyboard(book.getFileFormat())));
            sendMessage(TextData.FORMAT_CHOSE_PROMPT);
            levelOfProgram = 2;
        }
        else if (library.size() != 0 && !isNumeric(position)
                && levelOfProgram == 1){
            sendMessage(TextData.NUMBER_PROMPT);
        }
        else if(library.size() != 0 && isNumeric(position)
                && Integer.parseInt(position) > library.size() && levelOfProgram == 1){
            sendMessage(TextData.ERROR_INPUTTING_POSITION_PROMPT);
        }
    }

    private void sendingBookToUser(String trigger){
        String mime;
        if (levelOfProgram == 2 && (mime = catchMimeTypeFromUser(trigger, book.getFileFormat())).equals(trigger)) {
            if (mime.equals("pdf")){
                mime = "download";
            }

            String link = book.getLink();
            String name = book.getBookName() + '.' + mime;

            setButtonsRow(addingRowToKeyboard());
            sendMessage(TextData.STARTING_DOWNLOADING_PROMPT);

            MyRunnableSendFile myRunnable = new MyRunnableSendFile(name, link, mime);
            myRunnable.run();

            levelOfProgram = 0;
        }
    }
    /*_______________________________Massage Settings____________________________________________*/

    private void sendMessage(String mainMessageForUser){
        messageSettings.enableMarkdown(false);
        messageSettings.setChatId(message.getChatId().toString());
        try {
            execute(messageSettings.setText(mainMessageForUser));
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    /*__________________________Sending of File_________________________________________________*/




    /*___________________________________________________________________________*/
    private Book setBookData(List<Library> list, int position){
        FormatParser formatParser = new FormatParser(list.get(position).getLink());
        List<String> formatList = formatParser.getFormats();

        String name = list.get(position).getName();
        String author = list.get(position).getAuthor();
        String link = TextData.MAIN_LINK + list.get(position).getLink();
        return new Book(formatList, name, author, link);
    }

    /*_________________________Keyboard Settings__________________________________________________*/

    private static ReplyKeyboardMarkup setButtonsRow(List<KeyboardRow> keyboardRow){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        messageSettings.setReplyMarkup(replyKeyboardMarkup);

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setKeyboard(keyboardRow);

        return replyKeyboardMarkup;
    }

    private static List<KeyboardRow> addingRowToKeyboard(){
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add( new KeyboardButton(TextData.CANCEL_OF_SEARCHING_PROMPT));
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        keyboardRowList.add(keyboardRow);
        return keyboardRowList;
    }

    private static List<KeyboardRow> addingRowToKeyboard(List<String> formats){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();

        KeyboardRow keyboardRow1 = new KeyboardRow();
        keyboardRow1.add(new KeyboardButton(TextData.CANCEL_OF_SEARCHING_PROMPT));
        keyboardRowList.add(keyboardRow1);

        KeyboardRow keyboardRow2 = new KeyboardRow();
        for (String format : formats){
            keyboardRow2.add(format);
        }
        keyboardRowList.add(keyboardRow2);
        return keyboardRowList;
    }

    /*___________________________________________________________________________*/

    private String catchMimeTypeFromUser(String massageFromUser, List<String> mimeList){
        for (String s : mimeList){
            if (s.equals(massageFromUser)){
                return s;
            }
        } return TextData.ERROR_FORMAT_PROMPT;
    }

    private static boolean isNumeric(String strNum) {
        return strNum.matches("-?\\d+(\\.\\d+)?");
    }

    /*_______________________________Separate Stream____________________________________________*/
    protected class MyRunnableSendFile implements Runnable{

        private final String name;
        private final String link;
        private final String mime;

        public MyRunnableSendFile(String name, String link, String mime) {
            this.name = name;
            this.link = link;
            this.mime = mime;
        }

        @Override
        public void run() {
            sendFile(name, link, mime);
        }

        private void sendFile(String nameOfFile, String link, String mime){
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(message.getChatId());
            try {
                execute(sendDocument.setDocument(nameOfFile, setFileInputStream(link, mime)));
                sendMessage(TextData.FINISH_PROMPT);
            } catch (TelegramApiException e) {
                e.printStackTrace();
                sendMessage(TextData.ERROR_DOWNLOADING);
            }
        }

        private InputStream setFileInputStream(String link, String mime){
            HttpURLConnection connection;
            try {
                URL url = new URL(link+"/" + mime);
                connection = (HttpURLConnection) url.openConnection();
                return connection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            } return null;
        }

    }
}

