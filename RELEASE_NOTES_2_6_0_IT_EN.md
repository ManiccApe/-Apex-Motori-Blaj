# A.M.B. – Apex Motori Blaj v2.6.0

**Stato: RELEASE CANDIDATE / build e test Android da eseguire.** Non presentare come release stabile finché non sono completate le verifiche.

## Italiano
Aggiornamento basato sulla versione 2.5.2, con nuova identità A.M.B. e logo, palette **LMR** (in precedenza viola), sezione **Video**, possibilità di aggiungere creator tramite URL di canale YouTube e il canale [ManicApe](https://www.youtube.com/@maniccape) in primo piano. Schermata novità della 2.6 e prima implementazione dei download di file video autorizzati con scelta **solo Wi-Fi** o **Wi-Fi e dati mobili**. Sono mantenute le funzioni già presenti nella base, salvo regressioni da verificare.

**Limitazioni note:** feed dei creator e player richiedono test su dispositivo; il download non deve aggirare restrizioni di YouTube o di altri servizi. Il Brief giornaliero alle 19:00, il widget calendario mensile, la copertura verificata degli eventi di tutte le regioni italiane, l'aggiornamento APK interno e il player offline completo **non sono certificati come completi**. Non è stata verificata la compatibilità con l'APK 2.5.2 già installato, né la firma digitale di aggiornamento.

## English
Source update from 2.5.2 with refreshed A.M.B. branding and icon, **LMR** palette (formerly purple), dedicated **Video** area, creator channels added by YouTube URL, and **ManicApe** prioritized. Includes a 2.6 welcome screen and an initial implementation of authorized direct-file video downloads with Wi-Fi-only or mobile-data permission.

**Known limitations:** device testing is pending for creator feeds, playback and downloads. YouTube restrictions must not be bypassed. Daily 7 PM Brief, monthly calendar widget, verified regional events, in-app APK updater and complete offline player are **not certified as finished**. Upgrade signature compatibility with the installed 2.5.2 APK has not been established.

## Release assets
- Source ZIP: complete Android Studio/Gradle project.
- APK: attach only **after** GitHub Actions successfully compiles it and testing passes. Debug APKs are for testing; a publicly distributed production APK should be release-signed with a safely managed keystore.

## Installation
Android 10+ (minSdk 29). Install a signed APK only after validating the source, package ID and signature. A different signing key may require uninstalling the prior app (which can erase local data); do not uninstall without backing up data.
