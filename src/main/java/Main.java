import logic.QuoteGetter;
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
        }
        catch (Exception ex) {

            logger.error("No arguments!");
        }
        QuoteGetter quoteGetter = new QuoteGetter(quoteId);
        quoteGetter.showQuote();
    }
}
