import logic.NoDataException;
import logic.QuoteGetter;
import logic.SocketReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Evgeny_Akulenko on 5/30/2016.
 */
public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String quoteId = "";
        try {
            quoteId = args[0];
        } catch (Exception ex) {
            logger.error("No arguments!");
        }
        String link = "/quote/" + quoteId;
        SocketReader socketReader = new SocketReader();
        try {
            String htmlContentString = socketReader.getHTMLContent("bash.im", 80, link);
            QuoteGetter quoteGetter = new QuoteGetter();
            String quote = quoteGetter.getQuote(htmlContentString, "<div class=\"text\">");
            System.out.println(quote);
        } catch (NoDataException ex) {
            logger.error("Error: ", ex);
        }
    }
}
