package com.example.bomberman.models.game;

import com.example.bomberman.models.entities.Bomb;
import com.example.bomberman.models.entities.Flag;
import com.example.bomberman.models.entities.Player;
import com.example.bomberman.models.world.Game;
import com.example.bomberman.models.world.GameBoard;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * Classe représentant une partie en mode Capture the Flag
 * Basé sur les règles de Super Bomberman 4
 */
public class CTFGame extends Game {
    
    private Flag player1Flag;  // Drapeau du joueur 1
    private Flag player2Flag;  // Drapeau du joueur 2
    private int player1Score;  // Score du joueur 1
    private int player2Score;  // Score du joueur 2
    private int scoreToWin;    // Score nécessaire pour gagner
    private static final int TILE_SIZE = 40;  // Taille d'une case en pixels
    private boolean canPlaceBombsOnFlags; // Indique si on peut placer des bombes sur les drapeaux
    
    /**
     * Constructeur
     */
    public CTFGame() {
        super();
        this.scoreToWin = 3; // Par défaut, il faut 3 points pour gagner
        this.player1Score = 0;
        this.player2Score = 0;
        this.canPlaceBombsOnFlags = false; // Selon Super Bomberman 4, on ne peut pas placer de bombes sur les drapeaux
        
        // Créer les drapeaux pour chaque joueur
        initializeFlags();
    }
    
    /**
     * Initialise les drapeaux pour chaque joueur
     */
    private void initializeFlags() {
        // Placer les drapeaux près de chaque joueur
        Player player1 = getPlayer1();
        Player player2 = getPlayer2();
        
        if (player1 != null && player2 != null) {
            // Créer le drapeau du joueur 1 (bleu)
            this.player1Flag = new Flag(player1.getX() + 1, player1.getY(), 1);
            player1Flag.setOwner(player1);
            
            // Créer le drapeau du joueur 2 (rouge)
            this.player2Flag = new Flag(player2.getX() - 1, player2.getY(), 2);
            player2Flag.setOwner(player2);
            
            // Ajouter les drapeaux à la liste des entités
            getBoard().getPowerUps().add(player1Flag);
            getBoard().getPowerUps().add(player2Flag);
        }
    }
    
    /**
     * Met à jour l'état du jeu
     */
    @Override
    public void update() {
        super.update();
        
        // Vérifier si un drapeau a été touché par une explosion
        checkFlagsInExplosion();
        
        // Vérifier si un joueur portant un drapeau est mort
        checkFlagCarrierDeath();
        
        // Vérifier si un joueur a ramassé un drapeau
        checkFlagPickup();
        
        // Vérifier si un joueur a posé un drapeau
        checkFlagDrop();
        
        // Vérifier si une équipe a gagné
        checkWinCondition();
    }
    
    /**
     * Vérifie si un joueur a ramassé un drapeau
     */
    private void checkFlagPickup() {
        Player player1 = getPlayer1();
        Player player2 = getPlayer2();
        
        // Vérifier si le joueur 1 ramasse un drapeau
        if (player1 != null && player1.isAlive()) {
            // Vérifier pour le drapeau du joueur 2
            if (!player2Flag.isPickedUp() && !player2Flag.isDestroyed() && 
                player1.getX() == player2Flag.getX() && player1.getY() == player2Flag.getY()) {
                player2Flag.pickUp(player1);
            }
        }
        
        // Vérifier si le joueur 2 ramasse un drapeau
        if (player2 != null && player2.isAlive()) {
            // Vérifier pour le drapeau du joueur 1
            if (!player1Flag.isPickedUp() && !player1Flag.isDestroyed() && 
                player2.getX() == player1Flag.getX() && player2.getY() == player1Flag.getY()) {
                player1Flag.pickUp(player2);
            }
        }
    }
    
    /**
     * Vérifie si un joueur portant un drapeau est mort
     */
    private void checkFlagCarrierDeath() {
        // Si le porteur d'un drapeau est mort, le drapeau est lâché
        if (player1Flag.isPickedUp() && !player1Flag.getCarrier().isAlive()) {
            player1Flag.drop();
        }
        
        if (player2Flag.isPickedUp() && !player2Flag.getCarrier().isAlive()) {
            player2Flag.drop();
        }
    }
    
    /**
     * Vérifie si un joueur a posé un drapeau (touche Y dans Super Bomberman 4)
     * Dans notre cas, on simule cela quand un joueur place une bombe
     */
    private void checkFlagDrop() {
        // Si un joueur place une bombe, il pose le drapeau qu'il porte
        Player player1 = getPlayer1();
        Player player2 = getPlayer2();
        
        // Vérifier si le joueur 1 porte un drapeau et place une bombe
        if (player1 != null && (player1Flag.isPickedUp() && player1Flag.getCarrier() == player1 || 
                               player2Flag.isPickedUp() && player2Flag.getCarrier() == player1)) {
            // Si le joueur 1 a placé une bombe récemment, lâcher le drapeau
            if (player1.getCurrentBombs() > 0) {
                if (player1Flag.isPickedUp() && player1Flag.getCarrier() == player1) {
                    player1Flag.drop();
                }
                if (player2Flag.isPickedUp() && player2Flag.getCarrier() == player1) {
                    player2Flag.drop();
                }
            }
        }
        
        // Vérifier si le joueur 2 porte un drapeau et place une bombe
        if (player2 != null && (player1Flag.isPickedUp() && player1Flag.getCarrier() == player2 || 
                               player2Flag.isPickedUp() && player2Flag.getCarrier() == player2)) {
            // Si le joueur 2 a placé une bombe récemment, lâcher le drapeau
            if (player2.getCurrentBombs() > 0) {
                if (player1Flag.isPickedUp() && player1Flag.getCarrier() == player2) {
                    player1Flag.drop();
                }
                if (player2Flag.isPickedUp() && player2Flag.getCarrier() == player2) {
                    player2Flag.drop();
                }
            }
        }
    }
    
    /**
     * Vérifie si un drapeau a été touché par une explosion
     * Dans Super Bomberman 4, si un drapeau est détruit, son propriétaire est éliminé
     */
    private void checkFlagsInExplosion() {
        GameBoard board = getBoard();
        
        // Vérifier si le drapeau du joueur 1 est dans une explosion
        if (!player1Flag.isDestroyed() && board.isExplosion(player1Flag.getX(), player1Flag.getY())) {
            if (player1Flag.destroy()) {
                // Éliminer le propriétaire du drapeau
                Player owner = player1Flag.getOwner();
                if (owner != null && owner.isAlive()) {
                    owner.takeDamage();
                    player2Score++; // Le joueur 2 marque un point
                }
            }
        }
        
        // Vérifier si le drapeau du joueur 2 est dans une explosion
        if (!player2Flag.isDestroyed() && board.isExplosion(player2Flag.getX(), player2Flag.getY())) {
            if (player2Flag.destroy()) {
                // Éliminer le propriétaire du drapeau
                Player owner = player2Flag.getOwner();
                if (owner != null && owner.isAlive()) {
                    owner.takeDamage();
                    player1Score++; // Le joueur 1 marque un point
                }
            }
        }
    }
    
    /**
     * Vérifie si un joueur est éliminé
     * Si un joueur est éliminé, son drapeau est également détruit
     */
    private void checkPlayerElimination() {
        Player player1 = getPlayer1();
        Player player2 = getPlayer2();
        
        // Si le joueur 1 est éliminé, son drapeau est détruit
        if (player1 != null && !player1.isAlive() && !player1Flag.isDestroyed()) {
            player1Flag.destroy();
            player2Score++; // Le joueur 2 marque un point
        }
        
        // Si le joueur 2 est éliminé, son drapeau est détruit
        if (player2 != null && !player2.isAlive() && !player2Flag.isDestroyed()) {
            player2Flag.destroy();
            player1Score++; // Le joueur 1 marque un point
        }
    }
    
    /**
     * Vérifie si une équipe a gagné
     */
    private void checkWinCondition() {
        // Vérifier d'abord si un joueur est éliminé
        checkPlayerElimination();
        
        // Vérifier si un joueur a atteint le score nécessaire pour gagner
        if (player1Score >= scoreToWin) {
            System.out.println("Joueur 1 gagne!");
            // Fin de la partie
        } else if (player2Score >= scoreToWin) {
            System.out.println("Joueur 2 gagne!");
            // Fin de la partie
        }
    }
    
    /**
     * Vérifie si on peut placer une bombe à une position donnée
     * Dans Super Bomberman 4, on ne peut pas placer de bombes sur les drapeaux
     */
    public boolean isBombPlacementAllowed(int x, int y) {
        // Vérifier si la position est occupée par un drapeau
        if (!canPlaceBombsOnFlags && 
            ((player1Flag != null && x == player1Flag.getX() && y == player1Flag.getY() && !player1Flag.isPickedUp()) ||
             (player2Flag != null && x == player2Flag.getX() && y == player2Flag.getY() && !player2Flag.isPickedUp()))) {
            return false;
        }
        
        // Sinon, la position est valide pour placer une bombe
        return true;
    }
    
    /**
     * Dessine les éléments du jeu spécifiques au mode CTF
     */
    public void renderCTFElements(GraphicsContext gc) {
        // Dessiner les drapeaux portés par les joueurs
        Player player1 = getPlayer1();
        Player player2 = getPlayer2();
        
        if (player1 != null && player2Flag.isPickedUp() && player2Flag.getCarrier() == player1) {
            player2Flag.renderCarried(gc, TILE_SIZE, player1.getX(), player1.getY());
        }
        
        if (player2 != null && player1Flag.isPickedUp() && player1Flag.getCarrier() == player2) {
            player1Flag.renderCarried(gc, TILE_SIZE, player2.getX(), player2.getY());
        }
        
        // Afficher les scores
        int mapWidth = getBoard().getWidth();
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        String scoreText = "SCORE: " + player1Score + " - " + player2Score;
        gc.fillText(scoreText, (mapWidth * TILE_SIZE) / 2 - 50, 25);
        
        // Afficher des instructions sur le mode de jeu
        gc.setFont(Font.font("Arial", 14));
        gc.fillText("Capture the Flag: Détruisez le drapeau adverse ou protégez le vôtre!", 20, getBoard().getHeight() * TILE_SIZE - 20);
    }
    
    /**
     * Définit le nombre de points nécessaires pour gagner
     */
    public void setScoreToWin(int scoreToWin) {
        this.scoreToWin = scoreToWin;
    }
    
    /**
     * Retourne le score du joueur 1
     */
    public int getBlueScore() {
        return player1Score;
    }
    
    /**
     * Retourne le score du joueur 2
     */
    public int getRedScore() {
        return player2Score;
    }
    
    /**
     * Retourne le drapeau du joueur 1
     */
    public Flag getBlueFlag() {
        return player1Flag;
    }
    
    /**
     * Retourne le drapeau du joueur 2
     */
    public Flag getRedFlag() {
        return player2Flag;
    }
    
    /**
     * Définit si on peut placer des bombes sur les drapeaux
     */
    public void setCanPlaceBombsOnFlags(boolean canPlaceBombsOnFlags) {
        this.canPlaceBombsOnFlags = canPlaceBombsOnFlags;
    }
    
    /**
     * Définit le gagnant de la partie
     */
    private void setWinner(int playerId) {
        System.out.println("Joueur " + playerId + " gagne la partie CTF!");
    }
    
    /**
     * Définit si la partie est terminée
     */
    private void setGameOver(boolean gameOver) {
        // La classe parent Game n'a pas de méthode setGameRunning, donc on utilise une approche différente
        if (gameOver) {
            System.out.println("Partie CTF terminée");
        }
    }
} 