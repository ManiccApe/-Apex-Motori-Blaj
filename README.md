# Apex Motori Blaj — A.M.B. 2.1

**Creato da Emanuel Mihai Blaj** · codice open source con licenza MIT.

App motorsport Android **indipendente** con News, Calendario, Preferiti, Fonti personalizzate, Impostazioni e **widget Android nativo scorrevole**. L'app è sviluppata in Java, HTML5, CSS3, JavaScript e XML, con Android SDK / Gradle. Non contiene player video interno, né sezioni Nürburgring o Misha Charoudin. I contenuti sono dei rispettivi editori.

## Compatibilità

- Minimo Android 10 (API 29), previsto per Android 11–17 e successive, salvo modifiche delle piattaforme future.
- App nativa con WebView locale e rete gestita nativamente per superare le limitazioni CORS dei feed RSS/Atom e dei calendari ICS.
- Supporto a schermi piccoli (320 dp), smartphone grandi e tablet; layout e widget responsivi.
- Un **APK completo** per nuove installazioni, non patch/OTA/AAB. Il pacchetto deve essere compilato con Android SDK, *non* con lo sperimentale script manuale delle versioni 1.x.

## Funzioni

- **Home e notizie**: F1, WRC, WEC, GT3 ed endurance, MotoGP, GT3; categorie in una riga orizzontale scorrevole, ricerca, schede dense ma leggibili, piccolo pulsante Aggiorna in testata, immagini dai feed originali quando disponibili.
- **Fonti**: feed italiani e inglesi da editori originali; aggiunta URL RSS/Atom o URL sito con auto-rilevamento feed, attivazione, rimozione delle fonti personali. Per siti senza feed non è possibile garantire un aggiornamento automatico. GT3 usa il feed ENDURANCE/GT di Motorsport.com e filtra le notizie pertinenti; la disponibilità dipende dalla fonte.
- **Calendario**: eventi nelle stesse categorie, filtri, ricerca, prossimi 1/3/6/12 mesi, import .ics, sincronizzazione all'apertura e mentre è in uso. Orari Europe/Rome (se forniti dalle fonti); link informativi su trasmissioni e streaming, non garanzie di programmazione evento per evento.
- **Preferiti**: offline; backup/importazione JSON dei preferiti e delle fonti.
- **Impostazioni**: tema chiaro/scuro/automatico, dimensione testo, densità, immagini, cache, calendario, widget, riconoscimenti del creatore (Emanuel Mihai Blaj) e strumenti utilizzati.
- **Widget scorrevole**: Android `AppWidgetProvider` + `RemoteViewsService`, 20/40/60 notizie (fino al limite delle fonti), immagini quando le forniscono gli editori, categorie selezionabili, temi Chiaro/Scuro/AMOLED/Trasparente, testo chiaro/scuro, più widget indipendenti, refresh. I dati vengono sincronizzati dall'app o dal widget; la frequenza in background è soggetta alle politiche Android/launcher. Nessun invio di dati personali a server propri.
- **Media**: nessun player video; articoli e video si aprono nel browser esterno.

### AVVERTENZA sulle build

Qui è incluso il **progetto sorgente completo**, ma **non** un APK sperimentale ottenuto manualmente: le precedenti versioni 1.2–1.4 non si installavano sul dispositivo. Per creare un APK realmente standard usa la toolchain ufficiale Android Studio oppure GitHub Actions. Solo un test su un telefono/emulatore certifica che tutte le funzioni girino correttamente.

## APK installabile con GitHub Actions (nessun Android Studio necessario)

1. Crea un repository GitHub (può essere inizialmente privato) e carica **il contenuto di questa cartella**, inclusa `.github`.
2. Vai in **Actions → Build Android APK standalone A.M.B. → Run workflow** (o attendi la build del push su `main`).
3. La build compila codice Java e risorse Android con **Android SDK ufficiale**, quindi esegue `apksigner verify` e `aapt dump badging`.
4. Sotto **Artifacts**, scarica **Apex-Motori-Blaj-AMB-2.0-standalone-APK** e estrai `app-debug.apk` dallo ZIP. Condividi questo APK agli amici: **non è un file di aggiornamento**.

L'APK debug è firmato automaticamente con la chiave debug generata dall'ambiente GitHub Actions. **La chiave può cambiare tra build**: nuove versioni potrebbero richiedere disinstallazione, con perdita dei dati locali. Per distribuzione duratura, configura una chiave di firma *release* privata e stabile usando i segreti GitHub o Android Studio. Non pubblicare mai chiavi private con il codice.

## Build con Android Studio

1. Apri questa cartella in Android Studio; usa JDK 17, Android SDK Platform 36 e Build Tools 36.0.0.
2. **Build → Build APK(s)**. Attendi `app/build/outputs/apk/debug/app-debug.apk`.
3. Per una versione stabile usa **Build → Generate Signed Bundle / APK → APK** e conserva la chiave di firma.
4. Installa sul telefono da Archivio → Download. Il file APK deve essere ottenuto dalla build ufficiale. Se anche la build ufficiale fallisce, estrai l'errore tramite `adb install -r app-debug.apk` sul PC, che restituisce un codice diagnostico specifico.

## Codice e privacy

- `app/src/main/java/com/apexmotoriblaj/amb/`: Activity, bridge di rete sicura, widget, parser RSS e configurazione nativa.
- `app/src/main/assets/index.html`: UI completa con News, Calendario, Preferiti, Fonti, Impostazioni.
- `app/src/main/res/layout/`: layout widget compatibili da Android 10.
- `app/src/main/res/xml/widget_info.xml`: definizione del widget.
- `.github/workflows/android.yml`: pipeline ufficiale che genera e verifica l'APK.
- `LICENSE`: MIT per il codice originale. Non include marchi, articoli, immagini e trasmissioni di terzi.

Il progetto usa internet per RSS/ICS e immagini degli articoli. Non ci sono account, traccianti o analytics proprietari. Le preferenze restano nel dispositivo. Solo URL **HTTPS** sono accettati dal bridge di rete nativo. I siti terzi possono modificare/disabilitare feed o limitare l'accesso; non viene inventata alcuna notizia o gara.

### Test eseguiti

Test automatici del codice Java e dell'interfaccia web possono essere eseguiti localmente. La compilazione ufficiale con tool Android e l'installazione fisica **non sono verificabili in un ambiente senza Android SDK**. I test statici **non equivalgono** alla prova su smartphone.
