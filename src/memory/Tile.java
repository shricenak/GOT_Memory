package memory;

/**
 * The class that represents a tile in the memory game.
 * 
 * @author Steven Hricenak
 */
public class Tile {
    
    private int id;
    private boolean matched;
    
    public Tile(int id){
        setID(id);
        matched = false;
    }
    
    /**
     * Returns the ID of the tile. 
     */
    public int getID(){
        return id;
    }
    
    /**
     * Sets the ID of the tile.
     * @param id the new value
     */
    private void setID(int id){
        this.id = id;
    }
    
    /**
     * Returns the ID of the tile.
     */
    public boolean isMatched(){
        return matched;
    }
    
    /**
     * Sets if the tile has been matched or not.
     * @param match the new value
     */
    public void setMatched(boolean match){
        this.matched = match;
    }
    
    /**
     * When passed another tile, it checks if they match.
     * @param check the tile to check
     * @return true if the tiles have the same ID, false otherwise
     */
    public boolean matches(Tile check){
        return this.id == check.getID();
    }
    
    /**
     * Switches this tile's ID with the other's. Used for randomizing the 
     * board.
     */
    public void switchTile(Tile swap){
        int temp = swap.getID();
        swap.setID(this.id);
        this.setID(temp);
    }
}
