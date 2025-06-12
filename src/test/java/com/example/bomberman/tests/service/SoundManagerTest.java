package com.example.bomberman.tests.service;

import com.example.bomberman.service.SoundManager;
import com.example.bomberman.utils.ResourceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour le gestionnaire de sons
 */
public class SoundManagerTest {
    
    private SoundManager soundManager;
    
    @Mock
    private ResourceManager resourceManagerMock;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        soundManager = SoundManager.getInstance();
    }

    @Test
    public void testGetInstance() {
        // Vérifier que getInstance ne lance pas d'exception
        assertDoesNotThrow(() -> SoundManager.getInstance());
        
        // Vérifier que getInstance retourne toujours la même instance
        SoundManager instance1 = SoundManager.getInstance();
        SoundManager instance2 = SoundManager.getInstance();
        assertSame(instance1, instance2, "getInstance doit retourner la même instance");
    }
    
    @Test
    public void testSoundVolume() {
        // Valeur par défaut
        assertEquals(0.5, soundManager.getSoundVolume(), 0.01, "Le volume des sons par défaut doit être 0.5");
        
        // Définir une nouvelle valeur
        soundManager.setSoundVolume(0.75);
        assertEquals(0.75, soundManager.getSoundVolume(), 0.01, "Le volume des sons doit être 0.75");
        
        // Valeur hors limites (doit être limité entre 0 et 1)
        soundManager.setSoundVolume(1.5);
        assertEquals(1.0, soundManager.getSoundVolume(), 0.01, "Le volume des sons doit être limité à 1.0");
        
        soundManager.setSoundVolume(-0.5);
        assertEquals(0.0, soundManager.getSoundVolume(), 0.01, "Le volume des sons doit être limité à 0.0");
    }
    
    @Test
    public void testMusicVolume() {
        // Valeur par défaut
        assertEquals(0.3, soundManager.getMusicVolume(), 0.01, "Le volume de la musique par défaut doit être 0.3");
        
        // Définir une nouvelle valeur
        soundManager.setMusicVolume(0.6);
        assertEquals(0.6, soundManager.getMusicVolume(), 0.01, "Le volume de la musique doit être 0.6");
        
        // Valeur hors limites (doit être limité entre 0 et 1)
        soundManager.setMusicVolume(1.5);
        assertEquals(1.0, soundManager.getMusicVolume(), 0.01, "Le volume de la musique doit être limité à 1.0");
        
        soundManager.setMusicVolume(-0.5);
        assertEquals(0.0, soundManager.getMusicVolume(), 0.01, "Le volume de la musique doit être limité à 0.0");
    }
    
    @Test
    public void testSoundEnabled() {
        // Par défaut, les sons sont activés
        assertTrue(soundManager.isSoundEnabled(), "Les sons doivent être activés par défaut");
        
        // Désactiver les sons
        soundManager.setSoundEnabled(false);
        assertFalse(soundManager.isSoundEnabled(), "Les sons doivent être désactivés");
        
        // Réactiver les sons
        soundManager.setSoundEnabled(true);
        assertTrue(soundManager.isSoundEnabled(), "Les sons doivent être réactivés");
    }
    
    @Test
    public void testMusicEnabled() {
        // Par défaut, la musique est activée
        assertTrue(soundManager.isMusicEnabled(), "La musique doit être activée par défaut");
        
        // Désactiver la musique
        soundManager.setMusicEnabled(false);
        assertFalse(soundManager.isMusicEnabled(), "La musique doit être désactivée");
        
        // Réactiver la musique
        soundManager.setMusicEnabled(true);
        assertTrue(soundManager.isMusicEnabled(), "La musique doit être réactivée");
    }
    
    @Test
    public void testPlaySound() {
        // Ce test vérifie simplement que la méthode ne lance pas d'exception
        assertDoesNotThrow(() -> soundManager.playSound("bomb_place"), 
                "La lecture d'un son ne doit pas lancer d'exception");
    }
    
    @Test
    public void testPlayBackgroundMusic() {
        // Ce test vérifie simplement que la méthode ne lance pas d'exception
        assertDoesNotThrow(() -> soundManager.playBackgroundMusic("game_music"), 
                "La lecture de la musique ne doit pas lancer d'exception");
    }
} 