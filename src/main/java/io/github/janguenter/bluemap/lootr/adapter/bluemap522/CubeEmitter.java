/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.lootr.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

import java.util.EnumMap;
import java.util.Map;

/** Emits bounded cuboids using only admitted installed textures. */
final class CubeEmitter {

    private static final Direction[] DIRECTIONS = {
            Direction.DOWN, Direction.UP, Direction.NORTH,
            Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    private final ResourcePack resourcePack;
    private final TextureGallery textures;

    CubeEmitter(ResourcePack resourcePack, TextureGallery textures) {
        this.resourcePack = resourcePack;
        this.textures = textures;
    }

    boolean uniform(
            BlockNeighborhood block,
            TileModelView target,
            Bounds bounds,
            Key texture,
            Color mapColor
    ) {
        EnumMap<Direction, Key> faces = new EnumMap<>(Direction.class);
        for (Direction direction : DIRECTIONS) {
            faces.put(direction, texture);
        }
        return cuboid(block, target, bounds, faces, mapColor);
    }

    boolean cuboid(
            BlockNeighborhood block,
            TileModelView target,
            Bounds bounds,
            Map<Direction, Key> faces,
            Color mapColor
    ) {
        for (Direction direction : DIRECTIONS) {
            Key key = faces.get(direction);
            if (key == null || resourcePack.getTextures().get(key) == null) {
                return false;
            }
        }
        for (Direction direction : DIRECTIONS) {
            emitFace(block, target, bounds, direction, faces.get(direction), mapColor);
        }
        return true;
    }

    private void emitFace(
            BlockNeighborhood block,
            TileModelView target,
            Bounds bounds,
            Direction direction,
            Key textureKey,
            Color mapColor
    ) {
        Vertex[] vertices = vertices(bounds, direction);
        int start = target.add(2);
        TileModel mesh = target.getTileModel();
        setTriangle(mesh, start, vertices[0], vertices[1], vertices[2],
                0F, 1F, 1F, 1F, 1F, 0F);
        setTriangle(mesh, start + 1, vertices[0], vertices[2], vertices[3],
                0F, 1F, 1F, 0F, 0F, 0F);
        int material = textures.get(textureKey);
        FaceLighting.Sample light = FaceLighting.sample(block, direction);
        for (int index = start; index < start + 2; index++) {
            mesh.setMaterialIndex(index, material);
            mesh.setColor(index, 1F, 1F, 1F);
            mesh.setAOs(index, 1F, 1F, 1F);
            mesh.setSunlight(index, light.sunlight());
            mesh.setBlocklight(index, light.blocklight());
        }
        if (direction == Direction.UP) {
            Texture texture = resourcePack.getTextures().get(textureKey);
            mapColor.add(new Color().set(texture.getColorPremultiplied()));
        }
    }

    private static Vertex[] vertices(Bounds bounds, Direction face) {
        float x0 = bounds.minimumX();
        float y0 = bounds.minimumY();
        float z0 = bounds.minimumZ();
        float x1 = bounds.maximumX();
        float y1 = bounds.maximumY();
        float z1 = bounds.maximumZ();
        return switch (face) {
            case DOWN -> vertices(x0, y0, z0, x1, y0, z0,
                    x1, y0, z1, x0, y0, z1);
            case UP -> vertices(x0, y1, z1, x1, y1, z1,
                    x1, y1, z0, x0, y1, z0);
            case NORTH -> vertices(x1, y0, z0, x0, y0, z0,
                    x0, y1, z0, x1, y1, z0);
            case SOUTH -> vertices(x0, y0, z1, x1, y0, z1,
                    x1, y1, z1, x0, y1, z1);
            case WEST -> vertices(x0, y0, z0, x0, y0, z1,
                    x0, y1, z1, x0, y1, z0);
            case EAST -> vertices(x1, y0, z1, x1, y0, z0,
                    x1, y1, z0, x1, y1, z1);
        };
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private static Vertex[] vertices(
            float ax, float ay, float az,
            float bx, float by, float bz,
            float cx, float cy, float cz,
            float dx, float dy, float dz
    ) {
        return new Vertex[]{
                new Vertex(ax, ay, az), new Vertex(bx, by, bz),
                new Vertex(cx, cy, cz), new Vertex(dx, dy, dz)
        };
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private static void setTriangle(
            TileModel model,
            int index,
            Vertex first,
            Vertex second,
            Vertex third,
            float u1,
            float v1,
            float u2,
            float v2,
            float u3,
            float v3
    ) {
        model.setPositions(index,
                first.x(), first.y(), first.z(),
                second.x(), second.y(), second.z(),
                third.x(), third.y(), third.z());
        model.setUvs(index, u1, v1, u2, v2, u3, v3);
    }

    record Bounds(
            float minimumX,
            float minimumY,
            float minimumZ,
            float maximumX,
            float maximumY,
            float maximumZ
    ) {

        Bounds {
            if (minimumX >= maximumX || minimumY >= maximumY
                    || minimumZ >= maximumZ) {
                throw new IllegalArgumentException("empty cuboid bounds");
            }
        }
    }

    private record Vertex(float x, float y, float z) {
    }
}
