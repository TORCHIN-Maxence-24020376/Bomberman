package com.example.bomberman.models.entities;

import javafx.scene.canvas.GraphicsContext;

/**
 * Interface définissant les méthodes communes à tous les éléments du jeu
 */
public interface GameElement {
    /**
     * Met à jour l'état de l'élément
     */
    void update();
    
    /**
     * Dessine l'élément sur le canvas
     * @param gc Contexte graphique
     * @param tileSize Taille d'une case en pixels
     */
    void render(GraphicsContext gc, int tileSize);
} 