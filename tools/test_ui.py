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
        page.route('https://example.com/**',lambda r: r.fulfill(status=200,content_type='image/png',body=bytes.fromhex('89504e470d0a1a0a0000000d49484452000000010000000108060000001f15c4890000000b49444154789c636000020000050001a5f645400000000049454e44ae426082')))
        page.set_content(html,wait_until='load')
        page.evaluate('''() => { state.settings.auto = false;state.items = Array.from({length:12},(_,i) => ({id:'id'+i,link:'https://example.com/article/'+i,title:'Notizie e risultati del campionato mondiale motorsport, prossime gare e classifiche',category:['F1','WRC','GT3'][i%3],source:'A.M.B.',date:Date.now()-10000*i,image:'https://example.com/picture.png',desc:'',custom:false}));render(); }''')
        assert page.locator('.article').count()==12
        assert not page.locator('.alert').count(), 'La Home non deve mostrare il vecchio banner rosso'
        page.evaluate("state.error='Alcune fonti non rispondono';render()")
        assert page.locator('.alert').count()==0, 'Nessun avviso rosso sulla Home anche con fonti non disponibili'
        # A feed containing both text-only and photo articles retains the latter only.
        counts=page.evaluate("""() => {
          const sample='<rss xmlns:media="http://search.yahoo.com/mrss/"><channel><item><title>Senza foto</title><link>https://example.com/a</link></item><item><title>Con foto</title><link>https://example.com/b</link><media:thumbnail url="https://example.com/photo.jpg" /></item></channel></rss>';
          return parseFeed(sample,{id:'probe',name:'Sito di prova',category:'Auto',url:'https://example.com/feed.xml',locked:true}).map(x=>x.title);
        }""")
        assert counts==['Con foto'],counts
        assert page.evaluate('document.documentElement.scrollWidth <= window.innerWidth'), f'Overflow su {width}px'
        page.locator('#tab-calendar').click()
        assert 'Calendario motorsport' in page.locator('#main').inner_text()
        page.locator('#tab-settings').click()
        assert 'Creato da Emanuel Mihai Blaj' in page.locator('#main').inner_text()
        assert '2.4' in page.locator('#main').inner_text()
        assert page.get_by_text('AMOLED',exact=True).count() >= 1
        # Layout choices are functional and persist without horizontal overflow.
        page.evaluate("go('news')")
        for mode in ['row','stacked']:
            for size in ['small','medium','large']:
                page.evaluate('([mode,size]) => {changePref("articleLayout",mode);changePref("photoSize",size)}',[mode,size])
                assert page.evaluate('document.documentElement.scrollWidth <= window.innerWidth'), f'Articles overflow {width}px / {mode}/{size}'
                assert page.locator('.article').count()==12
        page.evaluate("changePref('photoFit','contain')")
        assert page.evaluate('document.body.dataset.photoFit')=='contain'
        page.evaluate("changePref('articleLayout','row')")
        page.evaluate("changePref('photoSize','medium')")
        page.evaluate("go('settings')")
        page.evaluate("changePref('theme','amoled')")
        assert page.evaluate("getComputedStyle(document.body).backgroundColor") == 'rgb(0, 0, 0)'
        assert not page.locator('.cat').filter(has_text='GT2').count()
        assert not page.locator('.cat').filter(has_text='GT4').count()
        page.locator('#tab-news').click()
        page.get_by_role('button',name='Auto',exact=True).click()
        assert page.locator('#auto-topic-select').count()==1
        page.locator('#auto-topic-select').select_option('Motori')
        assert page.locator('#auto-topic-select').input_value() == 'Motori'
        page.locator('#tab-sources').click()
        assert page.locator('.source-card').count() >= 20
        page.locator('.sources-filter').get_by_role('button',name='Auto',exact=True).click()
        assert page.locator('.source-card').count() >= 17
        assert page.evaluate('document.documentElement.scrollWidth <= window.innerWidth'), f'Sources overflow su {width}px' 
        assert page.evaluate('document.documentElement.scrollWidth <= window.innerWidth'), f'Auto overflow su {width}px'
        assert page.evaluate('document.documentElement.scrollWidth <= window.innerWidth'), f'Overflow settings su {width}px'
        assert not errors, errors
        print(f'OK viewport {width}x{height}')
        page.close()
    browser.close()
