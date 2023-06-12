import java.io.*;
import java.net.URL;
import java.util.Scanner;

public class App {

    public static final int EXIT = 0;
    public static final int CARRY_ON = 1;

    public static void main(String[] args) {
        String serverAddress = "127.0.0.1";
        int maxNumberOfConnections = 100;
        int port = 8080;

        writeAsciiArt();
        System.out.println("Welcome to the Backgammon Game Server.");
        System.out.println("Server address is -> " + serverAddress);
        System.out.println("Max Number of Connections is -> " + maxNumberOfConnections);
        System.out.println("Port Number is -> " + port);
        System.out.println("Would you like to change them? (y/n)");
        Scanner scanner = new Scanner(System.in);
        System.out.print("-> ");
        String changeChoice = scanner.nextLine();

        if (changeChoice.equals("y")) {
            String externalIp = getExternalIp();
            System.out.println("Type the new server address. (Your ext. ip adr. is [" + externalIp +"] )");
            System.out.print("-> ");
            serverAddress = scanner.nextLine();

            System.out.println("Type the max number of connections available. Must be positive integer.");
            System.out.print("-> ");
            maxNumberOfConnections = scanner.nextInt();

            System.out.println("Type the new server port. Must be positive and smaller than 65535.");
            System.out.print("-> ");
            port = scanner.nextInt();

            System.out.println("Changes are saved.");
        }

        System.out.println("To start to server type 'start'.");

        String isStart;
        do {
            System.out.print("-> ");
            isStart = scanner.nextLine();
        } while (!isStart.equals("start"));

        System.out.println("Server is starting...");
        Server server = Server.server;
        server.init(port, maxNumberOfConnections, serverAddress);
        server.start();
        System.out.println("Server is started!");

        int choice;
        int status;
        do {
            System.out.println("\nSelect a number from below.");
            System.out.println("1. Get Players");
            System.out.println("2. Get Rooms");
            System.out.println("3. Toggle Player Monitoring");
            System.out.println("4. Read Log File");
            System.out.println("5. Read Players Log File");
            System.out.println("6. Shut Down The Server And Exit");
            System.out.print("-> ");
            choice = scanner.nextInt();
            status = choiceHandler(choice);
        } while (status != EXIT);

        System.out.println("Exiting from program, closing server...");
        server.stop();
        System.out.println("Server closed. Good bye.");
    }

    public static int choiceHandler(int choice) {
        switch (choice) {
            case 1 -> Player.players.forEach(player -> System.out.println(player.toString()));
            case 2 -> Room.rooms.forEach(room -> System.out.println(room.toLog()));
            case 3 -> {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Type the username as it is");
                System.out.print("-> ");
                String username = scanner.nextLine();
                Player player = Player.players.stream().filter(pl -> pl.getUsername().equals(username)).findFirst().orElse(null);

                if (player == null) {
                    System.out.println("User not found!");
                    return CARRY_ON;
                }

                player.getLogger().setMonitoring(!player.getLogger().isMonitoring());
                System.out.println("Player's logger is toggled.");
            }
            case 4 -> Server.server.getLogger().printLog();
            case 5 -> {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Type the username as it is or type SERVER");
                System.out.print("-> ");
                String input = scanner.nextLine();

                if (input.equals("SERVER")) {
                    Server.server.getLogger().printLog("SERVER");
                    return CARRY_ON;
                }

                Player player = Player.players.stream().filter(pl -> pl.getUsername().equals(input)).findFirst().orElse(null);
                if (player == null) {
                    System.out.println("User not found!");
                    return CARRY_ON;
                }

                player.getLogger().printLog("CLIENT | " + player.getSocket().getRemoteSocketAddress());
            }
            case 6 -> {
                return EXIT;
            }
            default -> System.out.println("You entered an incorrect entry!");
        }

        return CARRY_ON;
    }

    public static void writeAsciiArt() {
        String s = File.separator;
        String filePath = System.getProperty("user.dir") + String.format("%ssrc%smain%sresources%sascii_art.txt", s, s, s, s);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getExternalIp() {
        String externalIp = null;

        try {
            URL whatIsMyIp = new URL("https://checkip.amazonaws.com");
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(whatIsMyIp.openStream()));
                externalIp = in.readLine();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return externalIp;
    }
}
