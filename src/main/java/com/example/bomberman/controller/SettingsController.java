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
    
    // Pour éviter de jouer le son trop fréquemment
    private long lastSoundPlayTime = 0;
    private static final long SOUND_PLAY_DELAY = 200; // 200ms entre les sons

    // Flag pour éviter de jouer des sons lors de l'initialisation
    private boolean isInitializing = true;

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
            
            // Jouer un son pour tester le volume des effets
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastSoundPlayTime > SOUND_PLAY_DELAY && soundManager.isSoundEnabled()) {
                soundManager.playSound("powerup_collect");
                lastSoundPlayTime = currentTime;
            }
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
            } else if (!isInitializing) {
                // Jouer un son de test seulement si ce n'est pas l'initialisation
                soundManager.playSound("powerup_collect");
            }
        });

        musicEnabledCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            soundManager.setMusicEnabled(newVal);
            userPreferences.setMusicEnabled(newVal);
            if (!newVal) {
                soundManager.stopBackgroundMusic();
            } else if (oldVal != newVal && !isInitializing) {
                // Ne jouer la musique que si l'état a changé et ce n'est pas l'initialisation
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
        
        // Désactiver le flag d'initialisation
        isInitializing = false;
    }

    private void handleSave() {
        // Les paramètres sont déjà sauvegardés dans UserPreferences via les listeners
        // On applique les préférences pour s'assurer que tout est cohérent
        userPreferences.applyPreferences();
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
        
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
} 