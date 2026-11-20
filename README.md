# A.M.B. – Apex Motori Blaj · 2.6.1 RC5

Applicazione Android (minSdk 29 / Android 10+). Questa è una *release candidate*, non una build certificata.

## Modifiche RC5
- Caricamento iniziale leggero e fallback sicuro per widget Notizie e widget Calendario.
- Calendario mensile trasparente con 42 giorni e indicatori per eventi sincronizzati.
- Limite di memoria per le miniature del widget Notizie.
- Controllo periodico delle nuove release GitHub per gli utenti che **hanno già abilitato le notifiche**; nessuna installazione automatica.
- Le funzioni Video, Brief, fonti, download autorizzati e ManicApe entro 48 ore rimangono nel progetto.

## Build
Esegui GitHub Actions → Build Android APK standalone A.M.B. e scarica l'artefatto.
Per distribuire un aggiornamento a utenti esistenti serve un APK firmato con la **stessa chiave** della versione installata: l'artefatto `assembleDebug` è destinato ai test, non alla distribuzione pubblica.

## Limitazioni
La resa effettiva dei widget su One UI/Android 10+ richiede un test su dispositivo e, se necessario, la consultazione di logcat.
Le notifiche di aggiornamento funzionano solo dalle installazioni che contengono questa patch, con notifiche abilitate e accesso alla rete. Non sono push centralizzate e Android può ritardarle.
