# A.M.B. 2.6.1 — istruzioni GitHub

1. Carica il contenuto dello ZIP sorgenti nel repository, mantenendo `.github/workflows/android.yml` e `app/` alla radice.
2. In GitHub Actions esegui il workflow Android esistente (assembleDebug).
3. Scarica l'artefatto `Apex-Motori-Blaj-APK` e verifica la versione 2.6.1.
4. Se l'APK 2.5.2 era firmato con una chiave diversa, Android non potrà installare la 2.6.1 sopra quella versione: usa la stessa chiave di firma per aggiornamenti senza perdita dei dati.
5. Testa feed YouTube, player embedded, Brief, notifiche alle 19, widget, fonti, download Wi-Fi/dati, aggiornamenti GitHub e rotazione su smartphone/tablet.
6. Solo dopo test e firma, crea una pre-release `v2.6.1-rc1` con note `RELEASE_NOTES_2_6_1_IT.md` e APK testato.

**Non caricare l'APK 2.5.2 come 2.6.1.** Questo archivio contiene sorgenti, non un APK compilato. Nessun commit è stato effettuato automaticamente.
