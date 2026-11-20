#!/usr/bin/env python3
from pathlib import Path
import re,subprocess,sys,xml.etree.ElementTree as ET
from PIL import Image
root=Path(__file__).resolve().parents[1]
res=root/'app/src/main/res'
for p in res.rglob('*.xml'):ET.parse(p)
html=(root/'app/src/main/assets/index.html').read_text()
inline=re.findall(r'<script(?:\s[^>]*)?>([\s\S]*?)</script>',html)
for i,code in enumerate(inline):
 p=Path('/tmp/amb_rc3_test_'+str(i)+'.js');p.write_text(code)
 subprocess.run(['node','--check',str(p)],check=True)
assert 'AMB_REGIONAL_SOURCES' in html and 'Quattroruote · Italia' in html
assert 'window.AMBCalendarOpenDate' in html
assert (root/'app/src/main/java/com/apexmotoriblaj/amb/VideoPlayerActivity.java').exists()
assert (res/'drawable/widget_calendar_bg_261.xml').exists()
assert (res/'drawable/widget_calendar_cell_today_261.xml').exists()
for d in ('mdpi','hdpi','xhdpi','xxhdpi','xxxhdpi'):
 im=Image.open(res/f'mipmap-{d}/ic_launcher.png').convert('RGBA')
 assert im.getpixel((0,0))[3]==0, f'Not transparent: {d}'
widget=(res/'layout/widget_calendar_261.xml').read_text()
assert widget.count('cal261_cell_')==42
print('PASS: XML, JS syntax, 42 day cells, transparent icon set, creator and player assets')
