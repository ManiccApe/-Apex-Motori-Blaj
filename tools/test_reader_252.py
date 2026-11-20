#!/usr/bin/env python3
"""Reader regression checks; these are SOURCE + desktop browser checks, not Android instrumentation."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
reader=(root/'app/src/main/java/com/apexmotoriblaj/amb/ArticleActivity.java').read_text()
ad=(root/'app/src/main/java/com/apexmotoriblaj/amb/AdBlock.java').read_text()
for setting in ['setAcceptCookie(true)','setAcceptThirdPartyCookies(browser,true)',
                'setJavaScriptEnabled(true)','setDomStorageEnabled(true)',
                'setVerticalScrollBarEnabled(true)','toggleAdBlock()',
                'showMoreMenu(', 'removeAllCookies(', 'loadOffline(', 'openExternal()']:
    assert setting in reader, f'Missing reader component: {setting}'
for old in ['AdBlock.cosmeticScript()', 'applyPageFilters(', 'MutationObserver', 'document.body.style.overflow']:
    assert old not in reader, f'Old blocking behavior survives: {old}'
for old in ['__amb251Filter', 'function reject()', 'onetrust-banner-sdk', 'qc-cmp2-container', 'fc-consent-root', '.didomi-popup-container']:
    assert old not in ad, f'Unsafe cosmetic/consent handling survives: {old}'
assert 'request.isForMainFrame()' in reader
assert 'if(!adBlockEnabled || request.isForMainFrame())return null' in reader
assert "versionName '2.6.1-rc3'" in (root/'app/build.gradle').read_text()
assert "APP_VERSION='2.6.1-rc3'" in (root/'app/src/main/assets/index.html').read_text()
# Browser-simulated CMP gate: the site's own buttons remain tappable, and scrolling
# unlocks only after an explicit choice. The application no longer injects a script.
from playwright.sync_api import sync_playwright
fixture='''<!doctype html><html><head><meta name="viewport" content="width=device-width, initial-scale=1" /></head><body style="margin:0">
  <main style="height:2800px;background:linear-gradient(white,#ddd)">Test article and photographs</main>
  <div id="cmp" style="position:fixed;inset:0;background:#0008;display:grid;place-items:center">
  <section style="background:white;padding:20px"><h2>Privacy and cookies</h2><button id="reject">Reject optional</button><button id="accept">Accept</button></section></div>
  <script>document.documentElement.style.overflow='hidden';document.body.style.overflow='hidden';for(const name of ['reject','accept'])document.getElementById(name).onclick=()=>{
    document.body.dataset.choice=name;document.getElementById('cmp').remove();document.body.style.overflow='auto';document.documentElement.style.overflow='auto';};</script>
</body></html>'''
with sync_playwright() as p:
    browser=p.chromium.launch(headless=True,executable_path='/usr/bin/chromium',args=['--no-sandbox'])
    for width in [320,393,820]:
        page=browser.new_page(viewport={'width':width,'height':700})
        page.goto('about:blank')
        page.set_content(fixture)
        assert page.locator('#cmp').is_visible()
        assert page.locator('#reject').is_visible()
        assert page.evaluate('document.body.dataset.choice || null') is None
        page.mouse.move(width//2,400)
        page.mouse.wheel(0,800)
        page.wait_for_timeout(100)
        assert page.evaluate('window.scrollY')==0
        page.locator('#reject').click()
        assert page.evaluate('document.body.dataset.choice')=='reject'
        page.mouse.move(width//2,400)
        page.mouse.wheel(0,800)
        page.wait_for_timeout(120)
        assert page.evaluate('window.scrollY')>0
        page.close()
    browser.close()
print('PASS reader regression checks + consent/scroll fixture at 320/393/820px (not Android test)')
