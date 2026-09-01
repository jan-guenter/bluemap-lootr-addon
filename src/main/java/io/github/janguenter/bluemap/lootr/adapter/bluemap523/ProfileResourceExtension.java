/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.lootr.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.lootr.activation.AddonRuntime;
import io.github.janguenter.bluemap.lootr.profile.ExactArtifactDetector;
import io.github.janguenter.bluemap.lootr.profile.Lootr11137122Profile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Exact-artifact admission, installed-resource validation and target routing. */
final class ProfileResourceExtension implements ResourcePackExtension {

    private static final Map<Key, Size> REQUIRED_TEXTURES = requiredTextures();
    private static final Set<Key> REQUIRED_MODELS = Set.of(
            Key.parse("minecraft:entity/chest/chest"),
            Key.parse("minecraft:entity/shulker_box"),
            Key.parse("minecraft:entity/decorated_pot")
    );

    private final ResourcePack resourcePack;
    private final BlockRendererType renderer;
    private final AddonRuntime runtime;
    private boolean admitted;

    ProfileResourceExtension(
            ResourcePack resourcePack,
            BlockRendererType renderer,
            AddonRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.renderer = renderer;
        this.runtime = runtime;
    }

    @Override
    public void loadResources(Iterable<Path> roots) {
        admitted = false;
        if (Boolean.getBoolean("bluemap.lootr.disabled")) {
            runtime.inactive("operator-disabled");
            return;
        }
        if (!ExactArtifactDetector.matchesAll(roots, Lootr11137122Profile.ARTIFACTS)) {
            runtime.inactive("exact-artifact-missing-or-duplicate");
            return;
        }
        admitted = true;
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        return admitted ? REQUIRED_TEXTURES.keySet() : Set.of();
    }

    @Override
    public void bake() {
        if (!admitted) {
            return;
        }
        try {
            for (Map.Entry<Key, Size> requirement : REQUIRED_TEXTURES.entrySet()) {
                if (!validTexture(requirement.getKey(), requirement.getValue())) {
                    runtime.inactive("installed-render-resource-invalid");
                    return;
                }
            }
            for (Key model : REQUIRED_MODELS) {
                if (resourcePack.getModels().get(model) == null) {
                    runtime.inactive("installed-render-model-missing");
                    return;
                }
            }
            VariantRendererCatalog variants = VariantRendererCatalog.wrap(
                    resourcePack, renderer
            );
            RendererDataRegistry.install(resourcePack, variants);
            runtime.activate();
            System.out.println("BlueMap Lootr add-on active: wrapped "
                    + variants.size() + " exact variants across 8 blocks.");
        } catch (IOException | RuntimeException exception) {
            runtime.inactive("route-install-" + exception.getClass().getSimpleName());
        }
    }

    private boolean validTexture(Key key, Size size) throws IOException {
        Texture texture = resourcePack.getTextures().get(key);
        if (texture == null) {
            return false;
        }
        BufferedImage image = texture.getTextureImage();
        return image != null && image.getWidth() == size.width()
                && image.getHeight() == size.height();
    }

    private static Map<Key, Size> requiredTextures() {
        LinkedHashMap<Key, Size> textures = new LinkedHashMap<>();
        for (String name : Set.of(
                "chest", "chest_opened", "chest_trapped", "chest_trapped_opened",
                "shulker", "shulker_opened"
        )) {
            textures.put(Key.parse("lootr:" + name), new Size(64, 64));
        }
        for (String prefix : Set.of("barrel", "opened_barrel")) {
            for (String suffix : Set.of("bottom", "side", "top", "top_open")) {
                textures.put(
                        Key.parse("lootr:block/" + prefix + '_' + suffix),
                        new Size(16, 16)
                );
            }
        }
        for (String kind : Set.of("sand", "gravel")) {
            textures.put(
                    Key.parse("lootr:block/suspicious_" + kind + "_open"),
                    new Size(16, 16)
            );
            for (int stage = 0; stage <= 3; stage++) {
                textures.put(
                        Key.parse("minecraft:block/suspicious_" + kind + '_' + stage),
                        new Size(16, 16)
                );
            }
        }
        textures.put(Key.parse("lootr:entity/loot_pot"), new Size(32, 32));
        textures.put(Key.parse("lootr:entity/loot_pot_open"), new Size(64, 32));
        return Map.copyOf(textures);
    }

    private record Size(int width, int height) {
    }
}
