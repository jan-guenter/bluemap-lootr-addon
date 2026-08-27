/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.lootr.model;

import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;

import java.util.Set;

/** Exact Lootr 1.11.37.122 static appearance routing and bounded NBT decoding. */
public final class LootrRenderRules {

    public static final Set<String> TARGETS = Set.of(
            "lootr:lootr_chest",
            "lootr:lootr_trapped_chest",
            "lootr:lootr_inventory",
            "lootr:lootr_barrel",
            "lootr:lootr_shulker",
            "lootr:suspicious_sand",
            "lootr:suspicious_gravel",
            "lootr:decorated_pot"
    );

    private LootrRenderRules() {
    }

    /**
     * Approximates Lootr's player-specific client appearance with its persisted
     * global opened bit. An absent bit is the upstream default (unopened).
     */
    public static OpenedState openedState(Object raw) {
        if (raw == null) {
            return new OpenedState(false, true);
        }
        if (raw instanceof Byte value && (value == 0 || value == 1)) {
            return new OpenedState(value == 1, true);
        }
        return new OpenedState(false, false);
    }

    public static int horizontalQuarterTurns(String facing) {
        return switch (facing) {
            case "north" -> 0;
            case "east" -> 1;
            case "south" -> 2;
            case "west" -> 3;
            default -> -1;
        };
    }

    public static Direction direction(String facing) {
        return switch (facing) {
            case "down" -> Direction.DOWN;
            case "up" -> Direction.UP;
            case "north" -> Direction.NORTH;
            case "south" -> Direction.SOUTH;
            case "west" -> Direction.WEST;
            case "east" -> Direction.EAST;
            default -> null;
        };
    }

    public static Key brushableTexture(String id, boolean opened, String dusted) {
        if (opened) {
            return switch (id) {
                case "lootr:suspicious_sand" ->
                        Key.parse("lootr:block/suspicious_sand_open");
                case "lootr:suspicious_gravel" ->
                        Key.parse("lootr:block/suspicious_gravel_open");
                default -> null;
            };
        }
        if (dusted == null || !dusted.matches("[0-3]")) {
            return null;
        }
        String kind = switch (id) {
            case "lootr:suspicious_sand" -> "sand";
            case "lootr:suspicious_gravel" -> "gravel";
            default -> null;
        };
        return kind == null ? null
                : Key.parse("minecraft:block/suspicious_" + kind + '_' + dusted);
    }

    public record OpenedState(boolean opened, boolean valid) {
    }
}
