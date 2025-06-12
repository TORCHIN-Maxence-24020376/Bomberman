package com.example.bomberman.controller;

import com.example.bomberman.service.SoundManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur simplifié pour le menu principal
 */
public class MainMenuController implements Initializable {

    @FXML private Button playButton;
    @FXML private Button settingsButton;
    @FXML private Button levelEditorButton;
    @FXML private Button quitButton;
    @FXML private StackPane rootPane;  // Référence à l'élément racine StackPane

    private SoundManager soundManager;
    private boolean isInitialized = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (!isInitialized) {
            soundManager = SoundManager.getInstance();
            initializeComponents();
            setupBackground();
            startMenuMusic();
            isInitialized = true;
        }
    }

    /**
     * Initialise les composants de l'interface
     */
    private void initializeComponents() {
        // Configuration des boutons
        playButton.setOnAction(e -> startGame());
        settingsButton.setOnAction(e -> openSettings());
        levelEditorButton.setOnAction(e -> openLevelEditor());
        quitButton.setOnAction(e -> quitGame());
    }

    /**
     * Configure l'image de fond
     */
    private void setupBackground() {
        try {
            if (rootPane != null) {
                String imagePath = "/com/example/bomberman/Images/coocked_question_mark.png";
                URL imageUrl = getClass().getResource(imagePath);
                
                if (imageUrl != null) {
                    rootPane.setStyle("-fx-background-image: url('" + imagePath + "'); " +
                                    "-fx-background-size: cover; " +
                                    "-fx-background-position: center center;");
                } else {
                    System.err.println("Image non trouvée à : " + imagePath);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration de l'arrière-plan : " + e.getMessage());
        }
    }

    /**
     * Démarre la musique du menu
     */
    private void startMenuMusic() {
        if (soundManager.isMusicEnabled()) {
            soundManager.playBackgroundMusic("/sounds/menu_music.mp3");
        }
    }

    /**
     * Démarre une nouvelle partie
     */
    @FXML
    private void startGame() {
        try {
            soundManager.stopBackgroundMusic();

            // Charger la scène de jeu existante
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/view/game-view.fxml"));
            Parent gameRoot = loader.load();

            // Changer de scène
            Stage stage = (Stage) playButton.getScene().getWindow();
            Scene gameScene = new Scene(gameRoot, 800, 600);

            // Charger le CSS s'il existe
            try {
                var cssResource = getClass().getResource("/com/example/bomberman/style.css");
                if (cssResource != null) {
                    gameScene.getStylesheets().add(cssResource.toExternalForm());
                }
            } catch (Exception cssError) {
                System.out.println("CSS non trouvé, continuation sans styles");
                cssError.printStackTrace();
            }

            stage.setScene(gameScene);
            stage.setTitle("Super Bomberman - En jeu");

            System.out.println("Jeu lancé avec succès !");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le jeu.\nUtilisation du jeu de base.", Alert.AlertType.WARNING);

            // Fallback : lancer le jeu directement sans menu
            launchBasicGame();
        }
    }

    /**
     * Lance le jeu de base en cas d'erreur
     */
    private void launchBasicGame() {
        try {
            // Créer une nouvelle fenêtre avec le jeu original
            Stage gameStage = new Stage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/view/game-view.fxml"));
            Parent root = loader.load();

            Scene gameScene = new Scene(root, 800, 600);
            gameStage.setScene(gameScene);
            gameStage.setTitle("Super Bomberman");
            gameStage.show();

            // Fermer la fenêtre du menu
            Stage currentStage = (Stage) playButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception fallbackError) {
            showAlert("Erreur critique", "Impossible de lancer le jeu : " + fallbackError.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Ouvre les paramètres
     */
    @FXML
    private void openSettings() {
        try {
            // Créer une nouvelle fenêtre pour les paramètres
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/view/settings-view.fxml"));
            Parent settingsRoot = loader.load();
            
            // Créer une nouvelle scène
            Scene settingsScene = new Scene(settingsRoot, 600, 500);
            
            // Appliquer le CSS si disponible
            try {
                var cssResource = getClass().getResource("/com/example/bomberman/style.css");
                if (cssResource != null) {
                    settingsScene.getStylesheets().add(cssResource.toExternalForm());
                }
            } catch (Exception cssError) {
                System.out.println("CSS non trouvé pour les paramètres, continuation sans styles");
                cssError.printStackTrace();
            }
            
            // Créer une nouvelle fenêtre
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Paramètres - Super Bomberman");
            settingsStage.setScene(settingsScene);
            settingsStage.setResizable(false);
            
            // Afficher la fenêtre modale (bloque l'interaction avec la fenêtre principale)
            settingsStage.initOwner((Stage) settingsButton.getScene().getWindow());
            settingsStage.showAndWait();
            
            // Rafraîchir les paramètres audio après fermeture
            soundManager.playBackgroundMusic("/sounds/menu_music.mp3");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'interface des paramètres.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Ouvre l'éditeur de niveau
     */
    @FXML
    private void openLevelEditor() {
        try {
            soundManager.stopBackgroundMusic();

            // Charger la scène de l'éditeur de niveau
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/view/level-editor-view.fxml"));
            Parent editorRoot = loader.load();

            // Changer de scène
            Stage stage = (Stage) levelEditorButton.getScene().getWindow();
            Scene editorScene = new Scene(editorRoot, 800, 600);

            // Charger le CSS s'il existe
            try {
                var cssResource = getClass().getResource("/com/example/bomberman/style.css");
                if (cssResource != null) {
                    editorScene.getStylesheets().add(cssResource.toExternalForm());
                }
            } catch (Exception cssError) {
                System.out.println("CSS non trouvé, continuation sans styles");
                cssError.printStackTrace();
            }

            stage.setScene(editorScene);
            stage.setTitle("Super Bomberman - Éditeur de niveaux");

            System.out.println("Éditeur de niveaux lancé avec succès !");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'éditeur de niveaux.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Quitte le jeu
     */
    @FXML
    private void quitGame() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Quitter");
        confirmAlert.setHeaderText("Voulez-vous vraiment quitter le jeu ?");
        confirmAlert.setContentText("Toute progression non sauvegardée sera perdue.");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Platform.exit();
        }
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Retourne au menu principal depuis une autre vue
     */
    public void returnToMenu() {
        // Réinitialiser l'état du menu
        isInitialized = false;
        
        // Arrêter toute musique en cours
        soundManager.stopBackgroundMusic();
        
        // Réinitialiser les composants
        initializeComponents();
        setupBackground();
        startMenuMusic();
        
        isInitialized = true;
    }
}