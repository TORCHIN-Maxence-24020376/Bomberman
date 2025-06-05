package com.example.bomberman.models.map;

import javafx.scene.image.Image;
import com.example.bomberman.models.map.GameMap

public abstract class Tile {
    protected int x, y;
    protected boolean walkable;
    protected boolean destructible;

    public Tile(int x, int y, boolean walkable, boolean destructible) {
        this.x = x;
        this.y = y;
        this.walkable = walkable;
        this.destructible = destructible;
    }

    public boolean isWalkable() {
        return walkable;
    }

    public boolean isDestructible() {
        return destructible;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void destroy(){
        // A faire
    }

    public abstract Image getSprite();
}
