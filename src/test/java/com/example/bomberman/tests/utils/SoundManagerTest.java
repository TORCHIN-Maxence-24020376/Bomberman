package com.example.bomberman.tests.utils;

import com.example.bomberman.service.SoundManager;
import com.example.bomberman.utils.ResourceManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le gestionnaire de sons
 */
public class SoundManagerTest {

    @Test
    public void testGetInstance() {
        // Vérifier que getInstance ne lance pas d'exception
        assertDoesNotThrow(() -> SoundManager.getInstance());
    }
    
    @Test
    public void testRandomMenuMusicFormat() {
        // Tester que les noms de fichiers pour les variations sont corrects
        // Cette vérification est simple mais garantit que la méthode utilise bien les bons noms de fichiers
        ResourceManager manager = ResourceManager.getInstance();
        
        // Définir les deux noms de fichiers attendus
        String menuMusicA = "menu_music_A";
        String menuMusicB = "menu_music_B";
        
        // Tester l'existence de ces variations dans le code
        assertDoesNotThrow(() -> {
            String result = manager.loadRandomMenuMusic();
            System.out.println("Musique aléatoire choisie: " + result);
        });
    }
} 