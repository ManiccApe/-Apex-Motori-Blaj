# Pubblicazione GitHub – A.M.B. 2.6.1 RC3

1. Estrai lo ZIP dei **sorgenti completi** e carica **il contenuto** della cartella nel repository GitHub, rispettando i percorsi `app/`, `.github/workflows/`, `build.gradle` ecc.; non caricare lo ZIP come sostituto dei file di progetto.
2. Verifica in `app/build.gradle`: `versionCode 263` e `versionName '2.6.1-rc3'`.
3. Dopo il commit apri **Actions → Android APK** e avvia la compilazione con `:app:assembleDebug`. Apri i log se la compilazione fallisce.
4. Scarica l'artifact APK della workflow. Prima di pubblicarlo testa installazione/aggiornamento, navigazione, miniature, video YouTube, feedback fonti, widget calendario trasparente e frecce mese su Android 10+, incluso Samsung One UI.
5. Un APK debug non è adatto come release pubblica stabile: per distribuzione prepara una **build release firmata** con la stessa chiave delle release precedenti, altrimenti Android potrebbe non permettere l'aggiornamento senza disinstallare (e perdere dati).
6. GitHub **Releases → Draft a new release**: tag `v2.6.1-rc3`, titolo `A.M.B. 2.6.1 RC3 – Video & Calendar Fix`, spunta **Set as a pre-release**, allega *solo* l'APK generato dai sorgenti RC3 dopo le verifiche e incolla `RELEASE_NOTES_2_6_1_RC3.md`.
7. Non è stato eseguito alcun commit o pubblicazione da questo assistente.
