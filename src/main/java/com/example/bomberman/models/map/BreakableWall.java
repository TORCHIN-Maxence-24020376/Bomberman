package com.example.bomberman.models.map;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import com.example.bomberman.utils.SpriteLoader;

public class BreakableWall extends Tile {

    private boolean destroyed = false;

    public BreakableWall(int x, int y) {
        super(x, y, false, true);
    }

    @Override
    public Image getSprite() {
        return SpriteLoader.load(
            "/com/example/bomberman/default/tiles/breakable_wall.png",
            Color.ORANGE,
            32
        );
    }

    public boolean isDestroyed(){
        return destroyed;
    }

    public void destroy() {
        destroyed = true;
    }
}
