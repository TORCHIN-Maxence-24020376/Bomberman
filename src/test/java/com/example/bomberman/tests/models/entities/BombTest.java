package com.example.bomberman.tests.models.entities;

import com.example.bomberman.models.entities.Bomb;
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
public class BombTest {

    private Bomb bomb;
    
    @Mock
    private GraphicsContext mockGC;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        bomb = new Bomb(5, 5, 1);
    }
    
    @Test
    public void testInitialState() {
        assertEquals(5, bomb.getX());
        assertEquals(5, bomb.getY());
        assertEquals(1, bomb.getPlayerId());
        assertEquals(2, bomb.getRange());
        assertFalse(bomb.hasExploded());
        assertTrue(bomb.isActive());
    }
    
    @Test
    public void testSetRange() {
        // État initial
        assertEquals(2, bomb.getRange());
        
        // Modifier la portée
        bomb.setRange(4);
        assertEquals(4, bomb.getRange());
    }
    
    @Test
    public void testDeactivate() {
        // État initial
        assertTrue(bomb.isActive());
        
        // Désactiver la bombe
        bomb.deactivate();
        assertFalse(bomb.isActive());
    }
    
    @Test
    public void testRender() throws Exception {
        // Utiliser JavaFXThreadingRule pour exécuter le test sur le thread JavaFX
        JavaFXThreadingRule.runOnJavaFXThread(() -> {
            // Créer un canvas sur le thread JavaFX
            Canvas canvas = new Canvas(100, 100);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            
            // Ne devrait pas lever d'exception
            bomb.render(gc, 40);
        });
    }
} 