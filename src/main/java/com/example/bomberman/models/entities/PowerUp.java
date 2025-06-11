package com.example.bomberman.models.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Classe représentant un power-up dans le jeu
 */
public class PowerUp extends StaticEntity {
    /**
     * Types de power-ups disponibles
     */
    public enum Type {
        BOMB_UP,     // Augmente le nombre de bombes
        FIRE_UP,     // Augmente la portée des bombes
        SPEED_UP,    // Augmente la vitesse du joueur
        KICK,        // Permet de pousser les bombes
        SKULL        // Malédiction (effet négatif)
    }

    private Type type;
    private static final long BLINK_DURATION = 10000; // 10 secondes avant disparition

    /**
     * Constructeur d'un power-up
     * @param x Position X sur la grille
     * @param y Position Y sur la grille
     * @param type Type de power-up
     */
    public PowerUp(int x, int y, Type type) {
        super(x, y);
        this.type = type;
    }

    @Override
    public void update() {
        // Le power-up disparaît après un certain temps
        if (getElapsedTime() > BLINK_DURATION && isActive) {
            deactivate();
        }
    }

    /**
     * Collecte le power-up
     */
    public void collect() {
        deactivate();
    }

    /**
     * Vérifie si le power-up doit être supprimé
     * @return true si le power-up doit être supprimé
     */
    public boolean shouldRemove() {
        return !isActive || getElapsedTime() > BLINK_DURATION;
    }

    @Override
    public void render(GraphicsContext gc, int tileSize) {
        if (!isActive) return;

        long timeAlive = getElapsedTime();

        // Effet de clignotement avant disparition
        boolean shouldBlink = timeAlive > BLINK_DURATION - 3000; // 3 dernières secondes
        if (shouldBlink && (System.currentTimeMillis() / 200) % 2 == 0) {
            return; // Ne pas dessiner (effet clignotant)
        }

        int centerX = x * tileSize + tileSize / 2;
        int centerY = y * tileSize + tileSize / 2;
        int size = tileSize - 10;
        
        // Essayer de charger le sprite correspondant au type de power-up
        com.example.bomberman.utils.SpriteManager spriteManager = com.example.bomberman.utils.SpriteManager.getInstance();
        javafx.scene.image.Image sprite = null;
        
        switch (type) {
            case BOMB_UP:
                sprite = spriteManager.loadSprite("bomb_bonus");
                break;
            case FIRE_UP:
                sprite = spriteManager.loadSprite("bomb_range");
                break;
            case SPEED_UP:
                sprite = spriteManager.loadSprite("speed");
                break;
            case KICK:
                sprite = spriteManager.loadSprite("bomb_kick");
                break;
            case SKULL:
                sprite = spriteManager.loadSprite("doomed");
                break;
        }
        
        // Si un sprite est disponible, l'utiliser
        if (sprite != null) {
            // Animation simple: faire flotter le power-up
            double offsetY = Math.sin(timeAlive / 300.0) * 3;
            gc.drawImage(sprite, x * tileSize + 5, y * tileSize + 5 + offsetY, size, size);
        } else {
            // Fallback: rendu par défaut si le sprite n'est pas disponible
            switch (type) {
                case BOMB_UP:
                    gc.setFill(Color.ORANGE);
                    gc.fillRect(x * tileSize + 5, y * tileSize + 5, size, size);
                    gc.setFill(Color.BLACK);
                    gc.fillText("B+", centerX - 10, centerY + 5);
                    break;

                case FIRE_UP:
                    gc.setFill(Color.RED);
                    gc.fillRect(x * tileSize + 5, y * tileSize + 5, size, size);
                    gc.setFill(Color.WHITE);
                    gc.fillText("F+", centerX - 10, centerY + 5);
                    break;
                    
                case SPEED_UP:
                    gc.setFill(Color.CYAN);
                    gc.fillRect(x * tileSize + 5, y * tileSize + 5, size, size);
                    gc.setFill(Color.BLACK);
                    gc.fillText("S+", centerX - 10, centerY + 5);
                    break;

                case KICK:
                    gc.setFill(Color.GREEN);
                    gc.fillRect(x * tileSize + 5, y * tileSize + 5, size, size);
                    gc.setFill(Color.WHITE);
                    gc.fillText("K", centerX - 5, centerY + 5);
                    break;

                case SKULL:
                    gc.setFill(Color.PURPLE);
                    gc.fillRect(x * tileSize + 5, y * tileSize + 5, size, size);
                    gc.setFill(Color.WHITE);
                    gc.fillText("💀", centerX - 8, centerY + 5);
                    break;
            }

            // Contour
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.strokeRect(x * tileSize + 5, y * tileSize + 5, size, size);
        }
    }

    // Getter
    public Type getType() { return type; }
}