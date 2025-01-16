import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class FileManager {
    public static void editFile(Path path) {
        File file = path.toFile();
        List<String> lines = new ArrayList<>();

        if (file.exists() && file.isFile()) {
            try (Scanner scanner = new Scanner(new FileReader(file))) {
                // Read all lines into a list
                while (scanner.hasNextLine()) {
                    lines.add(scanner.nextLine());
                }
            } catch (IOException e) {
                System.err.println("\u001B[31mError: Could not read file.\u001B[0m");
                return;
            }
        } else {
            System.out.println("\u001B[33mFile does not exist, creating a new file.\u001B[0m");
        }

        // Start editing session
        System.out.println("\u001B[34mEditing file: " + file.getName() + "\u001B[0m");

        @SuppressWarnings("resource")
        Scanner inputScanner = new Scanner(System.in);
        while (true) {
            if (lines.isEmpty()) {
                System.out.println("\u001B[36mFile is empty. You can add content.\u001B[0m");
            } else {
                for (int i = 0; i < lines.size(); i++) {
                    System.out.println("\u001B[36m" + (i + 1) + ": " + lines.get(i) + "\u001B[0m");
                }
            }

            System.out.print("\u001B[33mEnter line number to edit (or 'new' to add a line, ':wq' to save and quit, ':q!' to discard changes): \u001B[0m");
            String lineInput = inputScanner.nextLine().trim();

            if (lineInput.equals("save")) {
                // Save changes and exit
                try (FileWriter writer = new FileWriter(file)) {
                    for (String line : lines) {
                        writer.write(line + System.lineSeparator());
                    }
                } catch (IOException e) {
                    System.err.println("\u001B[31mError: Could not save file.\u001B[0m");
                    return;
                }
                System.out.println("\u001B[32mChanges saved to " + file.getName() + "\u001B[0m");
                break;
            } else if (lineInput.equals("quit")) {
                System.out.println("\u001B[31mChanges discarded.\u001B[0m");
                break;
            } else if (lineInput.equalsIgnoreCase("new")) {
                // Add a new line
                System.out.print("\u001B[33mEnter new line content: \u001B[0m");
                String newLineContent = inputScanner.nextLine();
                lines.add(newLineContent);
                System.out.println("\u001B[32mNew line added.\u001B[0m");
            } else {
                // Edit specific line
                try {
                    int lineNumber = Integer.parseInt(lineInput);
                    if (lineNumber > 0 && lineNumber <= lines.size()) {
                        System.out.println("\u001B[36mCurrent line " + lineNumber + ": " + lines.get(lineNumber - 1) + "\u001B[0m");
                        System.out.print("\u001B[33mEnter new content for line " + lineNumber + ": \u001B[0m");
                        String newLine = inputScanner.nextLine();
                        lines.set(lineNumber - 1, newLine);
                        System.out.println("\u001B[32mLine " + lineNumber + " updated.\u001B[0m");
                    } else {
                        // No action taken if the line number is invalid
                        System.out.println("\u001B[31mInvalid line number. Please enter a valid line number or 'new' to add a line.\u001B[0m");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("\u001B[31mInvalid input. Use 'line_number' format.\u001B[0m");
                }
            }
        }
    }

    public static void listFiles(Path currentDir) {
        File[] files = currentDir.toFile().listFiles();

        if (files != null && files.length > 0) {
            File[] hiddenFiles = Arrays.stream(files)
                    .filter(File::isHidden)
                    .toArray(File[]::new);

            File[] visibleFiles = Arrays.stream(files)
                    .filter(file -> !file.isHidden())
                    .toArray(File[]::new);

            Arrays.sort(hiddenFiles, Comparator.comparing(File::getName));
            Arrays.sort(visibleFiles, Comparator.comparing(File::getName));

            printFiles(hiddenFiles);
            printFiles(visibleFiles);
        } else {
            System.out
                    .println("\u001B[31mNo files or directories found in " + currentDir.toAbsolutePath() + "\u001B[0m");
        }
    }

    public static void createFileOrDirectory(Path path, boolean isDirectory) {
        try {
            if (isDirectory) {
                Files.createDirectory(path);
                System.out.println("\u001B[32mDirectory created: " + path.toAbsolutePath() + "\u001B[0m");
            } else {
                Files.createFile(path);
                System.out.println("\u001B[32mFile created: " + path.toAbsolutePath() + "\u001B[0m");
            }
        } catch (IOException e) {
            System.err.println("\u001B[31mError: Unable to create " + (isDirectory ? "directory" : "file") +
                    " '" + path + "'. " + e.getMessage() + "\u001B[0m");
        }
    }

    public static void previewFile(Path path) {
        File file = path.toFile();
        if (file.exists() && file.isFile()) {
            try {
                Files.lines(path).limit(10).forEach(line -> System.out.println("\u001B[33m" + line + "\u001B[0m"));
            } catch (IOException e) {
                System.err.println("\u001B[31mError: Could not preview file.\u001B[0m");
            }
        } else {
            System.err.println("\u001B[31mError: No such file to preview.\u001B[0m");
        }
    }

    public static void deleteFileOrDirectory(Path path, boolean force) {
        File file = path.toFile();
        if (file.exists()) {
            if (file.isDirectory()) {
                if (force) {
                    deleteDirectoryRecursively(file);
                } else if (file.listFiles().length == 0) {
                    deleteFile(file);
                } else {
                    System.err.println(
                            "\u001B[31mError: Directory is not empty. Use 'delete --force <directory>' to force delete.\u001B[0m");
                }
            } else {
                deleteFile(file);
            }
        } else {
            System.err.println("\u001B[31mError: No such file or directory: " + path + "\u001B[0m");
        }
    }

    private static void deleteDirectoryRecursively(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectoryRecursively(file);
            }
        }
        deleteFile(directory);
    }

    private static void deleteFile(File file) {
        if (file.delete()) {
            System.out.println("\u001B[32mDeleted: " + file.getAbsolutePath() + "\u001B[0m");
        } else {
            System.err.println("\u001B[31mError: Failed to delete: " + file.getAbsolutePath() + "\u001B[0m");
        }
    }

    private static void printFiles(File[] files) {
        for (File file : files) {
            String color = file.isDirectory() ? "\u001B[34m" : "\u001B[0m";
            System.out.println(color + (file.isHidden() ? "[HIDDEN] " : "") + file.getName() + "\u001B[0m");
        }
    }
}
