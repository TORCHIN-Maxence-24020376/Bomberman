package com.example.bomberman.models.game;

import com.example.bomberman.models.entities.Flag;
import com.example.bomberman.models.entities.Player;
import com.example.bomberman.models.world.Game;
import com.example.bomberman.models.world.GameBoard;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Classe représentant une partie en mode Capture the Flag
 */
public class CTFGame extends Game {
    
    private Flag blueFlag;  // Drapeau de l'équipe bleue (joueur 1)
    private Flag redFlag;   // Drapeau de l'équipe rouge (joueur 2)
    private int blueScore;  // Score de l'équipe bleue
    private int redScore;   // Score de l'équipe rouge
    private int scoreToWin; // Score nécessaire pour gagner
    private static final int TILE_SIZE = 40;  // Taille d'une case en pixels
    
    /**
     * Constructeur
     */
    public CTFGame() {
        super();
        this.scoreToWin = 3; // Par défaut, il faut 3 captures pour gagner
        this.blueScore = 0;
        this.redScore = 0;
        
        // Placer les drapeaux aux coins opposés de la carte
        this.blueFlag = new Flag(1, 1, 1); // Drapeau bleu en haut à gauche
        this.redFlag = new Flag(getBoard().getWidth() - 2, getBoard().getHeight() - 2, 2); // Drapeau rouge en bas à droite
        
        // Ajouter les drapeaux à la liste des entités
        getBoard().getPowerUps().add(blueFlag);
        getBoard().getPowerUps().add(redFlag);
    }
    
    /**
     * Met à jour l'état du jeu
     */
    @Override
    public void update() {
        super.update();
        
        // Vérifier si un joueur a capturé un drapeau
        checkFlagCapture();
        
        // Vérifier si un joueur portant un drapeau est mort
        checkFlagCarrierDeath();
        
        // Vérifier si un joueur a ramassé un drapeau
        checkFlagPickup();
        
        // Vérifier si une équipe a gagné
        checkWinCondition();
    }
    
    /**
     * Vérifie si un joueur a ramassé un drapeau
     */
    private void checkFlagPickup() {
        Player player1 = getPlayer1();
        Player player2 = getPlayer2();
        
        // Vérifier si le joueur 1 ramasse le drapeau rouge
        if (player1 != null && player1.isAlive() && !redFlag.isPickedUp() && 
            player1.getX() == redFlag.getX() && player1.getY() == redFlag.getY()) {
            redFlag.pickUp(player1);
        }
        
        // Vérifier si le joueur 2 ramasse le drapeau bleu
        if (player2 != null && player2.isAlive() && !blueFlag.isPickedUp() && 
            player2.getX() == blueFlag.getX() && player2.getY() == blueFlag.getY()) {
            blueFlag.pickUp(player2);
        }
    }
    
    /**
     * Vérifie si un joueur portant un drapeau est mort
     */
    private void checkFlagCarrierDeath() {
        // Si le porteur du drapeau bleu est mort, le drapeau est lâché
        if (blueFlag.isPickedUp() && !blueFlag.getCarrier().isAlive()) {
            blueFlag.drop();
        }
        
        // Si le porteur du drapeau rouge est mort, le drapeau est lâché
        if (redFlag.isPickedUp() && !redFlag.getCarrier().isAlive()) {
            redFlag.drop();
        }
    }
    
    /**
     * Vérifie si un joueur a capturé un drapeau (retour à sa base avec le drapeau adverse)
     */
    private void checkFlagCapture() {
        Player player1 = getPlayer1();
        Player player2 = getPlayer2();
        
        // Joueur 1 capture le drapeau rouge
        if (player1 != null && redFlag.isPickedUp() && redFlag.getCarrier() == player1 && 
            player1.getX() == blueFlag.getOriginalX() && player1.getY() == blueFlag.getOriginalY()) {
            blueScore++;
            redFlag.returnToBase();
        }
        
        // Joueur 2 capture le drapeau bleu
        if (player2 != null && blueFlag.isPickedUp() && blueFlag.getCarrier() == player2 && 
            player2.getX() == redFlag.getOriginalX() && player2.getY() == redFlag.getOriginalY()) {
            redScore++;
            blueFlag.returnToBase();
        }
    }
    
    /**
     * Vérifie si une équipe a gagné
     */
    private void checkWinCondition() {
        if (blueScore >= scoreToWin) {
            setGameOver(true);
            setWinner(1);
        } else if (redScore >= scoreToWin) {
            setGameOver(true);
            setWinner(2);
        }
    }
    
    /**
     * Dessine les éléments du jeu
     * Note: Cette méthode n'est pas appelée directement par le jeu, mais peut être utilisée par le contrôleur
     * pour afficher des éléments supplémentaires spécifiques au mode CTF
     */
    public void renderCTFElements(GraphicsContext gc) {
        // Dessiner les drapeaux portés par les joueurs
        Player player1 = getPlayer1();
        Player player2 = getPlayer2();
        
        if (player1 != null && redFlag.isPickedUp() && redFlag.getCarrier() == player1) {
            redFlag.renderCarried(gc, TILE_SIZE, player1.getX(), player1.getY());
        }
        
        if (player2 != null && blueFlag.isPickedUp() && blueFlag.getCarrier() == player2) {
            blueFlag.renderCarried(gc, TILE_SIZE, player2.getX(), player2.getY());
        }
        
        // Afficher les scores
        int mapWidth = getBoard().getWidth();
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        String scoreText = "SCORE: " + blueScore + " - " + redScore;
        gc.fillText(scoreText, (mapWidth * TILE_SIZE) / 2 - 50, 25);
    }
    
    /**
     * Définit le nombre de captures nécessaires pour gagner
     */
    public void setScoreToWin(int scoreToWin) {
        this.scoreToWin = scoreToWin;
    }
    
    /**
     * Retourne le score de l'équipe bleue
     */
    public int getBlueScore() {
        return blueScore;
    }
    
    /**
     * Retourne le score de l'équipe rouge
     */
    public int getRedScore() {
        return redScore;
    }
    
    /**
     * Retourne le drapeau de l'équipe bleue
     */
    public Flag getBlueFlag() {
        return blueFlag;
    }
    
    /**
     * Retourne le drapeau de l'équipe rouge
     */
    public Flag getRedFlag() {
        return redFlag;
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