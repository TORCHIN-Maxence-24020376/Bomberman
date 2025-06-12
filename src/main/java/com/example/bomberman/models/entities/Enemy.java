package com.example.bomberman.models.entities;

import com.example.bomberman.models.world.GameBoard;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

/**
 * Classe représentant un ennemi dans le jeu
 */
public class Enemy extends MovableEntity {
    private static final int[] DX = {0, 1, 0, -1}; // Directions possibles en X
    private static final int[] DY = {-1, 0, 1, 0}; // Directions possibles en Y
    
    private int currentDirection;
    private int movementCounter;
    private int maxMovesInDirection;
    private Color color;
    private Random random;
    
    /**
     * Constructeur
     * @param x Position X initiale
     * @param y Position Y initiale
     */
    public Enemy(int x, int y) {
        super(x, y);
        this.color = Color.PURPLE;
        this.random = new Random();
        this.currentDirection = random.nextInt(4);
        this.movementCounter = 0;
        this.maxMovesInDirection = 5 + random.nextInt(5); // Entre 5 et 10 mouvements dans la même direction
    }
    
    @Override
    public void update() {
        // L'ennemi change de direction après un certain nombre de mouvements
        // ou quand il rencontre un obstacle
        if (movementCounter >= maxMovesInDirection) {
            changeDirection();
        }
    }
    
    /**
     * Change la direction de l'ennemi aléatoirement
     */
    private void changeDirection() {
        int previousDirection = currentDirection;
        
        // Éviter de revenir directement en arrière (direction opposée)
        do {
            currentDirection = random.nextInt(4);
        } while ((currentDirection + 2) % 4 == previousDirection);
        
        movementCounter = 0;
        maxMovesInDirection = 5 + random.nextInt(5);
    }
    
    /**
     * Déplace l'ennemi selon sa direction actuelle
     * @param board Plateau de jeu
     * @return true si le déplacement a été effectué
     */
    public boolean moveInCurrentDirection(GameBoard board) {
        int dx = DX[currentDirection];
        int dy = DY[currentDirection];
        
        boolean moved = move(dx, dy, board);
        
        if (moved) {
            movementCounter++;
        } else {
            // Si l'ennemi ne peut pas se déplacer, changer de direction
            changeDirection();
        }
        
        return moved;
    }
    
    @Override
    public void render(GraphicsContext gc, int tileSize) {
        // Corps de l'ennemi
        gc.setFill(color);
        int size = (int)(tileSize * 0.8);
        int offset = (tileSize - size) / 2;
        gc.fillOval(x * tileSize + offset, y * tileSize + offset, size, size);
        
        // Contour
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(x * tileSize + offset, y * tileSize + offset, size, size);
        
        // Yeux
        gc.setFill(Color.WHITE);
        int eyeSize = Math.max(3, tileSize / 8);
        gc.fillOval(x * tileSize + tileSize/3 - eyeSize/2, y * tileSize + tileSize/3, eyeSize, eyeSize);
        gc.fillOval(x * tileSize + 2*tileSize/3 - eyeSize/2, y * tileSize + tileSize/3, eyeSize, eyeSize);
        
        // Pupilles (qui regardent dans la direction du mouvement)
        gc.setFill(Color.BLACK);
        int pupilSize = Math.max(1, eyeSize / 2);
        int pupilOffsetX = 0;
        int pupilOffsetY = 0;
        
        switch (currentDirection) {
            case 0: // Haut
                pupilOffsetY = -1;
                break;
            case 1: // Droite
                pupilOffsetX = 1;
                break;
            case 2: // Bas
                pupilOffsetY = 1;
                break;
            case 3: // Gauche
                pupilOffsetX = -1;
                break;
        }
        
        gc.fillOval(
                x * tileSize + tileSize/3 - pupilSize/2 + pupilOffsetX, 
                y * tileSize + tileSize/3 + pupilOffsetY, 
                pupilSize, pupilSize);
        gc.fillOval(
                x * tileSize + 2*tileSize/3 - pupilSize/2 + pupilOffsetX, 
                y * tileSize + tileSize/3 + pupilOffsetY, 
                pupilSize, pupilSize);
    }
    
    /**
     * Vérifie si l'ennemi est en collision avec un joueur
     * @param player Le joueur à vérifier
     * @return true si l'ennemi est en collision avec le joueur
     */
    public boolean collidesWithPlayer(Player player) {
        return this.x == player.getX() && this.y == player.getY();
    }
    
    // Getters et setters
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
} 