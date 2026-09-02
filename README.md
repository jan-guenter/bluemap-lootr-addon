# BlueMap Lootr Add-on

A Java 21 BlueMap add-on for the exact `lootr-1.11.37.122-mc1.21.1` profile in All the Mons
`1.2.0` / Minecraft `1.21.1`.

Version `0.1.0-alpha.2` is the owner-accepted native BlueMap 5.23 release
candidate. It preserves the owner-accepted `0.1.0-alpha.1` rendering contract.
The add-on admits only the exact Lootr artifact, validates all installed
textures and BlueMap entity models, and wraps the eight block families whose
custom model loaders or block-entity renderers are absent from stock BlueMap.

## Build

```bash
git submodule update --init --recursive -- \
  tooling/bluemap-addon-toolkit modules/bluemap-addon-adapter-api
gradle --no-daemon -PbluemapSourcePath=/path/to/BlueMap-at-7e07f4e7 \
  -PlootrJar=/path/to/lootr-neoforge-1.21.1-1.11.37.122.jar \
  -PreleaseTag=v0.1.0-alpha.2 clean prototypeCheck build \
  generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication verifyReleaseCandidate
```

`check` is the quick Java/checkstyle/archive gate. `prototypeCheck` additionally
requires every exact candidate JAR property and validates the 14-cell gallery.
See `provenance/upstreams.json` for immutable artifact identities and
the [execution guide](docs/EXECUTION.md) for the prototype-to-release loop.

The add-on compiles the four helpers from the exact Adapter API source-module
gitlink. Its standalone JAR is neither installed nor nested.

## Install

Place the prototype JAR in BlueMap's add-on pack directory and restart the
BlueMap JVM. Removal plus one restart restores stock behavior; the add-on
creates no custom world state.

Set `-Dbluemap.lootr.disabled=true` to leave the exact profile inactive.

## Rendered scope

- Closed single-chest geometry for `lootr_chest`, `lootr_trapped_chest`, and
  `lootr_inventory`, with Lootr's gold/silver atlases and exact facing.
- Directional gold/silver barrels, closed shulkers, four brush stages, and the
  opened brush texture.
- Intact decorated-pot base geometry plus Lootr's five-cuboid static opened
  form with its exact atlas regions and neutral animation phase.
- Gold versus silver uses persisted `LootrHasBeenOpened`. Lootr's true client
  choice is per player, which a shared static map cannot reproduce.

Active opening animation, player-specific state, brush item protrusions,
particles, and custom pot-sherd contents remain deterministic-neutral. Unknown
or malformed inputs use the original stock renderer.

No Lootr binary, source, class, asset, captured mesh, or gallery is
bundled in the add-on.
