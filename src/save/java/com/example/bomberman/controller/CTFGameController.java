package com.example.bomberman.controller;

import com.example.bomberman.models.game.CTFGame;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

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
     * Initialise les composants après le chargement du FXML
     */
    public void initialize() {
        System.out.println("CTFGameController: Méthode initialize appelée");
        
        // Configurer les gestionnaires d'événements clavier
        setupKeyboardShortcuts();
        
        // Initialiser le jeu
        initializeGame();
    }
    
    /**
     * Configure les raccourcis clavier pour remplacer les boutons
     */
    private void setupKeyboardShortcuts() {
        // Les raccourcis seront gérés dans handleKeyPressed
        System.out.println("CTFGameController: Raccourcis clavier configurés");
    }
    
    /**
     * Gestion des touches pressées avec les raccourcis
     */
    @Override
    public void handleKeyPressed(KeyEvent event) {
        super.handleKeyPressed(event);
        
        // Raccourcis spécifiques au mode CTF
        if (event.getCode() == KeyCode.ESCAPE) {
            // Pause/Reprendre avec Échap
            if (isPaused) {
                resumeGame();
            } else {
                pauseGame();
            }
            event.consume();
        } else if (event.getCode() == KeyCode.R) {
            // Recommencer avec R
            handleRestartGame();
            event.consume();
        } else if (event.getCode() == KeyCode.Q && event.isControlDown()) {
            // Quitter avec Ctrl+Q
            handleQuitGame();
            event.consume();
        }
    }
    
    /**
     * Met le jeu en pause
     */
    private void pauseGame() {
        if (!isPaused) {
            isPaused = true;
            pauseStartTime = System.currentTimeMillis();
            System.out.println("CTFGameController: Jeu mis en pause");
            
            // Afficher une indication de pause
            showAlert("Pause", "Jeu en pause. Appuyez sur Échap pour continuer.", Alert.AlertType.INFORMATION);
        }
    }
    
    /**
     * Reprend le jeu après une pause
     */
    private void resumeGame() {
        if (isPaused) {
            isPaused = false;
            totalPauseTime += System.currentTimeMillis() - pauseStartTime;
            System.out.println("CTFGameController: Jeu repris");
        }
    }
    
    /**
     * Gère le redémarrage du jeu (touche R)
     */
    private void handleRestartGame() {
        System.out.println("CTFGameController: Redémarrage demandé");
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Recommencer");
        confirmAlert.setHeaderText("Voulez-vous vraiment recommencer la partie ?");
        confirmAlert.setContentText("Toute progression sera perdue.");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            initializeGame();
            System.out.println("CTFGameController: Jeu redémarré");
        }
    }
    
    /**
     * Gère la sortie du jeu (Ctrl+Q)
     */
    private void handleQuitGame() {
        System.out.println("CTFGameController: Sortie demandée");
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Quitter");
        confirmAlert.setHeaderText("Voulez-vous vraiment quitter la partie ?");
        confirmAlert.setContentText("Toute progression sera perdue.");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Arrêter la musique
                soundManager.stopBackgroundMusic();
                
                // Charger le menu principal
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/view/menu-view.fxml"));
                Parent menuRoot = loader.load();
                
                // Changer de scène en utilisant getGraphicsContext() qui est accessible
                Stage stage = (Stage) getGraphicsContext().getCanvas().getScene().getWindow();
                Scene menuScene = new Scene(menuRoot, 800, 600);
                
                // Charger le CSS s'il existe
                try {
                    URL cssResource = getClass().getResource("/com/example/bomberman/styles.css");
                    if (cssResource != null) {
                        menuScene.getStylesheets().add(cssResource.toExternalForm());
                    }
                } catch (Exception cssError) {
                    System.out.println("CSS non trouvé, continuation sans styles");
                }
                
                stage.setScene(menuScene);
                stage.setTitle("Super Bomberman");
                
                System.out.println("CTFGameController: Retour au menu principal");
                
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de charger le menu principal: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
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
            "• Le premier joueur à atteindre 3 points gagne\n\n" +
            "Raccourcis clavier:\n" +
            "• Échap: Pause/Reprendre\n" +
            "• R: Recommencer la partie\n" +
            "• Ctrl+Q: Quitter le jeu";
        
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