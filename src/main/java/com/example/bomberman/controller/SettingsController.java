package com.example.bomberman.controller;

import com.example.bomberman.service.SoundManager;
import com.example.bomberman.service.UserPreferences;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML private Slider effectsVolumeSlider;
    @FXML private Slider musicVolumeSlider;
    @FXML private Label effectsVolumeLabel;
    @FXML private Label musicVolumeLabel;
    @FXML private CheckBox soundEnabledCheck;
    @FXML private CheckBox musicEnabledCheck;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private SoundManager soundManager;
    private UserPreferences userPreferences;
    private boolean initialSoundEnabled;
    private boolean initialMusicEnabled;
    private double initialSoundVolume;
    private double initialMusicVolume;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        soundManager = SoundManager.getInstance();
        userPreferences = UserPreferences.getInstance();
        
        // Sauvegarder les paramètres initiaux
        saveInitialSettings();
        
        setupVolumeSliders();
        setupCheckBoxes();
        setupButtons();
        loadCurrentSettings();
    }

    private void saveInitialSettings() {
        initialSoundEnabled = soundManager.isSoundEnabled();
        initialMusicEnabled = soundManager.isMusicEnabled();
        initialSoundVolume = soundManager.getSoundVolume();
        initialMusicVolume = soundManager.getMusicVolume();
    }

    private void setupVolumeSliders() {
        effectsVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double volume = newVal.doubleValue() / 100.0;
            effectsVolumeLabel.setText(String.format("%.0f%%", newVal.doubleValue()));
            soundManager.setSoundVolume(volume);
            userPreferences.setSoundVolume(volume);
        });
        
        musicVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double volume = newVal.doubleValue() / 100.0;
            musicVolumeLabel.setText(String.format("%.0f%%", newVal.doubleValue()));
            soundManager.setMusicVolume(volume);
            userPreferences.setMusicVolume(volume);
        });
    }

    private void setupCheckBoxes() {
        soundEnabledCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            soundManager.setSoundEnabled(newVal);
            userPreferences.setSoundEnabled(newVal);
            if (!newVal) {
                soundManager.stopAllSounds();
            }
        });

        musicEnabledCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            soundManager.setMusicEnabled(newVal);
            userPreferences.setMusicEnabled(newVal);
            if (!newVal) {
                soundManager.stopBackgroundMusic();
            } else if (oldVal != newVal) {
                // Ne jouer la musique que si l'état a changé
                soundManager.playBackgroundMusic("menu_music");
            }
        });
    }

    private void setupButtons() {
        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> handleCancel());
    }

    private void loadCurrentSettings() {
        // Charger les paramètres depuis UserPreferences
        boolean soundEnabled = userPreferences.isSoundEnabled();
        boolean musicEnabled = userPreferences.isMusicEnabled();
        double soundVolume = userPreferences.getSoundVolume();
        double musicVolume = userPreferences.getMusicVolume();

        // Appliquer les paramètres aux contrôles
        soundEnabledCheck.setSelected(soundEnabled);
        musicEnabledCheck.setSelected(musicEnabled);
        effectsVolumeSlider.setValue(soundVolume * 100);
        musicVolumeSlider.setValue(musicVolume * 100);

        // Appliquer les paramètres au SoundManager
        soundManager.setSoundEnabled(soundEnabled);
        soundManager.setMusicEnabled(musicEnabled);
        soundManager.setSoundVolume(soundVolume);
        soundManager.setMusicVolume(musicVolume);
    }

    private void handleSave() {
        // Les paramètres sont déjà sauvegardés dans UserPreferences via les listeners
        // On applique les préférences pour s'assurer que tout est cohérent
        userPreferences.applyPreferences();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Paramètres");
        alert.setHeaderText(null);
        alert.setContentText("Les paramètres ont été sauvegardés avec succès.");
        alert.showAndWait();
        
        closeWindow();
    }

    private void handleCancel() {
        // Restaurer les paramètres initiaux
        soundManager.setSoundEnabled(initialSoundEnabled);
        soundManager.setMusicEnabled(initialMusicEnabled);
        soundManager.setSoundVolume(initialSoundVolume);
        soundManager.setMusicVolume(initialMusicVolume);
        
        // Restaurer les préférences utilisateur
        userPreferences.setSoundEnabled(initialSoundEnabled);
        userPreferences.setMusicEnabled(initialMusicEnabled);
        userPreferences.setSoundVolume(initialSoundVolume);
        userPreferences.setMusicVolume(initialMusicVolume);
        userPreferences.applyPreferences();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Paramètres");
        alert.setHeaderText(null);
        alert.setContentText("Les modifications ont été annulées.");
        alert.showAndWait();
        
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
} 