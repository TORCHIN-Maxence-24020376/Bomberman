package com.example.bomberman.tests.utils;

import com.example.bomberman.utils.ResourceManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le gestionnaire de ressources
 */
public class ResourceManagerTest {

    @Test
    public void testThemeFromString() {
        // Vérifier la conversion de chaîne en thème
        assertEquals(ResourceManager.Theme.DEFAULT, ResourceManager.themeFromString("default"));
        assertEquals(ResourceManager.Theme.DESERT, ResourceManager.themeFromString("DESERT"));
        assertEquals(ResourceManager.Theme.JUNGLE, ResourceManager.themeFromString("jungle"));
        
        // Cas d'erreur
        assertEquals(ResourceManager.Theme.DEFAULT, ResourceManager.themeFromString("invalid_theme"));
        assertEquals(ResourceManager.Theme.DEFAULT, ResourceManager.themeFromString(null));
    }

    @Test
    public void testThemeEnumValues() {
        // Vérifier les valeurs de l'énumération Theme
        assertEquals("default", ResourceManager.Theme.DEFAULT.getFolderName());
        assertEquals("desert", ResourceManager.Theme.DESERT.getFolderName());
        assertEquals("jungle", ResourceManager.Theme.JUNGLE.getFolderName());
    }
} 