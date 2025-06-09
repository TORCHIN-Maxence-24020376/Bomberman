package com.example.bomberman.tests.models.entities;

import com.example.bomberman.models.entities.PowerUp;
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
public class PowerUpTest {

    private PowerUp powerUp;
    
    @Mock
    private GraphicsContext mockGC;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        powerUp = new PowerUp(3, 4, PowerUp.Type.BOMB_UP);
    }
    
    @Test
    public void testInitialState() {
        assertEquals(3, powerUp.getX());
        assertEquals(4, powerUp.getY());
        assertEquals(PowerUp.Type.BOMB_UP, powerUp.getType());
        assertTrue(powerUp.isActive());
    }
    
    @Test
    public void testCollect() {
        // État initial
        assertTrue(powerUp.isActive());
        
        // Collecter le power-up
        powerUp.collect();
        assertFalse(powerUp.isActive());
        
        // Vérifier que shouldRemove() retourne true après collection
        assertTrue(powerUp.shouldRemove());
    }
    
    @Test
    public void testRender() throws Exception {
        // Utiliser JavaFXThreadingRule pour exécuter le test sur le thread JavaFX
        JavaFXThreadingRule.runOnJavaFXThread(() -> {
            // Créer un canvas sur le thread JavaFX
            Canvas canvas = new Canvas(100, 100);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            
            // Ne devrait pas lever d'exception
            powerUp.render(gc, 40);
        });
    }
    
    @Test
    public void testAllPowerUpTypes() {
        // Vérifier que tous les types de power-ups peuvent être créés
        for (PowerUp.Type type : PowerUp.Type.values()) {
            PowerUp p = new PowerUp(1, 1, type);
            assertEquals(type, p.getType());
            assertTrue(p.isActive());
        }
    }
} 