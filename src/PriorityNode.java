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
        // Lower level is better
        if (this.game.getCurLevel() != other.game.getCurLevel()) 
            return Integer.compare(this.game.getCurLevel(), other.game.getCurLevel());
        
        // Fewer remaining lives is worse (higher lives should be prioritized)
        if (this.game.getLivesRemaining() != other.game.getLivesRemaining()) 
            return -Integer.compare(this.game.getLivesRemaining(), other.game.getLivesRemaining());

        // Fewer active pills is better
        if (this.game.getNumActivePills() != other.game.getNumActivePills()) 
            return Integer.compare(this.game.getNumActivePills(), other.game.getNumActivePills());
        
        // Finally, lower priority values are better
        return Integer.compare(this.priority, other.priority);    
    }
}
