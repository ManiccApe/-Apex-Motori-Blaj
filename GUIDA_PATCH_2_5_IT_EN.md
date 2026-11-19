# A.M.B. – Apex Motori Blaj 2.5
## Guida pratica per GitHub e per ottenere l'APK (Italiano)

**Marchio:** A.M.B. – Apex Motori Blaj · **Created & Developed by ManicApe**  
**Compatibilità impostata:** Android 10 / API 29 e versioni successive, inclusi tablet.  
**Tipo di distribuzione:** sorgenti Android open source e patch sovrapponibile alla versione 2.4.  
**Nota importante:** questi sorgenti NON sono un APK già compilato. La compilazione verrà effettuata da GitHub Actions; la compatibilità e il funzionamento su telefoni reali vanno verificati.

### Quale ZIP utilizzare

- `Apex_Motori_Blaj_2_5_PATCH_GitHub.zip`: solo file nuovi e modificati rispetto ai sorgenti 2.4. Estrarre nella cartella del repository, **sovrascrivendo** i file corrispondenti.
- `Apex_Motori_Blaj_2_5_Sorgenti_OpenSource.zip`: progetto Android completo pronto per GitHub/Android Studio. È il metodo più semplice se vuoi evitare vecchi file rimasti dalla 2.4.

### Passaggi consigliati: GitHub Desktop da computer

1. Scarica e decomprimi **Sorgenti_OpenSource.zip** in una cartella sul PC.
2. Apri GitHub Desktop, seleziona **File → Clone repository** e scegli `ManiccApe/-Apex-Motori-Blaj` (usa il tuo repository corretto se il nome è cambiato).
3. Apri la cartella clonata con **Repository → Show in Explorer** (o Finder su macOS).
4. Copia **il contenuto** dello ZIP estratto, non la cartella esterna, nella cartella del repository. Mantieni le cartelle `app`, `.github`, `tools` e i file Gradle. Scegli **Sovrascrivi** per gli omonimi.
5. In GitHub Desktop controlla i file modificati e inserisci il messaggio `A.M.B. v2.5 - news, reader, widget, calendar and TmRexh`. Premi **Commit to main** e quindi **Push origin**.
6. Su github.com apri il repository, vai a **Actions → Build Android APK standalone A.M.B.**. La compilazione può avviarsi con il push; in alternativa premi **Run workflow** e scegli `main`.
7. Attendi il segno di spunta verde. Entra nell'esecuzione completata: nella sezione **Artifacts** scarica **Apex-Motori-Blaj-APK**.
8. Estrai lo ZIP appena scaricato: dentro troverai `app-debug.apk`. **Questo è l'APK standalone da installare o condividere per test.** Non confondere il file ZIP dei sorgenti con un APK.
9. Sul telefono Android apri `app-debug.apk` tramite Gestione file. Se richiesto, abilita temporaneamente l'installazione da quella fonte. Installa e verifica apertura app, widget, notizie, lettore e notifiche.
10. Se Android indica `App non installata` e c'è già una vecchia A.M.B., è possibile che gli APK siano firmati con certificati differenti. **Prima esporta preferiti e fonti** da Opzioni → Backup, poi valuta di disinstallare la vecchia app e installare la nuova. Disinstallare elimina i dati locali dell'app.

**Non sostituire a caso il file `.apk` con uno ZIP.** I pacchetti degli Artifacts di GitHub devono essere estratti per ottenere il vero APK.

### Come creare una release su GitHub

1. Apri il repository GitHub → **Releases → Draft a new release**.
2. Crea il tag `v2.5.0`, titolo `A.M.B. – Apex Motori Blaj 2.5`.
3. Copia la descrizione italiana e inglese da `RELEASE_2_5_IT_EN.md`.
4. Trascina **l'APK estratto** (`app-debug.apk`, eventualmente rinominato `A.M.B.-2.5-test.apk`) nella sezione allegati della release.
5. Pubblica la release soltanto dopo aver installato e provato quell'APK su almeno un dispositivo Android 10+.

### Importantissimo sulla firma APK

Il workflow incluso produce **un APK debug firmato automaticamente**, installabile come app nuova e utile per i test con gli amici, ma **non garantisce la stessa chiave di firma tra compilazioni diverse**. Per aggiornamenti senza disinstallare usa una **chiave di rilascio persistente** (Android Studio / sistema di firma con GitHub Secrets); non pubblicare mai la chiave privata nel repository open source.

### Cosa contiene la versione 2.5

- Identità A.M.B., autore ManicApe e interfaccia generale conservata; AMOLED nero puro come impostazione predefinita per le nuove installazioni, con palette alternative.
- News F1, WRC, WEC, GT3, MotoGP, Auto, sottocategorie e sorgenti aggiuntive; selezione lingua fonti. **24 nuove fonti candidate** sono incluse ma disattivate finché non vengono verificate feed e fotografie, per non rallentare l'app.
- TmRexh (Auto → Motori → Articoli tecnici): **eccezione unica per articoli senza foto**. Se manca la foto viene visualizzata l'icona del sito tramite un servizio favicon online; se non raggiungibile appare la scritta TmRexh. I normali articoli conservano la foto originale. Nell'offline e nel widget l'icona è memorizzata quando il download riesce. Per il **logo ufficiale in grande formato** serve un asset fornito/autorizzato dal sito: non è incluso nello ZIP.
- Lettore interno con **AdBlock integrato** (filtro di domini pubblicitari e nascondimento di elementi comuni). Non è possibile garantire che elimini ogni annuncio di ogni editore.
- **Modalità Lettura**: un tocco su `Lettura` aggiunge un filtro ambrato più scuro **senza rimuovere foto, testo o layout**; secondo tocco ripristina i colori.
- Preferiti con doppio tap e richiesta di download offline di testo e fotografia quando il sito permette l'estrazione. Le foto interne multiple non sono tutte archiviate da questo primo motore offline; la disponibilità dipende dalle pagine sorgenti. Eventuali errori vengono comunicati.
- Traduzione EN→IT tramite Google ML Kit, con primo download del modello; la traduzione completa è disponibile quando il testo è accessibile, la traduzione della UI è ancora parziale.
- Calendario con filtri regione/provincia per località riconoscibili e supporto a feed `.ics`. I link ACI Sport e Rallylink sono una rubrica di fonti e **non** costituiscono un calendario importato automaticamente.
- Spiegazione per l'autorizzazione alle notifiche prima della richiesta di sistema; notifiche solo previo consenso, nei limiti delle politiche Android.
- Widget scorrevole con immagini, temi AMOLED/trasparente, gestione cache e aggiornamenti limitati.

### Requisiti, test e limiti

- Java 17, Android Gradle Plugin 8.11.1, Gradle 8.13, Android SDK 36, minSdk 29.
- Sono stati eseguiti test statici su XML/JS e test browser su **320×700, 393×852, 820×1180**; questi test **non sostituiscono la build e i test Android**.
- Non è stata eseguita qui la compilazione APK, né una verifica live di tutte le sorgenti RSS, dell'icona remota TmRexh o delle prestazioni/batteria su dispositivi reali.
- Le notizie e le foto restano proprietà dei rispettivi editori. L'archiviazione offline è destinata all'uso personale, non alla redistribuzione dei testi.

---

## English – How to build the Android APK

**A.M.B. – Apex Motori Blaj 2.5**, created and developed by **ManicApe**, targets **Android 10 (API 29) and newer**, phones and tablets.

1. Extract the **full source ZIP**. Clone your GitHub repository with GitHub Desktop.
2. Copy the **contents** of the extracted source folder into the local repository, preserving `app/`, `.github/`, and Gradle files. Overwrite previous versions.
3. Commit and push changes.
4. Open **GitHub → Actions → Build Android APK standalone A.M.B.** and start or inspect the workflow.
5. When the build is successful, download the **Apex-Motori-Blaj-APK** artifact and **extract** its `app-debug.apk`.
6. Install the APK on a test device. If an older build is signed with a different certificate, back up personal data before uninstalling it.
7. Create a release via **Releases → Draft a new release**, tag `v2.5.0`, add the notes from `RELEASE_2_5_IT_EN.md` and attach the actual APK.

The included build creates a **debug-signed, standalone test APK**, not a consistently signed production release. Use a persistent private release signing key for future in-place updates. The warm reading mode **dims and warms the screen while preserving all article photographs**. TmRexh stories without editorial photos use the site's remotely fetched favicon; other feeds continue to require editorial photos. Live RSS, favicon, Android functionality, battery consumption and APK build have not yet been verified here.
