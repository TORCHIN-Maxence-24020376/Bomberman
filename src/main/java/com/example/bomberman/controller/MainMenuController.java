package com.example.bomberman.controller;

import com.example.bomberman.models.entities.PlayerProfile;
import com.example.bomberman.service.ProfileManager;
import com.example.bomberman.service.SoundManager;
import com.example.bomberman.utils.ResourceManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Optional;

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

        // Démarrer la musique de menu aléatoire (entre menu_music_A et menu_music_B)
        soundManager.playBackgroundMusic("menu");
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
            // Jouer la musique du jeu
            soundManager.playBackgroundMusic("game_music");

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
     * Ouvre les paramètres avec contrôles de volume
     */
    @FXML
    private void openSettings() {
        // Créer une fenêtre de dialogue personnalisée
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Paramètres");
        dialog.setHeaderText("Paramètres du jeu");
        
        // Configurer les boutons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Créer la mise en page
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Activer/désactiver les sons
        CheckBox soundEnabledCheckbox = new CheckBox("Sons activés");
        soundEnabledCheckbox.setSelected(soundManager.isSoundEnabled());
        
        // Bouton pour couper/activer rapidement les sons
        Button toggleSoundButton = new Button(soundManager.isSoundEnabled() ? "Couper les sons" : "Activer les sons");
        toggleSoundButton.setOnAction(e -> {
            boolean newState = !soundManager.isSoundEnabled();
            soundManager.setSoundEnabled(newState);
            soundEnabledCheckbox.setSelected(newState);
            toggleSoundButton.setText(newState ? "Couper les sons" : "Activer les sons");
        });
        
        // Mise à jour du checkbox qui met aussi à jour le bouton
        soundEnabledCheckbox.setOnAction(e -> {
            boolean selected = soundEnabledCheckbox.isSelected();
            soundManager.setSoundEnabled(selected);
            toggleSoundButton.setText(selected ? "Couper les sons" : "Activer les sons");
        });
        
        // Activer/désactiver la musique
        CheckBox musicEnabledCheckbox = new CheckBox("Musique activée");
        musicEnabledCheckbox.setSelected(soundManager.isMusicEnabled());
        
        // Bouton pour couper/activer rapidement la musique
        Button toggleMusicButton = new Button(soundManager.isMusicEnabled() ? "Couper la musique" : "Activer la musique");
        toggleMusicButton.setOnAction(e -> {
            boolean newState = !soundManager.isMusicEnabled();
            soundManager.setMusicEnabled(newState);
            musicEnabledCheckbox.setSelected(newState);
            toggleMusicButton.setText(newState ? "Couper la musique" : "Activer la musique");
        });
        
        // Mise à jour du checkbox qui met aussi à jour le bouton
        musicEnabledCheckbox.setOnAction(e -> {
            boolean selected = musicEnabledCheckbox.isSelected();
            soundManager.setMusicEnabled(selected);
            toggleMusicButton.setText(selected ? "Couper la musique" : "Activer la musique");
        });
        
        // Volume des sons
        Label soundVolumeLabel = new Label("Volume des sons:");
        Slider soundVolumeSlider = new Slider(0, 100, soundManager.getSoundVolume() * 100);
        soundVolumeSlider.setShowTickLabels(true);
        soundVolumeSlider.setShowTickMarks(true);
        soundVolumeSlider.setMajorTickUnit(25);
        soundVolumeSlider.setBlockIncrement(5);
        Label soundVolumeValueLabel = new Label(String.format("%d%%", (int)(soundManager.getSoundVolume() * 100)));
        
        // Mettre à jour l'étiquette et le volume en temps réel lors du changement
        soundVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            soundVolumeValueLabel.setText(String.format("%d%%", newVal.intValue()));
            // Appliquer le volume immédiatement
            soundManager.setSoundVolume(newVal.doubleValue() / 100.0);
        });
        
        // Jouer un son lorsque l'utilisateur relâche le slider
        soundVolumeSlider.setOnMouseReleased(event -> {
            // Jouer un son pour tester le volume
            if (soundManager.isSoundEnabled()) {
                soundManager.playSound("powerup_collect");
            }
        });
        
        // Volume de la musique
        Label musicVolumeLabel = new Label("Volume de la musique:");
        Slider musicVolumeSlider = new Slider(0, 100, soundManager.getMusicVolume() * 100);
        musicVolumeSlider.setShowTickLabels(true);
        musicVolumeSlider.setShowTickMarks(true);
        musicVolumeSlider.setMajorTickUnit(25);
        musicVolumeSlider.setBlockIncrement(5);
        Label musicVolumeValueLabel = new Label(String.format("%d%%", (int)(soundManager.getMusicVolume() * 100)));
        
        // Mettre à jour l'étiquette et le volume en temps réel lors du changement
        musicVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            musicVolumeValueLabel.setText(String.format("%d%%", newVal.intValue()));
            // Appliquer le volume immédiatement
            soundManager.setMusicVolume(newVal.doubleValue() / 100.0);
        });
        
        // Sélection du thème
        Label themeLabel = new Label("Thème graphique:");
        ComboBox<String> themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll("Default", "Desert", "Jungle");
        
        // Sélectionner le thème actuel
        ResourceManager resourceManager = ResourceManager.getInstance();
        ResourceManager.Theme currentTheme = resourceManager.getCurrentTheme();
        themeComboBox.setValue(capitalizeFirstLetter(currentTheme.name().toLowerCase()));
        
        // Ajouter les contrôles à la grille
        grid.add(soundEnabledCheckbox, 0, 0, 1, 1);
        grid.add(toggleSoundButton, 1, 0, 1, 1);
        grid.add(musicEnabledCheckbox, 0, 1, 1, 1);
        grid.add(toggleMusicButton, 1, 1, 1, 1);
        grid.add(soundVolumeLabel, 0, 2);
        grid.add(soundVolumeSlider, 1, 2);
        grid.add(soundVolumeValueLabel, 2, 2);
        grid.add(musicVolumeLabel, 0, 3);
        grid.add(musicVolumeSlider, 1, 3);
        grid.add(musicVolumeValueLabel, 2, 3);
        grid.add(themeLabel, 0, 4);
        grid.add(themeComboBox, 1, 4, 2, 1);
        
        // Ajouter un séparateur
        Separator separator = new Separator();
        grid.add(separator, 0, 5, 3, 1);
        
        // Ajouter la grille au dialogue
        dialog.getDialogPane().setContent(grid);
        
        // Appliquer les paramètres si OK est cliqué
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Appliquer les paramètres
            soundManager.setSoundEnabled(soundEnabledCheckbox.isSelected());
            soundManager.setMusicEnabled(musicEnabledCheckbox.isSelected());
            
            // Appliquer le thème sélectionné
            String selectedTheme = themeComboBox.getValue();
            ResourceManager.Theme theme = ResourceManager.themeFromString(selectedTheme);
            soundManager.setTheme(theme);
            
            // Afficher un message de confirmation
            showAlert("Thème appliqué", "Le thème " + selectedTheme + " a été appliqué. Les changements seront visibles au prochain chargement d'écran.", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * Ouvre l'éditeur de niveau
     */
    @FXML
    private void openLevelEditor() {
        try {
            soundManager.stopBackgroundMusic();
            // Jouer la musique de l'éditeur
            soundManager.playBackgroundMusic("editor_music");

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
     * Retourne au menu principal
     */
    public void returnToMenu() {
        try {
            // Arrêter la musique en cours
            soundManager.stopBackgroundMusic();
            // Jouer la musique du menu
            soundManager.playBackgroundMusic("menu");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/view/menu-view.fxml"));
            Parent menuRoot = loader.load();
            Stage stage = (Stage) playButton.getScene().getWindow();
            Scene menuScene = new Scene(menuRoot, 800, 600);
            stage.setScene(menuScene);
            stage.setTitle("Super Bomberman - Menu Principal");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Rafraîchit la liste des profils
     */
    public void refreshProfiles() {
        loadProfiles();
    }

    /**
     * Convertit la première lettre d'une chaîne en majuscule
     * @param str La chaîne à convertir
     * @return La chaîne avec la première lettre en majuscule
     */
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}