package logic;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Evgeny_Akulenko on 6/1/2016.
 */
public class HtmlHeaderGetter {



    public Map<String, String> getHeaderMap(String headerString) {
        Map<String, String> headerStringMap = new HashMap<>();
        String[] headerStringsArray = headerString.split("\r\n");
        for (String s : headerStringsArray) {
            if (s.indexOf(HtmlHeaderKeys.HTTP_STATUS) != -1) {
                headerStringMap.put(HtmlHeaderKeys.HTTP_STATUS, getHttpStatus(getHtmlHeaderValueString(s)));
            }
            if (s.indexOf(HtmlHeaderKeys.CONTENT_TYPE) != -1) {
                headerStringMap.put(HtmlHeaderKeys.CONTENT_TYPE, getCharSet(getHtmlHeaderValueString(s)));
            }
            if (s.indexOf(HtmlHeaderKeys.TRANSFER_ENCODING) != -1) {
                headerStringMap.put(HtmlHeaderKeys.TRANSFER_ENCODING, getTransferEncoding(getHtmlHeaderValueString(s)));
            }
        }
        return headerStringMap;
    }


    private String getCharSet(String contentTypeString) {
        String result = "";
        int indexOfCharsetName = contentTypeString.indexOf("charset=");
        result = contentTypeString.substring(indexOfCharsetName + 8);
        return result;
    }


    private String getHttpStatus(String httpStatusString) {
        String result = "";
        int indexOfFirstSpace = httpStatusString.indexOf(" ");
        if (indexOfFirstSpace != -1) {
            result = httpStatusString.substring(0, indexOfFirstSpace);
        }
        return result;

    }

    private String getHtmlHeaderValueString(String headerString) {
        int indexOfFirstSpace = headerString.indexOf(" ");
        return headerString.substring(indexOfFirstSpace + 1);
    }

    private String getTransferEncoding(String transferEncodingString) {
        if (transferEncodingString.equals("chunked")) {
            return "chunked";
        } else {
            return "";
        }
    }
}
