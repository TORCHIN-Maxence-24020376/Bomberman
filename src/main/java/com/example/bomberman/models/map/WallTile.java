package com.example.bomberman.models.map;

import com.example.bomberman.utils.SpriteLoader;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class WallTile extends Tile {
    public WallTile(int x, int y) {
        super(x, y, false, false);
    }

    @Override
    public Image getSprite() {
        return SpriteLoader.load(
                "/com/example/bomberman/default/tiles/wall.png",
                Color.RED,
                32
        );
    }

}
