package com.example.bomberman.models.map;

import javafx.geometry.Rectangle2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class GameMap {
    private Tile[][] tiles;
    private int width;
    private int height;
    private int tileSize;

    public GameMap( int width, int height) {
        this.width = width;
        this.height = height;
        this.tileSize = 32; // Valeur par défaut
        this.tiles = new Tile[height][width];
        // generateDefaultMap();
    }

    public Tile getTile(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return null;
        return tiles[y][x];
    }

    public void setTile(int x, int y, Tile tile) {
        if (x >= 0 && y >= 0 && x < width && y < height) {
            tiles[y][x] = tile;
        }
    }

    public boolean isWalkable(int x, int y) {
        Tile tile = getTile(x, y);
        return tile != null && tile.isWalkable();
    }

    public void loadFromFile(String filename) {
        try {
            InputStream input = getClass().getResourceAsStream("/" + filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String line;
            int y = 0;
            while ((line = reader.readLine()) != null && y < height) {
                for (int x = 0; x < Math.min(line.length(), width); x++) {
                    char c = line.charAt(x);
                    Tile tile;
                    switch (c) {
                        case 'W':
                            tile = new WallTile(x, y);
                            break;
                        case 'B':
                            tile = new BreakableWall(x, y);
                            break;
                        case '.':
                        case ' ':
                            tile = new FloorTile(x, y);
                            break;
                        default:
                            tile = new FloorTile(x, y); // fallback
                            break;
                    }
                    tiles[y][x] = tile;
                }
                y++;
            }

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Rectangle2D getCollisionBounds(int x, int y) {
        return new Rectangle2D(x * tileSize, y * tileSize, tileSize, tileSize);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTileSize() {
        return tileSize;
    }
}
