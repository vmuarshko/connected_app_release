package com.cookoo.life.utilities;
import java.io.*;

public class IOUtil {

    public static byte[] readFile(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[4096];
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int len = 0;
        while ((len = inputStream.read(bytes)) > 0)
            output.write(bytes, 0, len);
        return output.toByteArray();
    }
}