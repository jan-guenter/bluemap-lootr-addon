/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.lootr.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenedPotModelTest {

    @Test
    void emitsFiveSourceCuboidsWithBoundedUvs() {
        List<OpenedPotModel.Quad> quads = OpenedPotModel.forFacing("north");
        assertEquals(30, quads.size());
        assertEquals(120, quads.stream().mapToInt(quad -> quad.vertices().size()).sum());
        assertTrue(quads.stream().flatMap(quad -> quad.vertices().stream()).allMatch(vertex ->
                vertex.u() >= 0F && vertex.u() <= 1F
                        && vertex.v() >= 0F && vertex.v() <= 1F
                        && vertex.x() >= -0.02F && vertex.x() <= 1.02F
                        && vertex.y() >= -0.02F && vertex.y() <= 1.02F
                        && vertex.z() >= -0.02F && vertex.z() <= 1.02F
        ));
    }

    @Test
    void rotatesEveryHorizontalFacingAndRejectsOtherValues() {
        for (String facing : List.of("north", "east", "south", "west")) {
            assertEquals(30, OpenedPotModel.forFacing(facing).size());
        }
        assertThrows(
                IllegalArgumentException.class,
                () -> OpenedPotModel.forFacing("up")
        );
    }
}
