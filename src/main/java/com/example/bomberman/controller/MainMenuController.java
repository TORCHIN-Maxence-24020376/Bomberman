package com.example.bomberman.controller;

import com.example.bomberman.service.SoundManager;
import com.example.bomberman.service.UserPreferences;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Optional;

/**
 * Contrôleur simplifié pour le menu principal
 */
public class MainMenuController implements Initializable {

    @FXML private Button playButton;
    @FXML private Button vsBotButton;
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
            
            // Appliquer les préférences utilisateur avant de démarrer la musique
            UserPreferences userPreferences = UserPreferences.getInstance();
            userPreferences.applyPreferences();
            
            // Démarrer la musique immédiatement
            startMenuMusic();
            initializeComponents();
            setupBackground();
            isInitialized = true;
        }
    }

    /**
     * Initialise les composants de l'interface
     */
    private void initializeComponents() {
        // Configuration des boutons
        playButton.setOnAction(e -> startGame());
        if (vsBotButton != null) {
            vsBotButton.setOnAction(e -> startGameVsBot());
        }
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
                // Au lieu d'utiliser setStyle, nous allons appliquer directement la classe CSS
                // Le CSS est déjà configuré dans menu-styles.css avec l'image de fond
                rootPane.getStyleClass().add("menu-background");
                
                // S'assurer que le StackPane prend toute la taille disponible
                rootPane.setPrefWidth(Double.MAX_VALUE);
                rootPane.setPrefHeight(Double.MAX_VALUE);
                rootPane.setMaxWidth(Double.MAX_VALUE);
                rootPane.setMaxHeight(Double.MAX_VALUE);
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
            soundManager.playBackgroundMusic("menu_music");
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

            // Charger le CSS du jeu
            URL cssResource = getClass().getResource("/com/example/bomberman/view/game-styles.css");
            if (cssResource != null) {
                gameScene.getStylesheets().add(cssResource.toExternalForm());
            }

            stage.setScene(gameScene);
            stage.setTitle("Super Bomberman - En jeu");

            System.out.println("Jeu lancé avec succès !");

        } catch (IOException e) {
            e.printStackTrace();
            launchBasicGame();
        }
    }
    
    /**
     * Démarre une nouvelle partie contre un bot
     */
    @FXML
    private void startGameVsBot() {
        try {
            // Afficher une boîte de dialogue pour choisir la difficulté
            Alert difficultyDialog = new Alert(Alert.AlertType.CONFIRMATION);
            difficultyDialog.setTitle("Choisir la difficulté");
            difficultyDialog.setHeaderText("Choisissez le niveau de difficulté du bot");
            difficultyDialog.setContentText("Sélectionnez la difficulté de l'IA qui contrôlera le joueur 2.");
            
            // Personnaliser les boutons
            ButtonType easyButton = new ButtonType("Facile");
            ButtonType mediumButton = new ButtonType("Moyen");
            ButtonType hardButton = new ButtonType("Difficile");
            ButtonType cancelButton = new ButtonType("Annuler", ButtonType.CANCEL.getButtonData());
            
            difficultyDialog.getButtonTypes().setAll(easyButton, mediumButton, hardButton, cancelButton);
            
            // Styliser la boîte de dialogue
            DialogPane dialogPane = difficultyDialog.getDialogPane();
            dialogPane.getStyleClass().add("custom-dialog");
            
            // Appliquer le style CSS
            Scene scene = dialogPane.getScene();
            if (scene != null) {
                scene.getStylesheets().add(getClass().getResource("/com/example/bomberman/view/game-styles.css").toExternalForm());
            }
            
            // Styliser les boutons individuellement
            Button easyBtn = (Button) dialogPane.lookupButton(easyButton);
            if (easyBtn != null) {
                easyBtn.getStyleClass().add("easy-button");
                easyBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
            }
            
            Button mediumBtn = (Button) dialogPane.lookupButton(mediumButton);
            if (mediumBtn != null) {
                mediumBtn.getStyleClass().add("medium-button");
                mediumBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white; -fx-font-weight: bold;");
            }
            
            Button hardBtn = (Button) dialogPane.lookupButton(hardButton);
            if (hardBtn != null) {
                hardBtn.getStyleClass().add("hard-button");
                hardBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold;");
            }
            
            Button cancelBtn = (Button) dialogPane.lookupButton(cancelButton);
            if (cancelBtn != null) {
                cancelBtn.getStyleClass().add("cancel-button");
                cancelBtn.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");
            }
            
            // Styliser le texte du dialogue
            dialogPane.setStyle("-fx-background-color: #424242; -fx-text-fill: white;");
            
            // Styliser le texte d'en-tête
            dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #303030;");
            dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
            
            // Styliser le texte de contenu
            dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            
            // Attendre la réponse de l'utilisateur
            Optional<ButtonType> result = difficultyDialog.showAndWait();
            if (result.isPresent() && result.get() != cancelButton) {
                // Déterminer le niveau de difficulté
                int difficultyLevel = 2; // Moyen par défaut
                if (result.get() == easyButton) {
                    difficultyLevel = 1;
                } else if (result.get() == hardButton) {
                    difficultyLevel = 3;
                }
                
                soundManager.stopBackgroundMusic();

                // Charger la scène de jeu existante
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/view/game-view.fxml"));
                Parent gameRoot = loader.load();
                
                // Récupérer le contrôleur et activer le mode bot avec la difficulté choisie
                GameController gameController = loader.getController();
                gameController.enableBotMode(difficultyLevel);

                // Changer de scène
                Stage stage = (Stage) vsBotButton.getScene().getWindow();
                Scene gameScene = new Scene(gameRoot, 1000, 900); // Même taille que l'éditeur de niveau

                // Charger le CSS du jeu
                URL cssResource = getClass().getResource("/com/example/bomberman/view/game-styles.css");
                if (cssResource != null) {
                    gameScene.getStylesheets().add(cssResource.toExternalForm());
                }

                stage.setScene(gameScene);
                stage.setTitle("Super Bomberman - VS Bot (" + 
                               (difficultyLevel == 1 ? "Facile" : 
                                difficultyLevel == 3 ? "Difficile" : "Moyen") + ")");
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            
            // Charger le CSS du jeu
            URL cssResource = getClass().getResource("/com/example/bomberman/view/game-styles.css");
            if (cssResource != null) {
                gameScene.getStylesheets().add(cssResource.toExternalForm());
            }
            
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
            // Charger la vue des paramètres
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/view/settings-view.fxml"));
            Parent settingsRoot = loader.load();
            
            // Créer une nouvelle scène
            Scene settingsScene = new Scene(settingsRoot, 600, 500);
            
            // Appliquer le CSS du jeu
            URL cssResource = getClass().getResource("/com/example/bomberman/view/game-styles.css");
            if (cssResource != null) {
                settingsScene.getStylesheets().add(cssResource.toExternalForm());
            }
            
            // Créer une nouvelle fenêtre
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Paramètres - Super Bomberman");
            settingsStage.setScene(settingsScene);
            settingsStage.setResizable(true);
            
            // Afficher la fenêtre modale (bloque l'interaction avec la fenêtre principale)
            settingsStage.initOwner((Stage) settingsButton.getScene().getWindow());
            settingsStage.showAndWait();
            
            // Rafraîchir les paramètres audio après fermeture
            soundManager.playBackgroundMusic("menu_music");
            
        } catch (IOException e) {
            e.printStackTrace();
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
            Scene editorScene = new Scene(editorRoot, 1000, 900);

            // Charger le CSS du jeu
            URL cssResource = getClass().getResource("/com/example/bomberman/view/game-styles.css");
            if (cssResource != null) {
                editorScene.getStylesheets().add(cssResource.toExternalForm());
            }

            stage.setScene(editorScene);
            stage.setTitle("Super Bomberman - Éditeur de niveaux");

            System.out.println("Éditeur de niveaux lancé avec succès !");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Quitte le jeu
     */
    @FXML
    private void quitGame() {
        Platform.exit();
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
        
        // Réinitialiser les composants et démarrer la musique
        startMenuMusic();
        initializeComponents();
        setupBackground();
        
        isInitialized = true;
    }
}