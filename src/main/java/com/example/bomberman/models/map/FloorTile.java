package com.example.bomberman.models.map;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import com.example.bomberman.utils.SpriteLoader;

public class FloorTile extends Tile {

    public FloorTile(int x, int y) {
        super(x, y, true, false
        );
    }

    @Override
    public Image getSprite() {
        return SpriteLoader.load(
            "/com/example/bomberman/default/tiles/floor.png",
            Color.GRAY,
            32
        );
    }
}
