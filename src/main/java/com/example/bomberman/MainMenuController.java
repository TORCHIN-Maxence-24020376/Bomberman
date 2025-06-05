package com.example.bomberman;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur pour le menu principal du jeu
 */
public class MainMenuController implements Initializable {

    @FXML
    private VBox mainMenuPane;
    @FXML private Button playButton;
    @FXML private Button profilesButton;
    @FXML private Button settingsButton;
    @FXML private Button levelEditorButton;
    @FXML private Button quitButton;
    @FXML private Label titleLabel;
    @FXML private ComboBox<PlayerProfile> player1Combo;
    @FXML private ComboBox<PlayerProfile> player2Combo;

    private GameState currentState = GameState.MENU;
    private ProfileManager profileManager;
    private SoundManager soundManager;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        profileManager = ProfileManager.getInstance();
        soundManager = SoundManager.getInstance();

        initializeComponents();
        loadProfiles();

        // Démarrer la musique de menu
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
        player1Combo.setPromptText("Sélectionner Joueur 1");
        player2Combo.setPromptText("Sélectionner Joueur 2");

        // Convertisseurs pour affichage des profils
        player1Combo.setConverter(new StringConverter<PlayerProfile>() {
            @Override
            public String toString(PlayerProfile profile) {
                return profile != null ? profile.getFullName() : "";
            }

            @Override
            public PlayerProfile fromString(String string) {
                return profileManager.findProfile(string);
            }
        });

        player2Combo.setConverter(player1Combo.getConverter());
    }

    /**
     * Charge les profils dans les ComboBox
     */
    private void loadProfiles() {
        List<PlayerProfile> profiles = profileManager.getAllProfiles();

        player1Combo.getItems().clear();
        player2Combo.getItems().clear();

        player1Combo.getItems().addAll(profiles);
        player2Combo.getItems().addAll(profiles);

        // Sélection par défaut
        if (profiles.size() >= 2) {
            player1Combo.setValue(profiles.get(0));
            player2Combo.setValue(profiles.get(1));
        }
    }

    /**
     * Démarre une nouvelle partie
     */
    @FXML
    private void startGame() {
        PlayerProfile player1 = player1Combo.getValue();
        PlayerProfile player2 = player2Combo.getValue();

        if (player1 == null || player2 == null) {
            showAlert("Erreur", "Veuillez sélectionner deux profils de joueurs.", Alert.AlertType.WARNING);
            return;
        }

        if (player1.equals(player2)) {
            showAlert("Erreur", "Veuillez sélectionner deux profils différents.", Alert.AlertType.WARNING);
            return;
        }

        try {
            soundManager.stopBackgroundMusic();

            // Charger la scène de jeu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/game-view.fxml"));
            Parent gameRoot = loader.load();

            // Passer les profils au contrôleur de jeu
            GameController gameController = loader.getController();
            gameController.setPlayerProfiles(player1, player2);

            // Changer de scène
            Stage stage = (Stage) playButton.getScene().getWindow();
            Scene gameScene = new Scene(gameRoot, 800, 650);

            // Charger le CSS
            gameScene.getStylesheets().add(getClass().getResource("/com/example/bomberman/styles.css").toExternalForm());

            stage.setScene(gameScene);
            stage.setTitle("Super Bomberman - En jeu");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le jeu : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Ouvre la gestion des profils
     */
    @FXML
    private void openProfilesManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/profiles-view.fxml"));
            Parent profilesRoot = loader.load();

            Stage profilesStage = new Stage();
            profilesStage.setTitle("Gestion des Profils");
            profilesStage.setScene(new Scene(profilesRoot, 600, 400));
            profilesStage.setResizable(false);

            // Passer le contrôleur principal pour actualiser les profils
            ProfilesController profilesController = loader.getController();
            profilesController.setMainMenuController(this);

            profilesStage.showAndWait();

            // Actualiser les profils après fermeture
            loadProfiles();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la gestion des profils.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Ouvre les paramètres
     */
    @FXML
    private void openSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/settings-view.fxml"));
            Parent settingsRoot = loader.load();

            Stage settingsStage = new Stage();
            settingsStage.setTitle("Paramètres");
            settingsStage.setScene(new Scene(settingsRoot, 500, 400));
            settingsStage.setResizable(false);
            settingsStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les paramètres.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Ouvre l'éditeur de niveau
     */
    @FXML
    private void openLevelEditor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/level-editor-view.fxml"));
            Parent editorRoot = loader.load();

            Stage editorStage = new Stage();
            editorStage.setTitle("Éditeur de Niveaux");
            editorStage.setScene(new Scene(editorRoot, 900, 700));
            editorStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur de niveaux.", Alert.AlertType.ERROR);
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
        currentState = GameState.MENU;
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