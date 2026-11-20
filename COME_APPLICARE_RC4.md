# Aggiornamento A.M.B. 2.6.1 RC4

Base richiesta: **A.M.B. 2.6.1 RC3**.

## Opzione A — ZIP sorgenti completi
Estrarre `AMB_2_6_1_RC4_SORGENTI_COMPLETI.zip` sul PC. Caricare i file estratti nel repository GitHub, conservando esattamente la struttura del progetto (la cartella `app/` deve trovarsi alla radice del repository). Verificare GitHub Actions, quindi scaricare e provare l'APK compilato.

## Opzione B — ZIP patch
Estrarre `AMB_2_6_1_RC4_PATCH_MANICAPE_48H.zip` nella radice del progetto RC3, sovrascrivendo i soli file modificati. La patch **non** contiene tutto il progetto.

## Comportamento
- La fonte ManicApe viene interrogata insieme agli altri creator indipendentemente dalla sua visibilità.
- Il canale e le relative schede video sono mostrati nella scheda Video solo se YouTube comunica una pubblicazione **entro le ultime 48 ore** e con timestamp preciso e verificabile.
- Quando il feed è indisponibile o non fornisce la data, il canale rimane nascosto (scelta prudenziale).
- Il collegamento ManicApe in **Impostazioni → Contattami** resta sempre presente.
- Tutti gli altri canali non sono soggetti al nuovo limite di 48 ore.

## Versione e distribuzione
- `versionCode`: 264
- `versionName`: 2.6.1-rc4
- Tag consigliato: `v2.6.1-rc4`, **Pre-release**.
- Non pubblicato automaticamente; l'APK non è stato compilato/testato su dispositivo in questo ambiente.
