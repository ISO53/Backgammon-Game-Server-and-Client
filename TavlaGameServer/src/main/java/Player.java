import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Player implements Runnable {

    public static ArrayList<Player> players = new ArrayList<>();

    private final String MESSAGE_END = "#";
    private final char USER_NAME = '0';
    private final char CONNECT_TO_ROOM = '1';
    private final char GET_ROOMS = '2';
    private final char START_DICE = '3';
    private final char BOTH_PLAYER_CONNECTED = '4';
    private final char AVAILABLE_MOVES = '5';
    private final char MOVE_PLAYED = '6';
    private final char YOU_WON = '7';
    private final char YOU_LOST = '8';
    private final char GAME_STATE = '9';
    private final char IN_GAME_DICE = 'A';

    private final Logger logger;
    private final Socket socket;
    private final Thread thread;

    private String username;
    private boolean isRunning;

    public Player(Socket socket) {
        this.socket = socket;
        this.logger = new Logger("CLIENT | " + socket.getRemoteSocketAddress());
        this.logger.setMonitoring(false);
        this.isRunning = false;
        this.thread = new Thread(this);
    }

    @Override
    public void run() {
        this.logger.log("Connection Established: " + this.socket.getRemoteSocketAddress());

        while (isRunning) {

            String message = read();

            if (message == null || message.equals("")) {
                // Connection closing

                // Remove player from players list
                players.remove(this);

                // Remove player from the room its connected
                Room.rooms.stream()
                        .filter(room -> Arrays.asList(room.getPlayers()).contains(this))
                        .findFirst()
                        .ifPresent(room -> room.removePlayer(this));

                kill();

                break;
            }

            messageHandler(message);
        }
        this.logger.log(this.getUsername() + "'s connection closed: " + this.socket.getRemoteSocketAddress());
    }

    /**
     * Handles the incoming message received by the server.
     * The method processes different types of messages based on their identifiers:
     *
     * @param message the incoming message received by the server
     */
    private void messageHandler(String message) {
        String[] messages = message.split(MESSAGE_END);

        for (String msg : messages) {
            char identifier = msg.charAt(0);
            String clippedMessage = msg.substring(1);

            switch (identifier) {
                case USER_NAME -> {
                    // User sent its name to server for first connection
                    this.username = clippedMessage;
                    logger.log("Player sent it's username [ " + this.username + " ]");
                }
                case CONNECT_TO_ROOM -> {
                    // User sent a room name to connect
                    Room gameRoom = Room.rooms.stream()
                            .filter(room -> room.getRoomName().equals(clippedMessage))
                            .findFirst().orElse(null);
                    logger.log("Player sent a room name to connect [ " + clippedMessage + " ]");

                    if (gameRoom == null || gameRoom.getPlayerCount() == 2) {
                        break;
                    }

                    gameRoom.addPlayer(this);
                    if (gameRoom.getPlayerCount() != 2) {
                        break;
                    }

                    // There are two players in the room, start the game
                    Player[] players = gameRoom.getPlayers();
                    BackgammonGame game = gameRoom.getGame();

                    // Send information to both user that the game starting
                    players[0].send(BOTH_PLAYER_CONNECTED + String.format("{\"opponent_name\": \"%s\"}", players[1].getUsername())); // white
                    players[1].send(BOTH_PLAYER_CONNECTED + String.format("{\"opponent_name\": \"%s\"}", players[0].getUsername())); // black

                    int player1Dice;
                    int player2Dice;
                    do {
                        player1Dice = game.rollDice();
                        player2Dice = game.rollDice();
                    } while (player1Dice == player2Dice);

                    // Initialize the game
                    game.init(player1Dice > player2Dice ? BackgammonGame.WHITE : BackgammonGame.BLACK);

                    for (Player player : players) {
                        player.send(START_DICE + String.format("[{\"username\": \"%s\", \"dice\": %d}, {\"username\": \"%s\", \"dice\": %d}]",
                                players[0].getUsername(),
                                player1Dice,
                                players[1].getUsername(),
                                player2Dice));
                    }

                    for (Player player : players) {
                        player.send(GAME_STATE + game.getGameStateAsJson());
                    }

                    game.loadDiceResults(player1Dice, player2Dice);
                    ArrayList<Object> moves = new ArrayList<>(game.getPossibleMoves());

                    if (player1Dice > player2Dice) { // white starts
                        players[0].send(AVAILABLE_MOVES + jsonify(moves));
                    } else {
                        players[1].send(AVAILABLE_MOVES + jsonify(moves));
                    }
                }
                case GET_ROOMS -> {
                    // User wants the available rooms
                    ArrayList<Object> availableRooms = Room.rooms.stream()
                            .filter(Room::isAvailable)
                            .collect(Collectors.toCollection(ArrayList::new));
                    logger.log("Player asked for available rooms");

                    send(GET_ROOMS + jsonify(availableRooms));
                }
                case MOVE_PLAYED -> {
                    String[] nums = clippedMessage.split(",");
                    int from = Integer.parseInt(nums[0]);
                    int to = Integer.parseInt(nums[1]);

                    logger.log(String.format("Player made a move [from %d to %d]", from, to));

                    // Get players room
                    Room room = Room.rooms.stream()
                            .filter(rm -> Arrays.asList(rm.getPlayers()).contains(this))
                            .findFirst()
                            .orElse(null);

                    // Get players game from players room
                    BackgammonGame game = room.getGame(); // F*ck null safety :D

                    game.move(from, to);

                    // Send the new game state to players
                    for (Player player : players) {
                        player.send(GAME_STATE + game.getGameStateAsJson());
                    }

                    Player playerTurn = game.getTurn() == BackgammonGame.WHITE ? room.getPlayers()[0] : room.getPlayers()[1];
                    Player opponent = room.getPlayers()[0] == playerTurn ? room.getPlayers()[1] : room.getPlayers()[0];
                    // Check if player has won after the move
                    if (game.hasWon()) {
                        playerTurn.send(YOU_WON + "");
                        opponent.send(YOU_LOST + "");
                        return;
                    }

                    if (!game.hasResults()) { // There are no dice the player can play
                        game.switchTurn();

                        // It's the opposite player's turn, roll dice
                        int dice1 = game.rollDice();
                        int dice2 = game.rollDice();
                        game.loadDiceResults(dice1, dice2);
                        for (Player player : players) {
                            player.send(IN_GAME_DICE + "" + dice1 + "," + dice2);
                        }

                        // Calculate possible moves
                        ArrayList<Object> moves = new ArrayList<>(game.getPossibleMoves());
                        if (moves.isEmpty()) { // There are no move the player can make
                            game.switchTurn();

                            // It's the opposite player's turn, roll dice
                            dice1 = game.rollDice();
                            dice2 = game.rollDice();
                            game.loadDiceResults(dice1, dice2);
                            for (Player player : players) {
                                player.send(IN_GAME_DICE + "" + dice1 + "," + dice2);
                            }

                            moves = new ArrayList<>(game.getPossibleMoves());
                            playerTurn.send(AVAILABLE_MOVES + jsonify(moves));
                            return;
                        }

                        opponent.send(AVAILABLE_MOVES + jsonify(moves));
                        return;
                    }

                    ArrayList<Object> moves = new ArrayList<>(game.getPossibleMoves());
                    if (moves.isEmpty()) { // There are no move the player can make
                        game.switchTurn();

                        // It's the opposite player's turn, roll dice
                        int dice1 = game.rollDice();
                        int dice2 = game.rollDice();
                        game.loadDiceResults(dice1, dice2);
                        for (Player player : players) {
                            player.send(IN_GAME_DICE + "" + dice1 + "," + dice2);
                        }

                        moves = new ArrayList<>(game.getPossibleMoves());
                        opponent.send(AVAILABLE_MOVES + jsonify(moves));
                        return;
                    }

                    playerTurn.send(AVAILABLE_MOVES + jsonify(moves));
                }
                default -> logger.log("Player entered faulty input [ " + clippedMessage + " ]");
            }
        }
    }

    private void send(String message) {
        message += MESSAGE_END;
        try {
            this.socket.getOutputStream().write(message.getBytes(StandardCharsets.UTF_8));
            this.socket.getOutputStream().flush();
        } catch (IOException e) {
            this.logger.log(e.getMessage());
        }
    }

    private String read() {
        try {
            InputStream input = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int numBytesRead;
            numBytesRead = input.read(buffer);

            return new String(buffer, 0, numBytesRead, StandardCharsets.UTF_8);
        } catch (Exception e) {
            this.logger.log(e.getMessage());
            return null;
        }
    }

    public void start() {
        isRunning = true;
        this.thread.start();
    }

    public void stop() {
        isRunning = false;
    }

    public void kill() {
        try {
            this.socket.close();
        } catch (IOException e) {
            logger.log(e.getMessage());
            thread.stop();
        }
    }

    /**
     * Converts an {@code ArrayList} of objects into a JSON-like string representation.
     * If the input list is empty, the method returns "[]".
     *
     * @param list the ArrayList of objects to be converted
     * @return the JSON-like string representation of the input list
     */
    private String jsonify(ArrayList<Object> list) {
        if (list.isEmpty()) {
            return "[]";
        }

        StringBuilder asString = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            asString.append(list.get(i));
            if (i < list.size() - 1) {
                asString.append(", ");
            }
        }
        asString.append("]");

        return asString.toString();
    }

    public String getUsername() {
        return username;
    }

    public Logger getLogger() {
        return logger;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public String toString() {
        return String.format("Player [%s] ->\t{ isRunning: %b,\tsocketAddress: %s }", username, isRunning, socket.getRemoteSocketAddress());
    }
}
