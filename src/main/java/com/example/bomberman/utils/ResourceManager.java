package com.example.bomberman.utils;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Gestionnaire de ressources pour les sprites et les sons du jeu
 * Permet de charger les ressources en fonction du thème choisi
 */
public class ResourceManager {
    // Singleton
    private static ResourceManager instance;
    
    // Thèmes disponibles
    public enum Theme {
        DEFAULT("default"),
        DESERT("desert"),
        JUNGLE("jungle");
        
        private final String folderName;
        
        Theme(String folderName) {
            this.folderName = folderName;
        }
        
        public String getFolderName() {
            return folderName;
        }
    }
    
    // Cache pour les sprites
    private final Map<String, Image> spriteCache;
    
    // Thème actuel
    private Theme currentTheme;
    
    // Pour vérifier si JavaFX est disponible
    private boolean javafxAvailable;
    
    // Générateur de nombres aléatoires
    private final Random random;
    
    /**
     * Constructeur privé (singleton)
     */
    private ResourceManager() {
        spriteCache = new HashMap<>();
        currentTheme = Theme.DEFAULT;
        random = new Random();
        checkJavaFXAvailability();
    }
    
    /**
     * Vérifie si JavaFX est disponible
     */
    private void checkJavaFXAvailability() {
        try {
            // Vérifier si la classe Image est accessible
            Class.forName("javafx.scene.image.Image");
            javafxAvailable = true;
        } catch (ClassNotFoundException e) {
            javafxAvailable = false;
            System.out.println("ResourceManager: JavaFX Image non disponible");
        }
    }
    
    /**
     * Retourne l'instance unique du gestionnaire de ressources
     */
    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }
    
    /**
     * Définit le thème actuel
     * @param theme Le thème à utiliser
     */
    public void setTheme(Theme theme) {
        if (theme != currentTheme) {
            currentTheme = theme;
            clearCache(); // Vider le cache lors du changement de thème
            System.out.println("Thème changé pour: " + theme.getFolderName());
        }
    }
    
    /**
     * Retourne le thème actuel
     * @return Le thème actuel
     */
    public Theme getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * Vide le cache de sprites
     */
    public void clearCache() {
        spriteCache.clear();
        System.out.println("Cache de sprites vidé");
    }
    
    /**
     * Charge un sprite depuis les ressources
     * Essaie d'abord avec le thème actuel, puis avec le thème par défaut si non trouvé
     * @param spriteName Nom du sprite (sans extension)
     * @return L'image chargée ou null si non trouvée
     */
    public Image loadSprite(String spriteName) {
        // Si JavaFX n'est pas disponible, retourner null
        if (!javafxAvailable) {
            System.out.println("Impossible de charger le sprite: JavaFX non disponible");
            return null;
        }
        
        // Vérifier si le sprite est déjà dans le cache
        String cacheKey = currentTheme.getFolderName() + "/" + spriteName;
        if (spriteCache.containsKey(cacheKey)) {
            return spriteCache.get(cacheKey);
        }
        
        Image sprite = null;
        
        try {
            // Essayer de charger avec le thème actuel
            sprite = loadSpriteFromPath("/com/example/bomberman/Images/" + currentTheme.getFolderName() + "/" + spriteName + ".png");
            
            // Si non trouvé et que ce n'est pas le thème par défaut, essayer avec le thème par défaut
            if (sprite == null && currentTheme != Theme.DEFAULT) {
                System.out.println("Sprite non trouvé dans le thème " + currentTheme.getFolderName() + ", utilisation du thème par défaut");
                sprite = loadSpriteFromPath("/com/example/bomberman/Images/default/" + spriteName + ".png");
            }
            
            // Si toujours non trouvé, essayer de charger directement depuis le dossier Images
            if (sprite == null) {
                System.out.println("Sprite non trouvé dans les thèmes, recherche dans le dossier Images");
                sprite = loadSpriteFromPath("/com/example/bomberman/Images/" + spriteName + ".png");
            }
            
            // Mettre en cache si trouvé
            if (sprite != null) {
                spriteCache.put(cacheKey, sprite);
            } else {
                System.out.println("Sprite non trouvé: " + spriteName);
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement du sprite: " + spriteName + " - " + e.getMessage());
        }
        
        return sprite;
    }
    
    /**
     * Charge un sprite depuis un chemin spécifique
     * @param path Chemin vers le sprite
     * @return L'image chargée ou null si non trouvée
     */
    private Image loadSpriteFromPath(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement du sprite: " + path + " - " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Charge un son depuis les ressources
     * @param soundName Nom du son (sans extension)
     * @return Le chemin vers le son ou null si non trouvé
     */
    public String loadSound(String soundName) {
        // Essayer de charger le son MP3
        String soundPath = "/com/example/bomberman/sounds/" + soundName + ".mp3";
        
        // Vérifier si le son existe
        if (getClass().getResource(soundPath) != null) {
            return soundPath;
        } else {
            System.out.println("Son non trouvé: " + soundName);
            return null;
        }
    }
    
    /**
     * Charge une musique depuis les ressources
     * @param musicName Nom de la musique (sans extension)
     * @return Le chemin vers la musique ou null si non trouvée
     */
    public String loadMusic(String musicName) {
        // Essayer de charger la musique
        String musicPath = "/com/example/bomberman/sounds/" + musicName + ".mp3";
        
        // Vérifier si la musique existe
        if (getClass().getResource(musicPath) != null) {
            return musicPath;
        } else {
            System.out.println("Musique non trouvée: " + musicName);
            return null;
        }
    }
    
    /**
     * Charge aléatoirement une des deux variations de la musique du menu
     * @return Le chemin vers la musique du menu ou null si non trouvée
     */
    public String loadRandomMenuMusic() {
        // Choisir aléatoirement entre menu_music_A et menu_music_B
        String musicName = random.nextBoolean() ? "menu_music_A" : "menu_music_B";
        return loadMusic(musicName);
    }
    
    /**
     * Convertit une chaîne en thème
     * @param themeName Nom du thème
     * @return Le thème correspondant ou DEFAULT si non reconnu
     */
    public static Theme themeFromString(String themeName) {
        if (themeName == null) return Theme.DEFAULT;
        
        try {
            return Theme.valueOf(themeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Theme.DEFAULT;
        }
    }
    
    /**
     * Vérifie si JavaFX est disponible
     * @return true si JavaFX est disponible
     */
    public boolean isJavafxAvailable() {
        return javafxAvailable;
    }
} 