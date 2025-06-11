package com.example.bomberman.service;

import com.example.bomberman.utils.ResourceManager;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire des sons du jeu
 */
public class SoundManager {
    private static SoundManager instance;
    private Map<String, String> soundPaths;
    private boolean soundEnabled = true;
    private boolean musicEnabled = true;
    private double soundVolume = 0.5;
    private double musicVolume = 0.3;
    private boolean javafxMediaAvailable = false;
    private Object mediaPlayer; // Pour la musique de fond
    private Map<String, Object> soundPlayers; // Pour les effets sonores
    private ResourceManager resourceManager;

    private SoundManager() {
        soundPaths = new HashMap<>();
        soundPlayers = new HashMap<>();
        resourceManager = ResourceManager.getInstance();
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
            // Ajouter les chemins par défaut pour la compatibilité avec le code existant
            soundPaths.put("bomb_place", "/com/example/bomberman/sounds/bomb_place.mp3");
            soundPaths.put("bomb_explode", "/com/example/bomberman/sounds/bomb_explode.mp3");
            soundPaths.put("powerup_collect", "/com/example/bomberman/sounds/power_up_collect.mp3");
            soundPaths.put("powerup_destroy", "/com/example/bomberman/sounds/powerup_destroy.mp3");
            soundPaths.put("skull_effect", "/com/example/bomberman/sounds/skull_effect.mp3");
            
            // Essayer de charger avec ResourceManager
            String bombPlacePath = resourceManager.loadSound("bomb_place");
            if (bombPlacePath != null) {
                soundPaths.put("bomb_place", bombPlacePath);
            }
            
            String bombExplodePath = resourceManager.loadSound("bomb_explode");
            if (bombExplodePath != null) {
                soundPaths.put("bomb_explode", bombExplodePath);
            }
            
            String powerupCollectPath = resourceManager.loadSound("power_up_collect");
            if (powerupCollectPath != null) {
                soundPaths.put("powerup_collect", powerupCollectPath);
            }
            
            String powerupDestroyPath = resourceManager.loadSound("powerup_destroy");
            if (powerupDestroyPath != null) {
                soundPaths.put("powerup_destroy", powerupDestroyPath);
            }
            
            String skullEffectPath = resourceManager.loadSound("skull_effect");
            if (skullEffectPath != null) {
                soundPaths.put("skull_effect", skullEffectPath);
            }

            if (javafxMediaAvailable) {
                System.out.println("SoundManager: Prêt pour les sons JavaFX");
                preloadSounds();
            } else {
                System.out.println("SoundManager: Mode sons système activé");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des sons: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Précharge les sons pour une lecture plus rapide
     */
    private void preloadSounds() {
        if (!javafxMediaAvailable) return;
        
        try {
            for (Map.Entry<String, String> entry : soundPaths.entrySet()) {
                String soundName = entry.getKey();
                String soundPath = entry.getValue();
                
                try {
                    // Utiliser la réflexion pour créer les objets Media et MediaPlayer
                    Class<?> mediaClass = Class.forName("javafx.scene.media.Media");
                    Class<?> mediaPlayerClass = Class.forName("javafx.scene.media.MediaPlayer");
                    
                    // Créer l'objet Media
                    Object media = mediaClass.getConstructor(String.class)
                            .newInstance(getClass().getResource(soundPath).toExternalForm());
                    
                    // Créer l'objet MediaPlayer
                    Object player = mediaPlayerClass.getConstructor(mediaClass)
                            .newInstance(media);
                    
                    // Configurer le volume
                    mediaPlayerClass.getMethod("setVolume", double.class)
                            .invoke(player, soundVolume);
                    
                    // Ajouter au cache
                    soundPlayers.put(soundName, player);
                    
                    System.out.println("Son préchargé: " + soundName);
                } catch (Exception e) {
                    System.err.println("Erreur lors du préchargement du son " + soundName + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du préchargement des sons: " + e.getMessage());
        }
    }

    /**
     * Joue un effet sonore
     */
    public void playSound(String soundName) {
        if (!soundEnabled) return;

        try {
            if (javafxMediaAvailable) {
                System.out.println("Tentative de jouer le son: " + soundName);
                playJavaFXSound(soundName);
            } else {
                playSystemSound(soundName);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du son: " + soundName + " - " + e.getMessage());
            e.printStackTrace();
            // Fallback sur le son système
            playSystemSound(soundName);
        }
    }

    /**
     * Joue un son avec JavaFX
     */
    private void playJavaFXSound(String soundName) {
        if (!javafxMediaAvailable) return;
        
        try {
            if (soundPlayers.containsKey(soundName)) {
                // Utiliser le lecteur préchargé
                Object player = soundPlayers.get(soundName);
                Class<?> mediaPlayerClass = Class.forName("javafx.scene.media.MediaPlayer");
                
                // Arrêter si déjà en cours
                mediaPlayerClass.getMethod("stop").invoke(player);
                
                // Configurer le volume
                mediaPlayerClass.getMethod("setVolume", double.class).invoke(player, soundVolume);
                
                // Retourner au début
                Class<?> durationType = Class.forName("javafx.util.Duration");
                Object zeroDuration = durationType.getField("ZERO").get(null);
                mediaPlayerClass.getMethod("seek", durationType).invoke(player, zeroDuration);
                
                // Jouer
                mediaPlayerClass.getMethod("play").invoke(player);
                
                System.out.println("Son joué (préchargé): " + soundName);
            } else if (soundPaths.containsKey(soundName)) {
                // Créer un nouveau lecteur
                String soundPath = soundPaths.get(soundName);
                
                System.out.println("Création d'un nouveau lecteur pour: " + soundName + " (chemin: " + soundPath + ")");
                
                // Vérifier si la ressource existe
                if (getClass().getResource(soundPath) == null) {
                    System.err.println("Ressource son introuvable: " + soundPath);
                    playSystemSound(soundName);
                    return;
                }
                
                try {
                    // Utiliser la réflexion pour créer les objets Media et MediaPlayer
                    Class<?> mediaClass = Class.forName("javafx.scene.media.Media");
                    Class<?> mediaPlayerClass = Class.forName("javafx.scene.media.MediaPlayer");
                    
                    // Créer l'objet Media
                    Object media = mediaClass.getConstructor(String.class)
                            .newInstance(getClass().getResource(soundPath).toExternalForm());
                    
                    // Créer l'objet MediaPlayer
                    Object player = mediaPlayerClass.getConstructor(mediaClass)
                            .newInstance(media);
                    
                    // Configurer le volume
                    mediaPlayerClass.getMethod("setVolume", double.class)
                            .invoke(player, soundVolume);
                    
                    // Jouer
                    mediaPlayerClass.getMethod("play").invoke(player);
                    
                    System.out.println("Son joué (nouveau lecteur): " + soundName);
                } catch (Exception e) {
                    System.err.println("Erreur détaillée lors de la lecture du son: " + e.getMessage());
                    e.printStackTrace();
                    playSystemSound(soundName);
                }
            } else {
                System.out.println("Son non trouvé: " + soundName);
                playSystemSound(soundName);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du son JavaFX: " + soundName + " - " + e.getMessage());
            e.printStackTrace();
            playSystemSound(soundName);
        }
    }

    /**
     * Joue un son système simple
     */
    private void playSystemSound(String soundName) {
        try {
            Toolkit.getDefaultToolkit().beep();
            System.out.println("Son système joué: " + soundName);
        } catch (Exception e) {
            System.err.println("Impossible de jouer le son système: " + e.getMessage());
        }
    }

    /**
     * Joue la musique de fond
     */
    public void playBackgroundMusic(String musicPath) {
        if (!musicEnabled) return;

        try {
            stopBackgroundMusic(); // Arrêter la musique en cours
            
            System.out.println("Tentative de jouer la musique: " + musicPath);
            
            // Vérifier si c'est un chemin complet ou juste un nom de musique
            String resolvedPath = null;
            
            if (musicPath.startsWith("/")) {
                // Chemin complet
                resolvedPath = musicPath;
            } else if ("menu".equals(musicPath) || "menu_music".equals(musicPath)) {
                // Musique du menu - choisir aléatoirement entre les variations
                resolvedPath = resourceManager.loadRandomMenuMusic();
            } else {
                // Autre musique
                resolvedPath = resourceManager.loadMusic(musicPath);
            }
            
            if (resolvedPath != null) {
                System.out.println("Chemin de musique résolu: " + resolvedPath);
                if (javafxMediaAvailable) {
                    playJavaFXMusic(resolvedPath);
                } else {
                    System.out.println("Musique simulée: " + resolvedPath);
                }
            } else {
                System.out.println("Musique non trouvée: " + musicPath);
            }
        } catch (Exception e) {
            System.err.println("Impossible de jouer la musique: " + musicPath + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Joue la musique avec JavaFX Media
     */
    private void playJavaFXMusic(String musicPath) {
        if (!javafxMediaAvailable) return;
        
        try {
            System.out.println("Création du lecteur de musique pour: " + musicPath);
            
            // Vérifier si la ressource existe
            if (getClass().getResource(musicPath) == null) {
                System.err.println("Ressource musique introuvable: " + musicPath);
                return;
            }
            
            // Utiliser la réflexion pour créer les objets Media et MediaPlayer
            Class<?> mediaClass = Class.forName("javafx.scene.media.Media");
            Class<?> mediaPlayerClass = Class.forName("javafx.scene.media.MediaPlayer");
            
            // Créer l'objet Media
            String mediaUrl = getClass().getResource(musicPath).toExternalForm();
            System.out.println("URL de la musique: " + mediaUrl);
            
            Object media = mediaClass.getConstructor(String.class).newInstance(mediaUrl);
            
            // Créer l'objet MediaPlayer
            mediaPlayer = mediaPlayerClass.getConstructor(mediaClass)
                    .newInstance(media);
            
            // Configurer le volume
            mediaPlayerClass.getMethod("setVolume", double.class)
                    .invoke(mediaPlayer, musicVolume);
            
            // Configurer la lecture en boucle
            Object indefinite = mediaPlayerClass.getField("INDEFINITE").get(null);
            mediaPlayerClass.getMethod("setCycleCount", int.class)
                    .invoke(mediaPlayer, indefinite);
            
            // Jouer
            mediaPlayerClass.getMethod("play").invoke(mediaPlayer);
            
            System.out.println("Musique démarrée: " + musicPath);
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture de la musique JavaFX: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Arrête la musique de fond
     */
    public void stopBackgroundMusic() {
        try {
            if (mediaPlayer != null) {
                Class<?> mediaPlayerClass = Class.forName("javafx.scene.media.MediaPlayer");
                
                // Arrêter la lecture
                mediaPlayerClass.getMethod("stop").invoke(mediaPlayer);
                
                // Libérer les ressources
                mediaPlayerClass.getMethod("dispose").invoke(mediaPlayer);
                
                mediaPlayer = null;
                System.out.println("Musique arrêtée");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'arrêt de la musique: " + e.getMessage());
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
        
        // Mettre à jour le volume des lecteurs préchargés
        if (javafxMediaAvailable) {
            try {
                Class<?> mediaPlayerClass = Class.forName("javafx.scene.media.MediaPlayer");
                for (Object player : soundPlayers.values()) {
                    mediaPlayerClass.getMethod("setVolume", double.class)
                        .invoke(player, this.soundVolume);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la mise à jour du volume des sons: " + e.getMessage());
            }
        }
    }

    /**
     * Définit le volume de la musique
     */
    public void setMusicVolume(double volume) {
        this.musicVolume = Math.max(0.0, Math.min(1.0, volume));
        System.out.println("Volume de la musique: " + (int)(this.musicVolume * 100) + "%");
        
        // Mettre à jour le volume du lecteur de musique
        if (javafxMediaAvailable && mediaPlayer != null) {
            try {
                Class<?> mediaPlayerClass = Class.forName("javafx.scene.media.MediaPlayer");
                mediaPlayerClass.getMethod("setVolume", double.class)
                    .invoke(mediaPlayer, this.musicVolume);
            } catch (Exception e) {
                System.err.println("Erreur lors de la mise à jour du volume de la musique: " + e.getMessage());
            }
        }
    }

    /**
     * Définit le thème à utiliser pour les ressources
     */
    public void setTheme(ResourceManager.Theme theme) {
        resourceManager.setTheme(theme);
    }

    // Getters
    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isMusicEnabled() { return musicEnabled; }
    public double getSoundVolume() { return soundVolume; }
    public double getMusicVolume() { return musicVolume; }
    public boolean isJavaFXMediaAvailable() { return javafxMediaAvailable; }
}