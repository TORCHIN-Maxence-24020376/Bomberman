package com.example.bomberman.models.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Bomb extends StaticEntity {
    private int playerId;
    private static final long EXPLOSION_DELAY = 3000; // 3 secondes
    private int range;
    private boolean exploded;

    public Bomb(int x, int y, int playerId) {
        super(x, y);
        this.playerId = playerId;
        this.range = 2; // Portée par défaut
        this.exploded = false;
    }

    @Override
    public void update() {
        if (!exploded && getElapsedTime() >= EXPLOSION_DELAY) {
            exploded = true;
            deactivate();
        }
    }

    @Override
    public void render(GraphicsContext gc, int tileSize) {
        if (!isActive || exploded) return;

        // Calculer la pulsation basée sur le temps restant
        long timeLeft = EXPLOSION_DELAY - getElapsedTime();
        double pulsation = Math.sin(getElapsedTime() / 200.0);

        // Couleur qui change selon le temps restant
        if (timeLeft > 2000) {
            gc.setFill(Color.BLACK);
        } else if (timeLeft > 1000) {
            gc.setFill(Color.DARKRED);
        } else {
            gc.setFill(Color.RED);
        }

        // Taille qui pulse
        int size = (int)(tileSize * 0.6 + pulsation * 5);
        int offset = (tileSize - size) / 2;

        gc.fillOval(x * tileSize + offset, y * tileSize + offset, size, size);

        // Mèche de la bombe
        gc.setStroke(Color.ORANGE);
        gc.setLineWidth(3);
        gc.strokeLine(
                x * tileSize + tileSize/2,
                y * tileSize + offset,
                x * tileSize + tileSize/2,
                y * tileSize + offset - 10
        );
    }

    public boolean hasExploded() {
        return exploded;
    }

    // Getters et setters
    public int getPlayerId() { return playerId; }
    public int getRange() { return range; }
    public void setRange(int range) { this.range = range; }
}
