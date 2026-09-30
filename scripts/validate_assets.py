"""Validate the initial Tiled content without changing assets or requiring Android."""
from pathlib import Path
import struct
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]


def validate():
    path = ROOT / "assets/maps/garden_01.tmx"
    world = ET.parse(path).getroot()
    assert world.get("orientation") == "orthogonal"
    assert world.get("infinite") == "0"
    width, height = int(world.get("width")), int(world.get("height"))
    assert (int(world.get("tilewidth")), int(world.get("tileheight"))) == (32, 32)
    properties = {p.get("name"): p.get("value") for p in world.findall("properties/property")}
    assert properties["mapId"] == "garden_01" and properties["mapVersion"] == "1"
    reference = world.find("tileset")
    tileset_path = (path.parent / reference.get("source")).resolve()
    tileset = ET.parse(tileset_path).getroot()
    image = tileset.find("image")
    png = (tileset_path.parent / image.get("source")).read_bytes()
    assert png[:8] == b"\x89PNG\r\n\x1a\n"
    image_width, image_height = struct.unpack(">II", png[16:24])
    assert (image_width, image_height) == (int(image.get("width")), int(image.get("height")))
    assert image_width % 32 == 0 and image_height % 32 == 0
    first_gid = int(reference.get("firstgid"))
    terrains = {first_gid + int(t.get("id")): t.find("properties/property[@name='terrainId']").get("value")
                for t in tileset.findall("tile")}
    layers = {}
    for layer in world.findall("layer"):
        data = layer.find("data")
        assert data.get("encoding") == "csv"
        cells = [int(n.strip()) for n in data.text.strip().split(",")]
        assert len(cells) == width * height, layer.get("name")
        assert all(gid == 0 or gid in terrains for gid in cells)
        layers[layer.get("name")] = cells
    assert set(layers) == {"ground", "decoration_back", "blocked", "plantable", "decoration_front"}
    assert {g.get("name") for g in world.findall("objectgroup")} == {"anchors", "props"}
    water = [i for i, gid in enumerate(layers["ground"]) if terrains[gid].startswith("water_")]
    assert water and all(layers["blocked"][i] for i in water)
    spawn = world.find("objectgroup[@name='anchors']/object[@name='spawn_main']")
    x, y = int(float(spawn.get("x")) // 32), int(float(spawn.get("y")) // 32)
    assert 0 <= x < width and 0 <= y < height
    assert layers["blocked"][y * width + x] == layers["plantable"][y * width + x] == 0
    print(f"OK: {width} × {height}, 7 capas, {len(terrains)} terrenos, {len(water)} celdas de agua, spawn transitable.")


if __name__ == "__main__":
    validate()
