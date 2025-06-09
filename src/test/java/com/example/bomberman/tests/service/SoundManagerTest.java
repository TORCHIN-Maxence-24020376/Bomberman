package com.example.bomberman.tests.service;

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
} 