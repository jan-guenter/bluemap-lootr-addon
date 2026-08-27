/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.lootr.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import io.github.janguenter.bluemap.lootr.activation.AddonRuntime;
import io.github.janguenter.bluemap.lootr.profile.ExactArtifactDetector;
import io.github.janguenter.bluemap.lootr.profile.Lootr11137122Profile;

import java.nio.file.Path;

/** Exact-artifact admission hook; family routing deliberately remains stock. */
final class ProfileResourceExtension implements ResourcePackExtension {

    private final ResourcePack resourcePack;
    private final AddonRuntime runtime;

    ProfileResourceExtension(ResourcePack resourcePack, AddonRuntime runtime) {
        this.resourcePack = resourcePack;
        this.runtime = runtime;
    }

    @Override
    public void loadResources(Iterable<Path> roots) {
        if (Boolean.getBoolean("bluemap.lootr.disabled")) {
            runtime.inactive("operator-disabled");
            return;
        }
        if (!ExactArtifactDetector.matchesAll(roots, Lootr11137122Profile.ARTIFACTS)) {
            runtime.inactive("exact-artifact-missing-or-duplicate");
            return;
        }

        // SCAFFOLD_NOT_IMPLEMENTED: validate installed resources, register the
        // family renderer, route only owned hosts, then call runtime.activate().
        if (resourcePack.getBlockStates() == null) {
            runtime.fail("resource-pack-unavailable");
            return;
        }
        runtime.inactive("family-renderer-not-implemented");
    }
}
