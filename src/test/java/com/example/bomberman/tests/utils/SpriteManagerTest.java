package com.example.bomberman.tests.utils;

import com.example.bomberman.utils.ResourceManager;
import com.example.bomberman.utils.SpriteManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le gestionnaire de sprites
 */
@ExtendWith(JavaFXThreadingRule.class)
public class SpriteManagerTest {

    private SpriteManager spriteManager;

    @BeforeEach
    public void setUp() throws Exception {
        // Réinitialiser les instances des singletons pour chaque test
        resetSingleton(SpriteManager.class, "instance");
        resetSingleton(ResourceManager.class, "instance");
        
        // S'assurer que ResourceManager est initialisé avant SpriteManager
        ResourceManager.getInstance();
        
        spriteManager = SpriteManager.getInstance();
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Réinitialiser les instances des singletons après chaque test
        resetSingleton(SpriteManager.class, "instance");
        resetSingleton(ResourceManager.class, "instance");
    }

    @Test
    public void testGetInstance() {
        // Vérifier que getInstance retourne toujours la même instance
        SpriteManager instance1 = SpriteManager.getInstance();
        SpriteManager instance2 = SpriteManager.getInstance();
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }

    @Test
    public void testLoadSprite() {
        // Vérifier que loadSprite ne lance pas d'exception
        assertDoesNotThrow(() -> spriteManager.loadSprite("player"));
    }

    @Test
    public void testSetThemeEnum() {
        // Vérifier que setTheme avec enum ne lance pas d'exception
        assertDoesNotThrow(() -> spriteManager.setTheme(ResourceManager.Theme.DESERT));
    }

    @Test
    public void testSetThemeString() {
        // Vérifier que setTheme avec string ne lance pas d'exception
        assertDoesNotThrow(() -> spriteManager.setTheme("jungle"));
    }

    @Test
    public void testClearCache() {
        // Vérifier que clearCache ne lance pas d'exception
        assertDoesNotThrow(() -> spriteManager.clearCache());
    }

    /**
     * Réinitialise l'instance singleton pour les tests
     */
    private void resetSingleton(Class<?> clazz, String fieldName) throws Exception {
        java.lang.reflect.Field instance = clazz.getDeclaredField(fieldName);
        instance.setAccessible(true);
        instance.set(null, null);
    }
} 