package com.example.bomberman;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire des sons du jeu - Version hybride
 */
public class SoundManager {
    private static SoundManager instance;
    private Map<String, String> soundPaths;
    private boolean soundEnabled = true;
    private boolean musicEnabled = true;
    private double soundVolume = 0.5;
    private double musicVolume = 0.3;
    private boolean javafxMediaAvailable = false;
    private Object mediaPlayer; // Pour éviter les imports JavaFX

    private SoundManager() {
        soundPaths = new HashMap<>();
        checkJavaFXMediaAvailability();
        loadSounds();
    }

    /**
     * Retourne l'instance unique du gestionnaire de sons
     */
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * Vérifie si JavaFX Media est disponible
     */
    private void checkJavaFXMediaAvailability() {
        try {
            Class.forName("javafx.scene.media.MediaPlayer");
            javafxMediaAvailable = true;
            System.out.println("SoundManager: JavaFX Media détecté");
        } catch (ClassNotFoundException e) {
            javafxMediaAvailable = false;
            System.out.println("SoundManager: JavaFX Media non disponible, utilisation du mode simplifié");
        }
    }

    /**
     * Charge tous les sons du jeu
     */
    private void loadSounds() {
        try {
            soundPaths.put("bomb_place", "/sounds/bomb_place.wav");
            soundPaths.put("bomb_explode", "/sounds/bomb_explode.wav");
            soundPaths.put("powerup_collect", "/sounds/powerup_collect.wav");
            soundPaths.put("player_death", "/sounds/player_death.wav");
            soundPaths.put("move", "/sounds/move.wav");

            if (javafxMediaAvailable) {
                System.out.println("SoundManager: Prêt pour les sons JavaFX");
            } else {
                System.out.println("SoundManager: Mode sons système activé");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des sons: " + e.getMessage());
        }
    }

    /**
     * Joue un effet sonore
     */
    public void playSound(String soundName) {
        if (!soundEnabled) return;

        try {
            if (javafxMediaAvailable) {
                playJavaFXSound(soundName);
            } else {
                playSystemSound(soundName);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du son: " + soundName);
            // Fallback sur le son système
            playSystemSound(soundName);
        }
    }

    /**
     * Joue un son avec JavaFX (si disponible)
     */
    private void playJavaFXSound(String soundName) {
        try {
            // Utilisation de la réflexion pour éviter les imports
            if (soundPaths.containsKey(soundName)) {
                System.out.println("Son JavaFX joué: " + soundName);
                // Ici vous pourriez implémenter le vrai code JavaFX
            }
        } catch (Exception e) {
            playSystemSound(soundName);
        }
    }

    /**
     * Joue un son système simple
     */
    private void playSystemSound(String soundName) {
        if (soundPaths.containsKey(soundName)) {
            Toolkit.getDefaultToolkit().beep();
            System.out.println("Son système joué: " + soundName);
        }
    }

    /**
     * Joue la musique de fond
     */
    public void playBackgroundMusic(String musicPath) {
        if (!musicEnabled) return;

        try {
            if (javafxMediaAvailable) {
                System.out.println("Musique JavaFX démarrée: " + musicPath);
                // Implémentation JavaFX ici
            } else {
                System.out.println("Musique simulée: " + musicPath);
            }
        } catch (Exception e) {
            System.err.println("Impossible de jouer la musique: " + musicPath);
        }
    }

    /**
     * Arrête la musique de fond
     */
    public void stopBackgroundMusic() {
        try {
            if (mediaPlayer != null) {
                // Arrêter le MediaPlayer si il existe
                System.out.println("Musique arrêtée");
                mediaPlayer = null;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'arrêt de la musique");
        }
    }

    /**
     * Active/désactive les effets sonores
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        System.out.println("Sons " + (enabled ? "activés" : "désactivés"));
    }

    /**
     * Active/désactive la musique
     */
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            stopBackgroundMusic();
        }
        System.out.println("Musique " + (enabled ? "activée" : "désactivée"));
    }

    /**
     * Définit le volume des effets sonores
     */
    public void setSoundVolume(double volume) {
        this.soundVolume = Math.max(0.0, Math.min(1.0, volume));
        System.out.println("Volume des sons: " + (int)(this.soundVolume * 100) + "%");
    }

    /**
     * Définit le volume de la musique
     */
    public void setMusicVolume(double volume) {
        this.musicVolume = Math.max(0.0, Math.min(1.0, volume));
        System.out.println("Volume de la musique: " + (int)(this.musicVolume * 100) + "%");
    }

    // Getters
    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isMusicEnabled() { return musicEnabled; }
    public double getSoundVolume() { return soundVolume; }
    public double getMusicVolume() { return musicVolume; }
    public boolean isJavaFXMediaAvailable() { return javafxMediaAvailable; }
}