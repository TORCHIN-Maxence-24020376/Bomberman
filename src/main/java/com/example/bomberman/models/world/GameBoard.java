package com.example.bomberman.models.world;

import com.example.bomberman.models.entities.PowerUp;
import com.example.bomberman.service.SoundManager;
import com.example.bomberman.utils.SpriteManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Classe représentant le plateau de jeu amélioré
 */
public class GameBoard {
    public static final int EMPTY = 0;
    public static final int WALL = 1;
    public static final int DESTRUCTIBLE_WALL = 2;
    public static final int BOMB = 3;
    public static final int EXPLOSION = 4;

    private int[][] board;
    private long[][] explosionTime;
    private List<PowerUp> powerUps;
    private int width, height;
    private static final long EXPLOSION_DURATION = 1000; // 1 seconde
    private static final double POWERUP_SPAWN_CHANCE = 0.3; // 30% de chance
    
    // Sprites
    private SpriteManager spriteManager;
    private Image tileSprite;
    private Image wallSprite;
    private Image breakableWallSprite;
    private Image explosionSprite;

    /**
     * Constructeur par défaut avec dimensions standard
     */
    public GameBoard() {
        this(15, 13); // Dimensions standard par défaut
    }

    /**
     * Constructeur du plateau de jeu
     */
    public GameBoard(int width, int height) {
        this.width = width;
        this.height = height;
        this.board = new int[height][width];
        this.explosionTime = new long[height][width];
        this.powerUps = new ArrayList<>();
        
        // Initialiser le gestionnaire de sprites et charger les sprites
        this.spriteManager = SpriteManager.getInstance();
        loadSprites();
        
        initializeBoard();
    }
    
    /**
     * Charge les sprites nécessaires
     */
    private void loadSprites() {
        tileSprite = spriteManager.loadSprite("tile");
        wallSprite = spriteManager.loadSprite("unbreakable_wall");
        breakableWallSprite = spriteManager.loadSprite("breakable_wall");
        explosionSprite = spriteManager.loadSprite("explosion");
    }

    /**
     * Initialise le plateau avec des murs et des murs destructibles
     */
    private void initializeBoard() {
        // Initialiser avec des murs sur les bords et en damier
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    // Murs de bordure
                    board[y][x] = WALL;
                } else if (x % 2 == 0 && y % 2 == 0) {
                    // Murs indestructibles en damier
                    board[y][x] = WALL;
                } else if (Math.random() < 0.35 &&
                        !(x <= 2 && y <= 2) &&
                        !(x >= width - 3 && y >= height - 3)) {
                    // Murs destructibles aléatoirement (sauf près des spawn)
                    board[y][x] = DESTRUCTIBLE_WALL;
                } else {
                    board[y][x] = EMPTY;
                }
            }
        }

        // S'assurer que les positions de spawn sont libres
        clearSpawnAreas();
    }

    /**
     * Nettoie les zones de spawn des joueurs
     */
    private void clearSpawnAreas() {
        // Zone joueur 1 (coin supérieur gauche)
        for (int y = 1; y <= 2; y++) {
            for (int x = 1; x <= 2; x++) {
                board[y][x] = EMPTY;
            }
        }

        // Zone joueur 2 (coin inférieur droit)
        for (int y = height - 3; y < height - 1; y++) {
            for (int x = width - 3; x < width - 1; x++) {
                board[y][x] = EMPTY;
            }
        }
    }

    /**
     * Met à jour l'état du plateau
     */
    public void update() {
        // Mettre à jour les power-ups
        Iterator<PowerUp> iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();
            powerUp.update();
            if (powerUp.shouldRemove()) {
                iterator.remove();
            }
        }
    }

    /**
     * Vérifie si un mouvement est valide
     */
    public boolean isValidMove(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height &&
                (board[y][x] == EMPTY || board[y][x] == EXPLOSION);
    }

    /**
     * Vérifie si une explosion peut se propager à cette position
     */
    public boolean canExplode(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height &&
                board[y][x] != WALL;
    }

    /**
     * Vérifie si c'est un mur
     */
    public boolean isWall(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height &&
                (board[y][x] == WALL || board[y][x] == DESTRUCTIBLE_WALL);
    }
    
    /**
     * Vérifie s'il y a une bombe à cette position
     */
    public boolean isBomb(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height && board[y][x] == BOMB;
    }

    /**
     * Place une bombe sur le plateau
     */
    public void placeBomb(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            board[y][x] = BOMB;
        }
    }

    /**
     * Retire une bombe du plateau
     */
    public void removeBomb(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height && board[y][x] == BOMB) {
            board[y][x] = EMPTY;
        }
    }
    
    /**
     * Déplace une bombe d'une position à une autre
     */
    public void moveBomb(int fromX, int fromY, int toX, int toY) {
        if (fromX >= 0 && fromX < width && fromY >= 0 && fromY < height &&
            toX >= 0 && toX < width && toY >= 0 && toY < height) {
            
            // Vérifier que la position source contient une bombe et que la destination est vide
            if (board[fromY][fromX] == BOMB && board[toY][toX] == EMPTY) {
                board[fromY][fromX] = EMPTY;
                board[toY][toX] = BOMB;
                
                // Jouer un son de déplacement de bombe
                SoundManager.getInstance().playSound("bomb_kick");
            }
        }
    }

    /**
     * Crée une explosion à la position donnée
     */
    public void explode(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            boolean wasDestructibleWall = (board[y][x] == DESTRUCTIBLE_WALL);

            if (board[y][x] == DESTRUCTIBLE_WALL) {
                board[y][x] = EMPTY; // Détruire le mur

                // Chance d'apparition d'un power-up
                if (Math.random() < POWERUP_SPAWN_CHANCE) {
                    spawnRandomPowerUp(x, y);
                }
            }

            if (board[y][x] != WALL) {
                board[y][x] = EXPLOSION;
                explosionTime[y][x] = System.currentTimeMillis();
            }
        }
    }

    /**
     * Fait apparaître un power-up aléatoire
     */
    private void spawnRandomPowerUp(int x, int y) {
        // Probabilités différentes pour chaque type
        double random = Math.random();
        PowerUp.Type selectedType;

        if (random < 0.3) {
            selectedType = PowerUp.Type.BOMB_UP;
        } else if (random < 0.6) {
            selectedType = PowerUp.Type.FIRE_UP;
        } else if (random < 0.75) {
            selectedType = PowerUp.Type.SPEED_UP;
        } else if (random < 0.9) {
            selectedType = PowerUp.Type.KICK;
        } else {
            selectedType = PowerUp.Type.SKULL; // Plus rare
        }

        powerUps.add(new PowerUp(x, y, selectedType));
    }

    /**
     * Vérifie s'il y a une explosion à cette position
     */
    public boolean isExplosion(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height &&
                board[y][x] == EXPLOSION;
    }

    /**
     * Vérifie s'il y a un power-up à cette position
     */
    public PowerUp getPowerUpAt(int x, int y) {
        return powerUps.stream()
                .filter(p -> p.getX() == x && p.getY() == y && p.isActive())
                .findFirst()
                .orElse(null);
    }

    /**
     * Collecte un power-up à la position donnée
     */
    public PowerUp collectPowerUp(int x, int y) {
        PowerUp powerUp = getPowerUpAt(x, y);
        if (powerUp != null) {
            powerUp.collect();
            return powerUp;
        }
        return null;
    }

    /**
     * Dessine le plateau de jeu
     */
    public void render(GraphicsContext gc, int tileSize) {
        long currentTime = System.currentTimeMillis();

        // Dessiner les cases du plateau
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int cellX = x * tileSize;
                int cellY = y * tileSize;

                switch (board[y][x]) {
                    case EMPTY:
                        // Utiliser le sprite de tuile si disponible
                        if (tileSprite != null) {
                            gc.drawImage(tileSprite, cellX, cellY, tileSize, tileSize);
                        } else {
                            // Fallback: Sol avec effet de damier subtil
                            Color grassColor = ((x + y) % 2 == 0) ? Color.LIGHTGREEN : Color.LIGHTGREEN.darker();
                            gc.setFill(grassColor);
                            gc.fillRect(cellX, cellY, tileSize, tileSize);
                        }
                        break;

                    case WALL:
                        // Utiliser le sprite de mur si disponible
                        if (wallSprite != null) {
                            gc.drawImage(wallSprite, cellX, cellY, tileSize, tileSize);
                        } else {
                            // Fallback: Mur indestructible avec effet 3D
                            gc.setFill(Color.DARKGRAY);
                            gc.fillRect(cellX, cellY, tileSize, tileSize);

                            // Effet de relief
                            gc.setFill(Color.LIGHTGRAY);
                            gc.fillRect(cellX, cellY, tileSize - 2, tileSize - 2);
                            gc.setFill(Color.GRAY);
                            gc.fillRect(cellX + 2, cellY + 2, tileSize - 4, tileSize - 4);

                            gc.setStroke(Color.BLACK);
                            gc.setLineWidth(2);
                            gc.strokeRect(cellX, cellY, tileSize, tileSize);
                        }
                        break;

                    case DESTRUCTIBLE_WALL:
                        // Utiliser le sprite de mur destructible si disponible
                        if (breakableWallSprite != null) {
                            gc.drawImage(breakableWallSprite, cellX, cellY, tileSize, tileSize);
                        } else {
                            // Fallback: Mur destructible avec texture
                            gc.setFill(Color.BROWN);
                            gc.fillRect(cellX, cellY, tileSize, tileSize);

                            // Texture de brique
                            gc.setStroke(Color.DARKRED);
                            gc.setLineWidth(1);

                            // Lignes horizontales
                            for (int i = 0; i < 3; i++) {
                                gc.strokeLine(cellX, cellY + i * tileSize/3, cellX + tileSize, cellY + i * tileSize/3);
                            }

                            // Lignes verticales décalées
                            for (int i = 0; i < 2; i++) {
                                int offset = (i % 2 == 0) ? 0 : tileSize/2;
                                gc.strokeLine(cellX + tileSize/2 + offset, cellY + i * tileSize/3,
                                        cellX + tileSize/2 + offset, cellY + (i + 1) * tileSize/3);
                            }

                            gc.setStroke(Color.BLACK);
                            gc.setLineWidth(2);
                            gc.strokeRect(cellX, cellY, tileSize, tileSize);
                        }
                        break;

                    case BOMB:
                        // Sol visible sous la bombe
                        if (tileSprite != null) {
                            gc.drawImage(tileSprite, cellX, cellY, tileSize, tileSize);
                        } else {
                            Color grassColorBomb = ((x + y) % 2 == 0) ? Color.LIGHTGREEN : Color.LIGHTGREEN.darker();
                            gc.setFill(grassColorBomb);
                            gc.fillRect(cellX, cellY, tileSize, tileSize);
                        }
                        break;

                    case EXPLOSION:
                        // Vérifier si l'explosion doit disparaître
                        if (currentTime - explosionTime[y][x] > EXPLOSION_DURATION) {
                            board[y][x] = EMPTY;
                            if (tileSprite != null) {
                                gc.drawImage(tileSprite, cellX, cellY, tileSize, tileSize);
                            } else {
                                Color grassColorExp = ((x + y) % 2 == 0) ? Color.LIGHTGREEN : Color.LIGHTGREEN.darker();
                                gc.setFill(grassColorExp);
                                gc.fillRect(cellX, cellY, tileSize, tileSize);
                            }
                        } else {
                            // Utiliser le sprite d'explosion si disponible
                            if (explosionSprite != null) {
                                // Animation simple: faire pulser l'explosion
                                double progress = (double)(currentTime - explosionTime[y][x]) / EXPLOSION_DURATION;
                                int size = (int)(tileSize * (1 - progress * 0.2));
                                int offset = (tileSize - size) / 2;
                                gc.drawImage(explosionSprite, cellX + offset, cellY + offset, size, size);
                            } else {
                                // Fallback: Animation d'explosion
                                double progress = (double)(currentTime - explosionTime[y][x]) / EXPLOSION_DURATION;

                                // Couleur qui évolue
                                Color explosionColor = Color.YELLOW.interpolate(Color.ORANGE, progress);
                                gc.setFill(explosionColor);
                                gc.fillRect(cellX, cellY, tileSize, tileSize);

                                // Effet d'explosion animé avec étincelles
                                gc.setFill(Color.RED);
                                int explosionSize = (int)(tileSize * (1 - progress * 0.3));
                                int offset = (tileSize - explosionSize) / 2;
                                gc.fillRect(cellX + offset, cellY + offset, explosionSize, explosionSize);

                                // Étincelles
                                gc.setFill(Color.WHITE);
                                for (int i = 0; i < 3; i++) {
                                    int sparkleX = cellX + (int)(Math.random() * tileSize);
                                    int sparkleY = cellY + (int)(Math.random() * tileSize);
                                    gc.fillOval(sparkleX, sparkleY, 3, 3);
                                }
                            }
                        }
                        break;
                }

                // Grille subtile (uniquement si on n'utilise pas de sprites)
                if (tileSprite == null) {
                    gc.setStroke(Color.DARKGREEN.darker());
                    gc.setLineWidth(0.5);
                    gc.strokeRect(cellX, cellY, tileSize, tileSize);
                }
            }
        }

        // Dessiner les power-ups
        for (PowerUp powerUp : powerUps) {
            if (powerUp.isActive()) {
                powerUp.render(gc, tileSize);
            }
        }
    }

    /**
     * Remet le plateau à zéro pour une nouvelle partie
     */
    public void reset() {
        powerUps.clear();
        initializeBoard();
    }

    /**
     * Retourne la largeur du plateau
     */
    public int getWidth() { return width; }

    /**
     * Retourne la hauteur du plateau
     */
    public int getHeight() { return height; }

    /**
     * Retourne la liste des power-ups (pour les tests)
     */
    public List<PowerUp> getPowerUps() { return new ArrayList<>(powerUps); }

    /**
     * Ajoute un power-up manuellement (pour les tests ou l'éditeur)
     */
    public void addPowerUp(PowerUp powerUp) {
        powerUps.add(powerUp);
    }

    /**
     * Charge un niveau personnalisé
     * @param levelData Les données du niveau
     */
    public void loadLevel(int[][] levelData) {
        if (levelData == null || levelData.length == 0 || levelData[0].length == 0) {
            initializeBoard(); // Fallback vers un niveau standard
            return;
        }
        
        this.height = levelData.length;
        this.width = levelData[0].length;
        this.board = new int[height][width];
        this.explosionTime = new long[height][width];
        this.powerUps.clear();
        
        // Copier les données du niveau
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                board[y][x] = levelData[y][x];
            }
        }
    }
}