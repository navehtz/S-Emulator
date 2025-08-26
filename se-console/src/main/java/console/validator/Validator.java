package console.validator;

import engine.Engine;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Validator {

    public static int getValidateUserInputForDegree(Scanner scanner, Engine engine) {
        String input = scanner.nextLine();

        if (input.isEmpty()) {
            throw new IllegalArgumentException("Invalid input. Choice cannot be empty.");
        }

        try {
            int degree = Integer.parseInt(input.trim());

            if (degree < 0 || degree > engine.getMaxDegree()) {
                throw new IllegalArgumentException("Degree must be between 0 and " + (engine.getMaxDegree()));
            }

            return degree;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid input. Please enter a number between 0 and " + (engine.getMaxDegree()));
        }
    }

    public static Path getValidateUserInputForDegree(Scanner scanner) {
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            throw new IllegalArgumentException("Path is empty.");
        }

        try {
            Path path = Path.of(input).toAbsolutePath().normalize();
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("Path does not exist: " + path);
            }
            if (!path.toString().toLowerCase().endsWith(".xml"))
                throw new IllegalArgumentException("File must end with .xml");

            return path;
        }
        catch (InvalidPathException e) {
            throw new IllegalArgumentException("Invalid path syntax: " + input, e);
        }
    }

    public static Long[] getValidateProgramInputs(Scanner scanner, Engine engine) {
        final Pattern INT_PATTERN = Pattern.compile("[-+]?\\d+");

        while (true) {
            String line = scanner.nextLine().trim();

            try {
                String[] parts = line.split(",");
                List<Long> values = new ArrayList<>(parts.length);

                for (String part : parts) {
                    String s = part.trim();
                    if (s.isEmpty()) {
                        return values.toArray(new Long[0]);
                    }
                    if (!INT_PATTERN.matcher(s).matches()) {
                        throw new NumberFormatException("Non-numeric value:" + s);
                    }
                    long numberInput = Long.parseLong(s);
                    if (numberInput < 0) {
                        throw new NumberFormatException("Negative value:" + s);
                    }
                    values.add(numberInput);
                }

                return values.toArray(new Long[0]);

            } catch (Exception e) {
                System.out.println("Invalid input: " + e.getMessage());
                System.out.println("Please try again. Please enter inputs values separated by commas: ");
            }
        }
    }
}
