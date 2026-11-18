# Apex Motori Blaj — A.M.B. 2.0

**Creato da Emanuel Mihai Blaj** · codice open source con licenza MIT.

App motorsport Android **indipendente** con News, Calendario, Preferiti, Fonti personalizzate, Impostazioni e **widget Android nativo scorrevole**. L'app è sviluppata in Java, HTML5, CSS3, JavaScript e XML, con Android SDK / Gradle. Non contiene player video interno. I contenuti sono dei rispettivi editori.

## Compatibilità

- Minimo Android 10 (API 29), previsto per Android 11–17 e successive, salvo modifiche delle piattaforme future.
- App nativa con WebView locale e rete gestita nativamente per superare le limitazioni CORS dei feed RSS/Atom e dei calendari ICS.
- Supporto a schermi piccoli (320 dp), smartphone grandi e tablet; layout e widget responsivi.
- Un **APK completo** per nuove installazioni, non patch/OTA/AAB. Il pacchetto deve essere compilato con Android SDK, *non* con lo sperimentale script manuale delle versioni 1.x.

## Funzioni

- **Home e notizie**: F1, WRC, WEC,GT3 ed endurance, MotoGP; categorie in una riga orizzontale scorrevole, ricerca, schede dense ma leggibili, piccolo pulsante Aggiorna in testata, immagini dai feed originali quando disponibili.
- **Fonti**: feed italiani e inglesi da editori originali; aggiunta URL RSS/Atom o URL sito con auto-rilevamento feed, attivazione, rimozione delle fonti personali. Per siti senza feed non è possibile garantire un aggiornamento automatico. Goodwood è presente come categoria; il sito ufficiale non è attivo come RSS predefinito finché non pubblica un feed verificato.
- **Calendario**: eventi nelle stesse categorie, filtri, ricerca, prossimi 1/3/6/12 mesi, import .ics, sincronizzazione all'apertura e mentre è in uso. Orari Europe/Rome (se forniti dalle fonti); link informativi su trasmissioni e streaming, non garanzie di programmazione evento per evento.
- **Preferiti**: offline; backup/importazione JSON dei preferiti e delle fonti.
- **Impostazioni**: tema chiaro/scuro/automatico, dimensione testo, densità, immagini, cache, calendario, widget, riconoscimenti del creatore (Emanuel Mihai Blaj) e strumenti utilizzati.
- **Widget scorrevole**: Android `AppWidgetProvider` + `RemoteViewsService`, 20/40/60 notizie (fino al limite delle fonti), immagini quando le forniscono gli editori, categorie selezionabili, sfondo trasparente/opaco, testo chiaro/scuro, più widget indipendenti, refresh. I dati vengono sincronizzati dall'app o dal widget; la frequenza in background è soggetta alle politiche Android/launcher. Nessun invio di dati personali a server propri.
- **Media**: nessun player video; articoli e video si aprono nel browser esterno.

### AVVERTENZA sulle build

Qui è incluso il **progetto sorgente completo**, ma **non** un APK sperimentale ottenuto manualmente: le precedenti versioni 1.2–1.4 non si installavano sul dispositivo. Per creare un APK realmente standard usa la toolchain ufficiale Android Studio oppure GitHub Actions. Solo un test su un telefono/emulatore certifica che tutte le funzioni girino correttamente.


## Codice e privacy

- `app/src/main/java/com/apexmotoriblaj/amb/`: Activity, bridge di rete sicura, widget, parser RSS e configurazione nativa.
- `app/src/main/assets/index.html`: UI completa con News, Calendario, Preferiti, Fonti, Impostazioni.
- `app/src/main/res/layout/`: layout widget compatibili da Android 10.
- `app/src/main/res/xml/widget_info.xml`: definizione del widget.
- `.github/workflows/build-apk.yml`: pipeline ufficiale che genera e verifica l'APK.
- `LICENSE`: MIT per il codice originale. Non include marchi, articoli, immagini e trasmissioni di terzi.

Il progetto usa internet per RSS/ICS e immagini degli articoli. Non ci sono account, traccianti o analytics proprietari. Le preferenze restano nel dispositivo. Solo URL **HTTPS** sono accettati dal bridge di rete nativo. I siti terzi possono modificare/disabilitare feed o limitare l'accesso; non viene inventata alcuna notizia o gara.

### Test eseguiti

Test automatici del codice Java e dell'interfaccia web possono essere eseguiti localmente. La compilazione ufficiale con tool Android e l'installazione fisica **non sono verificabili in un ambiente senza Android SDK**. I test statici **non equivalgono** alla prova su smartphone.
