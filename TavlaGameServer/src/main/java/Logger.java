import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class Logger {

    private String filePath;
    private final String owner;
    private static final Object lock = new Object();
    private boolean isMonitoring;

    public Logger(String owner) {
        this.owner = owner;
        this.isMonitoring = false;

        try {
            String s = File.separator;
            File file = new File(System.getProperty("user.dir") + String.format("%ssrc%smain%sresources%slog.txt", s, s, s, s));
            if (file.exists()) {
                // Log file already exists.
                filePath = file.getAbsolutePath();
            } else {
                boolean created = file.createNewFile();

                if (created) {
                    // Log file successfully created.
                    filePath = file.getAbsolutePath();
                } else {
                    System.out.println("There was an error while creating the log file.");
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void log(String message) {
        try {
            Path path = Paths.get(this.filePath);
            BufferedWriter bufferedWriter = Files.newBufferedWriter(
                    path,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND
            );

            String log = String.format("[%s] [%s] -> %s\n", LocalDateTime.now(), this.owner, message);
            if (this.isMonitoring) {
                System.out.println(log);
            }

            synchronized (lock) {
                bufferedWriter.write(log);
            }

            bufferedWriter.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Reads and prints the contents of the log file specified by {@code filePath}.
     * Each line of the log file is read and printed to the console.
     * If any exception occurs during the file reading process, it is logged using the {@code log()} method.
     */
    public void printLog() {
        BufferedReader bufferedReader;
        try {
            FileReader fileReader = new FileReader(this.filePath);
            bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
            bufferedReader.close();
        } catch (Exception exception) {
            log(exception.getMessage());
        }
    }

    /**
     * Reads and prints the log entries belonging to the specified {@code owner}.
     * Each line of the log file is read, and if the owner is found in the entry,
     * the line is printed to the console.
     * If any exception occurs during the file reading process, it is logged using the {@code log()} method.
     *
     * @param owner the owner whose log entries are to be printed
     */
    public void printLog(String owner) {
        BufferedReader bufferedReader;
        try {
            FileReader fileReader = new FileReader(this.filePath);
            bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.split(" -> ")[0].contains(owner)) {
                    System.out.println(line);
                }
            }
            bufferedReader.close();
        } catch (Exception exception) {
            log(exception.getMessage());
        }
    }

    public boolean isMonitoring() {
        return isMonitoring;
    }

    public void setMonitoring(boolean monitoring) {
        isMonitoring = monitoring;
    }
}
