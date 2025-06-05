package com.example.bomberman;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameBoard {
    public static final int EMPTY = 0;
    public static final int WALL = 1;
    public static final int DESTRUCTIBLE_WALL = 2;
    public static final int BOMB = 3;
    public static final int EXPLOSION = 4;

    private int[][] board;
    private long[][] explosionTime;
    private int width, height;
    private static final long EXPLOSION_DURATION = 1000; // 1 seconde

    public GameBoard(int width, int height) {
        this.width = width;
        this.height = height;
        this.board = new int[height][width];
        this.explosionTime = new long[height][width];
        initializeBoard();
    }

    private void initializeBoard() {
        // Initialiser avec des murs sur les bords et en damier
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    // Murs de bordure
                    board[y][x] = WALL;
                } else if (x % 2 == 0 && y % 2 == 0) {
                    // Murs indestructibles en damier
                    board[y][x] = WALL;
                } else if (Math.random() < 0.3 &&
                        !(x <= 2 && y <= 2) &&
                        !(x >= width - 3 && y >= height - 3)) {
                    // Murs destructibles aléatoirement (sauf près des spawn)
                    board[y][x] = DESTRUCTIBLE_WALL;
                } else {
                    board[y][x] = EMPTY;
                }
            }
        }

        // S'assurer que les positions de spawn sont libres
        board[1][1] = EMPTY;
        board[1][2] = EMPTY;
        board[2][1] = EMPTY;
        board[height - 2][width - 2] = EMPTY;
        board[height - 2][width - 3] = EMPTY;
        board[height - 3][width - 2] = EMPTY;
    }

    public boolean isValidMove(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height &&
                (board[y][x] == EMPTY || board[y][x] == EXPLOSION);
    }

    public boolean canExplode(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height &&
                board[y][x] != WALL;
    }

    public boolean isWall(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height &&
                (board[y][x] == WALL || board[y][x] == DESTRUCTIBLE_WALL);
    }

    public void placeBomb(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            board[y][x] = BOMB;
        }
    }

    public void removeBomb(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height && board[y][x] == BOMB) {
            board[y][x] = EMPTY;
        }
    }

    public void explode(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            if (board[y][x] == DESTRUCTIBLE_WALL) {
                board[y][x] = EMPTY; // Détruire le mur
            }
            if (board[y][x] != WALL) {
                board[y][x] = EXPLOSION;
                explosionTime[y][x] = System.currentTimeMillis();
            }
        }
    }

    public boolean isExplosion(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height &&
                board[y][x] == EXPLOSION;
    }

    public void render(GraphicsContext gc, int tileSize) {
        long currentTime = System.currentTimeMillis();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int cellX = x * tileSize;
                int cellY = y * tileSize;

                switch (board[y][x]) {
                    case EMPTY:
                        gc.setFill(Color.LIGHTGREEN);
                        gc.fillRect(cellX, cellY, tileSize, tileSize);
                        break;

                    case WALL:
                        gc.setFill(Color.DARKGRAY);
                        gc.fillRect(cellX, cellY, tileSize, tileSize);
                        gc.setStroke(Color.BLACK);
                        gc.strokeRect(cellX, cellY, tileSize, tileSize);
                        break;

                    case DESTRUCTIBLE_WALL:
                        gc.setFill(Color.BROWN);
                        gc.fillRect(cellX, cellY, tileSize, tileSize);
                        gc.setStroke(Color.DARKRED);
                        gc.strokeRect(cellX, cellY, tileSize, tileSize);
                        break;

                    case BOMB:
                        gc.setFill(Color.LIGHTGREEN);
                        gc.fillRect(cellX, cellY, tileSize, tileSize);
                        break;

                    case EXPLOSION:
                        // Vérifier si l'explosion doit disparaître
                        if (currentTime - explosionTime[y][x] > EXPLOSION_DURATION) {
                            board[y][x] = EMPTY;
                            gc.setFill(Color.LIGHTGREEN);
                        } else {
                            gc.setFill(Color.YELLOW);
                        }
                        gc.fillRect(cellX, cellY, tileSize, tileSize);

                        if (board[y][x] == EXPLOSION) {
                            // Effet d'explosion animé
                            gc.setFill(Color.ORANGE);
                            double progress = (double)(currentTime - explosionTime[y][x]) / EXPLOSION_DURATION;
                            int explosionSize = (int)(tileSize * (1 - progress));
                            int offset = (tileSize - explosionSize) / 2;
                            gc.fillRect(cellX + offset, cellY + offset, explosionSize, explosionSize);
                        }
                        break;
                }

                // Grille
                gc.setStroke(Color.DARKGREEN);
                gc.setLineWidth(1);
                gc.strokeRect(cellX, cellY, tileSize, tileSize);
            }
        }
    }
}