# A.M.B. – Apex Motori Blaj 2.6.1 RC3

**Stato: pre-release / sorgenti. APK non compilato né firmato.**

## Correzioni in RC3

- **Video come Notizie:** anteprima 16:9, titolo, nome del creator, descrizione breve e pulsante «Guarda con YouTube». I canali sono gestiti in un riquadro richiudibile sotto il feed, non occupano più tutta la schermata.
- **Caricamento progressivo:** 4 canali in parallelo; le schede vengono mostrate appena recuperate. Cache locale e indicatori degli errori per i feed non disponibili. Fallback sulle pagine video pubbliche quando l’RSS è indisponibile. Non è garantita la disponibilità di tutti i feed YouTube.
- **Tre video selezionati:** selezione reale da Motor1 Italia e Dueruote per evitare una schermata completamente vuota quando il caricamento iniziale non è disponibile. Etichettati esplicitamente «SELEZIONE · NON IN TEMPO REALE», non spacciati per ultimi video.
- **Creator italiani:** Quattroruote, Motor1 Italia, alVolante, DriveK Italia, RED Live, Dueruote, con ID di canale verificati; rimangono i creator internazionali e ManicApe prioritario.
- **Player YouTube:** nuova Activity Android con iframe ufficiale YouTube e apertura alternativa su YouTube; annunci e restrizioni della piattaforma non vengono eliminati. È possibile la modalità schermo intero quando consentita dal player.
- **Icone:** asset PNG a fondo realmente trasparente in tutte le densità, round icon e adaptive icon con sfondo trasparente e logo più interno alla maschera. Anche il logo nell’intestazione dell’app non ha riquadro colorato. Il launcher/Play Protect può comunque applicare una maschera propria.
- **Fonti calendario:** 15 collegamenti a calendari federali/club/testate (ACI Sport, ASI, FAMS San Marino, Rallylink, ecc.) e 40 collegamenti a ricerche di copertura regionale per le 20 regioni italiane. Distinzione visibile tra calendari `.ics` realmente sincronizzati e link di consultazione non automatici. Nessuna data inventata.
- **Widget Calendario:** layout nativo trasparente, 7 colonne x 6 settimane, lunedì-domenica, evidenza del giorno attuale, etichette colorate arrotondate per categorie e titoli brevi degli eventi presenti nei `.ics`; frecce per cambiare mese e tap sul giorno per aprirlo nell’app. Dimensioni minime maggiori, widget Notizie preservato.

## Limitazioni note

1. Il recupero dei video è soggetto alle risposte dei siti YouTube, ai blocchi/rate limit e alla rete. L’accesso a *ogni* canale non è garantito senza API/autorizzazione specifica.
2. I link regionali non sono sorgenti `.ics`: indicano siti e ricerche consultabili, non popolano automaticamente il calendario. Servono feed strutturati verificabili per importare eventi.
3. La resa del widget, l’adaptive icon, lo schermo intero del player e le notifiche richiedono test su One UI e altri dispositivi.
4. Nessun APK 2.6.1 RC3 è stato compilato in questo ambiente; la versione precedente non costituisce un test valido delle modifiche.
5. Il dialogo di Play Protect è gestito da Google/Android, non dall’app e non viene disattivato da questo aggiornamento.

**Tag GitHub suggerito:** `v2.6.1-rc3` (pre-release). **Version Code:** 263. **Version Name:** `2.6.1-rc3`.
