# A.M.B. – Apex Motori Blaj 2.5.1 Beta

**Created & Developed by ManicApe**

## Italiano — Testo per GitHub Releases

**A.M.B. 2.5.1 Beta — Calendario mensile, lettore corretto, palette realmente applicate e miglioramenti privacy.**

A.M.B. è un aggregatore indipendente di notizie Auto, F1, WRC, WEC, GT3 e MotoGP. Non mostra annunci propri. Il lettore interno include filtri pubblicitari e tenta di rifiutare i cookie facoltativi, quando l'editore offre tale opzione.

### Cosa cambia rispetto alla 2.5

- **Lettore e barra di stato:** gestione esplicita degli spazi riservati a status bar, barra di navigazione e ritagli degli schermi Android 10+.
- **Logo A.M.B.:** il logo grafico fornito per il progetto sostituisce la scritta A.M.B. nell'intestazione.
- **Calendario mensile:** griglia completa di 7 colonne, tutti i giorni visibili, navigazione mese precedente/successivo, ritorno a oggi, puntini colorati per categoria e lista di gare con orari e fonti nel giorno selezionato.
- **Collegamento al calendario del dispositivo:** pulsante “＋ Calendario” che apre la proposta di inserimento (non inserisce appuntamenti senza conferma).
- **Palette coerenti:** tutti gli accenti dell'interfaccia (pulsanti, schede selezionate, navigazione, cuore animato) seguono la palette scelta, anziché conservare il rosso A.M.B.; i diversi colori delle discipline sportive restano identificativi.
- **AdBlock e privacy:** più domini pubblicitari bloccati; filtro degli elementi pubblicitari e tentativo di rifiutare automaticamente i consensi opzionali. Non viene mai premuto “Accetta tutti”.
- **TmRexh / Auto → Articoli tecnici:** caricamento dedicato, conservazione di articoli storici già acquisiti e nessuna esclusione per mancanza della foto; in tale caso viene mostrata l'icona TmRexh recuperata online, con marchio testuale di riserva.
- **Notifiche:** piccola icona monocromatica di automobile da corsa in stile 🏎️ per la barra di stato (Android non mostra emoji colorate come small icon).
- **Compatibilità:** Android 10+ (minSdk 29), smartphone e tablet. Il tema AMOLED originale e le funzioni 2.5 restano disponibili.

### Limiti e note importanti

- **Nessuna promessa di eliminazione totale dei cookie o degli annunci**: alcuni siti hanno sistemi di consenso complessi, contenuti caricati dinamicamente o paywall. La patch rifiuta dove possibile e nasconde gli overlay CMP più comuni; non accetta tracciamenti in automatico.
- Gli eventi del calendario provengono **solo da feed .ics disponibili**; senza feed per gare locali, non vengono inventati eventi.
- L'archivio tecnico mantiene gli articoli prelevati dal feed e dai precedenti aggiornamenti. Non può recuperare automaticamente tutte le vecchie pagine del sito se non esposte nel feed: è presente il link all'archivio originale TmRexh.
- L'icona remota del sito può essere indisponibile offline prima che sia stata memorizzata. Il marchio testuale rimane visibile.
- Beta: i controlli HTML/JS e i test browser sono passati, **APK Android non compilato e non testato su smartphone in questo ambiente**. Pubblicare come prerelease dopo il controllo della build e del funzionamento reale.
- Il file `app-debug.apk` generato da GitHub Actions è firmato per il test, non una release di produzione con chiave persistente.

## English — GitHub Releases text

**A.M.B. 2.5.1 Beta — Month-grid calendar, reader insets, full color palettes and improved ad/privacy filtering.**

- Status and navigation bar overlap fixes across Android 10+ screen layouts.
- Project artwork now appears in the app header instead of plain “A.M.B.” text.
- Seven-column month calendar with individually selectable days, category-colored indicators, daily agenda, event start times, viewing links and an “Add to Calendar” action.
- Selected color palettes replace the UI's red highlights consistently; racing categories keep their own distinct identifying colors.
- Enhanced ad-domain blocking and best-effort rejection of optional cookies. **The app never automatically selects “Accept all”.** Cookie dialogs and ads cannot be guaranteed absent on every third-party site.
- Dedicated TmRexh technical archive that retains older collected articles, accepts text-only articles and shows the website icon or a text fallback.
- Monochrome racing-car notification icon inspired by 🏎️.
- Android 10 (API 29) and newer, including tablets. Existing 2.5 features remain.

**Developer: ManicApe**. Open-source source code, independent project. **Beta**: Android build and device testing required before publishing a stable release.
