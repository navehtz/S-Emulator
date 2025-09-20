package loader;

import exceptions.EngineLoadException;
import generatedFromXml.SFunction;
import operation.Operation;
import program.Program;
import generatedFromXml.SProgram;
import jakarta.xml.bind.*;
import generatedFromXml.SInstruction;
import generatedFromXml.SInstructionArgument;

import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class XmlProgramLoader {

    private static final JAXBContext JAXB_CTX;
    static {
        try {
            JAXB_CTX = JAXBContext.newInstance(
                    SProgram.class,
                    SInstruction.class,
                    SInstructionArgument.class,
                    SFunction.class
            );
        } catch (JAXBException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public Operation load(Path xmlPath) throws EngineLoadException {
        validatePath(xmlPath);
        SProgram sProgram = unmarshal(xmlPath);
        return XmlProgramMapper.map(sProgram);
    }

    private void validatePath(Path path) throws EngineLoadException {
        if (path == null) throw new EngineLoadException("Path is null");
        Path abs = path.toAbsolutePath().normalize();

        if (!Files.exists(abs) || !Files.isRegularFile(abs))
            throw new EngineLoadException("File not found at path: " + abs);

        if (!Files.isReadable(abs))
            throw new EngineLoadException("File is not readable: " + abs);

        if (!abs.toString().toLowerCase().endsWith(".xml"))
            throw new EngineLoadException("File must end with .xml");
    }

    private SProgram unmarshal(Path path) throws EngineLoadException {
        Path abs = path.toAbsolutePath().normalize();
        try (FileInputStream fis = new FileInputStream(abs.toFile())) {
            Unmarshaller um = JAXB_CTX.createUnmarshaller();

            StreamSource source = new StreamSource(fis);
            source.setSystemId(abs.toUri().toString());

            Object root = um.unmarshal(source);

            if (root instanceof SProgram) return (SProgram) root;
            if (root instanceof JAXBElement<?> je && je.getValue() instanceof SProgram sp) return sp;

            throw new EngineLoadException("Unexpected root element for file: " + abs);
        } catch (JAXBException e) {
            throw new EngineLoadException("Failed to parse XML: " + abs + ", " + e.getMessage(), e);
        } catch (Exception e) {
            throw new EngineLoadException("Failed to read XML: " + abs + ", " + e.getMessage(), e);
        }
    }
}
