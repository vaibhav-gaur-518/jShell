import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandExecutor {
    private final BufferedReader reader;
    private final DirectoryManager directoryManager;
    private final ExecutorService executorService;
    // private final Map<String, String> aliases;
    private final List<String> history;

    public CommandExecutor() {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.directoryManager = new DirectoryManager();
        this.executorService = Executors.newCachedThreadPool();
        // this.aliases = new HashMap<>();
        this.history = new ArrayList<>();
    }

    public void start() {
        while (true) {
            String prompt = directoryManager.getPrompt();
            System.out.print(prompt);
            try {
                String input = reader.readLine();
                if (input == null) {
                    continue;
                }

                // input = resolveAlias(input.trim());
                if (!input.isEmpty()) {
                    history.add(input);
                    execInput(input.trim());
                }
            } catch (IOException e) {
                System.err.println("\u001B[31m" + e.getMessage() + "\u001B[0m");
            }
        }
    }

    private boolean execInput(String input) {
        String[] args = input.split(" ");
        boolean success = true;

        switch (args[0]) {
            case "goto":
                directoryManager.changeDirectory(args.length > 1 ? args[1] : System.getProperty("user.home"));
                break;
            case "back":
                directoryManager.goBack();
                break;
            case "exit":
                System.out.println("Good Bye!");
                executorService.shutdown();
                System.exit(0);
                break;
            case "show":
                FileManager.listFiles(directoryManager.getCurrentDirectory());
                break;
            case "create":
                if (args.length > 2 && (args[1].equals("file") || args[1].equals("dir"))) {
                    boolean isDirectory = args[1].equals("dir");
                    FileManager.createFileOrDirectory(directoryManager.getCurrentDirectory().resolve(args[2]),
                            isDirectory);
                } else {
                    System.err.println("\u001B[31mUsage: create <file|dir> <name>\u001B[0m");
                }
                break;
            case "edit":
                if (args.length > 1) {
                    FileManager.editFile(directoryManager.getCurrentDirectory().resolve(args[1]));
                } else {
                    System.err.println("\u001B[31mUsage: edit <filename>\u001B[0m");
                }
                break;
            case "delete":
                if (args.length > 1) {
                    boolean force = Arrays.asList(args).contains("--force");
                    FileManager.deleteFileOrDirectory(directoryManager.getCurrentDirectory().resolve(args[1]), force);
                } else {
                    System.err.println("\u001B[31mUsage: delete <name> [--force for directories]\u001B[0m");
                }
                break;
            case "clear":
                clearConsole();
                break;
            case "help":
                printHelp();
                break;
            case "preview":
                if (args.length > 1) {
                    FileManager.previewFile(directoryManager.getCurrentDirectory().resolve(args[1]));
                } else {
                    System.err.println("\u001B[31mUsage: preview <filename>\u001B[0m");
                }
                break;
            // case "alias":
            // if (args.length == 3) {
            // setAlias(args[1], args[2]);
            // } else {
            // System.err.println("\u001B[31mUsage: alias <name> <command>\u001B[0m");
            // }
            // break;
            case "history":
                printHistory();
                break;
            default:
                success = executeExternalCommand(args);
        }

        return success;
    }

    private void clearConsole() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            try {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } catch (IOException | InterruptedException e) {
                System.err.println("\u001B[31mFailed to clear console.\u001B[0m");
            }
        } else {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }

    private boolean executeExternalCommand(String[] args) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("\u001B[31mError: Command '" + String.join(" ", args) + "' exited with code "
                        + exitCode + "\u001B[0m");
                return false;
            }
        } catch (IOException e) {
            System.err.println("\u001B[31mError: Command '" + String.join(" ", args) + "' is not recognized.\u001B[0m");
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("\u001B[31mError: Command execution interrupted.\u001B[0m");
            return false;
        }
        return true;
    }

    private void printHelp() {
        System.out.println("\u001B[33mAvailable Commands:\u001B[0m");
        System.out.println("  goto <path>          - Change directory to the specified path.");
        System.out.println("  back                 - Go back to the parent directory.");
        System.out.println("  show                 - List files and directories in the current directory.");
        System.out.println("  create <file|dir>    - Create a new file or directory.");
        System.out.println("  delete <name> [--force] - Delete a file or directory (force for non-empty directories).");
        System.out.println("  preview <filename>   - Preview the contents of a file.");
        System.out.println("  edit <filename>      - Edit the contents of the specified file (add new lines or update existing lines).");
        // System.out.println(" alias <name> <cmd> - Set an alias for a command.");
        System.out.println("  history              - Show command history.");
        System.out.println("  clear                - Clear the console.");
        System.out.println("  help                 - Show this help message.");
        System.out.println("  exit                 - Exit the shell.");
    }

    // private void setAlias(String name, String command) {
    // aliases.put(name, command);
    // System.out.println("\u001B[32mAlias set: " + name + " = '" + command +
    // "'\u001B[0m");
    // }

    // private String resolveAlias(String input) {
    // String[] args = input.split(" ");
    // if (aliases.containsKey(args[0])) {
    // input = aliases.get(args[0])
    // + (args.length > 1 ? " " + String.join(" ", Arrays.copyOfRange(args, 1,
    // args.length)) : "");
    // }
    // return input;
    // }

    private void printHistory() {
        for (int i = 0; i < history.size(); i++) {
            System.out.println("\u001B[33m[" + i + "] " + history.get(i) + "\u001B[0m");
        }
    }
}