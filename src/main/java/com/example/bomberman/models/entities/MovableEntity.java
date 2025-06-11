package com.example.bomberman.models.entities;

import com.example.bomberman.models.world.GameBoard;

/**
 * Classe abstraite représentant une entité qui peut se déplacer
 * Exemples: joueurs, ennemis
 */
public abstract class MovableEntity extends Entity {
    // Temps du dernier déplacement
    protected long lastMoveTime;
    
    /**
     * Constructeur
     * @param x Position X initiale
     * @param y Position Y initiale
     */
    public MovableEntity(int x, int y) {
        super(x, y);
        this.lastMoveTime = 0;
    }
    
    /**
     * Vérifie si l'entité peut se déplacer maintenant
     * @return true si l'entité peut se déplacer
     */
    public boolean canMove() {
        // Système de déplacement simple avec délai fixe
        return System.currentTimeMillis() - lastMoveTime >= 200; // 200ms de délai fixe entre les déplacements
    }
    
    /**
     * Déplace l'entité dans une direction
     * @param dx Déplacement en X
     * @param dy Déplacement en Y
     * @param board Plateau de jeu pour vérifier les collisions
     * @return true si le déplacement a été effectué
     */
    public boolean move(int dx, int dy, GameBoard board) {
        if (!canMove()) {
            return false;
        }
        
        int newX = x + dx;
        int newY = y + dy;
        
        // Vérifier si la case est libre
        if (board.isValidMove(newX, newY)) {
            x = newX;
            y = newY;
            lastMoveTime = System.currentTimeMillis();
            return true;
        }
        
        return false;
    }
    
    /**
     * Force le déplacement sans vérifier les contraintes de temps ou de collision
     * @param newX Nouvelle position X
     * @param newY Nouvelle position Y
     */
    public void forcePosition(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }
} 