package logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Map;

/**
 * Created by Evgeny_Akulenko on 5/30/2016.
 */
public class SocketReader {

    final static int STRING_END_BYTE = 13;
    final static int NEXT_STRING_BYTE = 10;
    private final Logger logger = LoggerFactory.getLogger(SocketReader.class);
    private String url;
    private int port;

    public SocketReader(String url, int port) {
        this.url = url;
        this.port = port;
    }

    public String getHTMLContent(String querry) {
        String content = "";

        BufferedReader reader = null;
        try (Socket socket = new Socket(url, port);
             OutputStream outputStream = socket.getOutputStream();
             ByteArrayOutputStream byteArrayOutputStreamForHeader = new ByteArrayOutputStream();
             ByteArrayOutputStream byteArrayOutputStreamForBytesCountString = new ByteArrayOutputStream();
             InputStream inputStream = socket.getInputStream()) {

            outputStream.write(querry.getBytes());
            outputStream.flush();

            int data;
            String headerString;
            int count = 0;
            while (true) {
                data = inputStream.read();
                if (data == STRING_END_BYTE || data == NEXT_STRING_BYTE) {
                    count++;
                } else {
                    count = 0;
                }
                if ((count == 4) || data == -1) {
                    break;
                }
                byteArrayOutputStreamForHeader.write(data);
            }
            byteArrayOutputStreamForHeader.flush();
            headerString = byteArrayOutputStreamForHeader.toString();
            Map<String, String> headerStringMap = new HtmlHeaderGetter().getHeaderMap(headerString);
            int httpStatus = 0;
            String transferEncoding = headerStringMap.get(HtmlHeaderKeys.TRANSFER_ENCODING);

            try {
                httpStatus = Integer.parseInt(headerStringMap.get(HtmlHeaderKeys.HTTP_STATUS));
            } catch (Exception ex) {
                logger.error("Error while get http status...not integer: {}", ex);
            }

            if (!(httpStatus == 200) || !transferEncoding.equals("chunked")) {
                throw new NoDataException();
            }
            while (true) {
                data = inputStream.read();
                if (data == STRING_END_BYTE || data == NEXT_STRING_BYTE || data == -1) {
                    break;
                }
                byteArrayOutputStreamForBytesCountString.write(data);
            }
            byteArrayOutputStreamForBytesCountString.flush();
            String byteCode = byteArrayOutputStreamForBytesCountString.toString();
            int bytes = Integer.parseInt(byteCode, 16);
            reader = new BufferedReader(new InputStreamReader(inputStream, headerStringMap.get(HtmlHeaderKeys.CONTENT_TYPE)), bytes);
            String contentString;
            StringBuilder stringBuilder = new StringBuilder();
            while (!(contentString = reader.readLine()).equals("0")) {
                stringBuilder.append(contentString);
            }
            content = stringBuilder.toString();
        } catch (Exception ex) {
            logger.error("Error", ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                logger.error("Error", ex);
            }
        }
        return content;
    }
}
