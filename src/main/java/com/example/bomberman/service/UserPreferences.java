package com.example.bomberman.service;

import com.example.bomberman.utils.ResourceManager;

import java.io.*;
import java.util.Properties;

/**
 * Gestionnaire des préférences utilisateur (son, musique, thème)
 */
public class UserPreferences {
    private static UserPreferences instance;
    private static final String PREFS_FILE = "profiles/preferences.properties";
    private Properties properties;

    // Clés des propriétés
    private static final String SOUND_ENABLED = "sound.enabled";
    private static final String MUSIC_ENABLED = "music.enabled";
    private static final String SOUND_VOLUME = "sound.volume";
    private static final String MUSIC_VOLUME = "music.volume";
    private static final String THEME = "theme";

    // Valeurs par défaut
    private static final boolean DEFAULT_SOUND_ENABLED = true;
    private static final boolean DEFAULT_MUSIC_ENABLED = true;
    private static final double DEFAULT_SOUND_VOLUME = 0.5;
    private static final double DEFAULT_MUSIC_VOLUME = 0.3;
    private static final String DEFAULT_THEME = "DEFAULT";

    /**
     * Constructeur privé (singleton)
     */
    private UserPreferences() {
        properties = new Properties();
        createPreferencesDirectory();
        loadPreferences();
    }

    /**
     * Retourne l'instance unique du gestionnaire de préférences
     */
    public static UserPreferences getInstance() {
        if (instance == null) {
            instance = new UserPreferences();
        }
        return instance;
    }

    /**
     * Crée le répertoire des préférences s'il n'existe pas
     */
    private void createPreferencesDirectory() {
        File dir = new File("profiles");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Charge les préférences depuis le fichier
     */
    private void loadPreferences() {
        File file = new File(PREFS_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);
                System.out.println("Préférences utilisateur chargées");
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement des préférences: " + e.getMessage());
                setDefaultPreferences();
            }
        } else {
            setDefaultPreferences();
        }
    }

    /**
     * Sauvegarde les préférences dans le fichier
     */
    public void savePreferences() {
        try (FileOutputStream fos = new FileOutputStream(PREFS_FILE)) {
            properties.store(fos, "Bomberman User Preferences");
            System.out.println("Préférences utilisateur sauvegardées");
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde des préférences: " + e.getMessage());
        }
    }

    /**
     * Définit les préférences par défaut
     */
    private void setDefaultPreferences() {
        properties.setProperty(SOUND_ENABLED, String.valueOf(DEFAULT_SOUND_ENABLED));
        properties.setProperty(MUSIC_ENABLED, String.valueOf(DEFAULT_MUSIC_ENABLED));
        properties.setProperty(SOUND_VOLUME, String.valueOf(DEFAULT_SOUND_VOLUME));
        properties.setProperty(MUSIC_VOLUME, String.valueOf(DEFAULT_MUSIC_VOLUME));
        properties.setProperty(THEME, DEFAULT_THEME);
        savePreferences();
    }

    /**
     * Applique les préférences au SoundManager et au ResourceManager
     */
    public void applyPreferences() {
        SoundManager soundManager = SoundManager.getInstance();
        ResourceManager resourceManager = ResourceManager.getInstance();

        // Appliquer les paramètres audio
        soundManager.setSoundEnabled(isSoundEnabled());
        soundManager.setMusicEnabled(isMusicEnabled());
        soundManager.setSoundVolume(getSoundVolume());
        soundManager.setMusicVolume(getMusicVolume());

        // Appliquer le thème
        ResourceManager.Theme theme = ResourceManager.themeFromString(getTheme());
        resourceManager.setTheme(theme);
        soundManager.setTheme(theme);
    }

    // Getters et setters

    public boolean isSoundEnabled() {
        return Boolean.parseBoolean(properties.getProperty(SOUND_ENABLED, String.valueOf(DEFAULT_SOUND_ENABLED)));
    }

    public void setSoundEnabled(boolean enabled) {
        properties.setProperty(SOUND_ENABLED, String.valueOf(enabled));
        savePreferences();
    }

    public boolean isMusicEnabled() {
        return Boolean.parseBoolean(properties.getProperty(MUSIC_ENABLED, String.valueOf(DEFAULT_MUSIC_ENABLED)));
    }

    public void setMusicEnabled(boolean enabled) {
        properties.setProperty(MUSIC_ENABLED, String.valueOf(enabled));
        savePreferences();
    }

    public double getSoundVolume() {
        try {
            return Double.parseDouble(properties.getProperty(SOUND_VOLUME, String.valueOf(DEFAULT_SOUND_VOLUME)));
        } catch (NumberFormatException e) {
            return DEFAULT_SOUND_VOLUME;
        }
    }

    public void setSoundVolume(double volume) {
        properties.setProperty(SOUND_VOLUME, String.valueOf(volume));
        savePreferences();
    }

    public double getMusicVolume() {
        try {
            return Double.parseDouble(properties.getProperty(MUSIC_VOLUME, String.valueOf(DEFAULT_MUSIC_VOLUME)));
        } catch (NumberFormatException e) {
            return DEFAULT_MUSIC_VOLUME;
        }
    }

    public void setMusicVolume(double volume) {
        properties.setProperty(MUSIC_VOLUME, String.valueOf(volume));
        savePreferences();
    }

    public String getTheme() {
        return properties.getProperty(THEME, DEFAULT_THEME);
    }

    public void setTheme(String theme) {
        properties.setProperty(THEME, theme);
        savePreferences();
    }
} 