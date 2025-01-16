import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

public class DirectoryManager {
    private Path currentDir;

    public DirectoryManager() {
        String rootDir = System.getProperty("os.name").toLowerCase().contains("win") ? "C:\\" : "/";
        this.currentDir = Paths.get(rootDir);
    }

    public String getPrompt() {
        String userName = System.getProperty("user.name");
        String path = currentDir.toAbsolutePath().toString();
        return "\u001B[32m" + userName + "\u001B[34m (" + path + ") > \u001B[0m";
    }

    public Path getCurrentDirectory() {
        return currentDir;
    }

    public void changeDirectory(String path) {
        Path newPath = Paths.get(path).isAbsolute() ? Paths.get(path) : currentDir.resolve(path);
        File newDir = newPath.toFile();
        if (newDir.exists() && newDir.isDirectory()) {
            currentDir = newPath.normalize();
            System.out.println("\u001B[32mChanged directory to: " + currentDir.toAbsolutePath() + "\u001B[0m");
        } else {
            System.err.println("\u001B[31mNo such directory: " + path + "\u001B[0m");
        }
    }

    public void goBack() {
        Path parentDir = currentDir.getParent();
        if (parentDir != null) {
            currentDir = parentDir;
        } else {
            System.out.println("\u001B[31mAlready at the root directory.\u001B[0m");
        }
    }
}
