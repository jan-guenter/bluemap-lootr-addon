/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.lootr.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.math.VectorM3f;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

/** Samples the host and exposed neighbor for one transformed face. */
final class FaceLighting {

    private FaceLighting() {
    }

    static Sample sample(BlockNeighborhood block, Direction direction) {
        LightData own = block.getLightData();
        LightData faced = block.getNeighborBlock(
                direction.toVector().getX(),
                direction.toVector().getY(),
                direction.toVector().getZ()
        ).getLightData();
        return new Sample(
                Math.max(own.getSkyLight(), faced.getSkyLight()),
                Math.max(own.getBlockLight(), faced.getBlockLight())
        );
    }

    static Sample sample(
            BlockNeighborhood block,
            Direction direction,
            Variant transform
    ) {
        VectorM3f relative = new VectorM3f(0F, 0F, 0F).set(direction.toVector());
        if (transform.isTransformed()) {
            relative.rotateAndScale(transform.getTransformMatrix());
        }
        LightData own = block.getLightData();
        LightData faced = block.getNeighborBlock(
                Math.round(relative.x),
                Math.round(relative.y),
                Math.round(relative.z)
        ).getLightData();
        return new Sample(
                Math.max(own.getSkyLight(), faced.getSkyLight()),
                Math.max(own.getBlockLight(), faced.getBlockLight())
        );
    }

    record Sample(int sunlight, int blocklight) {
    }
}
