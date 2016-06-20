package logic;

/**
 * Created by Evgeny_Akulenko on 6/1/2016.
 */
public class QuoteGetter {

    public String getQuote(String htmlString, String div) throws NoDataException {
        String result;
        int indexOfFirstDiv = htmlString.indexOf(div);
        if (indexOfFirstDiv != -1) {
            result = htmlString.substring(indexOfFirstDiv + div.length());
        } else {
            throw new NoDataException();
        }
        int indexOfDivClosing = result.indexOf("</div>");
        result = result.substring(0, indexOfDivClosing);
        result = result.replace("<br>", "\n");
        result = result.replace("&quot;", "\"");
        return result;
    }
}
