# Caricare A.M.B. 2.6.1 RC5 su GitHub `main`

1. Scarica lo ZIP `AMB_2_6_1_RC5_MAIN_REPOSITORY.zip` ed estrailo.
2. Nel repository GitHub `ManiccApe/-Apex-Motori-Blaj`, branch `main`, carica **i contenuti della cartella estratta**, non la cartella contenitore.
3. Sovrascrivi i file omonimi. I file da mantenere nella root sono `.github/`, `app/`, `.gitignore`, `build.gradle`, `gradle.properties`, `settings.gradle`, `README.md`, `LICENSE`, `THIRD_PARTY_NOTICES.md`, `RELEASE_NOTES_RC5.md` e questa guida.
4. **Pulizia reale:** GitHub non cancella i file vecchi solo perché mancano nello ZIP. Elimina esplicitamente dalla root i vecchi `CHANGELOG_*.md`, `GUIDA_*.md`, `PATCH_MANIFEST_*.md`, `QA_*.md`, `RELEASE_*.md`, `RELEASE_NOTES_2_*.md`, `COME_*.md` e `FONTI_RSS_2_2.md`, se non ti servono per archivio. Non eliminare le cartelle `app` o `.github`.
5. Esegui commit. Vai in **Actions → Build Android APK standalone A.M.B. → Run workflow** oppure attendi l'esecuzione al push.
6. Scarica l'artefatto APK e prova su Android 10+, rimuovendo e riaggiungendo entrambi i widget se necessario. Verifica rendering, cambio mese, contenuti notizie, temi AMOLED/trasparente.
7. Per aggiornare un APK già installato serve **la stessa chiave di firma**. La build `assembleDebug` pubblicata da Actions è destinata ai test, non è automaticamente una release firmata compatibile con gli APK installati.

## Notifiche aggiornamenti
Chi aveva abilitato le notifiche in A.M.B. e installa RC5 riceverà controlli periodici delle release GitHub (non esatti e non push); il canale degli aggiornamenti Android deve essere abilitato. Non è possibile notificare a distanza dispositivi che eseguono ancora RC4 o precedenti senza un backend push preinstallato.

## Diagnosi se il widget non funziona ancora
Su un PC con ADB collegato al telefono:
`adb logcat -v time -s AMB-NewsWidget:E AMB-CalendarWidget:E AMB-WidgetList:E AppWidgetHostView:E`

**Stato:** patch sorgente da verificare tramite compilazione e prova reale su Android/One UI.
