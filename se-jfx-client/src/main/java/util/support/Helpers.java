package util.support;

import exceptions.EngineLoadException;

import java.nio.file.Files;
import java.nio.file.Path;

public class Helpers {
    public static void validatePath(Path path) throws EngineLoadException {
        if (path == null) throw new EngineLoadException("Path is null");
        Path abs = path.toAbsolutePath().normalize();

        if (!Files.exists(abs) || !Files.isRegularFile(abs))
            throw new EngineLoadException("File not found at path: " + abs);

        if (!Files.isReadable(abs))
            throw new EngineLoadException("File is not readable: " + abs);

        if (!abs.toString().toLowerCase().endsWith(".xml"))
            throw new EngineLoadException("File must end with .xml");
    }
}
