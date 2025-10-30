package kamokr.mapsforge.theme.editor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Util {
    public static File getResourceAsFile(String resourcePath) {
        try {
            InputStream in = Util.class.getClassLoader().getResourceAsStream(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            File tempFile = File.createTempFile("tempfile", ".tmp");
            tempFile.deleteOnExit();

            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp file from resource", e);
        }
    }
}
