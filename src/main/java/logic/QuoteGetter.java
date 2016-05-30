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

    private String url = "bash.im";
    private int port = 80;
    private String quoteId;

    public QuoteGetter(String quoteId) {
        this.quoteId = quoteId;
        socketReader(url, port);
    }

    private void socketReader(String url, int port) {
        Socket socket = null;
        String querry = "GET /quote/439521 HTTP/1.1\nHost: bash.im\n\n";
        ByteArrayOutputStream byteArrayOutputStreamForHeader;
        ByteArrayOutputStream byteArrayOutputStreamForContent;
        try {
            socket = new Socket(url, port);
            OutputStream os = socket.getOutputStream();
            os.write(querry.getBytes());
            os.flush();

            InputStream inputStream = socket.getInputStream();

            byteArrayOutputStreamForHeader = new ByteArrayOutputStream();
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
            String string = byteArrayOutputStreamForHeader.toString();
            String[] arr = string.split("\n");
            String charset = getCharSet(arr[3]);
            String httpStatus = arr[0];

            if (getHttpStatus(httpStatus) == 200 && isChunked(arr[4])) {
                byte buf[] = new byte[64 * 1024];
                byteArrayOutputStreamForContent = new ByteArrayOutputStream();
                while (true) {
                    data = inputStream.read(buf);
                    if (data == -1) {
                        break;
                    }
                    if (data > 0) {
                        byteArrayOutputStreamForContent.write(buf, 0, data);
                    }
                }
                String bashContent = byteArrayOutputStreamForContent.toString(charset);
                //
                // System.out.println(bashContent);
                System.out.println(divByClassParser(bashContent, "<div class=\"text\">"));
            } else {
                System.out.println("Error");
            }

        } catch (IOException ex) {
            logger.error("IOException");
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                logger.error("Exception when close socket");
            }
        }
    }

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

    private int getHttpStatus(String httpStatusString) {
        int result = 0;
        String[] httpStatusStringArray = httpStatusString.split(" ");
        if (httpStatusStringArray.length > 2) {
            try {
                result = Integer.parseInt(httpStatusStringArray[1]);
            } catch (Exception ex) {
                logger.error("Error while get http status...not integer");
            }
        }
        return result;
    }

    private boolean isChunked(String transferEncodingString){
        int indexOfSpace = transferEncodingString.indexOf(" ");
        int indexOfNewLineChar = transferEncodingString.indexOf("\r");
        String result = transferEncodingString.substring(indexOfSpace + 1, indexOfNewLineChar);
        if ("chunked".equals(result)) {
            return true;
        } else {
            return false;
        }
    }
    private String divByClassParser(String htmlString, String div){
        String result = "";
        int indexOfFirstDiv = htmlString.indexOf(div);
        result = htmlString.substring(indexOfFirstDiv + div.length());
        int indexOfDivClosing = result.indexOf("</div>");
        result = result.substring(0, indexOfDivClosing);
        result = result.replace("<br>","\n");
        result = result.replace("&quot;","\"");
        return result;
    }
}
