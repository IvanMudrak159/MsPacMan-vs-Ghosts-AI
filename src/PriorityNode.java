import game.core.Game;

public class PriorityNode implements Comparable<PriorityNode> {

    public int node; // or other types depending on your graph
    public int priority;
    public Game game;

    public PriorityNode(int node, int priority, Game game) {
        this.node = node;
        this.priority = priority;
        this.game = game;
    }

    @Override
    public int compareTo(PriorityNode other) {
        return Integer.compare(this.priority, other.priority); // Lower priority comes first
    }    
}
