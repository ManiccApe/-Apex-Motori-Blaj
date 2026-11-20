# QA per A.M.B. RC3

Test locali eseguiti: sintassi JavaScript di tutti gli script inline (`node --check`), parsing XML delle risorse, verifica delle icone PNG con angoli alfa=0, parser dei dati YouTube mediante fixture HTML, integrazione di 42 celle nella griglia mensile, test UI Playwright (video ManicApe mock con miniatura/titolo/descrizione, 20 regioni, 40 link, navigazione da widget al calendario) e test regression UI/reader.

Non eseguiti: Gradle/Android SDK (non disponibili), installazione APK, prove WebView su rete reale, feed YouTube dal telefono, funzionamento pubblicità/player a schermo intero, usabilità widget One UI, controlli Play Protect.

Test manuali consigliati prima di rendere stabile la release:
- Sezione Video aperta con rete: vedere subito i video della selezione, poi l'aggiornamento progressivo, miniature, titoli e descrizioni. Controllare canali italiane e ManicApe.
- Riproduzione: video dentro l'app col player YouTube; gli annunci restano gestiti da YouTube; prova schermo intero e pulsante esterno.
- Calendario: installare widget di almeno 4x5, controllare trasparenza sullo sfondo, 42 celle, eventi scritti nei giorni e navigazione mesi. Provare il tap su un giorno.
- Avvio APK: verificare logo su icona launcher e nel dialogo di installazione, tenendo conto della maschera applicata dal sistema.
- Importare un vero `.ics` e verificare la comparsa del titolo evento nel widget.
