package com.example.bomberman.controller;

import com.example.bomberman.service.SoundManager;
import com.example.bomberman.service.UserPreferences;
import com.example.bomberman.utils.ResourceManager;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur pour le menu principal du jeu
 */
public class MainMenuController implements Initializable {

    @FXML private BorderPane mainContainer;
    @FXML private VBox menuOptions;
    @FXML private Button playButton;
    @FXML private Button botGameButton;
    @FXML private Button levelEditorButton;
    @FXML private Button settingsButton;
    @FXML private Button quitButton;
    @FXML private Label titleLabel;

    private SoundManager soundManager;
    private UserPreferences userPreferences;
    private ResourceManager resourceManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        soundManager = SoundManager.getInstance();
        userPreferences = UserPreferences.getInstance();
        resourceManager = ResourceManager.getInstance();

        // Appliquer les préférences utilisateur
        userPreferences.applyPreferences();

        // Jouer la musique du menu si elle est activée
        if (userPreferences.isMusicEnabled()) {
        soundManager.playBackgroundMusic("menu");
    }

        // Animation du titre
        animateTitle();
    }

    /**
     * Anime le titre avec un effet de pulsation
     */
    private void animateTitle() {
        if (titleLabel != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), titleLabel);
            fadeTransition.setFromValue(0.7);
            fadeTransition.setToValue(1.0);
            fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();
        }
    }

    /**
     * Démarre une nouvelle partie
     */
    @FXML
    private void startNewGame() {
        try {
            // Arrêter la musique du menu et jouer celle du jeu si la musique est activée
            if (userPreferences.isMusicEnabled()) {
            soundManager.stopBackgroundMusic();
            soundManager.playBackgroundMusic("game_music");
            }

            // Charger la vue du jeu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/view/game-view.fxml"));
            Parent gameRoot = loader.load();

            // Créer une nouvelle scène pour le jeu
            Scene gameScene = new Scene(gameRoot, 1000, 900);
            
            // Ajouter le CSS pour le jeu
            URL cssResource = getClass().getResource("/com/example/bomberman/view/game-styles.css");
                if (cssResource != null) {
                    gameScene.getStylesheets().add(cssResource.toExternalForm());
            }

            // Obtenir le stage actuel
            Stage stage = (Stage) playButton.getScene().getWindow();
            
            // Changer la scène
            stage.setScene(gameScene);
            stage.setTitle("Super Bomberman - Partie en cours");

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur au chargement du jeu", e.getMessage());
        }
    }
    
    /**
     * Démarre une partie contre l'IA
     */
    @FXML
    private void startBotGame() {
        try {
            // Afficher une boîte de dialogue pour choisir la difficulté
            int difficultyLevel = showDifficultyDialog();
            if (difficultyLevel == 0) {
                // L'utilisateur a annulé, ne rien faire
                return;
            }
            
            // Arrêter la musique du menu et jouer celle du jeu si la musique est activée
            if (userPreferences.isMusicEnabled()) {
                soundManager.stopBackgroundMusic();
                soundManager.playBackgroundMusic("game_music");
            }

            // Charger la vue du jeu standard (nous modifierons le contrôleur)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/view/game-view.fxml"));
            Parent gameRoot = loader.load();
            
            // Récupérer le contrôleur de jeu
            GameController gameController = loader.getController();
            
            // Créer une instance de BotGame
            com.example.bomberman.models.world.BotGame botGame = 
                new com.example.bomberman.models.world.BotGame(difficultyLevel);
            
            // Définir le jeu du contrôleur
            gameController.loadCustomGame(botGame);

            // Créer une nouvelle scène pour le jeu
            Scene gameScene = new Scene(gameRoot, 1000, 900);
            
            // Ajouter le CSS pour le jeu
            URL cssResource = getClass().getResource("/com/example/bomberman/view/game-styles.css");
            if (cssResource != null) {
                gameScene.getStylesheets().add(cssResource.toExternalForm());
            }

            // Obtenir le stage actuel
            Stage stage = (Stage) botGameButton.getScene().getWindow();
            
            // Changer la scène
            stage.setScene(gameScene);
            stage.setTitle("Super Bomberman - Mode VS IA (Niveau " + difficultyLevel + ")");
            
            // Afficher les instructions du mode IA
            showBotInstructions(difficultyLevel);

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur au chargement du mode IA", e.getMessage());
        }
    }
    
    /**
     * Affiche une boîte de dialogue pour choisir la difficulté du bot
     * @return Le niveau de difficulté (1-3) ou 0 si annulé
     */
    private int showDifficultyDialog() {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Difficulté du Bot");
        dialog.setHeaderText("Choisissez le niveau de difficulté");
        
        // Ajouter des boutons pour chaque niveau de difficulté
        ButtonType easyButton = new ButtonType("Facile", ButtonBar.ButtonData.OK_DONE);
        ButtonType mediumButton = new ButtonType("Moyen", ButtonBar.ButtonData.OK_DONE);
        ButtonType hardButton = new ButtonType("Difficile", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        dialog.getDialogPane().getButtonTypes().addAll(easyButton, mediumButton, hardButton, cancelButton);
        
        // Convertir le résultat en entier
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == easyButton) {
                return 1;
            } else if (dialogButton == mediumButton) {
                return 2;
            } else if (dialogButton == hardButton) {
                return 3;
            }
            return 0; // Annulé
        });
        
        Optional<Integer> result = dialog.showAndWait();
        return result.orElse(0);
    }
    
    /**
     * Affiche les instructions du mode Bot
     */
    private void showBotInstructions(int difficultyLevel) {
        String difficultyText;
        switch (difficultyLevel) {
            case 1: difficultyText = "Facile"; break;
            case 2: difficultyText = "Moyen"; break;
            case 3: difficultyText = "Difficile"; break;
            default: difficultyText = "Inconnu";
        }
        
        String instructions = 
            "Mode VS IA - Niveau : " + difficultyText + "\n\n" +
            "Bienvenue dans le mode VS IA !\n\n" +
            "Vous affrontez un bot avec intelligence artificielle.\n" +
            "Collectez des power-ups et utilisez des stratégies pour vaincre votre adversaire robot.\n\n" +
            "Contrôles:\n" +
            "• ZQSD: Déplacer le joueur\n" +
            "• A: Placer une bombe\n" +
            "• Échap: Pause/Menu\n\n" +
            "Bonne chance !";
        
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Mode VS IA");
            alert.setHeaderText(null);
            alert.setContentText(instructions);
            alert.showAndWait();
        });
    }

    /**
     * Ouvre l'éditeur de niveaux
     */
    @FXML
    private void openLevelEditor() {
        try {
            // Arrêter la musique du menu et jouer celle de l'éditeur si la musique est activée
            if (userPreferences.isMusicEnabled()) {
            soundManager.stopBackgroundMusic();
            soundManager.playBackgroundMusic("editor_music");
            }

            // Charger la vue de l'éditeur de niveaux
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/view/level-editor-view.fxml"));
            Parent editorRoot = loader.load();

            // Créer une nouvelle scène pour l'éditeur
            Scene editorScene = new Scene(editorRoot, 1000, 900);

            // Ajouter le CSS pour l'éditeur
            URL cssResource = getClass().getResource("/com/example/bomberman/view/level-editor-styles.css");
                if (cssResource != null) {
                    editorScene.getStylesheets().add(cssResource.toExternalForm());
            }

            // Obtenir le stage actuel
            Stage stage = (Stage) levelEditorButton.getScene().getWindow();
            
            // Changer la scène
            stage.setScene(editorScene);
            stage.setTitle("Super Bomberman - Éditeur de niveaux");

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur au chargement de l'éditeur", e.getMessage());
        }
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
        soundEnabledCheckbox.setSelected(userPreferences.isSoundEnabled());
        
        // Bouton pour couper/activer rapidement les sons
        Button toggleSoundButton = new Button(userPreferences.isSoundEnabled() ? "Couper les sons" : "Activer les sons");
        toggleSoundButton.setOnAction(e -> {
            boolean newState = !userPreferences.isSoundEnabled();
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
        musicEnabledCheckbox.setSelected(userPreferences.isMusicEnabled());
        
        // Bouton pour couper/activer rapidement la musique
        Button toggleMusicButton = new Button(userPreferences.isMusicEnabled() ? "Couper la musique" : "Activer la musique");
        toggleMusicButton.setOnAction(e -> {
            boolean newState = !userPreferences.isMusicEnabled();
            soundManager.setMusicEnabled(newState);
            musicEnabledCheckbox.setSelected(newState);
            toggleMusicButton.setText(newState ? "Couper la musique" : "Activer la musique");
            
            // Relancer la musique si elle est activée
            if (newState) {
                soundManager.playBackgroundMusic("menu");
            }
        });
        
        // Mise à jour du checkbox qui met aussi à jour le bouton
        musicEnabledCheckbox.setOnAction(e -> {
            boolean selected = musicEnabledCheckbox.isSelected();
            soundManager.setMusicEnabled(selected);
            toggleMusicButton.setText(selected ? "Couper la musique" : "Activer la musique");
            
            // Relancer la musique si elle est activée
            if (selected) {
                soundManager.playBackgroundMusic("menu");
            }
        });
        
        // Volume des sons
        Label soundVolumeLabel = new Label("Volume des sons:");
        Slider soundVolumeSlider = new Slider(0, 100, userPreferences.getSoundVolume() * 100);
        soundVolumeSlider.setShowTickLabels(true);
        soundVolumeSlider.setShowTickMarks(true);
        soundVolumeSlider.setMajorTickUnit(25);
        soundVolumeSlider.setBlockIncrement(5);
        Label soundVolumeValueLabel = new Label(String.format("%d%%", (int)(userPreferences.getSoundVolume() * 100)));
        
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
        Slider musicVolumeSlider = new Slider(0, 100, userPreferences.getMusicVolume() * 100);
        musicVolumeSlider.setShowTickLabels(true);
        musicVolumeSlider.setShowTickMarks(true);
        musicVolumeSlider.setMajorTickUnit(25);
        musicVolumeSlider.setBlockIncrement(5);
        Label musicVolumeValueLabel = new Label(String.format("%d%%", (int)(userPreferences.getMusicVolume() * 100)));
        
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
            boolean soundEnabled = soundEnabledCheckbox.isSelected();
            boolean musicEnabled = musicEnabledCheckbox.isSelected();
            double soundVolume = soundVolumeSlider.getValue() / 100.0;
            double musicVolume = musicVolumeSlider.getValue() / 100.0;
            String selectedTheme = themeComboBox.getValue().toUpperCase();
            
            // Mettre à jour les préférences utilisateur
            userPreferences.setSoundEnabled(soundEnabled);
            userPreferences.setMusicEnabled(musicEnabled);
            userPreferences.setSoundVolume(soundVolume);
            userPreferences.setMusicVolume(musicVolume);
            userPreferences.setTheme(selectedTheme);
            
            // Appliquer les paramètres
            soundManager.setSoundEnabled(soundEnabled);
            soundManager.setMusicEnabled(musicEnabled);
            soundManager.setSoundVolume(soundVolume);
            soundManager.setMusicVolume(musicVolume);
            
            // Relancer la musique si elle est activée
            if (musicEnabled) {
                soundManager.playBackgroundMusic("menu");
            }
            
            // Appliquer le thème sélectionné
            ResourceManager.Theme theme = ResourceManager.themeFromString(selectedTheme);
            resourceManager.setTheme(theme);
            soundManager.setTheme(theme);
            
            // Rafraîchir les sprites
            resourceManager.clearCache();
            
            // Afficher un message de confirmation
            showInfoAlert("Thème appliqué", "Le thème " + themeComboBox.getValue() + " a été appliqué. Les changements seront visibles au prochain chargement d'écran.");
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
            // Sauvegarder les préférences utilisateur avant de quitter
            userPreferences.savePreferences();
            Platform.exit();
        }
    }

    /**
     * Affiche une alerte d'erreur
     */
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte d'information
     */
    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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