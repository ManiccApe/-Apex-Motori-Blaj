# Guida alla release GitHub A.M.B. 2.6.0

1. Crea un nuovo branch nel repository e carica il contenuto della cartella `-Apex-Motori-Blaj-main` (non il contenitore esterno dello ZIP). Controlla la diff prima di unire in `main`.
2. In GitHub, apri **Actions → Build Android APK standalone A.M.B. → Run workflow**. Aspetta il risultato verde e scarica l'artifact `Apex-Motori-Blaj-APK`.
3. L'artifact del workflow attuale è **app-debug.apk**. Installalo solo per test, non trattarlo come APK di produzione.
4. Testa avvio, migrazione dati 2.5.2→2.6.0, logo, navigazione, Video, ManicApe, feed, rete assente, download Wi-Fi/dati, notifiche, widget, rotazione e Android 10–17. Confronta la firma dell'APK precedente con quella nuova.
5. Per la distribuzione prepara un APK **release firmato** con una chiave conservata in sicurezza. Non pubblicare chiavi/password in GitHub. La firma deve coincidere con quella delle versioni installate per consentire l'aggiornamento senza disinstallazione.
6. Apri **Releases → Draft a new release**, tag `v2.6.0-rc1` finché i test non sono completati. Titolo `A.M.B. 2.6.0 – Release Candidate`. Incolla `RELEASE_NOTES_2_6_0_IT_EN.md`; allega APK verificato e ZIP sorgenti.
7. Dopo test e firma corretti, usa il tag `v2.6.0` e dichiara chiaramente eventuali funzioni ancora sperimentali.

**Nota:** il presente pacchetto non è stato pubblicato e non contiene un APK 2.6 compilato o verificato.
