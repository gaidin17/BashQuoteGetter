package logic;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Evgeny_Akulenko on 6/1/2016.
 */
public class HtmlHeaderGetter {

    public Map<String, String> getHeaderMap(String headerString) throws NoDataException {
        if (headerString == null || headerString.length() == 0) {
            throw new NoDataException();
        }
        Map<String, String> headerStringMap = new HashMap<>();
        String[] headerStringsArray = headerString.split("\r\n");
        for (String s : headerStringsArray) {
            if (s.contains(HtmlHeaderKeys.HTTP_STATUS)) {
                headerStringMap.put(HtmlHeaderKeys.HTTP_STATUS, getHttpStatus(getHtmlHeaderValueString(s)));
            }
            if (s.contains(HtmlHeaderKeys.CONTENT_TYPE)) {
                headerStringMap.put(HtmlHeaderKeys.CONTENT_TYPE, getCharSet(getHtmlHeaderValueString(s)));
            }
            if (s.contains(HtmlHeaderKeys.TRANSFER_ENCODING)) {
                headerStringMap.put(HtmlHeaderKeys.TRANSFER_ENCODING, getTransferEncoding(getHtmlHeaderValueString(s)));
            }
        }
        return headerStringMap;
    }


    private String getCharSet(String contentTypeString) throws NoDataException {
        if (contentTypeString == null || contentTypeString.length() == 0) {
            throw new NoDataException();
        }
        String result = "";
        int indexOfCharsetName = contentTypeString.indexOf("charset=");
        result = contentTypeString.substring(indexOfCharsetName + 8);
        return result;
    }


    private String getHttpStatus(String httpStatusString) throws NoDataException {
        if (httpStatusString == null || httpStatusString.length() == 0) {
            throw new NoDataException();
        }
        String result = "";
        int indexOfFirstSpace = httpStatusString.indexOf(" ");
        if (indexOfFirstSpace != -1) {
            result = httpStatusString.substring(0, indexOfFirstSpace);
        }
        return result;
    }

    private String getHtmlHeaderValueString(String headerString) throws NoDataException {
        if (headerString == null || headerString.length() == 0) {
            throw new NoDataException();
        }
        int indexOfFirstSpace = headerString.indexOf(" ");
        return headerString.substring(indexOfFirstSpace + 1);
    }

    private String getTransferEncoding(String transferEncodingString) throws NoDataException {
        if (transferEncodingString == null || transferEncodingString.length() == 0) {
            throw new NoDataException();
        }
        if (transferEncodingString.equals("chunked")) {
            return "chunked";
        } else {
            return "";
        }
    }
}
