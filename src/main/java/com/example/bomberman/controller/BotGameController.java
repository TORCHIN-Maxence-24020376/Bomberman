package com.example.bomberman.controller;

import com.example.bomberman.models.entities.Player;
import com.example.bomberman.models.world.BotGame;
import com.example.bomberman.models.world.Game;
import com.example.bomberman.service.SoundManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur pour le mode VS IA
 */
public class BotGameController extends GameController {
    
    // Labels spécifiques au mode Bot
    @FXML private Label difficultyLabel;
    
    private BotGame botGame;
    private int difficultyLevel = 2; // Par défaut: niveau moyen
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        System.out.println("BotGameController: Initialisation");
    }
    
    /**
     * Remplace l'initialisation standard par une avec un bot
     */
    @Override
    protected void initializeGame() {
        // Créer une instance de BotGame
        botGame = new BotGame(difficultyLevel);
        super.setGame(botGame);
        
        // Continuer avec le reste de l'initialisation
        super.initializeGame();
        
        // Afficher les instructions
        showInstructions();
    }
    
    /**
     * Gestion des touches spécifiques au mode Bot
     */
    @Override
    public void handleKeyPressed(KeyEvent event) {
        // Appel à la méthode parent
        super.handleKeyPressed(event);
        
        // Raccourcis spécifiques au mode Bot
        if (event.getCode() == KeyCode.B && event.isControlDown()) {
            showBotInfo();
            event.consume();
        } else if (event.getCode() == KeyCode.F1) {
            showHelp();
            event.consume();
        }
    }
    
    /**
     * Définit le niveau de difficulté du bot
     */
    public void setDifficultyLevel(int level) {
        this.difficultyLevel = Math.max(1, Math.min(3, level));
        if (botGame != null) {
            botGame.setDifficultyLevel(difficultyLevel);
        }
    }
    
    /**
     * Affiche les informations sur le bot
     */
    private void showBotInfo() {
        if (botGame == null) return;
        
        String difficultyText;
        switch (botGame.getDifficultyLevel()) {
            case 1: difficultyText = "Facile"; break;
            case 2: difficultyText = "Moyen"; break;
            case 3: difficultyText = "Difficile"; break;
            default: difficultyText = "Inconnu";
        }
        
        String message = 
            "Informations sur le Bot\n\n" +
            "Niveau de difficulté: " + difficultyText + "\n" +
            "Position: (" + botGame.getBotPlayer().getX() + ", " + botGame.getBotPlayer().getY() + ")\n" +
            "Bombes disponibles: " + (botGame.getBotPlayer().getMaxBombs() - botGame.getBotPlayer().getCurrentBombs()) + "\n" +
            "Vies restantes: " + botGame.getBotPlayer().getLives() + "\n" +
            "Portée des bombes: " + botGame.getBotPlayer().getBombRange();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bot Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Affiche l'aide du mode Bot
     */
    private void showHelp() {
        String help = 
            "Mode VS IA - Aide\n\n" +
            "Contrôles:\n" +
            "• ZQSD: Déplacer le joueur\n" +
            "• A: Placer une bombe\n" +
            "• Échap: Pause/Menu\n" +
            "• Ctrl+B: Informations sur le bot\n" +
            "• F1: Afficher cette aide\n\n" +
            "Objectif:\n" +
            "Éliminez le bot en plaçant des bombes stratégiquement.\n" +
            "Collectez des power-ups pour améliorer vos capacités.\n\n" +
            "Difficultés:\n" +
            "• Facile: Le bot se déplace aléatoirement\n" +
            "• Moyen: Le bot se déplace vers vous occasionnellement\n" +
            "• Difficile: Le bot vous traque et pose des bombes intelligemment";
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Aide");
        alert.setHeaderText(null);
        alert.setContentText(help);
        alert.showAndWait();
    }
    
    /**
     * Affiche les instructions du mode Bot
     */
    private void showInstructions() {
        String instructions = 
            "Mode VS IA\n\n" +
            "Bienvenue dans le mode VS IA !\n\n" +
            "Vous affrontez un bot avec intelligence artificielle.\n" +
            "Collectez des power-ups et utilisez des stratégies pour vaincre votre adversaire robot.\n\n" +
            "Appuyez sur F1 à tout moment pour afficher l'aide.\n" +
            "Appuyez sur Ctrl+B pour voir les informations sur le bot.";
        
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Mode VS IA");
            alert.setHeaderText(null);
            alert.setContentText(instructions);
            alert.showAndWait();
        });
    }
} 