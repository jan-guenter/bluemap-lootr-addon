#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Lint the generated Lootr gallery without starting Minecraft."""

from __future__ import annotations

import json
from pathlib import Path
import re
import sys

sys.dont_write_bytecode = True
import cases
import generate


ROOT = Path(__file__).resolve().parent
TARGETS = {
    "lootr:lootr_chest",
    "lootr:lootr_trapped_chest",
    "lootr:lootr_inventory",
    "lootr:lootr_barrel",
    "lootr:lootr_shulker",
    "lootr:suspicious_sand",
    "lootr:suspicious_gravel",
    "lootr:decorated_pot",
}


def block_id(block_state: str) -> str:
    return block_state.split("[", maxsplit=1)[0]


def main() -> int:
    for relative, payload in generate.generated_files().items():
        path = ROOT / relative
        if not path.is_file() or path.read_bytes() != payload:
            raise ValueError(f"generated file differs: {relative}")

    json.loads((ROOT / "datapack/pack.mcmeta").read_text(encoding="utf-8"))
    load_tag = json.loads(
        (ROOT / "datapack/data/minecraft/tags/function/load.json").read_text(
            encoding="utf-8"
        )
    )
    if load_tag != {"values": [f"{cases.NAMESPACE}:load"]}:
        raise ValueError("load tag differs from the exact namespace")
    if len(cases.PLACEMENTS) != 14:
        raise ValueError("gallery must contain exactly fourteen compact cases")
    if len({row.case_id for row in cases.PLACEMENTS}) != len(cases.PLACEMENTS):
        raise ValueError("duplicate gallery case id")
    if len({(row.x, row.y, row.z) for row in cases.PLACEMENTS}) != len(
        cases.PLACEMENTS
    ):
        raise ValueError("duplicate gallery coordinate")

    minimum_x, minimum_y, minimum_z, maximum_x, maximum_y, maximum_z = (
        cases.ENVELOPE
    )
    represented = set()
    for placement in cases.PLACEMENTS:
        if not (
            minimum_x <= placement.x <= maximum_x
            and minimum_y <= placement.y <= maximum_y
            and minimum_z <= placement.z <= maximum_z
        ):
            raise ValueError(f"case escaped bounded envelope: {placement.case_id}")
        identifier = block_id(placement.block_state)
        if identifier in TARGETS:
            represented.add(identifier)
            if placement.block_nbt not in (
                "{LootrHasBeenOpened:0b}",
                "{LootrHasBeenOpened:1b}",
            ):
                raise ValueError(f"missing deterministic state: {placement.case_id}")
        elif identifier != "lootr:trophy" or placement.block_nbt:
            raise ValueError(f"unexpected stock control: {placement.case_id}")
    if represented != TARGETS:
        raise ValueError(f"target coverage mismatch: {represented ^ TARGETS}")

    function_root = ROOT / f"datapack/data/{cases.NAMESPACE}/function"
    functions = "\n".join(
        path.read_text(encoding="utf-8")
        for path in sorted(function_root.glob("*.mcfunction"))
    )
    if len(re.findall(r"^setblock ", functions, re.MULTILINE)) != 14:
        raise ValueError("gallery must place exactly fourteen comparison blocks")
    lowered = functions.lower()
    for forbidden in ("summon ", "data merge", "op ", "deop ", "stop "):
        if re.search(rf"^{re.escape(forbidden)}", lowered, re.MULTILINE):
            raise ValueError(f"forbidden gallery command: {forbidden}")
    print("Lootr gallery lint passed: 13 owned cases and one stock control")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, ValueError) as error:
        print(f"gallery lint failed: {error}", file=sys.stderr)
        raise SystemExit(1)
