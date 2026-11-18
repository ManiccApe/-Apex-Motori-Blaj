# Guida aggiornamento A.M.B. 2.4

1. Fai un backup delle preferenze dall'app A.M.B. già installata prima di disinstallarla.
2. Estrai `Apex_Motori_Blaj_2_4_PATCH_GitHub.zip` sul PC. Lo ZIP contiene già la struttura `app/...` necessaria.
3. Apri https://github.com/ManiccApe/-Apex-Motori-Blaj (ramo `main`) → **Code → Add file → Upload files**.
4. Trascina i file e le cartelle estratte, conservando i percorsi originali e sostituendo i file preesistenti.
5. Premi **Commit changes** sul ramo `main`.
6. Apri **Actions**, scegli la nuova build con segno verde e scarica l'archivio **Apex-Motori-Blaj-APK** nella sezione **Artifacts**.
7. Estrai lo ZIP ricevuto da GitHub e copia `app-debug.apk` sul telefono. L'APK e' standalone, non una patch; la firma debug puo' cambiare tra build, percio' potrebbe essere necessario disinstallare la precedente installazione.
8. In A.M.B. → **Opzioni → Aspetto e leggibilità** prova `Disposizione degli articoli`, `Dimensione immagini` e `Adattamento foto`.
9. Sul widget esistente tocca l'icona **⚙**, abilita **Mostra miniature degli articoli**, scegli `Dimensione notizie e fotografie` e premi **Salva e applica**.
10. Apri l'app e usa **Aggiorna** per riempire la cache del widget. Le foto compariranno gradualmente quando la rete e gli editori consentono il download.

Non confondere **Artifacts** (APK generato dalle Actions) con il codice sorgente ZIP. Per pubblicare l'APK su GitHub, crea una Release e allega il file `.apk` estratto.
