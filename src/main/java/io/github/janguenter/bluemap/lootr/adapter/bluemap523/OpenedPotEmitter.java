/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.lootr.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.lootr.model.OpenedPotModel;

/** Emits Lootr's five-cuboid static opened-pot model with exact atlas regions. */
final class OpenedPotEmitter {

    private static final Key TEXTURE = Key.parse("lootr:entity/loot_pot_open");

    private final ResourcePack resourcePack;
    private final TextureGallery textures;

    OpenedPotEmitter(ResourcePack resourcePack, TextureGallery textures) {
        this.resourcePack = resourcePack;
        this.textures = textures;
    }

    boolean emit(
            BlockNeighborhood block,
            TileModelView target,
            String facing,
            Color mapColor
    ) {
        Texture texture = resourcePack.getTextures().get(TEXTURE);
        if (texture == null) {
            return false;
        }
        int material = textures.get(TEXTURE);
        for (OpenedPotModel.Quad quad : OpenedPotModel.forFacing(facing)) {
            int start = target.add(2);
            TileModel mesh = target.getTileModel();
            var vertices = quad.vertices();
            setTriangle(mesh, start, vertices.get(0), vertices.get(1), vertices.get(2));
            setTriangle(
                    mesh, start + 1,
                    vertices.get(0), vertices.get(2), vertices.get(3)
            );
            FaceLighting.Sample light = FaceLighting.sample(
                    block, direction(quad.face())
            );
            for (int index = start; index < start + 2; index++) {
                mesh.setMaterialIndex(index, material);
                mesh.setColor(index, 1F, 1F, 1F);
                mesh.setAOs(index, 1F, 1F, 1F);
                mesh.setSunlight(index, light.sunlight());
                mesh.setBlocklight(index, light.blocklight());
            }
        }
        mapColor.add(new Color().set(texture.getColorPremultiplied()));
        return true;
    }

    private static void setTriangle(
            TileModel model,
            int index,
            OpenedPotModel.Vertex first,
            OpenedPotModel.Vertex second,
            OpenedPotModel.Vertex third
    ) {
        model.setPositions(index,
                first.x(), first.y(), first.z(),
                second.x(), second.y(), second.z(),
                third.x(), third.y(), third.z());
        model.setUvs(index,
                first.u(), first.v(), second.u(), second.v(), third.u(), third.v());
    }

    private static Direction direction(OpenedPotModel.Face face) {
        return switch (face) {
            case DOWN -> Direction.DOWN;
            case UP -> Direction.UP;
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case WEST -> Direction.WEST;
            case EAST -> Direction.EAST;
        };
    }
}
