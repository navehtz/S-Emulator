package loader;

import engine.LoadResult;
import engine.ProgramRegistry;
import exceptions.EngineLoadException;
import generatedFromXml.SFunction;
import operation.Operation;
import generatedFromXml.SProgram;
import jakarta.xml.bind.*;
import generatedFromXml.SInstruction;
import generatedFromXml.SInstructionArgument;
import users.UserManager;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlProgramLoader {
    private ProgramRegistry registry;
    private UserManager userManager;

    public XmlProgramLoader() {}
    public XmlProgramLoader(ProgramRegistry registry, UserManager userManager) {
        this.registry = registry;
        this.userManager = userManager;
    }

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

//    public Operation load(Path xmlPath) throws EngineLoadException {
//        validatePath(xmlPath);
//        SProgram sProgram = unmarshal(xmlPath);
//        return XmlProgramMapper.map(sProgram);
//    }

//    public LoadResult loadAll(Path xmlPath) throws EngineLoadException {
//        validatePath(xmlPath);
//        SProgram sProgram = unmarshal(xmlPath);
//        Operation mainProgram = XmlProgramMapper.map(sProgram);
//
//        //Map<String, Operation> allOperationsByName = new HashMap<>();
//        putUnique(registry, mainProgram);
//
//        List<SFunction> sFunctions = sProgram.getFunctions();
//
//        if(sFunctions != null) {
//            for (SFunction sFunction : sFunctions) {
//                if(sFunction == null) continue;
//                Operation functionOperation = XmlProgramMapper.map(sFunction);
//                putUnique(registry, functionOperation);
//            }
//        }
//
//        return new LoadResult(mainProgram, Collections.unmodifiableMap(registry.getAllProgramsByName()));
//    }

    public LoadResult loadAll(InputStream inputStream, String uploaderName) throws EngineLoadException {
        //validatePath(xmlPath);
        SProgram sProgram = unmarshal(inputStream);
        Operation mainProgram = XmlProgramMapper.map(sProgram, uploaderName);

        //Map<String, Operation> allOperationsByName = new HashMap<>();
        putUnique(registry, mainProgram);
        userManager.incrementPrograms(uploaderName);

        List<SFunction> sFunctions = sProgram.getFunctions();

        if(sFunctions != null) {
            for (SFunction sFunction : sFunctions) {
                if(sFunction == null) continue;
                Operation functionOperation = XmlProgramMapper.map(sFunction, mainProgram.getName(), uploaderName);
                putUnique(registry, functionOperation);
                userManager.incrementSubFunctions(uploaderName);
            }
        }

        return new LoadResult(mainProgram, Collections.unmodifiableMap(registry.getAllProgramsByName()));
    }

    private static void putUnique(ProgramRegistry registry, Operation op) throws EngineLoadException {
        String name = op.getName();
        if (name == null || name.isBlank()) {
            throw new EngineLoadException("Program/function has no name");
        }
        if (registry.getAllProgramsByName().containsKey(name)) {
            throw new EngineLoadException("Program/function : " + name + " already exist");
        }
        registry.register(op);
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

    private SProgram unmarshal(InputStream inputStream) throws EngineLoadException {
        try {
            Unmarshaller um = JAXB_CTX.createUnmarshaller();

            Object root = um.unmarshal(inputStream);

            if (root instanceof SProgram) return (SProgram) root;
            if (root instanceof JAXBElement<?> je && je.getValue() instanceof SProgram sp) return sp;

            throw new EngineLoadException("Unexpected root element for file.");
        } catch (JAXBException e) {
            throw new EngineLoadException("Failed to parse XML." + e.getMessage(), e);
        } catch (Exception e) {
            throw new EngineLoadException("Failed to read XML: " + e.getMessage(), e);
        }
    }
}
