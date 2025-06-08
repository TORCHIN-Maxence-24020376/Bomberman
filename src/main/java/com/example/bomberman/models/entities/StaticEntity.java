package com.example.bomberman.models.entities;

/**
 * Classe abstraite représentant une entité statique qui ne se déplace pas
 * Exemples: power-ups, bombes
 */
public abstract class StaticEntity extends Entity {
    // Indique si l'entité a été collectée ou détruite
    protected boolean isActive;
    // Temps de création de l'entité
    protected long creationTime;
    
    /**
     * Constructeur
     * @param x Position X
     * @param y Position Y
     */
    public StaticEntity(int x, int y) {
        super(x, y);
        this.isActive = true;
        this.creationTime = System.currentTimeMillis();
    }
    
    /**
     * Désactive l'entité (collectée ou détruite)
     */
    public void deactivate() {
        this.isActive = false;
    }
    
    /**
     * Vérifie si l'entité est active
     * @return true si l'entité est active
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Retourne le temps écoulé depuis la création de l'entité
     * @return Temps écoulé en millisecondes
     */
    protected long getElapsedTime() {
        return System.currentTimeMillis() - creationTime;
    }
} 