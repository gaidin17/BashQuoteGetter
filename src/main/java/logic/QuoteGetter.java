package logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * Created by Evgeny_Akulenko on 5/30/2016.
 */
public class QuoteGetter {
    private final Logger logger = LoggerFactory.getLogger(QuoteGetter.class);
    private String resultQuote;
    private String url = "bash.im";
    private int port = 80;
    private String quoteId;

    public QuoteGetter(String quoteId) {
        this.quoteId = quoteId;
        this.resultQuote = socketReader();
    }

    /**
     * Method make querry to socket
     * and get data
     *
     * @return resultQuote
     */
    private String socketReader() {
        String resultQuote = "";
        String querry = "GET /quote/" + quoteId + " HTTP/1.1\nHost:" + url + "\n\n";

        try (Socket socket = new Socket(url, port);
             OutputStream outputStream = socket.getOutputStream();
             ByteArrayOutputStream byteArrayOutputStreamForHeader = new ByteArrayOutputStream();
             ByteArrayOutputStream byteArrayOutputStreamForContent = new ByteArrayOutputStream();
             InputStream inputStream = socket.getInputStream();) {

            outputStream.write(querry.getBytes());
            outputStream.flush();

            int data;
            int count = 0;
            while (true) {
                data = inputStream.read();
                if (data == -1) {
                    break;
                }
                if ((data) == 13) {
                    count++;
                }
                if (count == 12) {
                    break;
                }
                if (data > 0) {
                    byteArrayOutputStreamForHeader.write(data);
                }
            }
            byteArrayOutputStreamForHeader.flush();
            String string = byteArrayOutputStreamForHeader.toString();
            String[] arr = string.split("\n");
            String charset = getCharSet(arr[3]);
            String httpStatus = arr[0];
            String transferEncoding = arr[4];

            if (getHttpStatus(httpStatus) == 200 && isChunked(transferEncoding)) {
                byte buf[] = new byte[64 * 1024];

                while (true) {
                    data = inputStream.read(buf);
                    if (data == -1) {
                        break;
                    }
                    if (data > 0) {
                        byteArrayOutputStreamForContent.write(buf, 0, data);
                    }
                }
                byteArrayOutputStreamForContent.flush();
                String bashContent = byteArrayOutputStreamForContent.toString(charset);
                resultQuote = divByClassParser(bashContent, "<div class=\"text\">");
            } else {
                resultQuote = "Unable to get data";
            }

        } catch (IOException ex) {
            logger.error("Error: {}", ex.getMessage());
        }
        return resultQuote;
    }

    /**
     * Metod get a charset from contentTypeString:
     *
     * @param contentTypeString
     * @return charset
     */
    private String getCharSet(String contentTypeString) {
        String result = "";
        String[] contentTypeStringArray = contentTypeString.split(" ");

        if (contentTypeStringArray.length >= 3) {
            result = contentTypeStringArray[2];
            int indexOfEqualsChar = result.indexOf("=");
            int indexOfNewLineChar = result.indexOf("\r");
            result = result.substring(indexOfEqualsChar + 1, indexOfNewLineChar);
        }
        return result;
    }

    /**
     * Metod get http Status from httpStatusString:
     *
     * @param httpStatusString
     * @return http Status
     */
    private int getHttpStatus(String httpStatusString) {
        int result = 0;
        String[] httpStatusStringArray = httpStatusString.split(" ");
        if (httpStatusStringArray.length > 2) {
            try {
                result = Integer.parseInt(httpStatusStringArray[1]);
            } catch (Exception ex) {
                logger.error("Error while get http status...not integer: {}", ex);
            }
        }
        return result;
    }

    /**
     * Metod check is this http-package chunked or not
     *
     * @param transferEncodingString
     * @return is chunked or not
     */
    private boolean isChunked(String transferEncodingString) {
        int indexOfSpace = transferEncodingString.indexOf(" ");
        int indexOfNewLineChar = transferEncodingString.indexOf("\r");
        String result = transferEncodingString.substring(indexOfSpace + 1, indexOfNewLineChar);
        if ("chunked".equals(result)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Metod search a result quote from content
     *
     * @param htmlString - html content
     * @param div        - div tag containing result quote
     * @return result quote
     */

    private String divByClassParser(String htmlString, String div) {
        String result;
        int indexOfFirstDiv = htmlString.indexOf(div);
        if (indexOfFirstDiv != -1) {
            result = htmlString.substring(indexOfFirstDiv + div.length());
        } else {
            return "unable to get quote";
        }
        int indexOfDivClosing = result.indexOf("</div>");
        result = result.substring(0, indexOfDivClosing);
        result = result.replace("<br>", "\n");
        result = result.replace("&quot;", "\"");
        return result;
    }

    /**
     * Method show result quote in console
     */
    public void showQuote() {
        System.out.println(resultQuote);
    }

}
