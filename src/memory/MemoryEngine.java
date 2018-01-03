package memory;

import java.util.Random;

/**
 * The engine of the memory game.
 *
 * @author Steven Hricenak
 */
public class MemoryEngine {

    Tile[][] tiles;
    private int width;
    private int height;
    private int matches;

    public MemoryEngine(int w, int h) {
        this.width = w;
        this.height = h;

        tiles = new Tile[w][h];
        setup();
    }

    /**
     * Instantiates a grid of tiles, with every tile having one pair.
     */
    private void setup() {
        float counter = 0; //keeps track of the tile's IDs to be passed
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                tiles[i][j] = new Tile((int) counter);
                counter += 0.5;
            }
        }
        scramble();
    }

    /**
     * Gets the ID of the specified tile.
     *
     * @param r the row of the tile
     * @param c the column of the tile
     * @return the ID of the tile
     */
    public int getTileID(int r, int c) {
        return tiles[r][c].getID();
    }

    /**
     * Returns true if the specified tile has found its match.
     *
     * @param r the row of the tile
     * @param c the column of the tile
     * @return if the tile has been matched
     */
    public boolean tileIsMatched(int r, int c) {
        return tiles[r][c].isMatched();
    }

    /**
     * Randomizes the board of tiles.
     */
    private void scramble() {
        Random rand = new Random();
        for (int i = 0; i < width*height*3; i++) {
            int swapX1 = rand.nextInt(width);
            int swapY1 = rand.nextInt(height);
            int swapX2 = rand.nextInt(width);
            int swapY2 = rand.nextInt(height);

            tiles[swapX1][swapY1].switchTile(tiles[swapX2][swapY2]);
        }
    }

    /**
     * Compares two tiles. Sets their matched variables to true if they match.
     *
     * @param x1 x coordinate of the first tile
     * @param y1 y coordinate of the first tile
     * @param x2 x coordinate of the second tile
     * @param y2 y coordinate of the second tile
     */
    public void compare(int x1, int y1, int x2, int y2) {
        if (tiles[x1][y1].matches(tiles[x2][y2])) {
            tiles[x1][y1].setMatched(true);
            tiles[x2][y2].setMatched(true);
            matches++;
        }
    }

    /**
     * Returns true if all the tiles are matched, false otherwise.
     */
    public boolean gameIsWon() {
        return matches == width * height / 2;
    }
}
