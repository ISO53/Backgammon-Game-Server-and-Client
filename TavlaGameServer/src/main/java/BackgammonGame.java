import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

/*
 * | 12 11 10  9  8  7 | B | 6  5  4  3  2  1 |
 * |-------------------|---|------------------|
 * | W  .  .  .  B  .  |   | B  .  .  .  .  W |
 * | W  .  .  .  B  .  |   | B  .  .  .  .  W |
 * | W  .  .  .  B  .  |   | B  .  .  .  .  . |
 * | W  .  .  .  .  .  |   | B  .  .  .  .  . |
 * | W  .  .  .  .  .  |   | B  .  .  .  .  . |
 * |                   |   |                  |
 * | B  .  .  .  .  .  |   | W  .  .  .  .  . |
 * | B  .  .  .  .  .  |   | W  .  .  .  .  . |
 * | B  .  .  .  .  W  |   | W  .  .  .  .  . |
 * | B  .  .  .  .  W  |   | W  .  .  .  .  B |
 * | B  .  .  .  .  W  |   | W  .  .  .  .  B |
 * |-------------------|---|------------------|
 * | 13 14 15 16 17 18 | W | 19 20 21 22 23 24|
 */
public class BackgammonGame {

    public static final byte BLACK = 0;
    public static final byte WHITE = 1;

    public static final byte CAPTURED_BAR = -1;
    public static final byte FINISHED_BAR = -2;

    private byte turn;
    private final ArrayList<Stack<Byte>> gameStatus;

    // For captured pieces
    private final Stack<Byte> capturedBlacks;
    private final Stack<Byte> capturedWhites;

    // For finished pieces
    private final Stack<Byte> finishedBlacks;
    private final Stack<Byte> finishedWhites;

    // Results of dices
    private final ArrayList<Integer> blacksResults = new ArrayList<>();
    private final ArrayList<Integer> whitesResults = new ArrayList<>();

    public BackgammonGame() {
        this.gameStatus = new ArrayList<>(24);
        this.capturedBlacks = new Stack<>();
        this.capturedWhites = new Stack<>();
        this.finishedBlacks = new Stack<>();
        this.finishedWhites = new Stack<>();
    }

    /**
     * Initializes the start state of the game board.
     *
     * @param turn the player whose turn it is at the start of the game
     */
    public void init(byte turn) {
        // Set the board to its initial position.
        gameStatus.clear();
        for (int i = 0; i < 24; i++) {
            gameStatus.add(new Stack<>());
        }

        for (int i = 0; i < 2; i++) {
            gameStatus.get(0).push(WHITE);
        }

        for (int i = 0; i < 5; i++) {
            gameStatus.get(5).push(BLACK);
        }

        for (int i = 0; i < 3; i++) {
            gameStatus.get(7).push(BLACK);
        }

        for (int i = 0; i < 5; i++) {
            gameStatus.get(11).push(WHITE);
        }

        for (int i = 0; i < 5; i++) {
            gameStatus.get(12).push(BLACK);
        }

        for (int i = 0; i < 3; i++) {
            gameStatus.get(16).push(WHITE);
        }

        for (int i = 0; i < 5; i++) {
            gameStatus.get(18).push(WHITE);
        }

        for (int i = 0; i < 2; i++) {
            gameStatus.get(23).push(BLACK);
        }

        this.turn = turn;
    }

    /**
     * Returns a random number between 1 and 6 (inclusive).
     *
     * @return a random number between 1 and 6
     */
    public int rollDice() {
        return (int) (Math.random() * 6) + 1;
    }

    /**
     * This method returns the possible moves a player can make. It is important to notice that this only return one move
     * a player can make at the moment. A player usually can make 2 moves and up to 4 moves (if dices are equal). On
     * each move of the player this method should be called again to get the current possible moves.
     *
     * @return a list of possible moves the player can make
     */
    public ArrayList<Move> getPossibleMoves() {
        ArrayList<Move> possibleMoves = new ArrayList<>();

        if (isInFinalState()) { // Check if the player is in final state
            if (turn == BLACK) {
                // Check for normal moves
                for (int i = 0; i < 6; i++) {
                    for (int diceNumber : blacksResults) {
                        if (i - diceNumber >= 0) {
                            possibleMoves.add(new Move(i, i - diceNumber));
                        }
                    }
                }

                // Check for finish moves
                for (int diceNumber : blacksResults) {
                    for (int i = 5; i >= 0; i--) {
                        if (i - diceNumber == -1) {
                            possibleMoves.add(new Move(i, FINISHED_BAR));
                            break;
                        }
                    }
                }
            } else { // WHITE
                // Check for normal moves
                for (int i = 18; i < 24; i++) {
                    for (int diceNumber : whitesResults) {
                        if (i + diceNumber <= 23) {
                            possibleMoves.add(new Move(i, i + diceNumber));
                        }
                    }
                }

                // Check for finish moves
                for (int diceNumber : whitesResults) {
                    for (int i = 18; i < 24; i++) {
                        if (i + diceNumber == 25) {
                            possibleMoves.add(new Move(i, FINISHED_BAR));
                            break;
                        }
                    }
                }
            }
        } else if (!(turn == BLACK ? capturedBlacks : capturedWhites).isEmpty()) { // Player has a captured piece
            if (turn == BLACK) { // Check white's base for any non-blocked stack. White's base is between 18 - 23
                for (int i = 18; i <= 23; i++) {
                    if (gameStatus.get(i).isEmpty() || (!gameStatus.get(i).isEmpty() && gameStatus.get(i).peek() != WHITE && gameStatus.get(i).size() < 2)) {
                        possibleMoves.add(new Move(CAPTURED_BAR, i));
                    }
                }
            } else { // Check black's base for any non-blocked stack. Black's base is between 5 - 0
                for (int i = 5; i >= 0; i--) {
                    if (gameStatus.get(i).isEmpty() || (!gameStatus.get(i).isEmpty() && gameStatus.get(i).peek() != BLACK && gameStatus.get(i).size() < 2)) {
                        possibleMoves.add(new Move(CAPTURED_BAR, i));
                    }
                }
            }
        } else { // Player can move freely
            if (turn == BLACK) {
                for (int i = 0; i < gameStatus.size(); i++) {
                    if (gameStatus.get(i).isEmpty()) { // There isn't a piece that can be moved
                        continue;
                    }

                    if (gameStatus.get(i).peek() != BLACK) { // There isn't a black piece that can be moved
                        continue;
                    }

                    for (int dice : blacksResults) {
                        if (i - dice < 0) {
                            continue;
                        }

                        if (gameStatus.get(i - dice).isEmpty() ||
                                gameStatus.get(i - dice).peek() == BLACK || gameStatus.get(i - dice).size() == 1) {
                            possibleMoves.add(new Move(i, i - dice));
                        }
                    }
                }
            } else {
                for (int i = 0; i < gameStatus.size(); i++) {
                    if (gameStatus.get(i).isEmpty()) { // There isn't a piece that can be moved
                        continue;
                    }

                    if (gameStatus.get(i).peek() != WHITE) { // There isn't a white piece that can be moved
                        continue;
                    }

                    for (int dice : whitesResults) {
                        if (i + dice > 23) {
                            continue;
                        }

                        if (gameStatus.get(i + dice).isEmpty()
                                || gameStatus.get(i + dice).peek() == WHITE || gameStatus.get(i + dice).size() == 1) {
                            possibleMoves.add(new Move(i, i + dice));
                        }
                    }
                }
            }
        }

        return possibleMoves;
    }


    /**
     * Loads the dice results into the player's array of dice values.
     * If the dice values are equal, four dice values will be added.
     * On each move of the player, the played dice value will be removed from the array.
     * If the array is empty, it means the player has no possible moves left.
     *
     * @param dice1 the value of the first dice
     * @param dice2 the value of the second dice
     */
    public void loadDiceResults(int dice1, int dice2) {
        ArrayList<Integer> results = turn == BLACK ? blacksResults : whitesResults;

        results.clear();

        if (dice1 == dice2) {
            results.add(dice1);
            results.add(dice1);
            results.add(dice1);
            results.add(dice1);
        } else {
            results.add(dice1);
            results.add(dice2);
        }
    }

    /**
     * Moves a game piece from one location to another on the game board.
     *
     * @param from the starting location of the piece
     * @param to   the destination location of the piece
     */
    public void move(int from, int to) {
        if (from == CAPTURED_BAR) { // piece is coming from captured pieces bar
            Stack<Byte> bar = turn == BLACK ? capturedBlacks : capturedWhites;

            if (gameStatus.get(to).size() > 0 && gameStatus.get(to).peek() != turn) {
                // There is an enemy piece on the target location
                Stack<Byte> enemyBar = turn == BLACK ? capturedWhites : capturedBlacks;

                // Add the enemy piece to its captured pieces bar
                enemyBar.push(gameStatus.get(to).pop());

                // Move the players piece to target location
                gameStatus.get(to).push(bar.pop());
            } else {
                // There isn't any enemy piece on the target location, just move the piece
                gameStatus.get(to).push(bar.pop());
            }
        } else { // piece is coming from a location on board
            if (to == FINISHED_BAR) {
                // Piece is going to "finished stack"
                (turn == BLACK ? finishedBlacks : finishedWhites).push(gameStatus.get(from).pop());
            } else if (gameStatus.get(to).size() > 0 && gameStatus.get(to).peek() != turn) {
                // There is an enemy piece on the target location
                Stack<Byte> enemyBar = turn == BLACK ? capturedWhites : capturedBlacks;

                // Add the enemy piece to its captured pieces bar
                enemyBar.push(gameStatus.get(to).pop());

                // Move the players piece to target location
                gameStatus.get(to).push(gameStatus.get(from).pop());
            } else {
                // There isn't any enemy piece on the target location, just move the piece
                gameStatus.get(to).push(gameStatus.get(from).pop());
            }
        }

        ArrayList<Integer> results = turn == BLACK ? blacksResults : whitesResults;
        if (from == CAPTURED_BAR) {
            results.remove((Integer) (turn == BLACK ? 22 - to : 5 - to)); // Don't remove int cast to Integer, cuz magic
        } else if (to == FINISHED_BAR) {
            if (results.contains((Integer) (turn == BLACK ? 22 - to : 5 - to))) {
                results.remove((Integer) (turn == BLACK ? 22 - to : 5 - to));
            } else {
                results.remove((Integer) Collections.max(results));
            }
        } else {
            int usedDice = Math.abs(from - to);
            results.remove((Integer) usedDice);
        }
    }

    /**
     * Checks if a player's pieces are in the final state, which means all pieces are in the home position
     * (after position 19 for white, before position 6 for black) and there are no captured pieces.
     * These numbers were formed by accepting the initial index as 1. The real gameState ArrayList
     * uses 0 as the initial index.
     *
     * @return true if the player is in the final state, false otherwise.
     */
    private boolean isInFinalState() {
        if (turn == BLACK) {
            if (!capturedBlacks.isEmpty()) {
                return false; // There is a captured black piece
            }

            for (int i = 6; i < gameStatus.size(); i++) {
                if (!gameStatus.get(i).isEmpty() && gameStatus.get(i).peek() == BLACK) {
                    return false; // There is a black piece outside of home
                }
            }
        } else {
            if (!capturedWhites.isEmpty()) {
                return false; // There is a captured white piece
            }

            for (int i = 17; i > 0; i--) {
                if (!gameStatus.get(i).isEmpty() && gameStatus.get(i).peek() == WHITE) {
                    return false; // There is a white piece outside of home
                }
            }
        }

        return true;
    }

    /**
     * Checks if the current player has any dice results available.
     *
     * @return true if the current player has dice results, false otherwise.
     */

    public boolean hasResults() {
        return turn == BLACK ? blacksResults.size() > 0 : whitesResults.size() > 0;
    }

    /**
     * Switches the turn to other player
     */
    public void switchTurn() {
        turn = turn == BLACK ? WHITE : BLACK;
    }

    /**
     * Returns the current game turn.
     *
     * @return The current game turn represented by BackgammonGame.BLACK or BackgammonGame.WHITE.
     */
    public int getTurn() {
        return turn;
    }

    /**
     * Returns whether the current player has won or not.
     *
     * @return true if the current player has won, false otherwise.
     */
    public boolean hasWon() {
        return gameStatus.stream()
                .filter(column -> !column.isEmpty())
                .noneMatch(column -> column.peek() == turn);
    }

    /**
     * Returns current game state as a JsonArray. The format should be something like this
     * [
     * ____[ {column: 0, count: 1, type: BLACK}, ... , {column: 23, count: 3, type: WHITE} ],
     * ____{ white_captured: 3, black_captured: 5 },
     * ____{ white_finished: 2, black_finished: 6 }
     * ]
     *
     * @return The game state as a JSON string.
     */
    public String getGameStateAsJson() {
        StringBuilder asString = new StringBuilder("[");

        asString.append("[");
        for (int i = 0; i < gameStatus.size(); i++) {
            if (gameStatus.get(i).isEmpty()) {
                continue;
            }

            asString.append("{");

            asString.append(String.format("\"column\": %d, \"count\": %d, \"type\": %d",
                    i,
                    gameStatus.get(i).size(),
                    gameStatus.get(i).peek()));

            asString.append("}");

            asString.append(",");
        }

        asString.deleteCharAt(asString.length() - 1); // remove last comma

        asString.append("]");
        asString.append(",");
        asString.append("{");
        asString.append(String.format("\"white_captured\": %d", capturedWhites.size()));
        asString.append(",");
        asString.append(String.format("\"black_captured\": %d", capturedBlacks.size()));
        asString.append("}");

        asString.append(",");

        asString.append("{");
        asString.append(String.format("\"white_finished\": %d", finishedWhites.size()));
        asString.append(",");
        asString.append(String.format("\"black_finished\": %d", finishedBlacks.size()));
        asString.append("}");
        asString.append("]");

        return asString.toString();
    }
}
