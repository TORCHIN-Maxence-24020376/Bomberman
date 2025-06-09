package com.example.bomberman.models.world;

import com.example.bomberman.models.entities.GameElement;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Classe représentant une case du plateau de jeu
 */
public class Tile implements GameElement {
    // Types de case
    public static final int EMPTY = 0;
    public static final int WALL = 1;
    public static final int DESTRUCTIBLE_WALL = 2;
    public static final int BOMB = 3;
    public static final int EXPLOSION = 4;
    
    private int type;
    private int x, y;
    private long explosionTime;
    private static final long EXPLOSION_DURATION = 1000; // 1 seconde
    
    /**
     * Constructeur
     * @param x Position X
     * @param y Position Y
     * @param type Type de case
     */
    public Tile(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.explosionTime = 0;
    }
    
    /**
     * Définit cette case comme une explosion
     */
    public void explode() {
        if (type != WALL) {
            type = EXPLOSION;
            explosionTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Vérifie si une explosion peut se propager sur cette case
     */
    public boolean canExplode() {
        return type != WALL;
    }
    
    /**
     * Vérifie si cette case est un mur (destructible ou non)
     */
    public boolean isWall() {
        return type == WALL || type == DESTRUCTIBLE_WALL;
    }
    
    /**
     * Vérifie si cette case est un mur destructible
     */
    public boolean isDestructibleWall() {
        return type == DESTRUCTIBLE_WALL;
    }
    
    /**
     * Vérifie si cette case est vide (peut être traversée)
     */
    public boolean isEmpty() {
        return type == EMPTY || type == EXPLOSION;
    }
    
    @Override
    public void update() {
        // Vérifier si l'explosion doit disparaître
        if (type == EXPLOSION && System.currentTimeMillis() - explosionTime > EXPLOSION_DURATION) {
            type = EMPTY;
        }
    }
    
    @Override
    public void render(GraphicsContext gc, int tileSize) {
        int cellX = x * tileSize;
        int cellY = y * tileSize;
        
        switch (type) {
            case EMPTY:
                // Sol avec effet de damier subtil
                Color grassColor = ((x + y) % 2 == 0) ? Color.LIGHTGREEN : Color.LIGHTGREEN.darker();
                gc.setFill(grassColor);
                gc.fillRect(cellX, cellY, tileSize, tileSize);
                break;
                
            case WALL:
                // Mur indestructible avec effet 3D
                gc.setFill(Color.DARKGRAY);
                gc.fillRect(cellX, cellY, tileSize, tileSize);
                
                // Effet de relief
                gc.setFill(Color.LIGHTGRAY);
                gc.fillRect(cellX, cellY, tileSize - 2, tileSize - 2);
                gc.setFill(Color.GRAY);
                gc.fillRect(cellX + 2, cellY + 2, tileSize - 4, tileSize - 4);
                
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(2);
                gc.strokeRect(cellX, cellY, tileSize, tileSize);
                break;
                
            case DESTRUCTIBLE_WALL:
                // Mur destructible avec texture
                gc.setFill(Color.BROWN);
                gc.fillRect(cellX, cellY, tileSize, tileSize);
                
                // Texture de brique
                gc.setStroke(Color.DARKRED);
                gc.setLineWidth(1);
                
                // Lignes horizontales
                for (int i = 0; i < 3; i++) {
                    gc.strokeLine(cellX, cellY + i * tileSize/3, cellX + tileSize, cellY + i * tileSize/3);
                }
                
                // Lignes verticales décalées
                for (int i = 0; i < 2; i++) {
                    int offset = (i % 2 == 0) ? 0 : tileSize/2;
                    gc.strokeLine(cellX + tileSize/2 + offset, cellY + i * tileSize/3,
                            cellX + tileSize/2 + offset, cellY + (i + 1) * tileSize/3);
                }
                
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(2);
                gc.strokeRect(cellX, cellY, tileSize, tileSize);
                break;
                
            case EXPLOSION:
                // Animation d'explosion
                long currentTime = System.currentTimeMillis();
                double progress = (double)(currentTime - explosionTime) / EXPLOSION_DURATION;
                
                // Couleur qui évolue
                Color explosionColor = Color.YELLOW.interpolate(Color.ORANGE, progress);
                gc.setFill(explosionColor);
                gc.fillRect(cellX, cellY, tileSize, tileSize);
                
                // Effet d'explosion animé avec étincelles
                gc.setFill(Color.RED);
                int explosionSize = (int)(tileSize * (1 - progress * 0.3));
                int offset = (tileSize - explosionSize) / 2;
                gc.fillRect(cellX + offset, cellY + offset, explosionSize, explosionSize);
                
                // Étincelles
                gc.setFill(Color.WHITE);
                for (int i = 0; i < 3; i++) {
                    int sparkleX = cellX + (int)(Math.random() * tileSize);
                    int sparkleY = cellY + (int)(Math.random() * tileSize);
                    gc.fillOval(sparkleX, sparkleY, 3, 3);
                }
                break;
                
            case BOMB:
                // Sol visible sous la bombe
                Color grassColorBomb = ((x + y) % 2 == 0) ? Color.LIGHTGREEN : Color.LIGHTGREEN.darker();
                gc.setFill(grassColorBomb);
                gc.fillRect(cellX, cellY, tileSize, tileSize);
                break;
        }
        
        // Grille subtile
        gc.setStroke(Color.DARKGREEN.darker());
        gc.setLineWidth(0.5);
        gc.strokeRect(cellX, cellY, tileSize, tileSize);
    }
    
    // Getters et setters
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
} 