package com.example.bomberman.models.entities;

import com.example.bomberman.service.SoundManager;
import com.example.bomberman.utils.SpriteManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Bomb extends StaticEntity {
    private int playerId;
    private static final long EXPLOSION_DELAY = 3000; // 3 secondes
    private int range;
    private boolean exploded;
    private SpriteManager spriteManager;
    private Image bombSprite;

    public Bomb(int x, int y, int playerId) {
        super(x, y);
        this.playerId = playerId;
        this.range = 1; // Portée par défaut = 1
        this.exploded = false;
        this.spriteManager = SpriteManager.getInstance();
        this.bombSprite = spriteManager.loadSprite("bomb");
    }

    /**
     * Constructeur avec portée spécifiée
     */
    public Bomb(int x, int y, int playerId, int range) {
        super(x, y);
        this.playerId = playerId;
        this.range = range;
        this.exploded = false;
        this.spriteManager = SpriteManager.getInstance();
        this.bombSprite = spriteManager.loadSprite("bomb");
    }

    @Override
    public void update() {
        if (!exploded && getElapsedTime() >= EXPLOSION_DELAY) {
            exploded = true;
            SoundManager.getInstance().playSound("bomb_explode");
            deactivate();
        }
    }

    @Override
    public void render(GraphicsContext gc, int tileSize) {
        if (!isActive || exploded) return;

        // Calculer la pulsation basée sur le temps restant
        long timeLeft = EXPLOSION_DELAY - getElapsedTime();
        double pulsation = Math.sin(getElapsedTime() / 200.0);

        // Taille qui pulse
        int size = (int)(tileSize * 0.8 + pulsation * 3);
        int offset = (tileSize - size) / 2;

        // Dessiner le sprite si disponible, sinon utiliser le rendu par défaut
        if (bombSprite != null) {
            gc.drawImage(bombSprite, x * tileSize + offset, y * tileSize + offset, size, size);
        } else {
            // Couleur qui change selon le temps restant
            if (timeLeft > 2000) {
                gc.setFill(Color.BLACK);
            } else if (timeLeft > 1000) {
                gc.setFill(Color.DARKRED);
            } else {
                gc.setFill(Color.RED);
            }

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
    }

    /**
     * Déplace la bombe à une nouvelle position
     */
    public void moveTo(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }

    public boolean hasExploded() {
        return exploded;
    }

    // Getters et setters
    public int getPlayerId() { return playerId; }
    public int getRange() { return range; }
    public void setRange(int range) { this.range = range; }
}
