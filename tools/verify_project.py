#!/usr/bin/env python3
"""Offline source checks. APK signing/build verification requires Android SDK."""
from pathlib import Path
from xml.etree import ElementTree as ET
import re, subprocess
root = Path(__file__).resolve().parents[1]
for xml in (root/'app/src/main').rglob('*.xml'):
    ET.parse(xml)
print('OK: manifest, widget, resources XML')
manifest = (root/'app/src/main/AndroidManifest.xml').read_text()
assert 'android.appwidget.provider' in manifest
assert 'WidgetListService' in manifest
assert 'WidgetConfigActivity' in manifest
java = list((root/'app/src/main/java').rglob('*.java'))
assert len(java) >= 7
web = (root/'app/src/main/assets/index.html').read_text()
for q in ['Apex Motori Blaj','Emanuel Mihai Blaj','Widget Android','GreenFlag','Calendario','20, 40 o 60','AMBAndroid','GT3','GT4']:
    assert q in web, q
for exclusion in ['Nürburgring','Misha Charoudin']:
    assert exclusion not in web
js = web[web.index('<script>')+8:web.index('</script>')]
from tempfile import NamedTemporaryFile
with NamedTemporaryFile(mode='w',suffix='.js') as f:
    f.write(js);f.flush()
    subprocess.run(['node','--check',f.name],check=True)
print('OK: JS syntax, 7 Android classes, official widget declared, features verified')
