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
assert len(java) >= 11
web = (root/'app/src/main/assets/index.html').read_text()
for q in ['Apex Motori Blaj','ManicApe','Widget Android','GreenFlag','Calendario','20, 40 o 60','AMBAndroid','GT3','AMOLED','amoled']:
    assert q in web, q
for exclusion in ['Nürburgring','Misha Charoudin']:
    assert exclusion not in web
js = web[web.index('<script>')+8:web.index('</script>')]
from tempfile import NamedTemporaryFile
with NamedTemporaryFile(mode='w',suffix='.js') as f:
    f.write(js);f.flush()
    subprocess.run(['node','--check',f.name],check=True)
assert "const CATS=['Tutte','F1','WRC','WEC','GT3','MotoGP','Auto','Personali']" in web
assert web.count("'category': 'Auto'")>=15 or web.count('\"category\": \"Auto\"')>=15
assert 'limitedResults(active,4,readFeed)' in web
assert 'auto-topic-select' in web
assert 'transparent' in (root/'app/src/main/java/com/apexmotoriblaj/amb/WidgetConfigActivity.java').read_text()
assert 'minSdk 29' in (root/'app/build.gradle').read_text()
assert "versionName '2.5.2'" in (root/'app/build.gradle').read_text()
for cls in ['OfflineStore','TranslateManager','AdBlock','NotificationScheduler']:
    assert (root/f'app/src/main/java/com/apexmotoriblaj/amb/{cls}.java').is_file(), cls
assert 'auto-tmrexh' in web and 'ambCalFilters' in web and 'ambCardTap' in web
assert len(__import__('re').findall(r'"candidate": true',web)) >= 20
assert 'ambCalendarPick' in web and 'ambCalendarAddEvent' in web
assert 'amb_logo.webp' in web and (root/'app/src/main/assets/amb_logo.webp').is_file()
assert 'amb_tmrexh_archive_251' in web
assert (root/'app/src/main/res/drawable/ic_racing_car_notification.xml').is_file()
print('OK: JS syntax, >=8 Android classes, category filters and widget themes verified')
