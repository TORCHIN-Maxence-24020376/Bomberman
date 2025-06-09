package com.example.bomberman.tests.models.entities;

import com.example.bomberman.models.entities.Player;
import com.example.bomberman.models.entities.PowerUp;
import com.example.bomberman.models.world.GameBoard;
import com.example.bomberman.service.SoundManager;
import com.example.bomberman.tests.utils.JavaFXThreadingRule;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(JavaFXThreadingRule.class)
public class PlayerTest {

    private Player player;
    
    @Mock
    private GameBoard mockBoard;
    
    @Mock
    private SoundManager mockSoundManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        player = new Player(1, 1, Color.BLUE, 1);
        
        // Configuration du mock pour permettre les mouvements
        when(mockBoard.isValidMove(anyInt(), anyInt())).thenReturn(true);
    }

    @Test
    public void testInitialState() {
        assertEquals(1, player.getX());
        assertEquals(1, player.getY());
        assertEquals(Color.BLUE, player.getColor());
        assertEquals(1, player.getPlayerId());
        assertEquals(3, player.getLives());
        assertTrue(player.isAlive());
    }

    @Test
    public void testTakeDamage() {
        try (MockedStatic<SoundManager> mockedStatic = Mockito.mockStatic(SoundManager.class)) {
            // Mock SoundManager.getInstance()
            mockedStatic.when(SoundManager::getInstance).thenReturn(mockSoundManager);
            
            // Vérifier l'état initial
            assertEquals(3, player.getLives());
            
            // Infliger des dégâts
            player.takeDamage();
            assertEquals(2, player.getLives());
            assertTrue(player.isAlive());
            
            // Infliger plus de dégâts
            player.takeDamage();
            player.takeDamage();
            assertEquals(0, player.getLives());
            assertFalse(player.isAlive());
        }
    }

    @Test
    public void testMove() {
        try (MockedStatic<SoundManager> mockedStatic = Mockito.mockStatic(SoundManager.class)) {
            // Mock SoundManager.getInstance()
            mockedStatic.when(SoundManager::getInstance).thenReturn(mockSoundManager);
            
            // Position initiale
            assertEquals(1, player.getX());
            assertEquals(1, player.getY());
            
            // Déplacer vers la droite
            boolean moved = player.move(1, 0, mockBoard);
            assertTrue(moved);
            assertEquals(2, player.getX());
            assertEquals(1, player.getY());
            
            // Déplacer vers le bas
            moved = player.move(0, 1, mockBoard);
            assertTrue(moved);
            assertEquals(2, player.getX());
            assertEquals(2, player.getY());
        }
    }

    @Test
    public void testBombPlacement() {
        try (MockedStatic<SoundManager> mockedStatic = Mockito.mockStatic(SoundManager.class)) {
            // Mock SoundManager.getInstance()
            mockedStatic.when(SoundManager::getInstance).thenReturn(mockSoundManager);
            
            // Vérifier l'état initial
            assertTrue(player.canPlaceBomb());
            assertEquals(0, player.getCurrentBombs());
            
            // Placer une bombe
            player.placeBomb();
            assertEquals(1, player.getCurrentBombs());
            assertFalse(player.canPlaceBomb()); // Ne peut plus placer de bombe
            
            // Une bombe explose
            player.bombExploded();
            assertEquals(0, player.getCurrentBombs());
            assertTrue(player.canPlaceBomb()); // Peut à nouveau placer une bombe
        }
    }

    @Test
    public void testPowerUps() {
        try (MockedStatic<SoundManager> mockedStatic = Mockito.mockStatic(SoundManager.class)) {
            // Mock SoundManager.getInstance()
            mockedStatic.when(SoundManager::getInstance).thenReturn(mockSoundManager);
            
            // État initial
            assertEquals(1, player.getSpeed());
            assertEquals(2, player.getBombRange());
            assertEquals(1, player.getMaxBombs());
            assertFalse(player.canKickBombs());
            
            // Appliquer des power-ups
            player.applyPowerUp(PowerUp.Type.SPEED_UP);
            assertEquals(2, player.getSpeed());
            
            player.applyPowerUp(PowerUp.Type.FIRE_UP);
            assertEquals(3, player.getBombRange());
            
            player.applyPowerUp(PowerUp.Type.BOMB_UP);
            assertEquals(2, player.getMaxBombs());
            
            player.applyPowerUp(PowerUp.Type.KICK);
            assertTrue(player.canKickBombs());
        }
    }

    @Test
    public void testReset() {
        // Modifier l'état du joueur
        player.setSpeed(3);
        player.setBombRange(5);
        player.setMaxBombs(3);
        player.setCanKickBombs(true);
        player.takeDamage();
        
        try (MockedStatic<SoundManager> mockedStatic = Mockito.mockStatic(SoundManager.class)) {
            // Mock SoundManager.getInstance()
            mockedStatic.when(SoundManager::getInstance).thenReturn(mockSoundManager);
            
            player.placeBomb();
            
            // Réinitialiser
            player.reset(5, 5);
            
            // Vérifier l'état après réinitialisation
            assertEquals(5, player.getX());
            assertEquals(5, player.getY());
            assertEquals(3, player.getLives());
            assertEquals(1, player.getSpeed());
            assertEquals(2, player.getBombRange());
            assertEquals(1, player.getMaxBombs());
            assertEquals(0, player.getCurrentBombs());
            assertFalse(player.canKickBombs());
        }
    }
    
    @Test
    public void testRender() throws Exception {
        // Utiliser JavaFXThreadingRule pour exécuter le test sur le thread JavaFX
        JavaFXThreadingRule.runOnJavaFXThread(() -> {
            // Créer un canvas sur le thread JavaFX
            Canvas canvas = new Canvas(100, 100);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            
            // Ne devrait pas lever d'exception
            player.render(gc, 40);
        });
    }
} 