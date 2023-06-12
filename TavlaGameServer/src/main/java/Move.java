public class Move {
    private final int from;
    private final int to;

    public Move(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return String.format("{\"from\": %d, \"to\": %d}", from, to);
    }
}