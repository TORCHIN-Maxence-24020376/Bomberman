package com.example.bomberman.controller;

import com.example.bomberman.models.game.CTFGame;
import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

/**
 * Contrôleur spécifique pour le mode Capture the Flag
 */
public class CTFGameController extends GameController {
    
    private CTFGame ctfGame;
    
    // Référence aux labels pour les scores
    @FXML private Label ctfPlayer1ScoreLabel;
    @FXML private Label ctfPlayer2ScoreLabel;
    
    /**
     * Constructeur par défaut
     */
    public CTFGameController() {
        super();
        System.out.println("CTFGameController: Constructeur appelé");
    }
    
    /**
     * Initialise une partie en mode Capture the Flag
     */
    @Override
    protected void initializeGame() {
        try {
            System.out.println("CTFGameController: Début de l'initialisation");
            
            // Créer une instance de CTFGame au lieu de Game
            ctfGame = new CTFGame();
            System.out.println("CTFGameController: CTFGame créé");
            
            setGame(ctfGame);
            System.out.println("CTFGameController: Game défini");
            
            // Ne pas appeler super.initializeGame() car cela recréerait un jeu standard
            // Au lieu de cela, initialiser les variables comme dans la méthode parente
            gameRunning = true;
            gameStartTime = System.currentTimeMillis();
            totalPauseTime = 0;
            isPaused = false;
    
            updateUI();
            System.out.println("CTFGameController: UI mise à jour");
    
            // Jouer la musique de jeu
            if (soundManager != null) {
                soundManager.playBackgroundMusic("/sounds/game_music.mp3");
                System.out.println("CTFGameController: Musique lancée");
            } else {
                System.out.println("CTFGameController: ERREUR - soundManager est null");
            }
            
            // Afficher un message d'instructions pour le mode CTF
            showInstructions();
            System.out.println("CTFGameController: Instructions affichées");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("CTFGameController: ERREUR - " + e.getMessage());
            showAlert("Erreur", "Impossible de charger le mode Capture the Flag. " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Affiche les instructions du mode Capture the Flag
     */
    private void showInstructions() {
        String instructions = 
            "Mode Capture the Flag - Instructions\n\n" +
            "• Chaque joueur a un drapeau de sa couleur\n" +
            "• Si le drapeau est détruit par une explosion, son propriétaire est éliminé\n" +
            "• Les joueurs peuvent ramasser et porter les drapeaux adverses\n" +
            "• Poser une bombe fait lâcher le drapeau porté\n" +
            "• Marquez des points en détruisant le drapeau adverse\n" +
            "• Le premier joueur à atteindre 3 points gagne";
        
        showAlert("Mode Capture the Flag", instructions, Alert.AlertType.INFORMATION);
    }
    
    /**
     * Surcharge de la méthode render pour afficher les éléments spécifiques au mode CTF
     */
    @Override
    public void render() {
        super.render(); // Appeler la méthode render du parent
        
        // Ajouter les éléments spécifiques au mode CTF
        if (ctfGame != null) {
            GraphicsContext gc = getGraphicsContext();
            ctfGame.renderCTFElements(gc);
        }
    }
    
    /**
     * Met à jour l'interface utilisateur avec les informations du mode CTF
     */
    @Override
    protected void updateUI() {
        super.updateUI();
        
        // Mettre à jour les informations spécifiques au mode CTF
        if (ctfGame != null) {
            updateCTFInfo();
        }
    }
    
    /**
     * Met à jour les informations spécifiques au mode CTF
     */
    private void updateCTFInfo() {
        // Mettre à jour les scores CTF dans l'interface
        javafx.application.Platform.runLater(() -> {
            if (getGameStatusLabel() != null) {
                String scoreText = "Score CTF: " + ctfGame.getBlueScore() + " - " + ctfGame.getRedScore();
                getGameStatusLabel().setText(scoreText);
            }
            
            if (ctfPlayer1ScoreLabel != null) {
                ctfPlayer1ScoreLabel.setText("Score: " + ctfGame.getBlueScore());
            }
            
            if (ctfPlayer2ScoreLabel != null) {
                ctfPlayer2ScoreLabel.setText("Score: " + ctfGame.getRedScore());
            }
        });
    }
    
    /**
     * Affiche une alerte
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
} 