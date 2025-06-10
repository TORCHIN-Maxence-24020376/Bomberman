package com.example.bomberman.models.world;

import com.example.bomberman.models.entities.Bomb;
import com.example.bomberman.models.entities.Player;
import com.example.bomberman.models.entities.PowerUp;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Classe principale du jeu - Version simplifiée avec méthodes complètes
 */
public class Game {
    private static final int BOARD_WIDTH = 15;
    private static final int BOARD_HEIGHT = 13;

    private GameBoard board;
    private Player player1;
    private Player player2;
    private List<Bomb> bombs;
    private boolean gameRunning;

    // Scores et statistiques
    private int player1Score = 0;
    private int player2Score = 0;
    private int bombsPlaced = 0;
    private int wallsDestroyed = 0;

    // Pour gérer les touches pressées simultanément
    private Set<KeyCode> pressedKeys;

    // Contrôles fixes pour les joueurs
    // Joueur 1: ZQSD + A (bombe) + E (capacité spéciale)
    private static final KeyCode P1_UP = KeyCode.Z;
    private static final KeyCode P1_DOWN = KeyCode.S;
    private static final KeyCode P1_LEFT = KeyCode.Q;
    private static final KeyCode P1_RIGHT = KeyCode.D;
    private static final KeyCode P1_BOMB = KeyCode.A;
    private static final KeyCode P1_SPECIAL = KeyCode.E;
    
    // Joueur 2: Flèches + Espace (bombe) + Ctrl droit (capacité spéciale)
    private static final KeyCode P2_UP = KeyCode.UP;
    private static final KeyCode P2_DOWN = KeyCode.DOWN;
    private static final KeyCode P2_LEFT = KeyCode.LEFT;
    private static final KeyCode P2_RIGHT = KeyCode.RIGHT;
    private static final KeyCode P2_BOMB = KeyCode.SPACE;
    private static final KeyCode P2_SPECIAL = KeyCode.CONTROL;

    public Game() {
        pressedKeys = new HashSet<>();
        initializeGame();
    }

    public void initializeGame() {
        board = new GameBoard(BOARD_WIDTH, BOARD_HEIGHT);
        player1 = new Player(1, 1, Color.BLUE, 1);
        player2 = new Player(BOARD_WIDTH - 2, BOARD_HEIGHT - 2, Color.RED, 2);
        bombs = new ArrayList<>();
        gameRunning = true;
        player1Score = 0;
        player2Score = 0;
        bombsPlaced = 0;
        wallsDestroyed = 0;
    }

    public void handleKeyPressed(KeyCode key) {
        if (!gameRunning) return;

        pressedKeys.add(key);

        // Gestion des bombes avec contrôles fixes
        if (key == P1_BOMB) {
            placeBomb(player1);
        } else if (key == P2_BOMB) {
            placeBomb(player2);
        }
    }

    public void handleKeyReleased(KeyCode key) {
        pressedKeys.remove(key);
    }

    // Méthode pour traiter les mouvements en continu avec contrôles fixes
    public void processMovement() {
        if (!gameRunning) return;

        // Joueur 1 avec contrôles fixes
        if (player1 != null && player1.isAlive()) {
            if (pressedKeys.contains(P1_UP)) {
                movePlayer(player1, 0, -1);
            }
            if (pressedKeys.contains(P1_DOWN)) {
                movePlayer(player1, 0, 1);
            }
            if (pressedKeys.contains(P1_LEFT)) {
                movePlayer(player1, -1, 0);
            }
            if (pressedKeys.contains(P1_RIGHT)) {
                movePlayer(player1, 1, 0);
            }
        }

        // Joueur 2 avec contrôles fixes
        if (player2 != null && player2.isAlive()) {
            if (pressedKeys.contains(P2_UP)) {
                movePlayer(player2, 0, -1);
            }
            if (pressedKeys.contains(P2_DOWN)) {
                movePlayer(player2, 0, 1);
            }
            if (pressedKeys.contains(P2_LEFT)) {
                movePlayer(player2, -1, 0);
            }
            if (pressedKeys.contains(P2_RIGHT)) {
                movePlayer(player2, 1, 0);
            }
        }
    }

    private void movePlayer(Player player, int dx, int dy) {
        if (player.move(dx, dy, board)) {
            // Collecter power-up si disponible
            try {
                PowerUp powerUp = board.collectPowerUp(player.getX(), player.getY());
                if (powerUp != null) {
                    player.applyPowerUp(powerUp.getType());
                    // Ajouter des points
                    if (player == player1) {
                        player1Score += 10;
                    } else {
                        player2Score += 10;
                    }
                }
            } catch (Exception e) {
                // Si PowerUp n'est pas encore implémenté, on ignore
            }
        }
    }

    private void placeBomb(Player player) {
        if (player.canPlaceBomb()) {
            Bomb bomb = new Bomb(player.getX(), player.getY(), player.getPlayerId());
            bombs.add(bomb);
            board.placeBomb(player.getX(), player.getY());
            player.placeBomb();
            bombsPlaced++; // ← AJOUTÉ : Compter les bombes placées
        }
    }

    public void update() {
        // Mettre à jour le plateau
        board.update();
        
        // Mettre à jour les joueurs
        player1.update();
        player2.update();
        
        // Traiter les mouvements en continu
        processMovement();

        // Mettre à jour les bombes
        Iterator<Bomb> bombIterator = bombs.iterator();
        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();
            bomb.update();

            if (bomb.hasExploded()) {
                // Gérer l'explosion
                int wallsDestroyedByThisBomb = handleExplosion(bomb);
                wallsDestroyed += wallsDestroyedByThisBomb; // ← AJOUTÉ : Compter les murs détruits

                bombIterator.remove();
                board.removeBomb(bomb.getX(), bomb.getY());

                // Rendre la bombe disponible au joueur
                if (bomb.getPlayerId() == 1) {
                    player1.bombExploded();
                } else {
                    player2.bombExploded();
                }
            }
        }

        // Vérifier les conditions de victoire
        checkWinConditions();
    }

    private int handleExplosion(Bomb bomb) {
        int x = bomb.getX();
        int y = bomb.getY();
        int wallsDestroyedByThisBomb = 0; // ← AJOUTÉ : Compter les murs détruits

        // Explosion au centre
        if (board.isWall(x, y)) {
            wallsDestroyedByThisBomb++;
        }
        board.explode(x, y);

        // Explosion dans les 4 directions
        for (int i = 1; i <= bomb.getRange(); i++) {
            // Droite
            if (x + i < BOARD_WIDTH && board.canExplode(x + i, y)) {
                if (board.isWall(x + i, y)) {
                    wallsDestroyedByThisBomb++;
                }
                board.explode(x + i, y);
                if (board.isWall(x + i, y)) break;
            } else {
                break;
            }
        }
        
        for (int i = 1; i <= bomb.getRange(); i++) {
            // Gauche
            if (x - i >= 0 && board.canExplode(x - i, y)) {
                if (board.isWall(x - i, y)) {
                    wallsDestroyedByThisBomb++;
                }
                board.explode(x - i, y);
                if (board.isWall(x - i, y)) break;
            } else {
                break;
            }
        }
        
        for (int i = 1; i <= bomb.getRange(); i++) {
            // Haut
            if (y - i >= 0 && board.canExplode(x, y - i)) {
                if (board.isWall(x, y - i)) {
                    wallsDestroyedByThisBomb++;
                }
                board.explode(x, y - i);
                if (board.isWall(x, y - i)) break;
            } else {
                break;
            }
        }
        
        for (int i = 1; i <= bomb.getRange(); i++) {
            // Bas
            if (y + i < BOARD_HEIGHT && board.canExplode(x, y + i)) {
                if (board.isWall(x, y + i)) {
                    wallsDestroyedByThisBomb++;
                }
                board.explode(x, y + i);
                if (board.isWall(x, y + i)) break;
            } else {
                break;
            }
        }

        // Ajouter des points pour les murs détruits
        if (bomb.getPlayerId() == 1) {
            player1Score += wallsDestroyedByThisBomb * 5;
        } else {
            player2Score += wallsDestroyedByThisBomb * 5;
        }

        // Vérifier si les joueurs sont touchés
        if (board.isExplosion(player1.getX(), player1.getY())) {
            player1.takeDamage();
        }
        if (board.isExplosion(player2.getX(), player2.getY())) {
            player2.takeDamage();
        }

        return wallsDestroyedByThisBomb; // ← AJOUTÉ : Retourner le nombre de murs détruits
    }

    private void checkWinConditions() {
        if (!player1.isAlive()) {
            System.out.println("Joueur 2 gagne!");
            gameRunning = false;
        } else if (!player2.isAlive()) {
            System.out.println("Joueur 1 gagne!");
            gameRunning = false;
        }
    }

    // Getters pour le contrôleur
    public GameBoard getBoard() { return board; }
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public List<Bomb> getBombs() { return new ArrayList<>(bombs); }
    public boolean isGameRunning() { return gameRunning; }
    public int getPlayer1Score() { return player1Score; }
    public int getPlayer2Score() { return player2Score; }
    public int getBombsPlaced() { return bombsPlaced; }
    public int getWallsDestroyed() { return wallsDestroyed; }

    /**
     * Charge un niveau personnalisé à partir d'un fichier
     * @param levelPath Chemin vers le fichier de niveau
     * @return true si le niveau a été chargé avec succès, false sinon
     */
    public boolean loadLevel(String levelPath) {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(levelPath))) {
            // Lire les dimensions
            String[] dimensions = reader.readLine().split(",");
            int levelWidth = Integer.parseInt(dimensions[0]);
            int levelHeight = Integer.parseInt(dimensions[1]);

            // Lire les positions des joueurs
            String[] players = reader.readLine().split(",");
            int player1X = Integer.parseInt(players[0]);
            int player1Y = Integer.parseInt(players[1]);
            int player2X = Integer.parseInt(players[2]);
            int player2Y = Integer.parseInt(players[3]);

            // Créer un nouveau plateau de jeu avec les dimensions lues
            board = new GameBoard();
            
            // Lire les données du niveau
            int[][] levelData = new int[levelHeight][levelWidth];
            for (int y = 0; y < levelHeight; y++) {
                String[] row = reader.readLine().split(",");
                for (int x = 0; x < levelWidth; x++) {
                    levelData[y][x] = Integer.parseInt(row[x]);
                }
            }
            
            // Charger le niveau dans le plateau de jeu
            board.loadLevel(levelData);
            
            // Repositionner les joueurs
            player1 = new Player(player1X, player1Y, Color.BLUE, 1);
            player2 = new Player(player2X, player2Y, Color.RED, 2);
            
            // Réinitialiser les bombes et l'état du jeu
            bombs = new ArrayList<>();
            gameRunning = true;
            player1Score = 0;
            player2Score = 0;
            bombsPlaced = 0;
            wallsDestroyed = 0;
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}