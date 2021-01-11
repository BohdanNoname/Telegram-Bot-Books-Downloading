package bot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
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
import pojo.BookOptions;

import java.util.ArrayList;
import java.util.List;


public class MyLibraryBot extends TelegramLongPollingBot {

    private Message message;
    private static SendMessage messageSettings;
    private boolean isGotPositionOfBook = false;
    private List<BookOptions> bookOptionsList;

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try{
            telegramBotsApi.registerBot(new MyLibraryBot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace(); }

        messageSettings = new SendMessage();
        setButtonsRow(addingOfNewRowToKeyboard(TextData.CANCEL_OF_SEARCHING_PROMPT));
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

        switch (message.getText()){
            case TextData.MAIN_START:
                sendMessage(TextData.WELCOMING_OF_NEW_USER_PROMPT + '\n' + '\n'
                        + TextData.STARTING_OF_NEW_SEARCHING_PROMPT); /*Сообщение при первом входе в бот*/
                break;

            case TextData.CANCEL_OF_SEARCHING_PROMPT:
                isGotPositionOfBook = false;
                setButtonsRow(addingOfNewRowToKeyboard(TextData.CANCEL_OF_SEARCHING_PROMPT));

                sendMessage(TextData.CANCEL_TRUE_PROMPT + '\n'
                        + TextData.STARTING_OF_NEW_SEARCHING_PROMPT); /*Сообщение при отмене текущего поиска и начало нового*/
                break;
//_________________________________
            default:
                if (!isGotPositionOfBook){
                    BookListParser bookListParser = new BookListParser(message.getText());
                    bookOptionsList = bookListParser.getAllOptionsOfBooks();
                    if (bookOptionsList == null){
                        sendMessage(TextData.ERROR_EMPTY_PROMPT);
                    } else {
                        sendMessage(bookOptionsList.toString()
                                .replace("[", "").replace("]", ""));

                        sendMessage(TextData.NUMBER_PROMPT);
                        isGotPositionOfBook = true;
                    }
                }
//_________________________________
                else if (isGotPositionOfBook && (bookOptionsList != null || bookOptionsList.size() != 0)){
                    getMainDataBook(bookOptionsList, (Integer.parseInt(message.getText()) - 1)).toString();
                } else {
                    sendMessage(TextData.ERROR_EMPTY_PROMPT);
                }
        }
    }
    /*___________________________________________________________________________*/

    private boolean sendMessage(String mainMessageForUser){
        messageSettings.enableMarkdown(false);
        messageSettings.setChatId(message.getChatId().toString());
        try {
            execute(messageSettings.setText(mainMessageForUser));
        } catch (TelegramApiException e){
            e.printStackTrace();
            return false;
        } return true;
    }

    private Book getMainDataBook(List<BookOptions> list, int position){
        FormatParser formatParser = new FormatParser(list.get(position).getLink());
        List<String> formatList = formatParser.getFormats();

        String name = list.get(position).getName();
        String author = list.get(position).getAuthor();
        String link = TextData.MAIN_LINK + list.get(position).getLink();

        setFormatsToKeyboard(formatList);

        Book book = new Book(formatList, name, author, link);

        isGotPositionOfBook = false;
        return book;
    }

    private void setFormatsToKeyboard(List<String> formatList){
        try {
            sendMessage(TextData.FORMAT_CHOSE_PROMPT);
            execute(messageSettings.setReplyMarkup(setButtonsRow(addingOfNewRowToKeyboard(formatList))));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    /*___________________________________________________________________________*/

    private static ReplyKeyboardMarkup setButtonsRow(List<KeyboardRow> keyboardRow){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        messageSettings.setReplyMarkup(replyKeyboardMarkup);

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setKeyboard(keyboardRow);

        return replyKeyboardMarkup;
    }

    private static List<KeyboardRow> addingOfNewRowToKeyboard(String button){
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add( new KeyboardButton(button));
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        keyboardRowList.add(keyboardRow);
        return keyboardRowList;
    }

    private static List<KeyboardRow> addingOfNewRowToKeyboard(List<String> conditions){
        List<KeyboardRow> keyboardRowList = new ArrayList<>();

        KeyboardRow mainRow = new KeyboardRow();
        mainRow.add(new KeyboardButton(TextData.CANCEL_OF_SEARCHING_PROMPT));
        keyboardRowList.add(mainRow);

        KeyboardRow keyboardRow = new KeyboardRow();
        for (String button : conditions){
            keyboardRow.add(button);
        }
        keyboardRowList.add(keyboardRow);
        return keyboardRowList;
    }
    /*___________________________________________________________________________*/
}
