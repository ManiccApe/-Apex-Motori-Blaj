#!/usr/bin/env python3
"""Browser behavior tests: ManicApe only with an upload younger than 48 hours.
No live network is used. Tests the Video tab with fixture YouTube RSS.
"""
from pathlib import Path
from datetime import datetime, timedelta, timezone
from playwright.sync_api import sync_playwright

HTML=(Path(__file__).resolve().parents[1]/'app/src/main/assets/index.html').read_text()
NOW=datetime.now(timezone.utc)
CHANNEL_ID='UC1234567890123456789012'

def rss_for(published):
    date_tag='<published>'+published+'</published>' if published else ''
    return ('''<?xml version="1.0" encoding="UTF-8"?>
    <feed xmlns:yt="http://www.youtube.com/xml/schemas/2015" xmlns:media="http://search.yahoo.com/mrss/">
    <entry><yt:videoId>abcDEFGhI12</yt:videoId>
    <title>ManicApe test rally</title>'''+date_tag+'''
    <media:group><media:description>Prova sul circuito</media:description></media:group></entry></feed>''')

def run_case(browser,title,published,expected):
    page=browser.new_page(viewport={'width':393,'height':852})
    page.add_init_script("""Object.defineProperty(window,'localStorage',{configurable:true,value:(function(){let m=new Map();return {getItem:k=>m.has(k)?m.get(k):null,setItem:(k,v)=>m.set(k,String(v)),removeItem:k=>m.delete(k),clear:()=>m.clear()}})()});""")
    page.goto('about:blank')
    errors=[]
    page.on('pageerror',lambda err:errors.append(str(err)))
    page.set_content(HTML,wait_until='load')
    rss=rss_for(published)
    page.evaluate('''(args)=>{
      const rss=args.rss;
      window.fetchText=async(url)=>{
        if(url.includes('/feeds/videos.xml?channel_id=UC1234567890123456789012'))return rss;
        if(url.includes('youtube.com/@maniccape/videos'))return '<html>"channelId":"UC1234567890123456789012"</html>';
        throw Error('No external network during tests');
      };
    }''',{'rss':rss})
    page.locator('#tab-videos26').click()
    page.wait_for_function("""() => {
      const status=document.querySelector('#amb26-feed-status')?.textContent||'';
      return status.includes('canali controllati') || status.includes('Nessun video recuperato');
    }""",timeout=15000)
    label_count=page.locator('.amb26-featured-label').count()
    article_count=page.locator('.amb-video-news-card').filter(has_text='ManicApe test rally').count()
    visible_channels=page.locator('#amb26-list').text_content()
    assert (label_count==1)==expected, (title,'featured label',label_count)
    assert (article_count==1)==expected, (title,'video card',article_count)
    assert ('ManicApe' in visible_channels)==expected, (title,'channel row',visible_channels)
    assert not errors,errors
    print('PASS',title,': visible' if expected else ': hidden')
    page.close()

with sync_playwright() as p:
    b=p.chromium.launch(headless=True,executable_path='/usr/bin/chromium',args=['--no-sandbox'])
    run_case(b,'4 ore prima', (NOW-timedelta(hours=4)).isoformat(),True)
    run_case(b,'47 ore prima', (NOW-timedelta(hours=47)).isoformat(),True)
    run_case(b,'49 ore prima', (NOW-timedelta(hours=49)).isoformat(),False)
    run_case(b,'data assente', '',False)
    run_case(b,'data futura', (NOW+timedelta(hours=1)).isoformat(),False)
    b.close()
