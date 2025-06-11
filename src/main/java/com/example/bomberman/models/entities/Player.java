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

    // Nouvelles propriétés pour les power-ups
    private int speed; // Vitesse de déplacement (1-5)
    private boolean canKickBombs;
    private boolean hasSkull; // Effet négatif temporaire
    private long skullEndTime;

    // Pour contrôler la vitesse de déplacement
    private long lastMoveTime;
    private long baseMoveDelay = 200; // Délai de base en millisecondes
    
    // Invincibilité temporaire après dégât
    private boolean isInvincible;
    private long invincibilityEndTime;
    private static final long INVINCIBILITY_DURATION = 2000; // 2 secondes

    // Animation
    private double animationOffset;
    private long lastAnimationTime;
    
    // Position de spawn initiale
    private int spawnX;
    private int spawnY;
    
    // Direction du joueur (pour les sprites)
    private enum Direction { UP, DOWN, LEFT, RIGHT }
    private Direction facing = Direction.DOWN;
    
    // Sprites du joueur
    private Image frontSprite;
    private Image backSprite;
    private Image leftSprite;

    /**
     * Constructeur du joueur
     */
    public Player(int x, int y, Color color, int playerId) {
        super(x, y, 1); // Vitesse initiale = 1
        this.color = color;
        this.playerId = playerId;
        this.lives = 3;
        this.maxBombs = 1;
        this.currentBombs = 0;
        this.bombRange = 1; // Portée initiale = 1
        this.speed = 1;
        this.canKickBombs = false;
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

        // Indicateur de vitesse
        if (speed > 1) {
            gc.setFill(Color.CYAN);
            gc.fillRect(startX + indicatorIndex * spacing, indicatorY, indicatorSize, indicatorSize);
            gc.setFill(Color.BLACK);
            gc.fillText("S", startX + indicatorIndex * spacing + 1, indicatorY + 7);
            indicatorIndex++;
        }

        // Indicateur de kick
        if (canKickBombs) {
            gc.setFill(Color.GREEN);
            gc.fillRect(startX + indicatorIndex * spacing, indicatorY, indicatorSize, indicatorSize);
            gc.setFill(Color.WHITE);
            gc.fillText("K", startX + indicatorIndex * spacing + 1, indicatorY + 7);
            indicatorIndex++;
        }

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
        // Mettre à jour la direction du joueur
        int dx = newX - x;
        int dy = newY - y;
        updateFacingDirection(dx, dy);
        
        // Vérifier s'il y a une bombe devant le joueur et tenter de la pousser
        if (canKickBombs) {
            int frontX = x + dx;
            int frontY = y + dy;
            if (board.isBomb(frontX, frontY)) {
                tryPushBomb(dx, dy, board);
            }
        }
        
        if (move(dx, dy, board)) {
            return true;
        }
        return false;
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
     * Essaie de pousser une bombe dans la direction du mouvement
     */
    private void tryPushBomb(int dx, int dy, GameBoard board) {
        // Si le joueur n'a pas le power-up pour pousser les bombes, on ne fait rien
        if (!canKickBombs) return;
        
        // Vérifier s'il y a une bombe devant le joueur
        int frontBombX = x + dx;
        int frontBombY = y + dy;
        
        if (board.isBomb(frontBombX, frontBombY)) {
            // Calculer la nouvelle position de la bombe
            int newBombX = frontBombX + dx;
            int newBombY = frontBombY + dy;
            
            // Vérifier si la nouvelle position est valide
            if (board.isValidMove(newBombX, newBombY)) {
                // Déplacer la bombe
                board.moveBomb(frontBombX, frontBombY, newBombX, newBombY);
            }
        }
        
        // Vérifier également s'il y a une bombe à la position actuelle
        if (board.isBomb(x, y)) {
            // Calculer la nouvelle position de la bombe
            int newBombX = x + dx;
            int newBombY = y + dy;
            
            // Vérifier si la nouvelle position est valide
            if (board.isValidMove(newBombX, newBombY)) {
                // Déplacer la bombe
                board.moveBomb(x, y, newBombX, newBombY);
            }
        }
    }

    /**
     * Applique un power-up au joueur
     */
    public void applyPowerUp(PowerUp.Type powerUpType) {
        SoundManager.getInstance().playSound("powerup_collect");

        switch (powerUpType) {
            case BOMB_UP:
                maxBombs = Math.min(maxBombs + 1, 8); // Maximum 8 bombes
                break;

            case FIRE_UP:
                bombRange = Math.min(bombRange + 1, 10); // Maximum 10 de portée
                break;

            case SPEED_UP:
                setSpeed(Math.min(speed + 1, 5)); // Maximum vitesse 5
                break;

            case KICK:
                canKickBombs = true;
                break;

            case SKULL:
                applySkullEffect();
                break;
        }
    }

    /**
     * Applique l'effet négatif du skull
     */
    private void applySkullEffect() {
        hasSkull = true;
        skullEndTime = System.currentTimeMillis() + 10000; // 10 secondes

        // Effets aléatoires négatifs
        int randomEffect = (int)(Math.random() * 4);
        switch (randomEffect) {
            case 0: // Réduction de vitesse
                setSpeed(Math.max(1, speed - 1));
                break;
            case 1: // Réduction de portée
                bombRange = Math.max(1, bombRange - 1);
                break;
            case 2: // Réduction du nombre de bombes
                maxBombs = Math.max(1, maxBombs - 1);
                break;
            case 3: // Perte du kick
                canKickBombs = false;
                break;
        }
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
            SoundManager.getInstance().playSound("bomb_explode"); // Utiliser bomb_explode au lieu de player_death
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
     * Remet le joueur à son état initial (nouvelle partie)
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
        this.speed = 1;
        this.canKickBombs = false;
        this.hasSkull = false;
        this.skullEndTime = 0;
        this.lastMoveTime = 0;
        this.isInvincible = false;
        this.invincibilityEndTime = 0;
    }

    /**
     * Retourne une description des power-ups actifs
     */
    public String getPowerUpDescription() {
        StringBuilder desc = new StringBuilder();
        if (speed > 1) desc.append("Vitesse ").append(speed).append(", ");
        if (bombRange > 1) desc.append("Portée ").append(bombRange).append(", ");
        if (maxBombs > 1) desc.append("Bombes ").append(maxBombs).append(", ");
        if (canKickBombs) desc.append("Kick, ");
        
        if (desc.length() > 0) {
            desc.setLength(desc.length() - 2); // Supprimer la dernière virgule et espace
            return desc.toString();
        } else {
            return "Standard";
        }
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
    
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = Math.max(1, Math.min(5, speed)); }
    
    public boolean canKickBombs() { return canKickBombs; }
    public void setCanKickBombs(boolean canKick) { this.canKickBombs = canKick; }
    
    public boolean hasSkull() { return hasSkull; }
}