package com.example.bomberman.controller;

import com.example.bomberman.models.entities.PlayerProfile;
import com.example.bomberman.service.ProfileManager;
import com.example.bomberman.service.SoundManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur simplifié pour le menu principal
 */
public class MainMenuController implements Initializable {

    @FXML private Button playButton;
    @FXML private Button profilesButton;
    @FXML private Button settingsButton;
    @FXML private Button levelEditorButton;
    @FXML private Button quitButton;
    @FXML private ComboBox<String> player1Combo;
    @FXML private ComboBox<String> player2Combo;

    private ProfileManager profileManager;
    private SoundManager soundManager;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        profileManager = ProfileManager.getInstance();
        soundManager = SoundManager.getInstance();

        initializeComponents();
        loadProfiles();

        // Démarrer la musique de menu (simulation)
        soundManager.playBackgroundMusic("/sounds/menu_music.mp3");
    }

    /**
     * Initialise les composants de l'interface
     */
    private void initializeComponents() {
        // Configuration des boutons
        playButton.setOnAction(e -> startGame());
        profilesButton.setOnAction(e -> openProfilesManagement());
        settingsButton.setOnAction(e -> openSettings());
        levelEditorButton.setOnAction(e -> openLevelEditor());
        quitButton.setOnAction(e -> quitGame());

        // Style des combos
        if (player1Combo != null) {
            player1Combo.setPromptText("Sélectionner Joueur 1");
        }
        if (player2Combo != null) {
            player2Combo.setPromptText("Sélectionner Joueur 2");
        }
    }

    /**
     * Charge les profils dans les ComboBox
     */
    private void loadProfiles() {
        if (player1Combo == null || player2Combo == null) return;

        List<PlayerProfile> profiles = profileManager.getAllProfiles();

        player1Combo.getItems().clear();
        player2Combo.getItems().clear();

        // Convertir les profils en chaînes pour les ComboBox
        for (PlayerProfile profile : profiles) {
            String displayName = profile.getFullName();
            player1Combo.getItems().add(displayName);
            player2Combo.getItems().add(displayName);
        }

        // Sélection par défaut
        if (profiles.size() >= 2) {
            player1Combo.setValue(profiles.get(0).getFullName());
            player2Combo.setValue(profiles.get(1).getFullName());
        } else if (profiles.size() == 1) {
            player1Combo.setValue(profiles.get(0).getFullName());
        }
    }

    /**
     * Démarre une nouvelle partie - VERSION CORRIGÉE
     */
    @FXML
    private void startGame() {
        try {
            soundManager.stopBackgroundMusic();

            // Charger la scène de jeu existante (votre game-view.fxml original)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/view/game-view.fxml"));
            Parent gameRoot = loader.load();

            // NE PAS essayer de récupérer le contrôleur - laisser JavaFX le gérer
            // Le contrôleur sera automatiquement celui défini dans le FXML

            // Changer de scène
            Stage stage = (Stage) playButton.getScene().getWindow();
            Scene gameScene = new Scene(gameRoot, 800, 600);

            // Charger le CSS s'il existe
            try {
                var cssResource = getClass().getResource("/com/example/bomberman/styles.css");
                if (cssResource != null) {
                    gameScene.getStylesheets().add(cssResource.toExternalForm());
                }
            } catch (Exception cssError) {
                System.out.println("CSS non trouvé, continuation sans styles");
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
            // Créer une nouvelle fenêtre avec votre jeu original
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
     * Ouvre la gestion des profils
     */
    @FXML
    private void openProfilesManagement() {
        showAlert("Info", "Gestion des profils en cours de développement.", Alert.AlertType.INFORMATION);
    }

    /**
     * Ouvre les paramètres
     */
    @FXML
    private void openSettings() {
        // Dialogue simple pour les paramètres
        Alert settingsAlert = new Alert(Alert.AlertType.INFORMATION);
        settingsAlert.setTitle("Paramètres");
        settingsAlert.setHeaderText("Paramètres du jeu");

        StringBuilder settings = new StringBuilder();
        settings.append("Sons: ").append(soundManager.isSoundEnabled() ? "Activés" : "Désactivés").append("\n");
        settings.append("Musique: ").append(soundManager.isMusicEnabled() ? "Activée" : "Désactivée").append("\n");
        settings.append("Volume sons: ").append((int)(soundManager.getSoundVolume() * 100)).append("%\n");
        settings.append("Volume musique: ").append((int)(soundManager.getMusicVolume() * 100)).append("%");

        settingsAlert.setContentText(settings.toString());
        settingsAlert.showAndWait();
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
            // Sauvegarder les profils avant de quitter
            profileManager.saveProfiles();
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
        loadProfiles(); // Actualiser les profils
        soundManager.playBackgroundMusic("/sounds/menu_music.mp3");
    }

    /**
     * Actualise l'affichage des profils
     */
    public void refreshProfiles() {
        loadProfiles();
    }
}