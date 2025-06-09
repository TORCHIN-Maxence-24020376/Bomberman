package com.example.bomberman.tests.service;

import com.example.bomberman.service.SoundManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class SoundManagerTest {

    private SoundManager soundManager;

    @BeforeEach
    public void setUp() throws Exception {
        // Réinitialiser l'instance singleton pour les tests
        resetSingleton();
        
        // Obtenir une nouvelle instance
        soundManager = SoundManager.getInstance();
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        // Réinitialiser l'instance singleton après les tests
        resetSingleton();
    }
    
    @Test
    public void testGetInstance() {
        // Vérifier que getInstance retourne toujours la même instance
        SoundManager instance1 = SoundManager.getInstance();
        SoundManager instance2 = SoundManager.getInstance();
        
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }
    
    @Test
    public void testInitialState() {
        // Vérifier l'état initial
        assertTrue(soundManager.isSoundEnabled());
        assertTrue(soundManager.isMusicEnabled());
        assertEquals(0.5, soundManager.getSoundVolume(), 0.01);
        assertEquals(0.3, soundManager.getMusicVolume(), 0.01);
    }
    
    @Test
    public void testSetSoundEnabled() {
        // État initial
        assertTrue(soundManager.isSoundEnabled());
        
        // Désactiver les sons
        soundManager.setSoundEnabled(false);
        assertFalse(soundManager.isSoundEnabled());
        
        // Réactiver les sons
        soundManager.setSoundEnabled(true);
        assertTrue(soundManager.isSoundEnabled());
    }
    
    @Test
    public void testSetMusicEnabled() {
        // État initial
        assertTrue(soundManager.isMusicEnabled());
        
        // Désactiver la musique
        soundManager.setMusicEnabled(false);
        assertFalse(soundManager.isMusicEnabled());
        
        // Réactiver la musique
        soundManager.setMusicEnabled(true);
        assertTrue(soundManager.isMusicEnabled());
    }
    
    @Test
    public void testSetSoundVolume() {
        // État initial
        assertEquals(0.5, soundManager.getSoundVolume(), 0.01);
        
        // Modifier le volume
        soundManager.setSoundVolume(0.8);
        assertEquals(0.8, soundManager.getSoundVolume(), 0.01);
        
        // Tester les limites
        soundManager.setSoundVolume(-0.5); // Devrait être limité à 0
        assertEquals(0.0, soundManager.getSoundVolume(), 0.01);
        
        soundManager.setSoundVolume(1.5); // Devrait être limité à 1
        assertEquals(1.0, soundManager.getSoundVolume(), 0.01);
    }
    
    @Test
    public void testSetMusicVolume() {
        // État initial
        assertEquals(0.3, soundManager.getMusicVolume(), 0.01);
        
        // Modifier le volume
        soundManager.setMusicVolume(0.7);
        assertEquals(0.7, soundManager.getMusicVolume(), 0.01);
        
        // Tester les limites
        soundManager.setMusicVolume(-0.5); // Devrait être limité à 0
        assertEquals(0.0, soundManager.getMusicVolume(), 0.01);
        
        soundManager.setMusicVolume(1.5); // Devrait être limité à 1
        assertEquals(1.0, soundManager.getMusicVolume(), 0.01);
    }
    
    @Test
    public void testPlaySound() {
        // Vérifier que la méthode ne lève pas d'exception
        assertDoesNotThrow(() -> soundManager.playSound("bomb_place"));
        
        // Désactiver les sons et vérifier que la méthode ne lève pas d'exception
        soundManager.setSoundEnabled(false);
        assertDoesNotThrow(() -> soundManager.playSound("bomb_place"));
    }
    
    @Test
    public void testPlayBackgroundMusic() {
        // Vérifier que la méthode ne lève pas d'exception
        assertDoesNotThrow(() -> soundManager.playBackgroundMusic("/sounds/menu_music.mp3"));
        
        // Désactiver la musique et vérifier que la méthode ne lève pas d'exception
        soundManager.setMusicEnabled(false);
        assertDoesNotThrow(() -> soundManager.playBackgroundMusic("/sounds/menu_music.mp3"));
    }
    
    @Test
    public void testStopBackgroundMusic() {
        // Vérifier que la méthode ne lève pas d'exception
        assertDoesNotThrow(() -> soundManager.stopBackgroundMusic());
    }
    
    // Méthode utilitaire pour la réflexion
    private void resetSingleton() throws Exception {
        Field instance = SoundManager.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
} 