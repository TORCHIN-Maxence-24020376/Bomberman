package com.example.bomberman.models.entities;

import com.example.bomberman.models.map.GameMap;
import com.example.bomberman.utils.SpriteLoader;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;


public class Bomb extends Entity {
    private int range;
    private DoubleProperty timer = new SimpleDoubleProperty(25);
    private Player owner;
    private BooleanProperty exploded = new SimpleBooleanProperty(false);

    public Bomb(int x, int y, int range, Player owner, GameMap map) {
        super(x,y,map);
        this.range = range;
        this.owner = owner;
    }

    public void Update(){
        tick();

        List<Explosion> blasts = explode();

        exploded.set(true);

        for (Explosion e : blasts) {
            int ex = e.getX();
            int ey = e.getY();
            Tile t = map.getTile(ex, ey);
            if (t != null && t.isDestructible()) {
                t.destroy(map);
            }
        }
    }

    @Override
    public Rectangle2D getHitBox() {
        int ts = map.getTileSize();
        return new Rectangle2D(x * ts, y * ts, ts, ts);
    }

    public void tick(){
        this.timer.set(this.timer.get() - 1);
    }

    public List<Explosion> explode(){
        return List<Explosion>;
    }

    public boolean isExploded() {
        return exploded.get();
    }

    public Player getOwner() {
        return owner;
    }

    public int getRange() {
        return range;
    }

    @Override
    public Image getSprite() {
        return SpriteLoader.load(
                "/com/example/bomberman/default/bomb.png",
                Color.BLACK,
                32
        );
    }
}
