from pathlib import Path
from playwright.sync_api import sync_playwright
html=(Path(__file__).resolve().parents[1]/'app/src/main/assets/index.html').read_text()
with sync_playwright() as p:
 b=p.chromium.launch(headless=True,executable_path='/usr/bin/chromium',args=['--no-sandbox'])
 for w,h in [(320,700),(393,852),(820,1180)]:
  page=b.new_page(viewport={'width':w,'height':h});errors=[];page.on('pageerror',lambda e:errors.append(str(e)))
  page.set_content(html,wait_until='load')
  page.evaluate("ambShowPermission=()=>{};ambDismissPermission();state.settings.auto=false;state.settings.calAuto=false;render()")
  page.evaluate("changePref('theme','amoled');changePref('palette','blue');go('news')")
  colors=page.evaluate('''() => {const cat=document.querySelector('.cat.active');return {color:getComputedStyle(cat).color,bg:getComputedStyle(cat).backgroundColor,theme:document.documentElement.dataset.theme,palette:document.documentElement.dataset.palette};}''')
  assert colors['palette']=='blue' and colors['bg'] != 'rgb(100, 39, 33)',colors
  page.evaluate('''() => {calendar.items=[{id:'test1',date:new Date(new Date().getFullYear(),new Date().getMonth(),19,15,30).valueOf(),name:'Gara di prova',category:'F1',place:'Cuneo',source:'test',link:'https://example.com/a',allDay:false},{id:'test2',date:new Date(new Date().getFullYear(),new Date().getMonth(),19,11,0).valueOf(),name:'Secondo evento',category:'WRC',place:'Cuneo',source:'test',link:'https://example.com/b',allDay:false}];go('calendar');ambCalendarPick(new Date().getFullYear(),new Date().getMonth(),19)}''')
  assert page.locator('.amb-day').count()==42,page.locator('.amb-day').count()
  assert page.locator('.amb-weekday').count()==7
  assert page.locator('.amb-agenda-event').count()==2, page.locator('.amb-day-agenda').inner_text()
  assert page.locator('.amb-event-dot').count()>=2
  page.evaluate("ambCalFilters.region='Piemonte';ambCalFilters.province='Cuneo';render()")
  assert page.locator('.amb-agenda-event').count()==2
  page.evaluate("ambCalFilters.province='Torino';render()")
  assert page.locator('.amb-agenda-event').count()==0
  page.evaluate('''() => {go('news');selectCat('Auto');ambSelectSubcat('Articoli tecnici');ambSaveTechnicalArchive([{id:'auto-tmrexh:old',title:'Vecchio articolo sui diesel',date:Date.UTC(2021,1,1),source:'TmRexh · Articoli tecnici',link:'https://www.tmrexh.it/articolo/',image:TMREXH_BRAND_ICON,desc:'Guida vecchia',category:'Auto'}]);render()}''')
  assert page.locator('.article').count()>=1
  assert 'Vecchio articolo' in page.locator('.articles').inner_text()
  assert page.locator('.amb-technical-archive a').count()==1
  assert page.evaluate('document.documentElement.scrollWidth<=window.innerWidth'),f'overflow {w}'
  assert not errors,errors
  print('PASS',w,h,colors)
  page.close()
 b.close()
