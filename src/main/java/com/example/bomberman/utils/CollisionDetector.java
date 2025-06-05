package com.example.bomberman.utils;

import com.example.bomberman.models.entities.Entity;
import com.example.bomberman.models.entities.Player;
import com.example.bomberman.models.maps.GameMap;
import com.example.bomberman.models.entities.Bomb;
import javafx.geometry.Rectangle2D;

public class CollisionDetector {
    
    public static boolean checkEntityTileCollision(Entity entity, GameMap gameMap, int x, int y) {
        // Check if entity collides with a tile at position (x, y)
        return !gameMap.isWalkable(x, y);
    }
    
    public static boolean checkPlayerTileCollision(Player player, GameMap gameMap, int x, int y) {
        // Check if player collides with a tile at position (x, y)
        return !gameMap.isWalkable(x, y);
    }
    
    public static boolean checkEntityEntityCollision(Entity entity1, Entity entity2) {
        // Check if two entities collide
        Rectangle2D bounds1 = entity1.getBounds();
        Rectangle2D bounds2 = entity2.getBounds();
        return bounds1.intersects(bounds2);
    }
    
    public static boolean checkPlayerBombCollision(Player player, Bomb bomb) {
        // Check if player collides with a bomb
        Rectangle2D playerBounds = player.getBounds();
        Rectangle2D bombBounds = bomb.getBounds();
        return playerBounds.intersects(bombBounds);
    }
    
    public static boolean isInExplosionRange(int x, int y, int bombX, int bombY, int range, int direction) {
        // Check if position (x, y) is in explosion range
        return false;
    }
} 