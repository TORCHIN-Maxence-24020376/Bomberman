package com.example.bomberman;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Player {
    private int x, y;
    private Color color;
    private int playerId;
    private int lives;
    private int maxBombs;
    private int currentBombs;
    private int bombRange;

    public Player(int x, int y, Color color, int playerId) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.playerId = playerId;
        this.lives = 3;
        this.maxBombs = 1;
        this.currentBombs = 0;
        this.bombRange = 2;
    }

    public void render(GraphicsContext gc, int tileSize) {
        gc.setFill(color);
        gc.fillOval(x * tileSize + 5, y * tileSize + 5, tileSize - 10, tileSize - 10);

        // Ajouter un contour
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(x * tileSize + 5, y * tileSize + 5, tileSize - 10, tileSize - 10);

        // Numéro du joueur
        gc.setFill(Color.WHITE);
        gc.fillText(String.valueOf(playerId), x * tileSize + tileSize/2 - 5, y * tileSize + tileSize/2 + 5);
    }

    public boolean canPlaceBomb() {
        return currentBombs < maxBombs;
    }

    public void placeBomb() {
        if (canPlaceBomb()) {
            currentBombs++;
        }
    }

    public void bombExploded() {
        if (currentBombs > 0) {
            currentBombs--;
        }
    }

    public void takeDamage() {
        lives--;
        if (lives <= 0) {
            lives = 0;
        }
    }

    public boolean isAlive() {
        return lives > 0;
    }

    // Getters et Setters
    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    public int getPlayerId() { return playerId; }
    public int getLives() { return lives; }
    public int getBombRange() { return bombRange; }
    public void setBombRange(int range) { this.bombRange = range; }
    public int getMaxBombs() { return maxBombs; }
    public void setMaxBombs(int maxBombs) { this.maxBombs = maxBombs; }
}