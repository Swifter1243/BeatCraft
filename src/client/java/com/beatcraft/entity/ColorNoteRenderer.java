package com.beatcraft.entity;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class ColorNoteRenderer extends EntityRenderer<ColorNoteEntity> {

    public ColorNoteRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(ColorNoteEntity entity) {
        return null;
    }
}
