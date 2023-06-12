import java.io.*;
import java.net.*;

/*
 * Server class to run using Singleton Pattern to prevent multiple Server objects being created.
 **/
public class Server implements Runnable {

    public static Server server = new Server();

    private Thread thread;

    private int port;
    private int maxNumberOfConnectionRequest;
    private InetAddress serverAddress;
    private Logger logger;
    private boolean isRunning;
    private ServerSocket serverSocket;

    private Server() {
    }

    /**
     * Initializes the server with the specified port, maximum number of connection requests,
     * and server address.
     *
     * @param port                         the port number on which the server will listen for incoming connections
     * @param maxNumberOfConnectionRequest the maximum number of simultaneous connection requests the server can handle
     * @param serverAddress                the IP address or hostname of the server
     */
    public void init(int port, int maxNumberOfConnectionRequest, String serverAddress) {
        this.port = port;
        this.maxNumberOfConnectionRequest = maxNumberOfConnectionRequest;
        this.logger = new Logger("SERVER");
        this.logger.setMonitoring(false);
        this.isRunning = false;
        this.thread = new Thread(this);

        try {
            this.serverAddress = InetAddress.getByName(serverAddress);
        } catch (UnknownHostException e) {
            logger.log(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void run() {

        try {
            this.serverSocket = new ServerSocket(this.port, this.maxNumberOfConnectionRequest, this.serverAddress);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        this.logger.log("Server started and listening on port " + port);

        // Add test rooms to the Server
        Room.rooms.add(new Room("Test Room1"));
        Room.rooms.add(new Room("Test Room2"));

        while (isRunning) {
            try {
                Player player = new Player(serverSocket.accept());
                Player.players.add(player);
                player.start();

            } catch (IOException e) {
                System.out.println("Forced shut! " + e.getMessage());
            }
        }
    }

    public void start() {
        isRunning = true;
        this.thread.start();
    }

    public void stop() {
        for (Room room : Room.rooms) {
            for (Player player : room.getPlayers()) {
                if (player != null) {
                    player.kill();
                }
            }
        }

        isRunning = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            thread.stop();
        }
    }

    public Logger getLogger() {
        return logger;
    }
}
