package com.example.bomberman.models.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Classe représentant un drapeau pour le mode Capture the Flag
 */
public class Flag extends PowerUp {
    
    private Color color;
    private int teamId; // 1 pour l'équipe bleue, 2 pour l'équipe rouge
    private boolean isPickedUp;
    private Player carrier;
    private int originalX;
    private int originalY;
    
    /**
     * Constructeur
     * @param x Position X initiale
     * @param y Position Y initiale
     * @param teamId ID de l'équipe (1 ou 2)
     */
    public Flag(int x, int y, int teamId) {
        super(x, y, PowerUp.Type.KICK); // Le type n'est pas utilisé pour le drapeau
        this.originalX = x;
        this.originalY = y;
        this.teamId = teamId;
        this.color = (teamId == 1) ? Color.BLUE : Color.RED;
        this.isPickedUp = false;
        this.carrier = null;
    }
    
    /**
     * Dessine le drapeau
     */
    @Override
    public void render(GraphicsContext gc, int tileSize) {
        if (isPickedUp) {
            // Si le drapeau est porté, il n'est pas affiché séparément
            // (il sera affiché avec le joueur qui le porte)
            return;
        }
        
        // Dessiner le mât du drapeau
        gc.setFill(Color.SADDLEBROWN);
        int poleWidth = tileSize / 8;
        int poleHeight = tileSize * 3 / 4;
        int poleX = x * tileSize + tileSize / 2 - poleWidth / 2;
        int poleY = y * tileSize + tileSize / 4;
        gc.fillRect(poleX, poleY, poleWidth, poleHeight);
        
        // Dessiner le drapeau
        gc.setFill(color);
        int flagWidth = tileSize / 2;
        int flagHeight = tileSize / 3;
        int flagX = poleX + poleWidth;
        int flagY = poleY;
        gc.fillRect(flagX, flagY, flagWidth, flagHeight);
        
        // Ajouter un contour
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(flagX, flagY, flagWidth, flagHeight);
    }
    
    /**
     * Dessine le drapeau porté par un joueur
     */
    public void renderCarried(GraphicsContext gc, int tileSize, int playerX, int playerY) {
        if (!isPickedUp) return;
        
        // Dessiner un petit drapeau au-dessus du joueur
        gc.setFill(color);
        int flagWidth = tileSize / 3;
        int flagHeight = tileSize / 4;
        int flagX = playerX * tileSize + tileSize / 3;
        int flagY = playerY * tileSize - flagHeight;
        gc.fillRect(flagX, flagY, flagWidth, flagHeight);
        
        // Ajouter un contour
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(flagX, flagY, flagWidth, flagHeight);
    }
    
    /**
     * Le drapeau est ramassé par un joueur
     */
    public void pickUp(Player player) {
        if (player.getPlayerId() == teamId) {
            // Un joueur ne peut pas prendre son propre drapeau
            return;
        }
        
        this.isPickedUp = true;
        this.carrier = player;
        
        // Mettre à jour la position du drapeau pour suivre le joueur
        updatePosition();
    }
    
    /**
     * Le drapeau est lâché (quand le porteur meurt)
     */
    public void drop() {
        if (carrier != null) {
            // Le drapeau est déposé à la position actuelle du porteur
            this.x = carrier.getX();
            this.y = carrier.getY();
        }
        
        this.isPickedUp = false;
        this.carrier = null;
    }
    
    /**
     * Le drapeau est retourné à sa position d'origine
     */
    public void returnToBase() {
        this.x = originalX;
        this.y = originalY;
        this.isPickedUp = false;
        this.carrier = null;
    }
    
    /**
     * Met à jour la position du drapeau pour suivre le porteur
     */
    public void updatePosition() {
        if (isPickedUp && carrier != null) {
            this.x = carrier.getX();
            this.y = carrier.getY();
        }
    }
    
    /**
     * Vérifie si le drapeau est à sa base
     */
    public boolean isAtBase() {
        return x == originalX && y == originalY;
    }
    
    /**
     * Met à jour l'état du drapeau
     */
    @Override
    public void update() {
        if (isPickedUp && carrier != null) {
            updatePosition();
        }
    }
    
    // Getters et setters
    public boolean isPickedUp() {
        return isPickedUp;
    }
    
    public Player getCarrier() {
        return carrier;
    }
    
    public int getTeamId() {
        return teamId;
    }
    
    public int getOriginalX() {
        return originalX;
    }
    
    public int getOriginalY() {
        return originalY;
    }
    
    public Color getColor() {
        return color;
    }
    
    @Override
    public boolean shouldRemove() {
        return false; // Le drapeau ne doit jamais être supprimé automatiquement
    }
} 