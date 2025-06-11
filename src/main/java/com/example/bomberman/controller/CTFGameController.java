package com.example.bomberman.controller;

import com.example.bomberman.models.game.CTFGame;
import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;

/**
 * Contrôleur spécifique pour le mode Capture the Flag
 */
public class CTFGameController extends GameController {
    
    private CTFGame ctfGame;
    
    /**
     * Initialise une partie en mode Capture the Flag
     */
    @Override
    protected void initializeGame() {
        // Créer une instance de CTFGame au lieu de Game
        ctfGame = new CTFGame();
        setGame(ctfGame);
        
        // Ne pas appeler super.initializeGame() car cela recréerait un jeu standard
        // Au lieu de cela, initialiser les variables comme dans la méthode parente
        gameRunning = true;
        gameStartTime = System.currentTimeMillis();
        totalPauseTime = 0;
        isPaused = false;

        updateUI();

        // Jouer la musique de jeu
        soundManager.playBackgroundMusic("/sounds/game_music.mp3");
    }
    
    /**
     * Surcharge de la méthode render pour afficher les éléments spécifiques au mode CTF
     */
    @Override
    public void render() {
        super.render(); // Appeler la méthode render du parent
        
        // Ajouter les éléments spécifiques au mode CTF
        if (ctfGame != null) {
            GraphicsContext gc = getGraphicsContext();
            ctfGame.renderCTFElements(gc);
        }
    }
    
    /**
     * Met à jour l'interface utilisateur avec les informations du mode CTF
     */
    @Override
    protected void updateUI() {
        super.updateUI();
        
        // Mettre à jour les informations spécifiques au mode CTF
        if (ctfGame != null) {
            updateCTFInfo();
        }
    }
    
    /**
     * Met à jour les informations spécifiques au mode CTF
     */
    private void updateCTFInfo() {
        // Mettre à jour les scores CTF dans l'interface
        javafx.application.Platform.runLater(() -> {
            if (getGameStatusLabel() != null) {
                String scoreText = "Score CTF: " + ctfGame.getBlueScore() + " - " + ctfGame.getRedScore();
                getGameStatusLabel().setText(scoreText);
            }
        });
    }
} 