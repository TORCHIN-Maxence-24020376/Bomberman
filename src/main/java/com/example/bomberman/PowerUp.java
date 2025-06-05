package com.example.bomberman;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Classe représentant un power-up dans le jeu
 */
public class PowerUp {
    /**
     * Types de power-ups disponibles
     */
    public enum Type {
        BOMB_UP,     // Augmente le nombre de bombes
        FIRE_UP,     // Augmente la portée des bombes
        SPEED_UP,    // Augmente la vitesse de déplacement
        KICK,        // Permet de pousser les bombes
        SKULL        // Malédiction (effet négatif)
    }

    private int x, y;
    private Type type;
    private boolean collected;
    private long spawnTime;
    private static final long BLINK_DURATION = 10000; // 10 secondes avant disparition

    /**
     * Constructeur d'un power-up
     * @param x Position X sur la grille
     * @param y Position Y sur la grille
     * @param type Type de power-up
     */
    public PowerUp(int x, int y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.collected = false;
        this.spawnTime = System.currentTimeMillis();
    }

    /**
     * Met à jour l'état du power-up
     */
    public void update() {
        // Le power-up disparaît après un certain temps
        long currentTime = System.currentTimeMillis();
        if (currentTime - spawnTime > BLINK_DURATION && !collected) {
            // Marquer comme collecté pour le supprimer
            collected = true;
        }
    }

    /**
     * Dessine le power-up
     * @param gc Contexte graphique
     * @param tileSize Taille d'une case
     */
    public void render(GraphicsContext gc, int tileSize) {
        if (collected) return;

        long currentTime = System.currentTimeMillis();
        long timeAlive = currentTime - spawnTime;

        // Effet de clignotement avant disparition
        boolean shouldBlink = timeAlive > BLINK_DURATION - 3000; // 3 dernières secondes
        if (shouldBlink && (currentTime / 200) % 2 == 0) {
            return; // Ne pas dessiner (effet clignotant)
        }

        int centerX = x * tileSize + tileSize / 2;
        int centerY = y * tileSize + tileSize / 2;
        int size = tileSize - 10;

        // Couleur et forme selon le type
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

    /**
     * Marque le power-up comme collecté
     */
    public void collect() {
        this.collected = true;
    }

    /**
     * Vérifie si le power-up doit être supprimé
     * @return true si le power-up doit être supprimé
     */
    public boolean shouldRemove() {
        return collected || (System.currentTimeMillis() - spawnTime > BLINK_DURATION);
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public Type getType() { return type; }
    public boolean isCollected() { return collected; }
}