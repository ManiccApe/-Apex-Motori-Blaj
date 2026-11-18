# Apex Motori Blaj 2.1 — istruzioni

## Modifiche

- Tema **AMOLED** con sfondo `#000000`, disponibile in Opzioni > Tema dell’app. Scuro classico resta separato.
- Widget nativo: quattro temi indipendenti **Chiaro / Scuro / AMOLED (nero puro) / Trasparente**, più scelta testo chiaro/scuro su sfondo trasparente. La trasparenza in percentuale continua a funzionare per i temi Chiaro e Scuro.
- Categorie disponibili: Tutte, F1, WRC, WEC, GT3, MotoGP, Personali.
- GT3: nuova classificazione dei feed ENDURANCE/GT che riconosce anche GT World Challenge, GTWC, SRO, IGTC, Spa 24 Hours e ADAC GT Masters quando i feed includono questi termini.
- Vengono eliminate dai menu GT, GT2, GT4, Storiche e Goodwood. Le fonti personali aggiunte da te e i preferiti sono conservati.
- Calendario GT World Challenge Europe riconfigurato come GT3.

## Aggiornare il repository su GitHub

Apri il repository ManiccApe/-Apex-Motori-Blaj, vai nella schermata `Code` e carica **i file contenuti nell'archivio PATCH**, mantenendo i percorsi completi `app/src/main/...`. Sostituisci i file esistenti e includi il nuovo `Gt3Classifier.java`. Conferma il commit su `main`. La configurazione `.github/workflows/android.yml` già presente avvierà una nuova build.

### Se GitHub non ti lascia sostituire file caricando cartelle

Usa GitHub Desktop sul PC o esegui `git clone`, copia sopra i file della PATCH, quindi commit e push. In alternativa apri ciascun file di testo con la matita e incolla i contenuti; il nuovo `Gt3Classifier.java` si crea con Add file > Create new file.

## APK

Il file APK **non è incluso** nello ZIP: è necessario generarlo tramite GitHub Actions (SDK Android ufficiale) e controllare l'Artifact dell'ultima build riuscita. Le build debug su GitHub possono avere firme diverse e richiedere disinstallazione dell'app precedente: salva i preferiti tramite Backup prima di farlo.

## Limiti

Non tutti i feed RSS contengono notizie GT3 ogni giorno e gli URL possono essere modificati dagli editori; la classificazione non può inventare notizie. Widget trasparente reale: il risultato visivo dipende anche dallo sfondo del launcher.
