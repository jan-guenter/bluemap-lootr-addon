# Lootr comparison gallery

This deterministic 14-cell gallery covers all eight custom-loader or
block-entity-rendered Lootr blocks in the exact ATMons 1.2.0 profile. Gold and
silver cases use the persisted `LootrHasBeenOpened` bit as a stable BlueMap
approximation of Lootr's player-specific client appearance. The final trophy is
an ordinary JSON-model stock control and is intentionally not routed through
the add-on.

Regenerate and validate it with:

```bash
python gallery/generate.py
python gallery/generate.py --check
python gallery/lint.py
bash gallery/package.sh /tmp/lootr-gallery.zip
```

The build function clears only `(162, 99, 170)` through `(190, 103, 178)`, lays
a light-gray comparison floor, places the 14 cells, and performs block-state
checks. It creates no entities, ticking machinery, or external state.
