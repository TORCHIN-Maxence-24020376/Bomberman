package com.example.bomberman.models.entities;

import com.example.bomberman.models.world.GameBoard;

/**
 * Classe abstraite représentant une entité qui peut se déplacer
 * Exemples: joueurs, ennemis
 */
public abstract class MovableEntity extends Entity {
    // Vitesse de déplacement
    protected int speed;
    // Temps du dernier déplacement
    protected long lastMoveTime;
    // Délai de base entre les déplacements
    protected long baseMoveDelay;
    
    /**
     * Constructeur
     * @param x Position X initiale
     * @param y Position Y initiale
     * @param speed Vitesse initiale
     */
    public MovableEntity(int x, int y, int speed) {
        super(x, y);
        this.speed = speed;
        this.lastMoveTime = 0;
        this.baseMoveDelay = 200; // 200ms par défaut
    }
    
    /**
     * Vérifie si l'entité peut se déplacer maintenant
     * @return true si l'entité peut se déplacer
     */
    public boolean canMove() {
        long moveDelay = baseMoveDelay / speed;
        return System.currentTimeMillis() - lastMoveTime >= moveDelay;
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
    
    // Getters et setters
    public int getSpeed() {
        return speed;
    }
    
    public void setSpeed(int speed) {
        this.speed = Math.max(1, Math.min(5, speed)); // Limité entre 1 et 5
    }
} 