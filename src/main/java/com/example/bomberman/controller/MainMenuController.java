package com.example.bomberman.controller;

import com.example.bomberman.models.entities.PlayerProfile;
import com.example.bomberman.service.ProfileManager;
import com.example.bomberman.service.SoundManager;
import com.example.bomberman.service.UserPreferences;
import com.example.bomberman.utils.ResourceManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    @FXML private Button editorButton;
    @FXML private Button settingsButton;
    @FXML private Button quitButton;
    @FXML private ComboBox<String> player1Combo;
    @FXML private ComboBox<String> player2Combo;

    private ProfileManager profileManager;
    private SoundManager soundManager;
    private UserPreferences userPreferences;
    private ResourceManager resourceManager;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        profileManager = ProfileManager.getInstance();
        soundManager = SoundManager.getInstance();
        userPreferences = UserPreferences.getInstance();
        resourceManager = ResourceManager.getInstance();

        // Appliquer les préférences utilisateur
        userPreferences.applyPreferences();
        
        // Jouer la musique du menu si la musique est activée
        if (userPreferences.isMusicEnabled()) {
            soundManager.playBackgroundMusic("menu");
        }

        // Initialiser les actions des boutons
        initializeButtons();
        
        // Charger les profils
        loadProfiles();

        System.out.println("Menu principal chargé avec succès !");
        System.out.println("Super Bomberman lancé avec succès !");
    }
    
    /**
     * Initialise les actions des boutons
     */
    private void initializeButtons() {
        // Configuration des boutons
        playButton.setOnAction(e -> startGame());
        profilesButton.setOnAction(e -> openProfilesManagement());
        editorButton.setOnAction(e -> openLevelEditor());
        settingsButton.setOnAction(e -> openSettings());
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
     * Charge les profils des joueurs
     */
    private void loadProfiles() {
        List<PlayerProfile> profiles = profileManager.getAllProfiles();
        
        player1Combo.getItems().clear();
        player2Combo.getItems().clear();
        
        for (PlayerProfile profile : profiles) {
            String fullName = profile.getFullName();
            player1Combo.getItems().add(fullName);
            player2Combo.getItems().add(fullName);
        }
        
        // Sélectionner les premiers profils par défaut
        if (!profiles.isEmpty()) {
            player1Combo.setValue(profiles.get(0).getFullName());
            
            if (profiles.size() > 1) {
                player2Combo.setValue(profiles.get(1).getFullName());
            } else {
                player2Combo.setValue(profiles.get(0).getFullName());
            }
        }
    }

    /**
     * Démarre une nouvelle partie
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
     * Ouvre la personnalisation des contrôles
     */
    @FXML
    private void openProfilesManagement() {
        // Créer une fenêtre de dialogue personnalisée
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Personnalisation des contrôles");
        dialog.setHeaderText("Personnaliser les touches des joueurs");
        
        // Configurer les boutons
        ButtonType okButtonType = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
        
        // Créer la mise en page
        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(10));
        
        // Sélection du profil pour les contrôles
        ComboBox<String> profileSelector = new ComboBox<>();
        List<PlayerProfile> profiles = profileManager.getAllProfiles();
        
        for (PlayerProfile profile : profiles) {
            profileSelector.getItems().add(profile.getFullName());
        }
        
        if (!profiles.isEmpty()) {
            profileSelector.setValue(profiles.get(0).getFullName());
        }
        
        // Grille pour les contrôles
        GridPane controlsGrid = new GridPane();
        controlsGrid.setHgap(10);
        controlsGrid.setVgap(10);
        controlsGrid.setPadding(new Insets(10));
        
        // Labels pour les actions
        controlsGrid.add(new Label("Haut:"), 0, 0);
        controlsGrid.add(new Label("Bas:"), 0, 1);
        controlsGrid.add(new Label("Gauche:"), 0, 2);
        controlsGrid.add(new Label("Droite:"), 0, 3);
        controlsGrid.add(new Label("Bombe:"), 0, 4);
        controlsGrid.add(new Label("Spécial:"), 0, 5);
        
        // Boutons pour configurer les touches
        Button upButton = new Button();
        Button downButton = new Button();
        Button leftButton = new Button();
        Button rightButton = new Button();
        Button bombButton = new Button();
        Button specialButton = new Button();
        
        controlsGrid.add(upButton, 1, 0);
        controlsGrid.add(downButton, 1, 1);
        controlsGrid.add(leftButton, 1, 2);
        controlsGrid.add(rightButton, 1, 3);
        controlsGrid.add(bombButton, 1, 4);
        controlsGrid.add(specialButton, 1, 5);
        
        // Bouton pour réinitialiser les contrôles
        Button resetControlsButton = new Button("Réinitialiser les contrôles");
        
        // Fonction pour mettre à jour l'affichage des contrôles
        Runnable updateControlsDisplay = () -> {
            String selectedProfileName = profileSelector.getValue();
            if (selectedProfileName != null) {
                PlayerProfile selectedProfile = profileManager.findProfile(selectedProfileName);
                if (selectedProfile != null) {
                    upButton.setText(selectedProfile.getControl(PlayerProfile.ACTION_UP).getName());
                    downButton.setText(selectedProfile.getControl(PlayerProfile.ACTION_DOWN).getName());
                    leftButton.setText(selectedProfile.getControl(PlayerProfile.ACTION_LEFT).getName());
                    rightButton.setText(selectedProfile.getControl(PlayerProfile.ACTION_RIGHT).getName());
                    bombButton.setText(selectedProfile.getControl(PlayerProfile.ACTION_BOMB).getName());
                    specialButton.setText(selectedProfile.getControl(PlayerProfile.ACTION_SPECIAL).getName());
                }
            }
        };
        
        // Mettre à jour les contrôles quand le profil change
        profileSelector.setOnAction(e -> updateControlsDisplay.run());
        
        // Configurer les boutons pour changer les contrôles
        EventHandler<ActionEvent> configureControl = event -> {
            Button sourceButton = (Button) event.getSource();
            final String action;
            
            if (sourceButton == upButton) action = PlayerProfile.ACTION_UP;
            else if (sourceButton == downButton) action = PlayerProfile.ACTION_DOWN;
            else if (sourceButton == leftButton) action = PlayerProfile.ACTION_LEFT;
            else if (sourceButton == rightButton) action = PlayerProfile.ACTION_RIGHT;
            else if (sourceButton == bombButton) action = PlayerProfile.ACTION_BOMB;
            else if (sourceButton == specialButton) action = PlayerProfile.ACTION_SPECIAL;
            else action = "";
            
            String selectedProfileName = profileSelector.getValue();
            if (selectedProfileName != null && !action.isEmpty()) {
                PlayerProfile selectedProfile = profileManager.findProfile(selectedProfileName);
                if (selectedProfile != null) {
                    // Capture la variable action dans une variable finale pour utilisation dans le lambda
                    final String finalAction = action;
                    
                    // Afficher une boîte de dialogue pour capturer la touche
                    Dialog<KeyCode> keyDialog = new Dialog<>();
                    keyDialog.setTitle("Configurer une touche");
                    keyDialog.setHeaderText("Appuyez sur une touche pour l'action: " + finalAction);
                    
                    // Ajouter un bouton d'annulation
                    keyDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
                    
                    // Zone de texte pour capturer la touche
                    TextField keyField = new TextField();
                    keyField.setEditable(false);
                    keyField.setText("Appuyez sur une touche...");
                    keyField.setOnKeyPressed(keyEvent -> {
                        keyField.setText(keyEvent.getCode().getName());
                        keyDialog.setResult(keyEvent.getCode());
                        keyDialog.close();
                    });
                    
                    keyDialog.getDialogPane().setContent(keyField);
                    
                    // Afficher la boîte de dialogue et attendre la saisie
                    Optional<KeyCode> keyResult = keyDialog.showAndWait();
                    keyResult.ifPresent(keyCode -> {
                        // Vérifier si la touche est déjà utilisée
                        if (selectedProfile.isKeyUsed(keyCode)) {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Touche déjà utilisée");
                            alert.setHeaderText(null);
                            alert.setContentText("Cette touche est déjà utilisée pour une autre action.");
                            alert.showAndWait();
                        } else {
                            // Assigner la touche à l'action
                            selectedProfile.setControl(finalAction, keyCode);
                            sourceButton.setText(keyCode.getName());
                        }
                    });
                }
            }
        };
        
        // Assigner l'événement aux boutons
        upButton.setOnAction(configureControl);
        downButton.setOnAction(configureControl);
        leftButton.setOnAction(configureControl);
        rightButton.setOnAction(configureControl);
        bombButton.setOnAction(configureControl);
        specialButton.setOnAction(configureControl);
        
        // Configurer le bouton de réinitialisation
        resetControlsButton.setOnAction(e -> {
            String selectedProfileName = profileSelector.getValue();
            if (selectedProfileName != null) {
                PlayerProfile selectedProfile = profileManager.findProfile(selectedProfileName);
                if (selectedProfile != null) {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Réinitialiser les contrôles");
                    confirmAlert.setHeaderText(null);
                    confirmAlert.setContentText("Êtes-vous sûr de vouloir réinitialiser les contrôles pour " + selectedProfileName + " ?");
                    
                    if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                        selectedProfile.resetControls();
                        updateControlsDisplay.run();
                    }
                }
            }
        });
        
        // Ajouter les éléments à la boîte principale
        mainBox.getChildren().addAll(
            new Label("Sélectionnez un joueur:"), 
            profileSelector, 
            controlsGrid, 
            resetControlsButton
        );
        
        // Initialiser l'affichage des contrôles
        if (!profiles.isEmpty()) {
            updateControlsDisplay.run();
        }
        
        dialog.getDialogPane().setContent(mainBox);
        
        // Afficher la boîte de dialogue
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okButtonType) {
            // Sauvegarder les profils
            profileManager.saveProfiles();
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
            showAlert("Thème appliqué", "Le thème " + themeComboBox.getValue() + " a été appliqué. Les changements seront visibles au prochain chargement d'écran.", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * Ouvre l'éditeur de niveau
     */
    @FXML
    private void openLevelEditor() {
        try {
            // Arrêter la musique en cours
            soundManager.stopBackgroundMusic();
            // Jouer la musique de l'éditeur
            soundManager.playBackgroundMusic("editor_music");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/view/level-editor-view.fxml"));
            Parent editorRoot = loader.load();
            Stage stage = (Stage) editorButton.getScene().getWindow();
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
            stage.setTitle("Super Bomberman - Éditeur de Niveaux");
            
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
            System.out.println("Menu chargé depuis: " + "/com/example/bomberman/view/menu-view.fxml");
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