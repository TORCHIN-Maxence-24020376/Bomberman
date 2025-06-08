package com.example.bomberman.tests.models.world;

import com.example.bomberman.models.entities.PowerUp;
import com.example.bomberman.models.world.GameBoard;
import com.example.bomberman.tests.utils.JavaFXThreadingRule;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFXThreadingRule.class)
public class GameBoardTest {

    private GameBoard gameBoard;
    
    @Mock
    private GraphicsContext mockGC;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        gameBoard = new GameBoard(15, 13);
    }
    
    @Test
    public void testInitialState() {
        assertEquals(15, gameBoard.getWidth());
        assertEquals(13, gameBoard.getHeight());
        
        // Vérifier que les coins sont des murs
        assertFalse(gameBoard.isValidMove(0, 0));
        assertFalse(gameBoard.isValidMove(0, 12));
        assertFalse(gameBoard.isValidMove(14, 0));
        assertFalse(gameBoard.isValidMove(14, 12));
        
        // Vérifier que les positions de départ des joueurs sont vides
        assertTrue(gameBoard.isValidMove(1, 1));
        assertTrue(gameBoard.isValidMove(13, 11));
    }
    
    @Test
    public void testIsValidMove() {
        // Positions valides (cases vides)
        assertTrue(gameBoard.isValidMove(1, 1));
        
        // Positions invalides (murs)
        assertFalse(gameBoard.isValidMove(0, 0)); // Bordure
        assertFalse(gameBoard.isValidMove(2, 2)); // Mur fixe en damier
        
        // Positions hors limites
        assertFalse(gameBoard.isValidMove(-1, 5));
        assertFalse(gameBoard.isValidMove(5, -1));
        assertFalse(gameBoard.isValidMove(15, 5));
        assertFalse(gameBoard.isValidMove(5, 13));
    }
    
    @Test
    public void testCanExplode() {
        // Les murs indestructibles ne peuvent pas exploser
        assertFalse(gameBoard.canExplode(0, 0)); // Bordure
        assertFalse(gameBoard.canExplode(2, 2)); // Mur fixe en damier
        
        // Les cases vides peuvent exploser
        assertTrue(gameBoard.canExplode(1, 1));
        
        // Les positions hors limites ne peuvent pas exploser
        assertFalse(gameBoard.canExplode(-1, 5));
        assertFalse(gameBoard.canExplode(15, 5));
    }
    
    @Test
    public void testPlaceBombAndRemoveBomb() {
        // Placer une bombe
        gameBoard.placeBomb(3, 3);
        
        // Retirer la bombe
        gameBoard.removeBomb(3, 3);
        
        // Vérifier que la position est à nouveau valide pour un mouvement
        assertTrue(gameBoard.isValidMove(3, 3));
    }
    
    @Test
    public void testExplode() {
        // Placer un mur destructible
        int[][] levelData = new int[13][15];
        for (int y = 0; y < 13; y++) {
            for (int x = 0; x < 15; x++) {
                if (x == 3 && y == 3) {
                    levelData[y][x] = GameBoard.DESTRUCTIBLE_WALL;
                } else {
                    levelData[y][x] = GameBoard.EMPTY;
                }
            }
        }
        gameBoard.loadLevel(levelData);
        
        // Vérifier que c'est un mur destructible
        assertTrue(gameBoard.isWall(3, 3));
        
        // Faire exploser le mur
        gameBoard.explode(3, 3);
        
        // Vérifier que le mur est détruit (devenu une explosion)
        assertTrue(gameBoard.isExplosion(3, 3));
    }
    
    @Test
    public void testPowerUps() {
        // Ajouter un power-up
        PowerUp powerUp = new PowerUp(5, 5, PowerUp.Type.BOMB_UP);
        gameBoard.addPowerUp(powerUp);
        
        // Vérifier que le power-up est présent
        PowerUp retrievedPowerUp = gameBoard.getPowerUpAt(5, 5);
        assertNotNull(retrievedPowerUp);
        assertEquals(PowerUp.Type.BOMB_UP, retrievedPowerUp.getType());
        
        // Collecter le power-up
        PowerUp collectedPowerUp = gameBoard.collectPowerUp(5, 5);
        assertNotNull(collectedPowerUp);
        assertEquals(PowerUp.Type.BOMB_UP, collectedPowerUp.getType());
        assertFalse(collectedPowerUp.isActive());
        
        // Vérifier qu'il n'y a plus de power-up à cette position
        assertNull(gameBoard.getPowerUpAt(5, 5));
    }
    
    @Test
    public void testRender() throws Exception {
        // Utiliser JavaFXThreadingRule pour exécuter le test sur le thread JavaFX
        JavaFXThreadingRule.runOnJavaFXThread(() -> {
            // Créer un canvas sur le thread JavaFX
            Canvas canvas = new Canvas(600, 520);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            
            // Ne devrait pas lever d'exception
            gameBoard.render(gc, 40);
        });
    }
    
    @Test
    public void testReset() {
        // Modifier l'état du plateau
        gameBoard.placeBomb(3, 3);
        gameBoard.explode(5, 5);
        gameBoard.addPowerUp(new PowerUp(7, 7, PowerUp.Type.SPEED_UP));
        
        // Réinitialiser le plateau
        gameBoard.reset();
        
        // Vérifier que les power-ups ont été supprimés
        assertTrue(gameBoard.getPowerUps().isEmpty());
    }
} 