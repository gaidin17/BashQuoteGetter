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

    public String getHTMLContent(String querry) throws NoDataException {
        String content = "";
        try (Socket socket = new Socket(url, port);
             OutputStream outputStream = socket.getOutputStream();
             ByteArrayOutputStream byteArrayOutputStreamForHeader = new ByteArrayOutputStream();
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
                byteArrayOutputStreamForHeader.write(data);
                if ((count == 4) || data == -1) {
                    break;
                }
            }
            byteArrayOutputStreamForHeader.flush();

            headerString = byteArrayOutputStreamForHeader.toString();
            Map<String, String> headerStringMap = new HtmlHeaderGetter().getHeaderMap(headerString);

            String transferEncoding = headerStringMap.get(HtmlHeaderKeys.TRANSFER_ENCODING);
            String charset = headerStringMap.get(HtmlHeaderKeys.CONTENT_TYPE);
            String httpStatus = headerStringMap.get(HtmlHeaderKeys.HTTP_STATUS);


            if (httpStatus.equals("200") && transferEncoding.equals("chunked")) {
                content = getHTMLContent(inputStream, charset);
            } else {
                throw new NoDataException();
            }

        } catch (IOException ex) {
            logger.error("Error", ex);
        }
        return content;
    }

    private String getHTMLContent(InputStream inputStream, String charset) throws NoDataException {
        String content = "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            StringBuilder buf = new StringBuilder();
            while (true) {
                String s = reader.readLine();
                if (s == null) {
                    throw new NoDataException();
                }
                if (s.length() == 0) {
                    continue;
                }

                int chunkToRead;

                try {
                    chunkToRead = Integer.parseInt(s, 16);
                } catch (NumberFormatException ex) {
                    throw new NoDataException();
                }
                if (chunkToRead == 0) {
                    break;
                }

                char[] dataChar = new char[chunkToRead];
                int read = 0;
                while (read != chunkToRead) {
                    read += reader.read(dataChar, read, chunkToRead - read);
                }
                buf.append(dataChar, 0, read);
            }
            content = buf.toString();
        } catch (IOException ex) {
            logger.error("Error: ", ex);
        }
        return content;
    }
}
