# A.M.B. 2.6.0 — Stato / Status

## IT
Pacchetto sorgenti di sviluppo, **non una release APK certificata**. Basato sui sorgenti 2.5.2 caricati dall’utente. Applicata la patch di sviluppo: versione 2.6.0, logo, icone, tema LMR, scheda Video, gestione canali YouTube, tentativo di feed pubblici, collegamenti originali, download MP4 HTTPS diretti autorizzati con scelta Wi-Fi/dati mobili, schermata novità.

**Non completati:** Brief delle 19:00, calendario widget, copertura verificata di tutte le regioni, player integrato completo, gestione video offline avanzata, AdBlock video completo, aggiornamento APK da GitHub. L'anteprima YouTube dipende dalla disponibilità dei feed e non equivale a riproduzione interna. Il download non supporta YouTube.

**Verifiche:** sintassi Python e JavaScript superata; verifica risorse XML eseguita; test UI Chromium non superato (restrizione localStorage su pagina di prova); compilazione APK e installazione Android non eseguite. Il test verify_project.py originale richiede la versione 2.5.2 e fallisce intenzionalmente dopo l'incremento a 2.6.0. Non pubblicare come versione stabile senza test Android.

## EN
Development source package, **not a certified distributable APK**. Based on user-supplied 2.5.2 source. Includes 2.6.0 version bump, logo, icons, LMR palette, Video tab, YouTube channel links and public-feed discovery attempt, original-video links, authorized direct HTTPS MP4 downloads with Wi-Fi/mobile selection, and what's-new screen.

**Incomplete:** 19:00 news brief, calendar widget, verified coverage of all Italian regions, full internal player, advanced offline media management, comprehensive video ad blocking, GitHub APK updater. YouTube downloads are not supported.

**Testing:** Python/JavaScript syntax and XML resource verification performed. Chromium UI smoke test blocked by test-page localStorage restrictions. APK compilation and Android device installation not tested. The legacy verify_project.py asserts 2.5.2 and therefore fails after version bump. Do not publish as stable without device testing.
