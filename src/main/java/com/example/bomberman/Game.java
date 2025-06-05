package com.example.bomberman;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Game {
    private static final int BOARD_WIDTH = 15;
    private static final int BOARD_HEIGHT = 13;
    private static final int TILE_SIZE = 40;

    private GameBoard board;
    private Player player1;
    private Player player2;
    private List<Bomb> bombs;
    private boolean gameRunning;

    // Pour gérer les touches pressées simultanément
    private Set<KeyCode> pressedKeys;

    public Game() {
        pressedKeys = new HashSet<>();
        initializeGame();
    }

    private void initializeGame() {
        board = new GameBoard(BOARD_WIDTH, BOARD_HEIGHT);
        player1 = new Player(1, 1, Color.BLUE, 1);
        player2 = new Player(BOARD_WIDTH - 2, BOARD_HEIGHT - 2, Color.RED, 2);
        bombs = new ArrayList<>();
        gameRunning = true;
    }

    public void handleKeyPressed(KeyCode key) {
        if (!gameRunning) return;

        pressedKeys.add(key);

        // Gestion des bombes (action instantanée)
        if (key == KeyCode.A) {
            placeBomb(player1);
        } else if (key == KeyCode.SPACE) {
            placeBomb(player2);
        }
    }

    public void handleKeyReleased(KeyCode key) {
        pressedKeys.remove(key);
    }

    // Nouvelle méthode pour traiter les mouvements en continu
    public void processMovement() {
        if (!gameRunning) return;

        // Joueur 1 (ZQSD)
        if (pressedKeys.contains(KeyCode.Z)) {
            movePlayer(player1, 0, -1);
        }
        if (pressedKeys.contains(KeyCode.S)) {
            movePlayer(player1, 0, 1);
        }
        if (pressedKeys.contains(KeyCode.Q)) {
            movePlayer(player1, -1, 0);
        }
        if (pressedKeys.contains(KeyCode.D)) {
            movePlayer(player1, 1, 0);
        }

        // Joueur 2 (Flèches)
        if (pressedKeys.contains(KeyCode.UP)) {
            movePlayer(player2, 0, -1);
        }
        if (pressedKeys.contains(KeyCode.DOWN)) {
            movePlayer(player2, 0, 1);
        }
        if (pressedKeys.contains(KeyCode.LEFT)) {
            movePlayer(player2, -1, 0);
        }
        if (pressedKeys.contains(KeyCode.RIGHT)) {
            movePlayer(player2, 1, 0);
        }
    }

    private void movePlayer(Player player, int dx, int dy) {
        if (!player.canMove()) return; // Vérifier si le joueur peut bouger

        int newX = player.getX() + dx;
        int newY = player.getY() + dy;

        if (board.isValidMove(newX, newY)) {
            player.setPosition(newX, newY);
        }
    }

    private void placeBomb(Player player) {
        if (player.canPlaceBomb()) {
            Bomb bomb = new Bomb(player.getX(), player.getY(), player.getPlayerId());
            bombs.add(bomb);
            board.placeBomb(player.getX(), player.getY());
            player.placeBomb();
        }
    }

    public void update() {
        // Traiter les mouvements en continu
        processMovement();

        // Mettre à jour les bombes
        Iterator<Bomb> bombIterator = bombs.iterator();
        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();
            bomb.update();

            if (bomb.hasExploded()) {
                // Gérer l'explosion
                handleExplosion(bomb);
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

    private void handleExplosion(Bomb bomb) {
        int x = bomb.getX();
        int y = bomb.getY();

        // Explosion au centre
        board.explode(x, y);

        // Explosion dans les 4 directions
        for (int i = 1; i <= bomb.getRange(); i++) {
            // Droite
            if (x + i < BOARD_WIDTH && board.canExplode(x + i, y)) {
                board.explode(x + i, y);
                if (board.isWall(x + i, y)) break;
            }
            // Gauche
            if (x - i >= 0 && board.canExplode(x - i, y)) {
                board.explode(x - i, y);
                if (board.isWall(x - i, y)) break;
            }
            // Haut
            if (y - i >= 0 && board.canExplode(x, y - i)) {
                board.explode(x, y - i);
                if (board.isWall(x, y - i)) break;
            }
            // Bas
            if (y + i < BOARD_HEIGHT && board.canExplode(x, y + i)) {
                board.explode(x, y + i);
                if (board.isWall(x, y + i)) break;
            }
        }

        // Vérifier si les joueurs sont touchés
        if (board.isExplosion(player1.getX(), player1.getY())) {
            player1.takeDamage();
        }
        if (board.isExplosion(player2.getX(), player2.getY())) {
            player2.takeDamage();
        }
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

    public void render(GraphicsContext gc) {
        // Dessiner le plateau
        board.render(gc, TILE_SIZE);

        // Dessiner les joueurs
        if (player1.isAlive()) {
            player1.render(gc, TILE_SIZE);
        }
        if (player2.isAlive()) {
            player2.render(gc, TILE_SIZE);
        }

        // Dessiner les bombes
        for (Bomb bomb : bombs) {
            bomb.render(gc, TILE_SIZE);
        }

        // Afficher les vies
        gc.setFill(Color.WHITE);
        gc.fillText("Joueur 1 (ZQSD + A) - Vies: " + player1.getLives(), 10, 20);
        gc.fillText("Joueur 2 (Flèches + Espace) - Vies: " + player2.getLives(), 10, 40);
    }
}