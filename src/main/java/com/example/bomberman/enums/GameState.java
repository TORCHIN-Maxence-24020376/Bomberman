package com.example.bomberman.enums;

/**
 * Énumération représentant les différents états du jeu
 */
public enum GameState {
    MENU,           // Menu principal
    PROFILE_SETUP,  // Configuration des profils
    PLAYING,        // En cours de jeu
    PAUSED,         // Jeu en pause
    GAME_OVER,      // Fin de partie
    LEVEL_EDITOR,   // Éditeur de niveaux
    SETTINGS        // Paramètres
}