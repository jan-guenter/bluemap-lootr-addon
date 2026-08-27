/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.lootr.model;

import de.bluecolored.bluemap.core.util.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LootrRenderRulesTest {

    @Test
    void persistedOpenedBitIsBoundedAndDefaultsUnopened() {
        assertEquals(
                new LootrRenderRules.OpenedState(false, true),
                LootrRenderRules.openedState(null)
        );
        assertEquals(
                new LootrRenderRules.OpenedState(false, true),
                LootrRenderRules.openedState((byte) 0)
        );
        assertEquals(
                new LootrRenderRules.OpenedState(true, true),
                LootrRenderRules.openedState((byte) 1)
        );
        assertFalse(LootrRenderRules.openedState((byte) 2).valid());
        assertFalse(LootrRenderRules.openedState(Boolean.TRUE).valid());
        assertFalse(LootrRenderRules.openedState("1").valid());
    }

    @Test
    void directionMappingsRejectUnknownState() {
        assertEquals(0, LootrRenderRules.horizontalQuarterTurns("north"));
        assertEquals(1, LootrRenderRules.horizontalQuarterTurns("east"));
        assertEquals(2, LootrRenderRules.horizontalQuarterTurns("south"));
        assertEquals(3, LootrRenderRules.horizontalQuarterTurns("west"));
        assertEquals(-1, LootrRenderRules.horizontalQuarterTurns("up"));
        assertEquals(Direction.DOWN, LootrRenderRules.direction("down"));
        assertEquals(Direction.UP, LootrRenderRules.direction("up"));
        assertEquals(Direction.WEST, LootrRenderRules.direction("west"));
        assertNull(LootrRenderRules.direction("invalid"));
    }

    @Test
    void brushableTextureUsesOpenedOverrideOrExactDustedStage() {
        assertEquals(
                "minecraft:block/suspicious_sand_3",
                LootrRenderRules.brushableTexture(
                        "lootr:suspicious_sand", false, "3"
                ).getFormatted()
        );
        assertEquals(
                "lootr:block/suspicious_gravel_open",
                LootrRenderRules.brushableTexture(
                        "lootr:suspicious_gravel", true, "bad"
                ).getFormatted()
        );
        assertNull(LootrRenderRules.brushableTexture(
                "lootr:suspicious_gravel", false, "4"
        ));
        assertNull(LootrRenderRules.brushableTexture(
                "minecraft:suspicious_sand", false, "0"
        ));
    }

    @Test
    void targetCatalogContainsOnlyEightOwnedBlocks() {
        assertEquals(8, LootrRenderRules.TARGETS.size());
        assertTrue(LootrRenderRules.TARGETS.contains("lootr:decorated_pot"));
        assertFalse(LootrRenderRules.TARGETS.contains("lootr:trophy"));
    }
}
