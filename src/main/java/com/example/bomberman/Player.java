package com.example.bomberman;

import com.example.bomberman.PlayerProfile;
import com.example.bomberman.PowerUp;
import com.example.bomberman.SoundManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Classe représentant un joueur dans le jeu Bomberman
 */
public class Player {
    private int x, y;
    private Color color;
    private int playerId;
    private int lives;
    private int maxBombs;
    private int currentBombs;
    private int bombRange;
    private PlayerProfile profile;

    // Nouvelles propriétés pour les power-ups
    private int speed; // Vitesse de déplacement (1-5)
    private boolean canKickBombs;
    private boolean hasSkull; // Effet négatif temporaire
    private long skullEndTime;

    // Pour contrôler la vitesse de déplacement
    private long lastMoveTime;
    private long baseMoveDelay = 200; // Délai de base en millisecondes

    // Animation
    private double animationOffset;
    private long lastAnimationTime;

    /**
     * Constructeur du joueur
     */
    public Player(int x, int y, Color color, int playerId) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.playerId = playerId;
        this.lives = 3;
        this.maxBombs = 1;
        this.currentBombs = 0;
        this.bombRange = 2;
        this.speed = 1;
        this.canKickBombs = false;
        this.hasSkull = false;
        this.skullEndTime = 0;
        this.lastMoveTime = 0;
        this.animationOffset = 0;
        this.lastAnimationTime = System.currentTimeMillis();
    }

    /**
     * Met à jour l'état du joueur
     */
    public void update() {
        // Gérer l'effet du skull
        if (hasSkull && System.currentTimeMillis() > skullEndTime) {
            hasSkull = false;
        }

        // Animation simple
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAnimationTime > 100) {
            animationOffset = (animationOffset + 0.2) % (2 * Math.PI);
            lastAnimationTime = currentTime;
        }
    }

    /**
     * Dessine le joueur avec des améliorations visuelles
     */
    public void render(GraphicsContext gc, int tileSize) {
        if (!isAlive()) return;

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

        // Indicateurs de power-ups (petits icônes)
        renderPowerUpIndicators(gc, tileSize);
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
        if (bombRange > 2) {
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
     * Vérifie si le joueur peut se déplacer
     */
    public boolean canMove() {
        long moveDelay = baseMoveDelay / speed; // Plus rapide avec la vitesse
        return System.currentTimeMillis() - lastMoveTime >= moveDelay;
    }

    /**
     * Définit la nouvelle position du joueur
     */
    public void setPosition(int newX, int newY) {
        if (canMove()) {
            this.x = newX;
            this.y = newY;
            this.lastMoveTime = System.currentTimeMillis();
            SoundManager.getInstance().playSound("move");
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
                speed = Math.min(speed + 1, 5); // Maximum vitesse 5
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
                speed = Math.max(1, speed - 1);
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
        lives--;
        if (lives <= 0) {
            lives = 0;
            SoundManager.getInstance().playSound("player_death");
        }

        // Mettre à jour les statistiques du profil
        if (profile != null && !isAlive()) {
            // La défaite sera enregistrée par le gestionnaire de jeu
        }
    }

    /**
     * Vérifie si le joueur est vivant
     */
    public boolean isAlive() {
        return lives > 0;
    }

    /**
     * Remet le joueur à son état initial (nouvelle partie)
     */
    public void reset(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.lives = 3;
        this.maxBombs = 1;
        this.currentBombs = 0;
        this.bombRange = 2;
        this.speed = 1;
        this.canKickBombs = false;
        this.hasSkull = false;
        this.skullEndTime = 0;
        this.lastMoveTime = 0;
    }

    /**
     * Retourne une description des power-ups actifs
     */
    public String getPowerUpDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("Bombes: ").append(maxBombs);
        desc.append(" | Portée: ").append(bombRange);
        desc.append(" | Vitesse: ").append(speed);
        if (canKickBombs) desc.append(" | Kick");
        if (hasSkull) desc.append(" | SKULL");
        return desc.toString();
    }

    // Getters et Setters
    public int getX() { return x; }
    public int getY() { return y; }

    /**
     * Méthode pour forcer le changement de position (sans délai)
     */
    public void forcePosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

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

    public PlayerProfile getProfile() { return profile; }
    public void setProfile(PlayerProfile profile) {
        this.profile = profile;
        System.out.println("Profil assigné au joueur " + playerId + ": " +
                (profile != null ? profile.getFullName() : "Aucun"));
    }
}