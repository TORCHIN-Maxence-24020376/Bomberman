package com.example.bomberman.models.world;

import com.example.bomberman.models.entities.BotPlayer;
import com.example.bomberman.models.entities.Player;
import com.example.bomberman.models.entities.PlayerProfile;
import javafx.scene.paint.Color;

/**
 * Classe représentant une partie contre un bot
 */
public class BotGame extends Game {
    
    private BotPlayer botPlayer;
    private Player humanPlayer;
    private int difficultyLevel;
    
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
        
        // Remplacer le joueur 2 par le bot (on ne peut pas utiliser setPlayer2 directement)
        replacePlayer2WithBot();
    }
    
    /**
     * Remplace le joueur 2 par le bot
     * Cette méthode contourne la limitation d'accès à player2 dans Game
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
            // Créer un nouveau niveau avec les mêmes données
            // mais en remplaçant le joueur 2 par le bot
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
                    java.lang.reflect.Field player2Field = Game.class.getDeclaredField("player2");
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
            java.io.File file = new java.io.File(tempFile);
            file.getParentFile().mkdirs();
            
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
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
        // Mettre à jour le comportement du bot
        if (botPlayer != null && humanPlayer != null) {
            botPlayer.updateBot(getBoard(), humanPlayer);
            
            // Déplacer le bot selon sa stratégie
            botPlayer.moveInCurrentDirection(getBoard());
            
            // Poser une bombe si le bot le souhaite
            if (botPlayer.wantToPlaceBomb()) {
                botPlaceBomb();
            }
        }
        
        // Appeler la méthode update de la classe parente pour le reste de la logique
        super.update();
    }
    
    /**
     * Permet au bot de placer une bombe
     * Cette méthode contourne la limitation d'accès à placeBomb dans Game
     */
    private void botPlaceBomb() {
        if (botPlayer != null && botPlayer.canPlaceBomb()) {
            // On doit utiliser la réflexion pour accéder à la méthode placeBomb
            try {
                java.lang.reflect.Method placeBombMethod = Game.class.getDeclaredMethod("placeBomb", Player.class);
                placeBombMethod.setAccessible(true);
                placeBombMethod.invoke(this, botPlayer);
            } catch (Exception e) {
                System.err.println("Impossible de faire placer une bombe au bot: " + e.getMessage());
                
                // Alternative: implémenter directement la logique de placeBomb ici
                // (copier le code de Game.placeBomb)
            }
        }
    }
    
    /**
     * Définit le niveau de difficulté du bot
     * @param level Niveau de difficulté (1-3)
     */
    public void setDifficultyLevel(int level) {
        this.difficultyLevel = Math.max(1, Math.min(3, level));
        if (botPlayer != null) {
            botPlayer.setDifficultyLevel(difficultyLevel);
        }
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
    public void setHumanPlayerProfile(PlayerProfile profile) {
        if (humanPlayer != null) {
            humanPlayer.setProfile(profile);
        }
    }
} 