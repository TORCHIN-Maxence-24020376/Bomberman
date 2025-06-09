package com.example.bomberman.tests.models.world;

import com.example.bomberman.models.entities.Bomb;
import com.example.bomberman.models.entities.Player;
import com.example.bomberman.models.entities.PlayerProfile;
import com.example.bomberman.models.world.Game;
import com.example.bomberman.models.world.GameBoard;
import com.example.bomberman.service.SoundManager;
import com.example.bomberman.tests.utils.JavaFXThreadingRule;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFXThreadingRule.class)
public class GameTest {

    private Game game;
    
    @Mock
    private GameBoard mockBoard;
    
    @Mock
    private SoundManager mockSoundManager;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        game = new Game();
    }
    
    @Test
    public void testInitialState() {
        assertNotNull(game.getBoard());
        assertNotNull(game.getPlayer1());
        assertNotNull(game.getPlayer2());
        assertTrue(game.getBombs().isEmpty());
        assertTrue(game.isGameRunning());
        assertEquals(0, game.getPlayer1Score());
        assertEquals(0, game.getPlayer2Score());
        assertEquals(0, game.getBombsPlaced());
        assertEquals(0, game.getWallsDestroyed());
    }
    
    @Test
    public void testSetPlayerProfiles() {
        // Créer des profils
        PlayerProfile profile1 = new PlayerProfile("Player", "One", "", 10, 5, "default");
        PlayerProfile profile2 = new PlayerProfile("Player", "Two", "", 8, 3, "default");
        
        // Assigner les profils
        game.setPlayerProfiles(profile1, profile2);
        
        // Vérifier que les profils ont été assignés
        assertEquals(profile1, game.getPlayer1().getProfile());
        assertEquals(profile2, game.getPlayer2().getProfile());
    }
    
    @Test
    public void testHandleKeyPressed() {
        try (MockedStatic<SoundManager> mockedStatic = Mockito.mockStatic(SoundManager.class)) {
            // Mock SoundManager.getInstance()
            mockedStatic.when(SoundManager::getInstance).thenReturn(mockSoundManager);
            
            // État initial
            assertEquals(0, game.getBombs().size());
            
            // Placer une bombe avec le joueur 1 (touche A)
            game.handleKeyPressed(KeyCode.A);
            assertEquals(1, game.getBombs().size());
            assertEquals(1, game.getBombsPlaced());
            
            // Placer une bombe avec le joueur 2 (touche ESPACE)
            game.handleKeyPressed(KeyCode.SPACE);
            assertEquals(2, game.getBombs().size());
            assertEquals(2, game.getBombsPlaced());
        }
    }
    
    @Test
    public void testLoadLevel() {
        // Créer un fichier de niveau temporaire pour le test
        try {
            File tempFile = File.createTempFile("test_level", ".level");
            tempFile.deleteOnExit();
            
            // Écrire un niveau simple
            try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
                writer.println("9,9"); // Dimensions
                writer.println("1,1,7,7"); // Positions des joueurs
                
                // Données du niveau (9x9)
                for (int y = 0; y < 9; y++) {
                    for (int x = 0; x < 9; x++) {
                        if (x == 0 || y == 0 || x == 8 || y == 8) {
                            writer.print("1"); // Mur
                        } else if (x % 2 == 0 && y % 2 == 0) {
                            writer.print("1"); // Mur fixe
                        } else {
                            writer.print("0"); // Vide
                        }
                        
                        if (x < 8) writer.print(",");
                    }
                    writer.println();
                }
            }
            
            // Charger le niveau
            boolean loaded = game.loadLevel(tempFile.getAbsolutePath());
            
            // Vérifier que le niveau a été chargé
            assertTrue(loaded);
            
            // Vérifier les positions des joueurs
            assertEquals(1, game.getPlayer1().getX());
            assertEquals(1, game.getPlayer1().getY());
            assertEquals(7, game.getPlayer2().getX());
            assertEquals(7, game.getPlayer2().getY());
            
        } catch (java.io.IOException e) {
            fail("Exception lors de la création du fichier temporaire: " + e.getMessage());
        }
    }
    
    @Test
    public void testUpdate() {
        try (MockedStatic<SoundManager> mockedStatic = Mockito.mockStatic(SoundManager.class)) {
            // Mock SoundManager.getInstance()
            mockedStatic.when(SoundManager::getInstance).thenReturn(mockSoundManager);
            
            // Placer une bombe
            game.handleKeyPressed(KeyCode.A);
            assertEquals(1, game.getBombs().size());
            
            // Simuler plusieurs mises à jour
            for (int i = 0; i < 10; i++) {
                game.update();
            }
            
            // Vérifier que la méthode update ne lève pas d'exception
            assertDoesNotThrow(() -> game.update());
        }
    }
} 