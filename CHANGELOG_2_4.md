# Apex Motori Blaj 2.4 — Widget con foto e dimensionamento articoli

**Versione Android**: 2.4 / versionCode 24 / Android 10+ (API 29).

## Widget
- Correzione di una causa delle miniature invisibili: quando la WebView sincronizza gli articoli col widget, ora viene avviato anche il precaricamento delle immagini (prima passavano solo titoli e URL).
- Ogni widget può scegliere la dimensione degli articoli e delle foto: Compatta, Normale o Grande. Include layout Android nativi separati e supporta temi AMOLED e Trasparente.
- Download delle foto HTTPS su executor dedicato, cache JPEG sul dispositivo, controllo sulle dimensioni (max 5 MB), notifiche periodiche alla Home dopo il download.
- Quando il widget viene aggiunto, aggiornato o configurato vengono recuperate le miniature mancanti per le righe visibili, senza ricaricare tutto il sito da zero.
- Se un editore blocca la foto, l'app non può forzarne il caricamento: il titolo rimane visibile.

## Home e Impostazioni
- Densità schede: Compatta / Normale / Ampia.
- Layout articoli: foto laterale / sopra il titolo a tutta larghezza.
- Immagini: Piccole / Medie / Grandi; modalità Riempi (ritaglio) / Intera (senza ritaglio).
- Layout responsivi per smartphone piccoli e tablet; preferenze memorizzate localmente e incluse nei backup.

## Note di distribuzione
- Il repository Github Actions compila e firma un **APK debug standalone**. Questa patch contiene soltanto sorgenti; dopo il caricamento deve essere compilata.
- Non è stata eseguita una prova fisica su telefono Android. I test di XML/JavaScript/layout da browser non sono una verifica di installazione.
