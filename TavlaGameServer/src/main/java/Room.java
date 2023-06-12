import java.util.ArrayList;

public class Room{

    public static ArrayList<Room> rooms = new ArrayList<>();

    private final String roomName;
    private final BackgammonGame game;
    private final Player[] players = new Player[2];

    public Room(String roomName) {
        this.roomName = roomName;
        game = new BackgammonGame();
    }

    public boolean isAvailable() {
        return players[0] == null || players[1] == null;
    }

    public int getPlayerCount() {
        int count = 0;
        for (Player player : players) {
            if (player != null) {
                count++;
            }
        }
        return count;
    }

    public void addPlayer(Player player) {
        if (players[0] == null) {
            players[0] = player;
        } else if (players[1] == null) {
            players[1] = player;
        }
    }

    public void removePlayer(Player player) {
        for (int i = 0; i < players.length; i++) {
            if (player == players[i]) {
                players[i] = null;
                break;
            }
        }
    }

    public Player[] getPlayers() {
        return players;
    }

    public String getRoomName() {
        return roomName;
    }

    public BackgammonGame getGame() {
        return game;
    }

    @Override
    public String toString() {
        return String.format("{ \"room_name\": \"%s\", \"player_count\": %d}", roomName, getPlayerCount());
    }

    public String toLog() {
        return String.format("Room [%s] ->\t{ isAvailable: %b,\tplayerCount: %d }", roomName, isAvailable(), getPlayerCount());
    }
}
