package com.example.bomberman.models.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Classe représentant un drapeau pour le mode Capture the Flag
 * Basé sur les règles de Super Bomberman 4
 */
public class Flag extends PowerUp {
    
    private Color color;
    private int teamId; // 1 pour l'équipe bleue, 2 pour l'équipe rouge
    private boolean isPickedUp;
    private Player carrier;
    private int originalX;
    private int originalY;
    private Player owner; // Propriétaire du drapeau (si détruit, le propriétaire est éliminé)
    private boolean isDestroyed; // Indique si le drapeau a été détruit
    
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
        this.isDestroyed = false;
    }
    
    /**
     * Définit le propriétaire du drapeau
     * @param player Le joueur propriétaire du drapeau
     */
    public void setOwner(Player player) {
        this.owner = player;
    }
    
    /**
     * Dessine le drapeau
     */
    @Override
    public void render(GraphicsContext gc, int tileSize) {
        if (isPickedUp || isDestroyed) {
            // Si le drapeau est porté ou détruit, il n'est pas affiché séparément
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
        if (!isPickedUp || isDestroyed) return;
        
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
        // Dans Super Bomberman 4, n'importe quel joueur peut ramasser n'importe quel drapeau
        this.isPickedUp = true;
        this.carrier = player;
        
        // Mettre à jour la position du drapeau pour suivre le joueur
        updatePosition();
    }
    
    /**
     * Le drapeau est lâché (quand le porteur meurt ou appuie sur Y)
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
        this.isDestroyed = false;
    }
    
    /**
     * Le drapeau est détruit par une explosion
     * Dans Super Bomberman 4, si un drapeau est détruit, son propriétaire est éliminé
     * @return true si le propriétaire doit être éliminé
     */
    public boolean destroy() {
        this.isDestroyed = true;
        this.isPickedUp = false;
        this.carrier = null;
        
        // Si le drapeau a un propriétaire, il doit être éliminé
        return owner != null;
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
    
    public Player getOwner() {
        return owner;
    }
    
    public boolean isDestroyed() {
        return isDestroyed;
    }
    
    @Override
    public boolean shouldRemove() {
        return false; // Le drapeau ne doit jamais être supprimé automatiquement
    }
} 