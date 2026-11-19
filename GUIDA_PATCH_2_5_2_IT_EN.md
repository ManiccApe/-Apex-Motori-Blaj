# A.M.B. 2.5.2 — Patch lettore / Reader hotfix

## Italiano — Che cosa risolve

Questa patch aggiorna i sorgenti Android della **2.5.1** a **2.5.2** per correggere il lettore web che poteva apparire bloccato dopo aver nascosto automaticamente le finestre dei cookie.

- I banner cookie, privacy e consenso delle testate **non vengono più nascosti o premuti automaticamente**. L'utente seleziona sul sito «Accetta», «Rifiuta» o «Personalizza», quando disponibili. Nessun consenso viene dato da A.M.B.
- Il lettore mantiene JavaScript, DOM storage e cookie necessari al funzionamento dei siti. Accetta anche cookie di terze parti nella WebView del lettore per consentire il funzionamento di alcune piattaforme di gestione consenso. **Questo può aumentare l'uso di cookie di terzi**: sono sempre i siti terzi a decidere quali cookie impostare e rispettare la scelta dell'utente.
- Eliminato l'inserimento di CSS/JavaScript che nascondeva popup CMP senza ripristinare lo scorrimento dell'articolo.
- Filtro pubblicitario più conservativo per evitare di bloccare le risorse dedicate a cookie e privacy.
- Pulsante **Filtro ON / Filtro OFF**: prova OFF se un sito non scorre o non mostra la finestra di consenso; il lettore ricarica la pagina dopo ogni cambio.
- Pulsante **↗**: apre l'articolo corrente nel browser esterno se il sito non è compatibile con Android System WebView.
- Menu **⋮ → Azzera cookie e dati dei siti…**: richiede conferma e cancella i dati WebView delle **testate visitate**, non i Preferiti di A.M.B. Usalo se il vecchio rifiuto automatico ha salvato una preferenza errata. Può scollegarti dai siti.
- **Originale** apre sempre la pagina live, non una copia offline che salterebbe il dialogo. La copia salvata resta nel menu **⋮ → Apri copia offline** se disponibile, e come fallback in caso di errore di rete.
- La **Modalità Lettura** continua a ridurre luminosità apparente e aumentare il calore dei colori senza eliminare immagini o elementi del sito. Traduzione, calendario, widget, TmRexh e le palette 2.5.1 sono conservati.

### Scarica, applica e compila

1. Per aggiornare un repository GitHub già alla versione **2.5.1**, scarica `Apex_Motori_Blaj_2_5_2_PATCH_su_2_5_1.zip` e **decomprimi** l'archivio. Copia il suo contenuto nella cartella principale del repository, mantenendo i percorsi `app/src/main/...`, `tools/...` e `app/build.gradle`; sovrascrivi i file omonimi.
2. Se non sei certo della versione nel repository, usa invece `Apex_Motori_Blaj_2_5_2_Sorgenti_Completi.zip`: contiene tutti i sorgenti. Estrai **il contenuto** nella root del repository, non la cartella esterna e non il file ZIP.
3. Fai il commit su `main` con messaggio `A.M.B. 2.5.2 - manual cookie consent and scroll fix`.
4. In GitHub vai a **Actions → Build Android APK standalone A.M.B.**, esegui il workflow su `main` se necessario e verifica l'esito. Apri l'esecuzione: **Artifacts → Apex-Motori-Blaj-APK**. Estrai il file `app-debug.apk`.
5. Salva prima una copia di Preferiti e impostazioni. Se Android rifiuta l'aggiornamento, la firma debug potrebbe essere diversa dalla versione installata; non disinstallare senza backup, perché perderesti i dati locali.
6. Per la release, usa tag `v2.5.2-beta.1`, titolo `A.M.B. 2.5.2 Beta – Reader & Cookie Consent Fix`, testo `RELEASE_2_5_2_IT_EN.md`; allega il **nuovo** `app-debug.apk` (non lo ZIP). Seleziona **Set as a pre-release**.

### Come provarla su più testate

1. Apri una notizia che prima restava bloccata. Il sito dovrebbe mostrare la propria richiesta cookie quando necessaria.
2. Scegli manualmente se rifiutare/accettare, poi prova a scorrere fino in fondo e a tornare indietro.
3. Se il banner non compare o la pagina non funziona, tocca **Filtro ON → Filtro OFF** e lascia ricaricare.
4. Se il vecchio consenso è stato salvato erroneamente, usa **⋮ → Azzera cookie e dati dei siti…** confermando consapevolmente la perdita degli accessi ai siti, poi riapri l'articolo.
5. Se la WebView continua a non funzionare su una singola testata, tocca **↗** per usare il browser del telefono.
6. Prova sia smartphone Android 10 sia versioni recenti e, se disponibile, un tablet. Verifica l'app WebView aggiornata.

### Compatibilità e limiti

- La patch è **generale, non specifica di un modello o testata**; il progetto richiede **Android 10 o superiore** (minSdk 29). Non è tecnicamente possibile garantire ogni sito, sistema anticookie, paywall, login e motore WebView, né ogni telefono precedente ad Android 10.
- Non elude paywall e non nasconde le richieste privacy. Il blocco annunci è **best effort** e il pulsante Filtro OFF serve proprio per i siti incompatibili.
- I filtri annuncio non sono la modalità «Shields» di Brave: Android WebView non incorpora Brave Shields.
- Verificato in questo ambiente: XML, sintassi JavaScript, controlli statici e test browser su 320, 393 e 820 pixel e simulazione del dialogo cookie. **Non verificato qui:** compilazione Gradle con SDK Android, APK installato, test end-to-end su tutti i siti reali. Build GitHub Actions e prova su dispositivi restano necessarie.

## English — Short instructions

Apply `Apex_Motori_Blaj_2_5_2_PATCH_su_2_5_1.zip` on top of the **2.5.1** repository (preserve paths), or replace project files using `Apex_Motori_Blaj_2_5_2_Sorgenti_Completi.zip`. Commit on `main`, run the existing **Build Android APK standalone A.M.B.** workflow and download/extract `app-debug.apk` from Artifacts. Tag the test release `v2.5.2-beta.1` and mark it as pre-release.

Publisher cookie/privacy dialogs are no longer hidden or auto-clicked; JavaScript/DOM storage/cookies work normally. Third-party cookies are allowed in the reader WebView for compatibility with some consent managers, with potential third-party tracking implications. Tap **Filtro OFF** to reload without filtering, use **↗** for the external browser, or **⋮** to reset all reader site data after explicit confirmation. Bookmarks/offline articles are not removed by the site-data reset. Online original and offline copy are separate actions. General Android 10+ fix, not guaranteed for every publisher or device. Android APK/device testing is still required.
