package com.example.bomberman.tests.controller;

import com.example.bomberman.controller.GameController;
import com.example.bomberman.models.entities.Player;
import com.example.bomberman.models.world.Game;
import com.example.bomberman.models.world.GameBoard;
import com.example.bomberman.service.SoundManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameControllerTest {

    @InjectMocks
    private GameController gameController;

    @Mock
    private Canvas gameCanvas;
    
    @Mock
    private GraphicsContext graphicsContext;
    
    @Mock
    private VBox gameInfoPanel;
    
    @Mock
    private Label player1InfoLabel;
    
    @Mock
    private Label player2InfoLabel;
    
    @Mock
    private Label gameStatusLabel;
    
    @Mock
    private ProgressBar player1HealthBar;
    
    @Mock
    private ProgressBar player2HealthBar;
    
    @Mock
    private HBox player1PowerUpsBox;
    
    @Mock
    private HBox player2PowerUpsBox;
    
    @Mock
    private Game game;
    
    @Mock
    private SoundManager soundManager;
    
    @Mock
    private ProfileManager profileManager;
    
    @Mock
    private GameBoard gameBoard;
    
    @Mock
    private Player player1;
    
    @Mock
    private Player player2;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Configurer les mocks
        when(gameCanvas.getGraphicsContext2D()).thenReturn(graphicsContext);
        when(game.getBoard()).thenReturn(gameBoard);
        when(game.getPlayer1()).thenReturn(player1);
        when(game.getPlayer2()).thenReturn(player2);
        
        // Injecter le jeu mock dans le contrôleur
        setPrivateField(gameController, "game", game);
        
        // Injecter les gestionnaires mockés
        setPrivateField(gameController, "soundManager", soundManager);
        setPrivateField(gameController, "profileManager", profileManager);
    }
    
    @Test
    public void testSetPlayerProfiles() {
        // Créer des profils
        PlayerProfile profile1 = new PlayerProfile("Player", "One", "", 10, 5, "default");
        PlayerProfile profile2 = new PlayerProfile("Player", "Two", "", 8, 3, "default");
        
        // Appeler la méthode
        gameController.setPlayerProfiles(profile1, profile2);
        
        // Vérifier que la méthode du jeu a été appelée
        verify(game).setPlayerProfiles(profile1, profile2);
    }
    
    @Test
    public void testHandleKeyPressed() {
        // Créer un événement clavier
        KeyEvent keyEvent = mock(KeyEvent.class);
        when(keyEvent.getCode()).thenReturn(KeyCode.A);
        
        // Appeler la méthode
        gameController.handleKeyPressed(keyEvent);
        
        // Vérifier que la méthode du jeu a été appelée
        verify(game).handleKeyPressed(KeyCode.A);
    }
    
    @Test
    public void testHandleKeyReleased() {
        // Créer un événement clavier
        KeyEvent keyEvent = mock(KeyEvent.class);
        when(keyEvent.getCode()).thenReturn(KeyCode.A);
        
        // Appeler la méthode
        gameController.handleKeyReleased(keyEvent);
        
        // Vérifier que la méthode du jeu a été appelée
        verify(game).handleKeyReleased(KeyCode.A);
    }
    
    @Test
    public void testUpdate() throws Exception {
        // Configurer le mock pour simuler une partie en cours
        setPrivateField(gameController, "gameRunning", true);
        setPrivateField(gameController, "isPaused", false);
        
        // Appeler la méthode
        gameController.update();
        
        // Vérifier que la méthode du jeu a été appelée
        verify(game).update();
    }
    
    @Test
    public void testRender() throws Exception {
        // Configurer les mocks
        when(gameCanvas.getWidth()).thenReturn(800.0);
        when(gameCanvas.getHeight()).thenReturn(600.0);
        
        // Appeler la méthode
        gameController.render();
        
        // Vérifier que le canvas a été effacé
        verify(graphicsContext).clearRect(0, 0, 800.0, 600.0);
        
        // Vérifier que le plateau a été rendu
        verify(gameBoard).render(eq(graphicsContext), anyInt());
        
        // Vérifier que les joueurs ont été rendus
        verify(player1).render(eq(graphicsContext), anyInt());
        verify(player2).render(eq(graphicsContext), anyInt());
    }
    
    @Test
    public void testLoadCustomLevel() {
        // Configurer le mock pour simuler le chargement d'un niveau
        when(game.loadLevel(anyString())).thenReturn(true);
        
        // Appeler la méthode
        gameController.loadCustomLevel("levels/test.level");
        
        // Vérifier que la méthode du jeu a été appelée
        verify(game).loadLevel("levels/test.level");
        
        // Vérifier que la musique a été démarrée
        verify(soundManager).playBackgroundMusic(anyString());
    }
    
    @Test
    public void testSetTestMode() {
        // État initial
        assertFalse(gameController.isTestMode());
        
        // Activer le mode test
        gameController.setTestMode(true);
        
        // Vérifier que le mode test est activé
        assertTrue(gameController.isTestMode());
    }
    
    // Méthode utilitaire pour la réflexion
    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = GameController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
} 