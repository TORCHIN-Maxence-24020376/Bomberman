package com.example.bomberman.utils;

import javafx.scene.image.Image;

/**
 * Gestionnaire de sprites (utilise ResourceManager)
 * Cette classe est maintenue pour la compatibilité avec le code existant
 */
public class SpriteManager {
    private static SpriteManager instance;
    private ResourceManager resourceManager;
    
    /**
     * Constructeur privé (singleton)
     */
    private SpriteManager() {
        resourceManager = ResourceManager.getInstance();
    }
    
    /**
     * Retourne l'instance unique du gestionnaire de sprites
     */
    public static SpriteManager getInstance() {
        if (instance == null) {
            instance = new SpriteManager();
        }
        return instance;
    }
    
    /**
     * Charge un sprite
     * @param spriteName Nom du sprite (sans extension)
     * @return L'image chargée ou null si non trouvée
     */
    public Image loadSprite(String spriteName) {
        return resourceManager.loadSprite(spriteName);
    }
    
    /**
     * Définit le thème à utiliser
     * @param theme Le thème à utiliser
     */
    public void setTheme(ResourceManager.Theme theme) {
        resourceManager.setTheme(theme);
    }
    
    /**
     * Définit le thème à utiliser à partir d'une chaîne
     * @param themeName Nom du thème
     */
    public void setTheme(String themeName) {
        resourceManager.setTheme(ResourceManager.themeFromString(themeName));
    }
    
    /**
     * Vide le cache de sprites
     */
    public void clearCache() {
        resourceManager.clearCache();
    }
}