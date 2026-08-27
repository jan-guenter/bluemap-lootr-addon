/*
 * SPDX-License-Identifier: MIT
 *
 * Model emission follows BlueMap's MIT-licensed resource-model coordinate and
 * UV conventions, limited to installed static models and texture overrides.
 */

package io.github.janguenter.bluemap.lootr.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.util.math.MatrixM4f;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

import java.util.Map;

/** Emits an installed ordinary JSON model with caller-owned texture overrides. */
final class InstalledJsonEmitter {

    private static final float BLOCK_SCALE = 1F / 16F;

    private final ResourcePack resourcePack;
    private final TextureGallery textures;

    InstalledJsonEmitter(ResourcePack resourcePack, TextureGallery textures) {
        this.resourcePack = resourcePack;
        this.textures = textures;
    }

    boolean emit(
            Key modelKey,
            BlockNeighborhood block,
            TileModelView target,
            float xRotation,
            float yRotation,
            float zRotation,
            Map<String, Key> overrides,
            Color mapColor
    ) {
        Model model = resourcePack.getModels().get(modelKey);
        if (!preflight(model, overrides)) {
            return false;
        }
        Variant transform = new Variant(
                new ResourcePath<>(modelKey), xRotation, yRotation, zRotation
        );
        int modelStart = target.getTileModel().size();
        boolean emitted = false;
        for (Element element : model.getElements()) {
            if (element == null) {
                continue;
            }
            int elementStart = target.getTileModel().size();
            emitted |= emitElement(
                    model, element, block, target, overrides, mapColor, transform
            );
            int elementCount = target.getTileModel().size() - elementStart;
            if (elementCount > 0) {
                target.initialize(elementStart);
                target.transform(new MatrixM4f()
                        .copy(element.getRotation().getMatrix())
                        .scale(BLOCK_SCALE, BLOCK_SCALE, BLOCK_SCALE));
            }
        }
        int count = target.getTileModel().size() - modelStart;
        if (count > 0 && transform.isTransformed()) {
            target.initialize(modelStart).transform(transform.getTransformMatrix());
        }
        target.initialize(modelStart);
        return emitted;
    }

    private boolean preflight(Model model, Map<String, Key> overrides) {
        if (model == null || model.getElements() == null
                || model.getElements().length == 0) {
            return false;
        }
        boolean hasFace = false;
        for (Element element : model.getElements()) {
            if (element == null) {
                continue;
            }
            for (Face face : element.getFaces().values()) {
                hasFace = true;
                Key texture = textureKey(model, face, overrides);
                if (texture == null || resourcePack.getTextures().get(texture) == null) {
                    return false;
                }
            }
        }
        return hasFace;
    }

    private boolean emitElement(
            Model model,
            Element element,
            BlockNeighborhood block,
            TileModelView target,
            Map<String, Key> overrides,
            Color mapColor,
            Variant transform
    ) {
        Vector3f from = element.getFrom();
        Vector3f to = element.getTo();
        float x0 = from.getX();
        float y0 = from.getY();
        float z0 = from.getZ();
        float x1 = to.getX();
        float y1 = to.getY();
        float z1 = to.getZ();
        boolean emitted = false;
        emitted |= emitFace(model, element, Direction.DOWN, block, target, overrides,
                mapColor, transform, x0, y0, z0, x1, y0, z0,
                x1, y0, z1, x0, y0, z1);
        emitted |= emitFace(model, element, Direction.UP, block, target, overrides,
                mapColor, transform, x0, y1, z1, x1, y1, z1,
                x1, y1, z0, x0, y1, z0);
        emitted |= emitFace(model, element, Direction.NORTH, block, target, overrides,
                mapColor, transform, x1, y0, z0, x0, y0, z0,
                x0, y1, z0, x1, y1, z0);
        emitted |= emitFace(model, element, Direction.SOUTH, block, target, overrides,
                mapColor, transform, x0, y0, z1, x1, y0, z1,
                x1, y1, z1, x0, y1, z1);
        emitted |= emitFace(model, element, Direction.WEST, block, target, overrides,
                mapColor, transform, x0, y0, z0, x0, y0, z1,
                x0, y1, z1, x0, y1, z0);
        emitted |= emitFace(model, element, Direction.EAST, block, target, overrides,
                mapColor, transform, x1, y0, z1, x1, y0, z0,
                x1, y1, z0, x1, y1, z1);
        return emitted;
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private boolean emitFace(
            Model model,
            Element element,
            Direction direction,
            BlockNeighborhood block,
            TileModelView target,
            Map<String, Key> overrides,
            Color mapColor,
            Variant transform,
            float ax, float ay, float az,
            float bx, float by, float bz,
            float cx, float cy, float cz,
            float dx, float dy, float dz
    ) {
        Face face = element.getFaces().get(direction);
        if (face == null) {
            return false;
        }
        Key textureKey = textureKey(model, face, overrides);
        Texture texture = textureKey == null
                ? null : resourcePack.getTextures().get(textureKey);
        if (texture == null) {
            return false;
        }
        int start = target.add(2);
        TileModel mesh = target.getTileModel();
        mesh.setPositions(start, ax, ay, az, bx, by, bz, cx, cy, cz);
        mesh.setPositions(start + 1, ax, ay, az, cx, cy, cz, dx, dy, dz);

        Vector4f raw = face.getUv();
        float[][] corners = {
                {raw.getX() / 16F, raw.getW() / 16F},
                {raw.getZ() / 16F, raw.getW() / 16F},
                {raw.getZ() / 16F, raw.getY() / 16F},
                {raw.getX() / 16F, raw.getY() / 16F}
        };
        int rotation = Math.floorMod(face.getRotation() / 90, 4);
        float[] uv0 = corners[rotation];
        float[] uv1 = corners[(rotation + 1) % 4];
        float[] uv2 = corners[(rotation + 2) % 4];
        float[] uv3 = corners[(rotation + 3) % 4];
        mesh.setUvs(start, uv0[0], uv0[1], uv1[0], uv1[1], uv2[0], uv2[1]);
        mesh.setUvs(start + 1, uv0[0], uv0[1], uv2[0], uv2[1], uv3[0], uv3[1]);

        int material = textures.get(textureKey);
        for (int index = start; index < start + 2; index++) {
            mesh.setMaterialIndex(index, material);
            mesh.setColor(index, 1F, 1F, 1F);
            mesh.setAOs(index, 1F, 1F, 1F);
        }
        FaceLighting.Sample light = FaceLighting.sample(block, direction, transform);
        mesh.setSunlight(start, light.sunlight());
        mesh.setSunlight(start + 1, light.sunlight());
        mesh.setBlocklight(start, light.blocklight());
        mesh.setBlocklight(start + 1, light.blocklight());
        if (direction == Direction.UP) {
            mapColor.add(new Color().set(texture.getColorPremultiplied()));
        }
        return true;
    }

    private static Key textureKey(
            Model model,
            Face face,
            Map<String, Key> overrides
    ) {
        String reference = face.getTexture().getReferenceName();
        if (reference != null && overrides.containsKey(reference)) {
            return overrides.get(reference);
        }
        ResourcePath<Texture> path = face.getTexture()
                .getTexturePath(model.getTextures()::get);
        return path;
    }
}
