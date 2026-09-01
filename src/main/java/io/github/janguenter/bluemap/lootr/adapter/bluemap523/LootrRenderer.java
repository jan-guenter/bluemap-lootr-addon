/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.lootr.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.lootr.activation.AddonRuntime;
import io.github.janguenter.bluemap.lootr.model.LootrRenderRules;
import io.github.janguenter.bluemap.lootr.model.LootrRenderRules.OpenedState;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Exact-profile static renderer for Lootr custom models and BER containers. */
final class LootrRenderer implements BlockRenderer {

    private static final ThreadLocal<Boolean> STOCK_FALLBACK =
            ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final Set<String> DIAGNOSTICS = ConcurrentHashMap.newKeySet();
    private static final Key CHEST_MODEL =
            Key.parse("minecraft:entity/chest/chest");
    private static final Key SHULKER_MODEL =
            Key.parse("minecraft:entity/shulker_box");
    private static final Key POT_MODEL =
            Key.parse("minecraft:entity/decorated_pot");
    private static final CubeEmitter.Bounds FULL_BLOCK =
            new CubeEmitter.Bounds(0F, 0F, 0F, 1F, 1F, 1F);

    private final ResourcePack resourcePack;
    private final TextureGallery textures;
    private final RenderSettings settings;
    private final AddonRuntime runtime;
    private final VariantRendererCatalog variants;
    private final InstalledJsonEmitter json;
    private final CubeEmitter cubes;
    private final OpenedPotEmitter openedPots;
    private final Map<BlockRendererType, BlockRenderer> stockRenderers =
            new IdentityHashMap<>();

    LootrRenderer(
            ResourcePack resourcePack,
            TextureGallery textures,
            RenderSettings settings,
            AddonRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.textures = textures;
        this.settings = settings;
        this.runtime = runtime;
        variants = RendererDataRegistry.get(resourcePack);
        json = new InstalledJsonEmitter(resourcePack, textures);
        cubes = new CubeEmitter(resourcePack, textures);
        openedPots = new OpenedPotEmitter(resourcePack, textures);
    }

    @Override
    public void render(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        int start = target.getTileModel().size();
        Color initialColor = new Color().set(mapColor);
        try {
            if (!runtime.active() || variants == null) {
                stock(block, variant, target, mapColor);
                return;
            }
            String id = block.getBlockState().getId().getFormatted();
            if (!LootrRenderRules.TARGETS.contains(id)) {
                stock(block, variant, target, mapColor);
                return;
            }
            if (!(block.getBlockEntity() instanceof LootrBlockEntityData data)) {
                diagnose(id, "block-entity-unavailable-stock-fallback");
                stock(block, variant, target, mapColor);
                return;
            }
            OpenedState opened = LootrRenderRules.openedState(data.hasBeenOpened());
            if (!opened.valid()) {
                diagnose(id, "malformed-opened-state-stock-fallback");
                stock(block, variant, target, mapColor);
                return;
            }

            mapColor.set(0F, 0F, 0F, 0F, true);
            boolean emitted = switch (id) {
                case "lootr:lootr_chest", "lootr:lootr_inventory" ->
                        renderChest(block, target, opened.opened(), false, mapColor);
                case "lootr:lootr_trapped_chest" ->
                        renderChest(block, target, opened.opened(), true, mapColor);
                case "lootr:lootr_barrel" ->
                        renderBarrel(block, target, opened.opened(), mapColor);
                case "lootr:lootr_shulker" ->
                        renderShulker(block, target, opened.opened(), mapColor);
                case "lootr:suspicious_sand", "lootr:suspicious_gravel" ->
                        renderBrushable(id, block, target, opened.opened(), mapColor);
                case "lootr:decorated_pot" ->
                        renderPot(block, target, opened.opened(), mapColor);
                default -> false;
            };
            if (!emitted) {
                throw new IllegalStateException("installed render resource unavailable");
            }
            if (mapColor.a > 0F) {
                mapColor.flatten().straight();
            }
        } catch (MaxCapacityReachedException exception) {
            reset(target, start, mapColor, initialColor);
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            reset(target, start, mapColor, initialColor);
            String id = block.getBlockState().getId().getFormatted();
            diagnose(id, exception.getClass().getSimpleName());
            runtime.inactive("renderer-" + exception.getClass().getSimpleName());
            stockSafely(block, variant, target, mapColor, start, initialColor);
        }
    }

    private boolean renderChest(
            BlockNeighborhood block,
            TileModelView target,
            boolean opened,
            boolean trapped,
            Color mapColor
    ) {
        float rotation = chestRotation(
                block.getBlockState().getProperties().get("facing")
        );
        if (rotation < 0F) {
            return false;
        }
        String texture = trapped ? "lootr:chest_trapped" : "lootr:chest";
        if (opened) {
            texture += "_opened";
        }
        return json.emit(
                CHEST_MODEL, block, target, 0F, rotation, 0F,
                Map.of("chest", Key.parse(texture)), mapColor
        );
    }

    private boolean renderShulker(
            BlockNeighborhood block,
            TileModelView target,
            boolean opened,
            Color mapColor
    ) {
        String facing = block.getBlockState().getProperties().get("facing");
        if (LootrRenderRules.direction(facing) == null) {
            return false;
        }
        float xRotation = switch (facing) {
            case "down" -> 180F;
            case "north", "south", "east", "west" -> 90F;
            default -> 0F;
        };
        float yRotation = switch (facing) {
            case "east" -> 90F;
            case "south" -> 180F;
            case "west" -> 270F;
            default -> 0F;
        };
        Key texture = Key.parse(opened ? "lootr:shulker_opened" : "lootr:shulker");
        return json.emit(
                SHULKER_MODEL, block, target, xRotation, yRotation, 0F,
                Map.of("0", texture), mapColor
        );
    }

    private boolean renderBarrel(
            BlockNeighborhood block,
            TileModelView target,
            boolean opened,
            Color mapColor
    ) {
        Direction facing = LootrRenderRules.direction(
                block.getBlockState().getProperties().get("facing")
        );
        String rawOpen = block.getBlockState().getProperties().get("open");
        if (facing == null || !Set.of("true", "false").contains(rawOpen)) {
            return false;
        }
        float xRotation = switch (facing) {
            case DOWN -> 180F;
            case NORTH, SOUTH, WEST, EAST -> 90F;
            default -> 0F;
        };
        float yRotation = switch (facing) {
            case EAST -> 90F;
            case SOUTH -> 180F;
            case WEST -> 270F;
            default -> 0F;
        };
        Key model = Key.parse(opened
                ? "lootr:block/lootr_opened_barrel"
                : "lootr:block/lootr_barrel_unopened");
        return json.emit(
                model, block, target, xRotation, yRotation, 0F,
                Map.of(), mapColor
        );
    }

    private boolean renderBrushable(
            String id,
            BlockNeighborhood block,
            TileModelView target,
            boolean opened,
            Color mapColor
    ) {
        Key texture = LootrRenderRules.brushableTexture(
                id, opened, block.getBlockState().getProperties().get("dusted")
        );
        return texture != null
                && cubes.uniform(block, target, FULL_BLOCK, texture, mapColor);
    }

    private boolean renderPot(
            BlockNeighborhood block,
            TileModelView target,
            boolean opened,
            Color mapColor
    ) {
        String facing = block.getBlockState().getProperties().get("facing");
        int turns = LootrRenderRules.horizontalQuarterTurns(facing);
        if (turns < 0) {
            return false;
        }
        if (!opened) {
            return json.emit(
                    POT_MODEL, block, target, 0F, turns * 90F, 0F,
                    Map.of("0", Key.parse("lootr:entity/loot_pot")), mapColor
            );
        }
        return openedPots.emit(block, target, facing, mapColor);
    }

    private static float chestRotation(String facing) {
        return switch (facing) {
            case "north" -> 90F;
            case "east" -> 180F;
            case "south" -> 270F;
            case "west" -> 0F;
            default -> -1F;
        };
    }

    private void stock(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        if (STOCK_FALLBACK.get()) {
            return;
        }
        STOCK_FALLBACK.set(Boolean.TRUE);
        try {
            BlockRendererType type = variants == null
                    ? BlockRendererType.DEFAULT : variants.original(variant);
            stockRenderers.computeIfAbsent(
                    type,
                    found -> found.create(resourcePack, textures, settings)
            ).render(block, variant, target, mapColor);
        } finally {
            STOCK_FALLBACK.set(Boolean.FALSE);
        }
    }

    private void stockSafely(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor,
            int start,
            Color initialColor
    ) {
        try {
            stock(block, variant, target, mapColor);
        } catch (RuntimeException | LinkageError exception) {
            reset(target, start, mapColor, initialColor);
            runtime.inactive("stock-fallback-" + exception.getClass().getSimpleName());
        }
    }

    private static void reset(
            TileModelView target,
            int start,
            Color mapColor,
            Color initialColor
    ) {
        target.getTileModel().reset(start);
        target.initialize(start);
        mapColor.set(initialColor);
    }

    private static void diagnose(String id, String outcome) {
        String key = id + ':' + outcome;
        if (DIAGNOSTICS.add(key)) {
            System.out.println("BlueMap Lootr diagnostic: " + id + " -> " + outcome);
        }
    }
}
