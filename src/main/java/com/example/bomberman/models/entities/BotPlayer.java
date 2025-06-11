package com.example.bomberman.models.entities;

import com.example.bomberman.models.world.GameBoard;
import javafx.scene.paint.Color;

import java.util.Random;

/**
 * Classe représentant un joueur contrôlé par l'IA
 */
public class BotPlayer extends Player {
    
    private static final int[] DX = {0, 1, 0, -1, 0}; // Directions possibles en X (0 = ne pas bouger)
    private static final int[] DY = {-1, 0, 1, 0, 0}; // Directions possibles en Y (0 = ne pas bouger)
    
    private Random random;
    private int moveCounter;
    private int directionChangeInterval;
    private int currentDirection;
    private int targetX;
    private int targetY;
    private boolean shouldPlaceBomb;
    private int bombCooldown;
    private int difficultyLevel;
    
    /**
     * Constructeur
     * @param x Position X initiale
     * @param y Position Y initiale
     * @param color Couleur du bot
     * @param playerId ID du joueur (1 ou 2)
     * @param difficultyLevel Niveau de difficulté (1-3)
     */
    public BotPlayer(int x, int y, Color color, int playerId, int difficultyLevel) {
        super(x, y, color, playerId);
        this.random = new Random();
        this.moveCounter = 0;
        this.directionChangeInterval = 10;
        this.currentDirection = random.nextInt(4);
        this.targetX = -1;
        this.targetY = -1;
        this.shouldPlaceBomb = false;
        this.bombCooldown = 0;
        this.difficultyLevel = Math.max(1, Math.min(3, difficultyLevel));
    }
    
    /**
     * Met à jour la logique du bot
     */
    @Override
    public void update() {
        super.update();
        
        // Gérer le cooldown des bombes
        if (bombCooldown > 0) {
            bombCooldown--;
        }
    }
    
    /**
     * Met à jour le comportement du bot en fonction du plateau et du joueur cible
     * @param board Le plateau de jeu
     * @param targetPlayer Le joueur cible
     */
    public void updateBot(GameBoard board, Player targetPlayer) {
        moveCounter++;
        
        // Décider si on change de direction
        if (moveCounter >= directionChangeInterval) {
            moveCounter = 0;
            
            // Décider de la stratégie en fonction de la difficulté
            switch (difficultyLevel) {
                case 1:
                    // Facile: Mouvements aléatoires avec quelques bombes
                    randomMovement();
                    shouldPlaceBomb = random.nextInt(20) == 0; // 5% de chance de poser une bombe
                    break;
                    
                case 2:
                    // Moyen: Se déplace vers le joueur ou aléatoirement
                    if (random.nextInt(3) == 0) { // 33% de chance de se déplacer aléatoirement
                        randomMovement();
                    } else {
                        moveTowardsPlayer(targetPlayer);
                    }
                    shouldPlaceBomb = random.nextInt(10) == 0; // 10% de chance de poser une bombe
                    break;
                    
                case 3:
                    // Difficile: Traque le joueur et pose des bombes intelligemment
                    moveTowardsPlayer(targetPlayer);
                    
                    // Poser une bombe si on est près du joueur
                    int distanceToPlayer = Math.abs(getX() - targetPlayer.getX()) + Math.abs(getY() - targetPlayer.getY());
                    shouldPlaceBomb = (distanceToPlayer <= 2) && (random.nextInt(5) == 0); // 20% de chance si proche
                    
                    // Éviter de se bloquer
                    if (isTrappedByWalls(board)) {
                        shouldPlaceBomb = true;
                    }
                    break;
            }
            
            // Limiter la fréquence des bombes
            if (shouldPlaceBomb && bombCooldown > 0) {
                shouldPlaceBomb = false;
            }
        }
    }
    
    /**
     * Déplace le bot dans sa direction actuelle
     * @param board Le plateau de jeu
     * @return true si le déplacement a été effectué
     */
    public boolean moveInCurrentDirection(GameBoard board) {
        int dx = DX[currentDirection];
        int dy = DY[currentDirection];
        
        boolean moved = move(dx, dy, board);
        
        // Si on ne peut pas se déplacer, changer de direction
        if (!moved && currentDirection < 4) { // Ne pas changer si on a choisi de ne pas bouger
            currentDirection = random.nextInt(4);
            moveCounter = directionChangeInterval - 2; // Pour rechanger rapidement
        }
        
        return moved;
    }
    
    /**
     * Stratégie de mouvement aléatoire
     */
    private void randomMovement() {
        // 80% de chance de se déplacer, 20% de rester immobile
        if (random.nextInt(5) == 0) {
            currentDirection = 4; // Ne pas bouger
        } else {
            currentDirection = random.nextInt(4); // Direction aléatoire
        }
    }
    
    /**
     * Stratégie pour se déplacer vers le joueur
     * @param targetPlayer Le joueur cible
     */
    private void moveTowardsPlayer(Player targetPlayer) {
        int dx = targetPlayer.getX() - getX();
        int dy = targetPlayer.getY() - getY();
        
        // Choisir la direction horizontale ou verticale en fonction de la distance
        if (Math.abs(dx) > Math.abs(dy)) {
            // Mouvement horizontal prioritaire
            if (dx > 0) {
                currentDirection = 1; // Droite
            } else if (dx < 0) {
                currentDirection = 3; // Gauche
            }
        } else {
            // Mouvement vertical prioritaire
            if (dy > 0) {
                currentDirection = 2; // Bas
            } else if (dy < 0) {
                currentDirection = 0; // Haut
            }
        }
        
        // Parfois, faire un mouvement aléatoire pour éviter de rester bloqué
        if (random.nextInt(10) == 0) { // 10% de chance
            currentDirection = random.nextInt(4);
        }
    }
    
    /**
     * Vérifie si le bot est entouré de murs
     * @param board Le plateau de jeu
     * @return true si le bot est entouré de murs
     */
    private boolean isTrappedByWalls(GameBoard board) {
        int wallCount = 0;
        
        // Vérifier les 4 directions
        for (int i = 0; i < 4; i++) {
            int newX = getX() + DX[i];
            int newY = getY() + DY[i];

            if (board.isWall(newX, newY)) {
                wallCount++;
            }
        }
        
        // Si 3 ou 4 directions sont bloquées, on est piégé
        return wallCount >= 3;
    }
    
    /**
     * Indique si le bot veut poser une bombe
     * @return true si le bot veut poser une bombe
     */
    public boolean wantToPlaceBomb() {
        if (shouldPlaceBomb && bombCooldown == 0) {
            shouldPlaceBomb = false;
            bombCooldown = 20; // Attendre 20 cycles avant de pouvoir reposer une bombe
            return true;
        }
        return false;
    }
    
    /**
     * Définit le niveau de difficulté du bot
     * @param level Niveau de difficulté (1-3)
     */
    public void setDifficultyLevel(int level) {
        this.difficultyLevel = Math.max(1, Math.min(3, level));
    }
    
    /**
     * Retourne le niveau de difficulté actuel
     * @return Niveau de difficulté
     */
    public int getDifficultyLevel() {
        return difficultyLevel;
    }
}