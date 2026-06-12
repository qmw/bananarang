"""Generate the banana item texture (16x16) and mod icon (128x128) as PNGs."""
import struct, zlib, sys

PALETTE = {
    '.': (0, 0, 0, 0),          # transparent
    'Y': (250, 210, 60, 255),   # banana yellow
    'O': (214, 162, 31, 255),   # shaded yellow (lower edge)
    'W': (255, 240, 150, 255),  # highlight
    'B': (104, 70, 28, 255),    # brown tip/stem
    'G': (140, 120, 40, 255),   # green-brown stem top
}

# A crescent banana: stem top-right, belly bulging down-left.
BANANA = [
    "................",
    "...........GB...",
    "..........YYB...",
    ".........YYY....",
    ".......WYYY.....",
    ".....WYYYY......",
    "....WYYYY.......",
    "...WYYYY........",
    "..WYYYY.........",
    "..WYYY..........",
    "..YYYY..........",
    ".BYYYO..........",
    ".BYYO...........",
    "..BBO...........",
    "................",
    "................",
]

for _row in BANANA:
    assert len(_row) == 16, f'bad row width: {_row!r}'

def make_png(rows, scale=1):
    h = len(rows)
    w = len(rows[0])
    raw = b''
    for row in rows:
        for _ in range(scale):
            line = b'\x00'  # filter type 0
            for ch in row:
                line += bytes(PALETTE[ch]) * scale
            raw += line
    def chunk(tag, data):
        c = tag + data
        return struct.pack('>I', len(data)) + c + struct.pack('>I', zlib.crc32(c))
    ihdr = struct.pack('>IIBBBBB', w * scale, h * scale, 8, 6, 0, 0, 0)
    return (b'\x89PNG\r\n\x1a\n'
            + chunk(b'IHDR', ihdr)
            + chunk(b'IDAT', zlib.compress(raw))
            + chunk(b'IEND', b''))

if __name__ == '__main__':
    base = sys.argv[1]
    with open(f'{base}/src/main/resources/assets/bananarang/textures/item/banana.png', 'wb') as f:
        f.write(make_png(BANANA))
    with open(f'{base}/src/main/resources/assets/bananarang/icon.png', 'wb') as f:
        f.write(make_png(BANANA, scale=8))
    print('textures written')
