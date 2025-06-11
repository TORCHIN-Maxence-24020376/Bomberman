package com.example.bomberman.models.entities;

import javafx.scene.canvas.GraphicsContext;

/**
 * Classe abstraite représentant une entité de base dans le jeu
 * Toutes les entités du jeu (joueurs, bombes, power-ups) héritent de cette classe
 */
public abstract class Entity implements GameElement {
    // Position sur la grille
    protected int x;
    protected int y;
    
    // Constructeur
    public Entity(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Met à jour l'état de l'entité
     */
    @Override
    public abstract void update();
    
    /**
     * Dessine l'entité sur le canvas
     * @param gc Contexte graphique
     * @param tileSize Taille d'une case en pixels
     */
    @Override
    public abstract void render(GraphicsContext gc, int tileSize);
    
    // Getters et setters
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    /**
     * Vérifie si cette entité est à la même position qu'une autre
     * @param other L'autre entité
     * @return true si les entités sont à la même position
     */
    public boolean collidesWith(Entity other) {
        return this.x == other.x && this.y == other.y;
    }
} 