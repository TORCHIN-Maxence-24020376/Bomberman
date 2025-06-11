package com.example.bomberman.models.entities;

import com.example.bomberman.models.world.GameBoard;
import com.example.bomberman.service.SoundManager;
import com.example.bomberman.utils.SpriteManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Classe représentant un joueur dans le jeu Bomberman
 */
public class Player extends MovableEntity {
    private Color color;
    private int playerId;
    private int lives;
    private int maxBombs;
    private int currentBombs;
    private int bombRange;
    
    // Effets spéciaux
    private boolean hasSkull; // Effet négatif temporaire
    private long skullEndTime;
    
    // Gestion du déplacement
    private long lastMoveTime;
    private long baseMoveDelay = 200; // Délai de base en millisecondes
    
    // Invincibilité temporaire après avoir pris des dégâts
    private boolean isInvincible;
    private long invincibilityEndTime;
    private static final long INVINCIBILITY_DURATION = 2000; // 2 secondes
    
    // Animation
    private double animationOffset;
    private long lastAnimationTime;
    
    // Position de spawn
    private int spawnX;
    private int spawnY;
    
    // Direction
    private enum Direction { UP, DOWN, LEFT, RIGHT }
    private Direction facing = Direction.DOWN;
    
    // Sprites
    private Image frontSprite;
    private Image backSprite;
    private Image leftSprite;

    /**
     * Constructeur du joueur
     */
    public Player(int x, int y, Color color, int playerId) {
        super(x, y);
        this.color = color;
        this.playerId = playerId;
        this.lives = 3;
        this.maxBombs = 1;
        this.currentBombs = 0;
        this.bombRange = 1; // Portée initiale = 1
        this.hasSkull = false;
        this.skullEndTime = 0;
        this.lastMoveTime = 0;
        this.animationOffset = 0;
        this.lastAnimationTime = System.currentTimeMillis();
        this.spawnX = x;
        this.spawnY = y;
        this.isInvincible = false;
        this.invincibilityEndTime = 0;
        
        // Charger les sprites du joueur
        loadSprites();
    }
    
    /**
     * Charge les sprites du joueur
     */
    private void loadSprites() {
        SpriteManager spriteManager = SpriteManager.getInstance();
        String prefix = (playerId == 1) ? "p1_" : "p2_";
        
        frontSprite = spriteManager.loadSprite(prefix + "front");
        backSprite = spriteManager.loadSprite(prefix + "back");
        leftSprite = spriteManager.loadSprite(prefix + "left");
        
        // Si les sprites ne sont pas trouvés, on utilisera le rendu par défaut
        if (frontSprite == null) {
            System.out.println("Sprite " + prefix + "front non trouvé, utilisation du rendu par défaut");
        }
    }

    @Override
    public void update() {
        // Gérer l'effet du skull
        if (hasSkull && System.currentTimeMillis() > skullEndTime) {
            hasSkull = false;
        }
        
        // Gérer l'invincibilité
        if (isInvincible && System.currentTimeMillis() > invincibilityEndTime) {
            isInvincible = false;
        }

        // Animation simple
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAnimationTime > 100) {
            animationOffset = (animationOffset + 0.2) % (2 * Math.PI);
            lastAnimationTime = currentTime;
        }
    }

    @Override
    public void render(GraphicsContext gc, int tileSize) {
        if (!isAlive()) return;

        int cellX = x * tileSize;
        int cellY = y * tileSize;
        
        // Effet de clignotement si invincible
        if (isInvincible && (System.currentTimeMillis() / 200) % 2 == 0) {
            return;
        }
        
        // Utiliser les sprites si disponibles
        Image spriteToUse = null;
        
        switch (facing) {
            case UP:
                spriteToUse = backSprite;
                break;
            case DOWN:
                spriteToUse = frontSprite;
                break;
            case LEFT:
                spriteToUse = leftSprite;
                break;
            case RIGHT:
                // Utiliser le sprite gauche mais inversé horizontalement
                spriteToUse = leftSprite;
                break;
        }
        
        if (spriteToUse != null) {
            // Si c'est la direction droite, on inverse horizontalement
            if (facing == Direction.RIGHT) {
                // Sauvegarder l'état actuel
                gc.save();
                
                // Configurer la transformation pour inverser horizontalement
                gc.translate(cellX + tileSize, cellY);
                gc.scale(-1, 1);
                
                // Dessiner le sprite inversé
                gc.drawImage(spriteToUse, 0, 0, tileSize, tileSize);
                
                // Restaurer l'état
                gc.restore();
            } else {
                // Dessiner normalement
                gc.drawImage(spriteToUse, cellX, cellY, tileSize, tileSize);
            }
            
            // Effet visuel si le joueur a un skull
            if (hasSkull) {
                gc.setGlobalAlpha(0.5);
                gc.setFill(Color.PURPLE);
                gc.fillOval(cellX, cellY, tileSize, tileSize);
                gc.setGlobalAlpha(1.0);
            }
        } else {
            // Rendu par défaut si les sprites ne sont pas disponibles
            renderDefaultPlayer(gc, tileSize);
        }

        // Indicateurs de power-ups (petits icônes)
        renderPowerUpIndicators(gc, tileSize);
    }
    
    /**
     * Rendu par défaut si les sprites ne sont pas disponibles
     */
    private void renderDefaultPlayer(GraphicsContext gc, int tileSize) {
        // Animation de pulsation si le joueur a un skull
        double pulseEffect = hasSkull ? Math.sin(animationOffset * 3) * 0.1 : 0;
        int effectiveSize = (int)((tileSize - 10) * (1 + pulseEffect));
        int offset = (tileSize - effectiveSize) / 2;

        // Couleur avec effet si nécessaire
        Color renderColor = hasSkull ? Color.PURPLE.interpolate(color, 0.5) : color;

        // Corps du joueur (plus stylé)
        gc.setFill(renderColor);
        gc.fillOval(x * tileSize + offset, y * tileSize + offset, effectiveSize, effectiveSize);

        // Contour plus épais
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);
        gc.strokeOval(x * tileSize + offset, y * tileSize + offset, effectiveSize, effectiveSize);

        // Yeux
        gc.setFill(Color.WHITE);
        int eyeSize = Math.max(3, tileSize / 8);
        gc.fillOval(x * tileSize + tileSize/3 - eyeSize/2, y * tileSize + tileSize/3, eyeSize, eyeSize);
        gc.fillOval(x * tileSize + 2*tileSize/3 - eyeSize/2, y * tileSize + tileSize/3, eyeSize, eyeSize);

        // Pupilles
        gc.setFill(Color.BLACK);
        int pupilSize = Math.max(1, eyeSize / 2);
        gc.fillOval(x * tileSize + tileSize/3 - pupilSize/2, y * tileSize + tileSize/3 + 1, pupilSize, pupilSize);
        gc.fillOval(x * tileSize + 2*tileSize/3 - pupilSize/2, y * tileSize + tileSize/3 + 1, pupilSize, pupilSize);

        // Numéro du joueur
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 12));
        gc.fillText(String.valueOf(playerId), x * tileSize + tileSize/2 - 3, y * tileSize + 2*tileSize/3);
    }

    /**
     * Affiche les indicateurs de power-ups actifs
     */
    private void renderPowerUpIndicators(GraphicsContext gc, int tileSize) {
        int indicatorY = y * tileSize - 5;
        int indicatorSize = 8;
        int spacing = 10;
        int startX = x * tileSize;

        int indicatorIndex = 0;

        // Indicateur de portée élevée
        if (bombRange > 1) {
            gc.setFill(Color.RED);
            gc.fillRect(startX + indicatorIndex * spacing, indicatorY, indicatorSize, indicatorSize);
            gc.setFill(Color.WHITE);
            gc.fillText("F", startX + indicatorIndex * spacing + 1, indicatorY + 7);
            indicatorIndex++;
        }

        // Indicateur de bombes multiples
        if (maxBombs > 1) {
            gc.setFill(Color.ORANGE);
            gc.fillRect(startX + indicatorIndex * spacing, indicatorY, indicatorSize, indicatorSize);
            gc.setFill(Color.BLACK);
            gc.fillText("B", startX + indicatorIndex * spacing + 1, indicatorY + 7);
        }
    }

    /**
     * Définit la nouvelle position du joueur
     * @param newX Nouvelle position X
     * @param newY Nouvelle position Y
     * @param board Plateau de jeu pour vérifier les collisions
     */
    public boolean moveToPosition(int newX, int newY, GameBoard board) {
        // Calculer le déplacement
        int dx = newX - x;
        int dy = newY - y;
        
        // Essayer de déplacer le joueur
        boolean moved = move(dx, dy, board);
        
        // Mettre à jour la direction du joueur seulement si le déplacement a réussi
        if (moved) {
            updateFacingDirection(dx, dy);
        }
        
        return moved;
    }
    
    /**
     * Met à jour la direction dans laquelle le joueur regarde
     */
    private void updateFacingDirection(int dx, int dy) {
        if (dx > 0) {
            facing = Direction.RIGHT;
        } else if (dx < 0) {
            facing = Direction.LEFT;
        } else if (dy < 0) {
            facing = Direction.UP;
        } else if (dy > 0) {
            facing = Direction.DOWN;
        }
    }

    /**
     * Applique un power-up au joueur
     */
    public void applyPowerUp(PowerUp.Type powerUpType) {
        SoundManager.getInstance().playSound("powerup_collect");
        
        switch (powerUpType) {
            case BOMB_UP:
                maxBombs++;
                break;
            case FIRE_UP:
                bombRange++;
                break;
            case SKULL:
                applySkullEffect();
                break;
            default:
                break;
        }
    }

    /**
     * Applique l'effet négatif du skull
     */
    private void applySkullEffect() {
        hasSkull = true;
        skullEndTime = System.currentTimeMillis() + 10000; // 10 secondes
        
        // Effet de la malédiction: perte de tous les power-ups
        maxBombs = 1;    // Retour à 1 bombe
        bombRange = 1;   // Retour à portée 1
        
        // Jouer un son spécial pour la malédiction
        SoundManager.getInstance().playSound("skull_effect");
    }

    /**
     * Vérifie si le joueur peut placer une bombe
     */
    public boolean canPlaceBomb() {
        return currentBombs < maxBombs;
    }

    /**
     * Place une bombe
     */
    public void placeBomb() {
        if (canPlaceBomb()) {
            currentBombs++;
            SoundManager.getInstance().playSound("bomb_place");
        }
    }

    /**
     * Notifie qu'une bombe a explosé
     */
    public void bombExploded() {
        if (currentBombs > 0) {
            currentBombs--;
        }
    }

    /**
     * Le joueur subit des dégâts
     */
    public void takeDamage() {
        if (isInvincible) return;
        
        lives--;
        if (lives <= 0) {
            lives = 0;
            SoundManager.getInstance().playSound("bomb_explode");
        } else {
            // Téléporter à la position de spawn et rendre invincible
            respawn();
        }
    }
    
    /**
     * Fait réapparaître le joueur à sa position de spawn
     */
    private void respawn() {
        this.x = spawnX;
        this.y = spawnY;
        this.isInvincible = true;
        this.invincibilityEndTime = System.currentTimeMillis() + INVINCIBILITY_DURATION;
    }

    /**
     * Vérifie si le joueur est vivant
     */
    public boolean isAlive() {
        return lives > 0;
    }
    
    /**
     * Vérifie si le joueur est invincible
     */
    public boolean isInvincible() {
        return isInvincible;
    }

    /**
     * Réinitialise le joueur à sa position de départ
     */
    public void reset(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.spawnX = startX;
        this.spawnY = startY;
        this.lives = 3;
        this.maxBombs = 1;
        this.currentBombs = 0;
        this.bombRange = 1;
        this.hasSkull = false;
        this.isInvincible = false;
    }

    /**
     * Retourne une description des power-ups actifs
     */
    public String getPowerUpDescription() {
        StringBuilder description = new StringBuilder();
        description.append("Bombes: ").append(maxBombs);
        description.append(", Portée: ").append(bombRange);
        
        if (hasSkull) {
            description.append(" [MAUDIT]");
        }
        
        return description.toString();
    }

    // Getters et Setters
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    
    public int getPlayerId() { return playerId; }
    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }
    
    public int getBombRange() { return bombRange; }
    public void setBombRange(int range) { this.bombRange = Math.max(1, range); }
    
    public int getMaxBombs() { return maxBombs; }
    public void setMaxBombs(int maxBombs) { this.maxBombs = Math.max(1, maxBombs); }
    
    public int getCurrentBombs() { return currentBombs; }
    
    public boolean hasSkull() { return hasSkull; }
}