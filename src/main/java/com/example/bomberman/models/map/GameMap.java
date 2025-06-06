package com.example.bomberman.models.map;

import javafx.geometry.Rectangle2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

public class GameMap {
    private Tile[][] tiles;
    private int width;
    private int height;
    private int tileSize;

    public GameMap( int width, int height) {
        this.width = width; // Max 150
        this.height = height; // Max 150
        this.tileSize = 32; // Valeur par défaut
        this.tiles = new Tile[height][width];
        generateMap();
    }

    private void generateMap() {
        Random rnd = new Random();

        // Définition des bornes intérieures des 3×3 zones libres dans chaque coin
        int margin = 1;           // mur de bordure en index 0 et width-1 ou height-1
        int cornerSize = 3;       // dimension 3×3
        int xTLMin = margin;              // top-left x ∈ [1..3]
        int xTLMax = margin + cornerSize; // =4 (exclu, on testera x < 4)
        int yTLMin = margin;              // top-left y ∈ [1..3]
        int yTLMax = margin + cornerSize; // =4 (exclu, y < 4)

        int xTRMin = width - 1 - margin - cornerSize;   // = width-4
        int xTRMax = width - 1 - margin;                // = width-2 (exclu dans le test, on testera x >= width-4 && x < width-1)
        int yTRMin = margin;              // y < 4
        int yTRMax = margin + cornerSize;

        int xBLMin = margin;              // bottom-left x ∈ [1..3]
        int xBLMax = margin + cornerSize;
        int yBLMin = height - 1 - margin - cornerSize;  // = height-4
        int yBLMax = height - 1 - margin;               // = height-2 (exclu)

        int xBRMin = width - 1 - margin - cornerSize;   // = width-4
        int xBRMax = width - 1 - margin;                // = width-2
        int yBRMin = height - 1 - margin - cornerSize;  // = height-4
        int yBRMax = height - 1 - margin;               // = height-2

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                //  BORDE → mur fixe
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    tiles[y][x] = new WallTile(x, y);
                    continue;
                }

                // 3×3 ZONES LIBRES (FloorTile) DANS CHAQUE COIN
                //    Top-Left
                if (x >= xTLMin && x < xTLMax && y >= yTLMin && y < yTLMax) {
                    tiles[y][x] = new FloorTile(x, y);
                    continue;
                }
                // Top-Right
                if (x >= xTRMin && x < xTRMax && y >= yTRMin && y < yTRMax) {
                    tiles[y][x] = new FloorTile(x, y);
                    continue;
                }
                // Bottom-Left
                if (x >= xBLMin && x < xBLMax && y >= yBLMin && y < yBLMax) {
                    tiles[y][x] = new FloorTile(x, y);
                    continue;
                }
                // Bottom-Right
                if (x >= xBRMin && x < xBRMax && y >= yBRMin && y < yBRMax) {
                    tiles[y][x] = new FloorTile(x, y);
                    continue;
                }

                // DAMIER DE MURS FIXES (un mur fixe toutes les deux cases, hors bords)
                if (x % 2 == 0 && y % 2 == 0) {
                    tiles[y][x] = new WallTile(x, y);
                }
                // MURS DESTRUCTIBLES ALÉATOIRES (45 % de chance)
                else if (rnd.nextDouble() < 0.45) {
                    tiles[y][x] = new BreakableWall(x, y);
                }
                // 5) SINON SOL
                else {
                    tiles[y][x] = new FloorTile(x, y);
                }
            }
        }
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
