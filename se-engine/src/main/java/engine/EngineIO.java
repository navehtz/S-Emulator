package engine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EngineIO {

    public static void save(EngineImpl engine, Path file) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (ObjectOutputStream out =
                     new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(file)))) {
            out.writeObject(engine);
        }
    }

    public static EngineImpl load(Path file) throws IOException, ClassNotFoundException {
        if (file == null) {
            throw new IllegalArgumentException("file path is null");
        }
        if (!Files.exists(file)) {
            throw new FileNotFoundException("File not found: " + file.toAbsolutePath());
        }

        try (ObjectInputStream in =
                     new ObjectInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
            return (EngineImpl) in.readObject();
        }
    }
}
