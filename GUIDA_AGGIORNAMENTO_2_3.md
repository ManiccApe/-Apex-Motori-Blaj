# Installare A.M.B. 2.3 da GitHub

1. Estrai lo ZIP `Apex_Motori_Blaj_2_3_PATCH_GitHub.zip`.
2. Apri https://github.com/ManiccApe/-Apex-Motori-Blaj e vai alla scheda Code.
3. Con **Add file → Upload files**, carica i file mantenendo i rispettivi percorsi (sovrascrivi le versioni precedenti), oppure usa Git sul computer per fare il commit.
4. Premi **Commit changes** sul ramo `main`.
5. Apri **Actions**. La build `Build Android APK standalone A.M.B.` dovrebbe avviarsi automaticamente.
6. Quando la compilazione è verde, nella sezione **Artifacts** scarica `Apex-Motori-Blaj-APK`, estrai `app-debug.apk` e installalo su Android 10+.
7. Per pubblicarlo agli amici: GitHub → **Releases → Draft a new release**, tag `v2.3`, carica `app-debug.apk` e pubblica.

**Importante:** l'APK generato da GitHub Actions usa una firma debug, che può cambiare tra build. Per aggiornamenti stabili serve una chiave release persistente; prima di disinstallare una vecchia versione esporta i preferiti. La compilazione ufficiale avviene su GitHub e non è verificata da questi sorgenti da soli.
