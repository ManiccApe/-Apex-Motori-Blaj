#!/usr/bin/env python3
"""Browser-only smoke test for the rebuilt Video/Calendar interface; not Android instrumentation."""
from pathlib import Path
from playwright.sync_api import sync_playwright
html=(Path(__file__).resolve().parents[1]/'app/src/main/assets/index.html').read_text()
rss='''<?xml version="1.0"?><feed xmlns:yt="http://www.youtube.com/xml/schemas/2015" xmlns:media="http://search.yahoo.com/mrss/"><entry><yt:videoId>abcDEFGhI12</yt:videoId><title>ManicApe: nuova prova motorsport</title><published>2026-11-20T09:00:00+00:00</published><media:group><media:description>Nuovo video dal canale ManicApe con prova su pista.</media:description><media:thumbnail url="https://i.ytimg.com/vi/abcDEFGhI12/hqdefault.jpg"/></media:group></entry></feed>'''
with sync_playwright() as p:
    browser=p.chromium.launch(headless=True,executable_path='/usr/bin/chromium',args=['--no-sandbox'])
    page=browser.new_page(viewport={'width':393,'height':852})
    page.add_init_script("""Object.defineProperty(window,'localStorage',{configurable:true,value:(function(){let m=new Map();return {getItem:k=>m.has(k)?m.get(k):null,setItem:(k,v)=>m.set(k,String(v)),removeItem:k=>m.delete(k),clear:()=>m.clear()}})()});""")
    errors=[]
    page.on('pageerror',lambda err:errors.append(str(err)))
    page.goto('about:blank')
    page.set_content(html,wait_until='load')
    page.evaluate('''([rss])=>{
      window.fetchText=async (url)=>{
        if(url.includes('/feeds/videos.xml?channel_id='))return rss;
        if(url.includes('youtube.com/@maniccape/videos'))return '<html><script type="application/json">{"channelId":"UC1234567890123456789012"}</script></html>';
        throw Error('Non raggiungibile in test');
      };
    }''',[rss])
    page.locator('#tab-videos26').click()
    page.wait_for_function('document.querySelectorAll(".amb-video-news-card").length>=4',timeout=8000)
    featured=page.locator('.amb-video-news-card').first
    assert 'MANICAPE' in featured.inner_text(),featured.inner_text()
    assert featured.locator('.amb26-cover').count()==1
    assert 'nuova prova motorsport' in featured.inner_text().lower()
    assert 'Nuovo video dal canale' in featured.inner_text()
    assert page.locator('.amb-video-channel-row').count()>=18
    print('PASS browser: auto-loading video cards with image, title, description, featured creator')
    page.evaluate("calendar.manage=true;go('calendar')")
    assert page.locator('.amb-local-source-row').count()==20
    assert page.locator('.amb-local-source-row a').count()==40
    assert page.locator('.amb-local-directory .amb-directory a').count()>=15
    print('PASS browser: 20 regions, 40 regional searches and verified organizer directory')
    page.evaluate("window.AMBCalendarOpenDate('2026-11-20')")
    assert page.locator('.amb-calendar-grid').count()==1
    assert 'novembre 2026' in page.locator('#main').inner_text().lower()
    assert not errors,errors
    print('PASS browser: widget date routes into month calendar')
    browser.close()
