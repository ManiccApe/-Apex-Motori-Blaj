# Guida patch A.M.B. 2.5.1 / Installation guide

## Italiano — Scegli l'archivio

- **`Apex_Motori_Blaj_2_5_1_PATCH_GitHub.zip`** contiene soltanto i file nuovi/modificati rispetto alla versione **2.5**. Usalo solo se nel tuo repository è già presente il codice 2.5.
- **`Apex_Motori_Blaj_2_5_1_Sorgenti_OpenSource.zip`** contiene tutti i sorgenti Android aggiornati. È il metodo più semplice e consigliato se vuoi evitare file mancanti.

### Passo passo su GitHub dal PC

1. Scarica lo ZIP **Sorgenti_OpenSource** e fai **Estrai tutto**.
2. Apri il tuo repository **Apex-Motori-Blaj** in GitHub. È importante che il repository contenga già il progetto Android, non soltanto README.
3. Per caricare da browser apri **Code → Add file → Upload files** nella cartella principale (*root*), non dentro `app/`.
4. Trascina **il contenuto della cartella estratta**, mantenendo i percorsi relativi: `.github/workflows/android.yml`, `app/build.gradle`, `app/src/main/...`, `tools/` e file Gradle. **Non caricare lo ZIP come unico file**, e non caricare la cartella superiore come sottocartella aggiuntiva.
5. Se GitHub non conserva correttamente cartelle e sottocartelle, usa GitHub Desktop: **Clone repository → Show in Explorer → copia il contenuto estratto → sovrascrivi → Commit → Push**.
6. Inserisci il commit **`A.M.B. 2.5.1 - calendar, reader, adblock, palettes and TmRexh`** e premi **Commit changes** sul ramo `main`.
7. Vai in **Actions → Build Android APK standalone A.M.B.**. Se non si avvia da solo, premi **Run workflow** sul ramo `main`.
8. Se appare la spunta verde, apri il run e vai a **Artifacts**. Scarica `Apex-Motori-Blaj-APK` (ZIP) e **estrailo** per ottenere il vero `app-debug.apk`.
9. Installa sul telefono Android 10+ e verifica: lettore senza sovrapposizione nella status bar, icona nell'header, calendario mensile, colori personalizzati, TmRexh e notifiche. **Non descrivere la versione come stabile prima di questi test.**
10. Per pubblicare: **Releases → Draft a new release**; crea tag `v2.5.1-beta.1`, titolo `A.M.B. – Apex Motori Blaj 2.5.1 Beta`, copia `RELEASE_2_5_1_IT_EN.md`, allega **`app-debug.apk` estratto** e scegli **Set as a pre-release**.

### Installazione sopra la 2.5

Per sostituire un'APK senza perdere Preferiti e cache deve rimanere **lo stesso package name e la stessa chiave di firma**. La patch mantiene `com.apexmotoriblaj.amb` e alza `versionCode` a 251, ma gli APK debug compilati in esecuzioni differenti possono avere certificati diversi. **Esporta prima il backup dalle Opzioni**: una disinstallazione può cancellare i preferiti e i salvataggi offline. Non condividere mai una chiave di firma privata in un repository open source.

### Checklist di test consigliata

- Android 10 e Android 15/16/17, se disponibili; prova anche un tablet.
- Ingrandimento del testo, AMOLED e palette blu/verde/viola/ambra: nessun accento rosso di interfaccia (F1 può conservare il colore di categoria).
- Calendario: mese completo, 42 caselle e 7 giorni settimanali; data con più eventi, filtri, apertura di `＋ Calendario`.
- Lettore: toolbar sempre sotto la status bar; pulsante Lettura mantiene le fotografie e scalda/diminuisce la luminosità apparente.
- Cookie: prova siti con `Reject`, `Rifiuta` e modali di consenso; verifica che NON sia stato premuto `Accetta` in automatico. Non tutte le UI sono gestibili.
- Auto → Articoli tecnici: feed TmRexh, articoli datati e senza foto, icona o scritta di riserva, apertura archivio.
- Notifica: su Android la piccola icona in status bar sarà bianca monocromatica, non emoji colorata.
- Dispositivi lenti: scorrimento, aggiornamenti e batteria devono essere misurati realmente; non sono benchmark completati.

### File importanti

- `app/src/main/assets/index.html`: UI, calendario a griglia, palette e archivio tecnico
- `app/src/main/assets/amb_logo.webp`: logo grafico in testata
- `app/src/main/java/com/apexmotoriblaj/amb/ArticleActivity.java`: toolbar reader, gestione inset e privacy
- `app/src/main/java/com/apexmotoriblaj/amb/AdBlock.java`: filtri ad/cookie best effort
- `app/src/main/java/com/apexmotoriblaj/amb/MainActivity.java`: integrazione calendario Android
- `app/src/main/java/com/apexmotoriblaj/amb/NotificationScheduler.java`: notifiche
- `app/src/main/res/drawable/ic_racing_car_notification.xml`: icona di stato monocromatica

### Limiti della compilazione qui

Eseguiti: verifica XML e sintassi JS con Node, controlli browser su 320×700, 393×852 e 820×1180, prova del calendario a doppio evento e delle palette, compilazione isolata del filtro AdBlock e controllo del suo script. **Non** eseguiti: build Gradle/Android SDK, installazione reale, misura FPS/batteria, test su tutti i siti e conferma di tutte le fonti esterne.

---

## English — Build the APK

1. Download and extract the **full source ZIP**.
2. Copy all extracted contents to the root of your existing GitHub Android repository; keep `.github/`, `app/`, `tools/` and the Gradle files in their proper paths. The patch ZIP is only for existing 2.5 sources.
3. Commit and push to `main`. Open **Actions → Build Android APK standalone A.M.B.** and wait for a green result.
4. Download the **Apex-Motori-Blaj-APK** artifact and extract `app-debug.apk`.
5. Test on a phone and tablet. The APK is debug-signed; in-place updates require the same signing certificate. Back up local data before any uninstall.
6. To publish a test release, use tag `v2.5.1-beta.1`, add notes from `RELEASE_2_5_1_IT_EN.md`, upload `app-debug.apk` and mark it as prerelease.

The ad blocker attempts to reject optional cookies and hide known consent overlays, but cannot guarantee that every publisher's consent dialog disappears. No automatic acceptance is performed.
