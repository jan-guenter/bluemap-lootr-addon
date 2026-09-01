# Changelog

## 0.1.0-alpha.2 - 2026-09-02

- Migrated the adapter and exact runtime admission to the tested BlueMap 5.23
  feature backport.
- Replaced local compatibility, registry, and extension-factory helpers with
  the pinned four-source `bluemap-addon-adapter-api` module.
- Preserved the eight routed families, failure behavior, and 14-cell gallery.

## 0.1.0-alpha.1 - 2026-08-28

- Generated a fail-closed Java 21 BlueMap add-on seed for `lootr-1.11.37.122-mc1.21.1`.
- Added exact-profile routes for eight Lootr custom-loader and block-entity
  renderer blocks while leaving the trophy on stock rendering.
- Reused BlueMap's installed chest, shulker, and decorated-pot geometry with
  Lootr's installed atlases; added directional barrel and brushable cubes.
- Added deterministic persisted opened-state decoding, atomic stock fallback,
  tests, and a compact 14-cell comparison gallery.
- Sealed the owner-accepted staging result for immutable release publication.
