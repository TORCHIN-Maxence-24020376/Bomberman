package com.example.bomberman.models.world;

import com.example.bomberman.models.entities.BotPlayer;
import com.example.bomberman.models.entities.Bomb;
import com.example.bomberman.models.entities.Player;
import com.example.bomberman.models.entities.PowerUp;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Classe représentant une partie contre un bot
 */
public class BotGame extends Game {
    
    private BotPlayer botPlayer;
    private Player humanPlayer;
    private int difficultyLevel;
    private boolean botMode = true;
    
    /**
     * Constructeur
     * @param difficultyLevel Niveau de difficulté du bot (1-3)
     */
    public BotGame(int difficultyLevel) {
        super();
        this.difficultyLevel = Math.max(1, Math.min(3, difficultyLevel));
        initializeBotGame();
    }
    
    /**
     * Initialise une partie contre un bot
     */
    private void initializeBotGame() {
        // Conserver le joueur 1 comme joueur humain
        humanPlayer = getPlayer1();
        
        // Remplacer le joueur 2 par un bot
        int boardWidth = getBoard().getWidth();
        int boardHeight = getBoard().getHeight();
        
        // Créer le bot à la position du joueur 2
        botPlayer = new BotPlayer(boardWidth - 2, boardHeight - 2, Color.RED, 2, difficultyLevel);
        
        // Remplacer le joueur 2 par le bot
        replacePlayer2WithBot();
    }
    
    /**
     * Remplace le joueur 2 par le bot
     */
    private void replacePlayer2WithBot() {
        // On doit recharger le niveau pour remplacer le joueur 2
        // On garde les positions actuelles
        int player1X = getPlayer1().getX();
        int player1Y = getPlayer1().getY();
        int player2X = getPlayer2().getX();
        int player2Y = getPlayer2().getY();
        
        // On réinitialise le jeu
        super.initializeGame();
        
        // On replace le joueur 1 à sa position
        getPlayer1().setX(player1X);
        getPlayer1().setY(player1Y);
        
        // On remplace le joueur 2 par le bot en utilisant la méthode loadLevel
        // qui permet de remplacer les joueurs
        int[][] currentLevel = extractLevelData(getBoard());
        if (currentLevel != null) {
            // Créer un niveau temporaire avec les mêmes données
            String tempLevelPath = createTempLevelWithBot(currentLevel, player1X, player1Y, player2X, player2Y);
            
            // Charger ce niveau temporaire
            if (tempLevelPath != null) {
                super.loadLevel(tempLevelPath);
                
                // Récupérer le joueur 1 et le bot
                humanPlayer = getPlayer1();
                botPlayer = new BotPlayer(player2X, player2Y, Color.RED, 2, difficultyLevel);
                
                // Maintenant on doit remplacer manuellement le joueur 2 par le bot
                // en utilisant la réflexion puisqu'on ne peut pas accéder directement à player2
                try {
                    Field player2Field = Game.class.getDeclaredField("player2");
                    player2Field.setAccessible(true);
                    player2Field.set(this, botPlayer);
                } catch (Exception e) {
                    System.err.println("Impossible de remplacer le joueur 2 par le bot: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Extrait les données du niveau à partir du plateau de jeu actuel
     * @param board Le plateau de jeu
     * @return Un tableau 2D représentant le niveau
     */
    private int[][] extractLevelData(GameBoard board) {
        int height = board.getHeight();
        int width = board.getWidth();
        int[][] levelData = new int[height][width];
        
        // Nous n'avons pas accès direct aux données du tableau board
        // donc nous allons créer un niveau basique avec les mêmes dimensions
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Par défaut, mettre des cases vides
                levelData[y][x] = GameBoard.EMPTY;
                
                // Ajouter les murs sur les bords
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    levelData[y][x] = GameBoard.WALL;
                }
                // Ajouter les murs indestructibles en damier
                else if (x % 2 == 0 && y % 2 == 0) {
                    levelData[y][x] = GameBoard.WALL;
                }
            }
        }
        
        return levelData;
    }
    
    /**
     * Crée un fichier de niveau temporaire avec le bot
     */
    private String createTempLevelWithBot(int[][] levelData, int player1X, int player1Y, int player2X, int player2Y) {
        try {
            // Créer un fichier temporaire
            String tempFile = "levels/temp_bot_level.level";
            File file = new File(tempFile);
            file.getParentFile().mkdirs();
            
            try (PrintWriter writer = new PrintWriter(file)) {
                // Écrire les dimensions
                writer.println(levelData[0].length + "," + levelData.length);
                
                // Écrire les positions des joueurs
                writer.println(player1X + "," + player1Y + "," + player2X + "," + player2Y);
                
                // Écrire les données du niveau
                for (int[] row : levelData) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < row.length; i++) {
                        sb.append(row[i]);
                        if (i < row.length - 1) {
                            sb.append(",");
                        }
                    }
                    writer.println(sb.toString());
                }
            }
            
            return tempFile;
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du niveau temporaire: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Met à jour la logique du jeu avec le bot
     */
    @Override
    public void update() {
        // Mettre à jour le plateau
        getBoard().update();
        
        // Mettre à jour le joueur 1
        getPlayer1().update();
        
        // Mettre à jour le bot
        botPlayer.update();
        botPlayer.updateBot(getBoard(), getPlayer1());
        
        // Faire bouger le bot
        botPlayer.moveInCurrentDirection(getBoard());
        
        // Vérifier si le bot veut poser une bombe
        if (botPlayer.wantToPlaceBomb()) {
            placeBomb(botPlayer);
        }
        
        // Traiter les mouvements du joueur 1 uniquement
        processPlayerMovement();
        
        // Mettre à jour les bombes (code copié de la classe parente)
        updateBombs();
    }
    
    /**
     * Traite uniquement les mouvements du joueur 1
     */
    private void processPlayerMovement() {
        if (!isGameRunning()) return;

        // Joueur 1 avec contrôles fixes
        Player player1 = getPlayer1();
        if (player1 != null && player1.isAlive()) {
            // Récupérer les touches pressées
            Set<KeyCode> pressedKeys = getPressedKeys();
            
            // Constantes pour les contrôles
            KeyCode P1_UP = KeyCode.Z;
            KeyCode P1_DOWN = KeyCode.S;
            KeyCode P1_LEFT = KeyCode.Q;
            KeyCode P1_RIGHT = KeyCode.D;
            
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
    }
    
    /**
     * Méthode pour déplacer un joueur
     * @param player Le joueur à déplacer
     * @param dx Déplacement en X
     * @param dy Déplacement en Y
     */
    private void movePlayer(Player player, int dx, int dy) {
        if (player.moveToPosition(player.getX() + dx, player.getY() + dy, getBoard())) {
            // Collecter power-up si disponible
            PowerUp powerUp = getBoard().collectPowerUp(player.getX(), player.getY());
            if (powerUp != null) {
                player.applyPowerUp(powerUp.getType());
                // Ajouter des points
                if (player == getPlayer1()) {
                    addPlayer1Score(10);
                } else {
                    addPlayer2Score(10);
                }
            }
        }
    }
    
    /**
     * Met à jour les bombes (copié de la classe parente)
     */
    private void updateBombs() {
        List<Bomb> bombs = getBombs();
        Iterator<Bomb> bombIterator = bombs.iterator();
        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();
            bomb.update();

            if (bomb.hasExploded()) {
                // Gérer l'explosion
                handleExplosion(bomb);
                
                bombIterator.remove();
                getBoard().removeBomb(bomb.getX(), bomb.getY());
            }
        }
    }
    
    /**
     * Gère l'explosion d'une bombe
     * @param bomb La bombe qui a explosé
     */
    private void handleExplosion(Bomb bomb) {
        // Comme nous n'avons pas accès à la méthode handleExplosion de la classe parente,
        // nous utilisons la réflexion pour l'invoquer
        try {
            java.lang.reflect.Method handleExplosionMethod = 
                Game.class.getDeclaredMethod("handleExplosion", Bomb.class);
            handleExplosionMethod.setAccessible(true);
            handleExplosionMethod.invoke(this, bomb);
        } catch (Exception e) {
            System.err.println("Erreur lors de la gestion de l'explosion: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Ajoute des points au score du joueur 1
     * @param points Points à ajouter
     */
    private void addPlayer1Score(int points) {
        try {
            java.lang.reflect.Field player1ScoreField = Game.class.getDeclaredField("player1Score");
            player1ScoreField.setAccessible(true);
            int currentScore = player1ScoreField.getInt(this);
            player1ScoreField.set(this, currentScore + points);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout de points au joueur 1: " + e.getMessage());
        }
    }
    
    /**
     * Ajoute des points au score du joueur 2
     * @param points Points à ajouter
     */
    private void addPlayer2Score(int points) {
        try {
            java.lang.reflect.Field player2ScoreField = Game.class.getDeclaredField("player2Score");
            player2ScoreField.setAccessible(true);
            int currentScore = player2ScoreField.getInt(this);
            player2ScoreField.set(this, currentScore + points);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout de points au joueur 2: " + e.getMessage());
        }
    }
    
    /**
     * Récupère l'ensemble des touches pressées
     * @return L'ensemble des touches pressées
     */
    private Set<KeyCode> getPressedKeys() {
        try {
            java.lang.reflect.Field pressedKeysField = Game.class.getDeclaredField("pressedKeys");
            pressedKeysField.setAccessible(true);
            return (Set<KeyCode>) pressedKeysField.get(this);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des touches pressées: " + e.getMessage());
            return new HashSet<>();
        }
    }
    
    /**
     * Surcharge de la méthode handleKeyPressed pour ignorer les commandes du joueur 2
     */
    @Override
    public void handleKeyPressed(KeyCode key) {
        if (!isGameRunning()) return;

        // Ajouter la touche à l'ensemble des touches pressées
        getPressedKeys().add(key);

        // Gestion des bombes pour le joueur 1 uniquement
        KeyCode P1_BOMB = KeyCode.A;
        KeyCode P1_SPECIAL = KeyCode.E;
        
        if (key == P1_BOMB && getPlayer1().isAlive()) {
            placeBomb(getPlayer1());
        } else if (key == P1_SPECIAL && getPlayer1().isAlive()) {
            useSpecialAbility(getPlayer1());
        }
        
        // Ignorer les commandes du joueur 2
    }
    
    /**
     * Utilise la capacité spéciale du joueur
     */
    private void useSpecialAbility(Player player) {
        try {
            java.lang.reflect.Method useSpecialAbilityMethod = 
                Game.class.getDeclaredMethod("useSpecialAbility", Player.class);
            useSpecialAbilityMethod.setAccessible(true);
            useSpecialAbilityMethod.invoke(this, player);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'utilisation de la capacité spéciale: " + e.getMessage());
        }
    }
    
    /**
     * Place une bombe à la position du joueur
     */
    private void placeBomb(Player player) {
        try {
            java.lang.reflect.Method placeBombMethod = 
                Game.class.getDeclaredMethod("placeBomb", Player.class);
            placeBombMethod.setAccessible(true);
            placeBombMethod.invoke(this, player);
        } catch (Exception e) {
            System.err.println("Erreur lors du placement de la bombe: " + e.getMessage());
        }
    }
    
    /**
     * Définit le niveau de difficulté du bot
     * @param level Niveau de difficulté (1-3)
     */
    public void setDifficultyLevel(int level) {
        this.difficultyLevel = Math.max(1, Math.min(3, level));
        botPlayer.setDifficultyLevel(difficultyLevel);
    }
    
    /**
     * Retourne le niveau de difficulté actuel
     * @return Niveau de difficulté
     */
    public int getDifficultyLevel() {
        return difficultyLevel;
    }
    
    /**
     * Retourne le joueur humain
     * @return Le joueur humain
     */
    public Player getHumanPlayer() {
        return humanPlayer;
    }
    
    /**
     * Retourne le bot
     * @return Le bot
     */
    public BotPlayer getBotPlayer() {
        return botPlayer;
    }
    
    /**
     * Définit le profil du joueur humain
     * @param profile Le profil du joueur
     */
    public void setHumanPlayerProfile(Object profile) {
        if (humanPlayer != null && profile != null) {
            // Utiliser setProfile si disponible
            try {
                Method setProfileMethod = Player.class.getMethod("setProfile", Object.class);
                setProfileMethod.invoke(humanPlayer, profile);
            } catch (Exception e) {
                System.err.println("Méthode setProfile non disponible: " + e.getMessage());
            }
        }
    }
    
    /**
     * Indique si le mode bot est activé
     * @return true si le mode bot est activé
     */
    public boolean isBotMode() {
        return botMode;
    }
} 