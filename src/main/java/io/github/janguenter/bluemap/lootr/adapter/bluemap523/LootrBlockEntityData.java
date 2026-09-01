/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.lootr.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

/** BlueNBT projection of Lootr's persisted global appearance approximation. */
public final class LootrBlockEntityData extends MCABlockEntity {

    @NBTName("LootrHasBeenOpened")
    private Object hasBeenOpened;

    public LootrBlockEntityData() {
    }

    Object hasBeenOpened() {
        return hasBeenOpened;
    }
}
