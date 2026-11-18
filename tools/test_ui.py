"""Smoke test della UI HTML con Chromium + Playwright (facoltativo)."""
from pathlib import Path
from playwright.sync_api import sync_playwright
root=Path(__file__).resolve().parents[1]
html=(root/'app/src/main/assets/index.html').read_text()
with sync_playwright() as p:
    browser=p.chromium.launch(headless=True,executable_path='/usr/bin/chromium',args=['--no-sandbox'])
    for width,height in [(320,700),(393,852),(820,1180)]:
        page=browser.new_page(viewport={'width':width,'height':height})
        errors=[]
        page.on('pageerror',lambda exc: errors.append(str(exc)))
        page.set_content(html,wait_until='load')
        page.evaluate('''() => { state.settings.auto = false;state.items = Array.from({length:12},(_,i) => ({id:'id'+i,link:'https://example.com/article/'+i,title:'Notizie e risultati del campionato mondiale motorsport, prossime gare e classifiche',category:['F1','WRC','GT3'][i%3],source:'A.M.B.',date:Date.now()-10000*i,image:'',desc:'',custom:false}));render(); }''')
        assert page.locator('.article').count()==12
        assert page.evaluate('document.documentElement.scrollWidth <= window.innerWidth'), f'Overflow su {width}px'
        page.locator('#tab-calendar').click()
        assert 'Calendario motorsport' in page.locator('#main').inner_text()
        page.locator('#tab-settings').click()
        assert 'Creato da Emanuel Mihai Blaj' in page.locator('#main').inner_text()
        assert '2.1' in page.locator('#main').inner_text()
        assert page.get_by_text('AMOLED',exact=True).count() >= 1
        page.evaluate("changePref('theme','amoled')")
        assert page.evaluate("getComputedStyle(document.body).backgroundColor") == 'rgb(0, 0, 0)'
        assert not page.locator('.cat').filter(has_text='GT2').count()
        assert not page.locator('.cat').filter(has_text='GT4').count()
        assert page.evaluate('document.documentElement.scrollWidth <= window.innerWidth'), f'Overflow settings su {width}px'
        assert not errors, errors
        print(f'OK viewport {width}x{height}')
        page.close()
    browser.close()
