# A.M.B. v2.3 — Changelog

- Nessuna notifica rossa per feed non raggiungibili nella Home.
- Le fonti vengono verificate a ogni sincronizzazione e producono notizie solo se contengono foto valide come link HTTPS negli item RSS/Atom.
- I feed senza immagini o irraggiungibili non contribuiscono alla rassegna; lo stato compare nella sezione Fonti.
- Nuovi siti personali accettati solo se forniscono articoli con foto riconoscibili.
- Parser fotografie migliorato: `media:thumbnail`, `media:content`, `enclosure` e immagini HTML nei feed.
- Cache vecchia senza foto filtrata in News; preferiti non cancellati.
- Widget: solo notizie con URL di foto; pre-caricamento parallelo delle anteprime per un massimo di 60 notizie.
- Categorie e funzionalità v2.2 conservate, incluse Auto, GT3, tema AMOLED, widget trasparente, calendario, logo A.M.B.

Limitazioni: presenza di un URL fotografico non equivale a certezza che l'editore ne consenta il caricamento; il widget può omettere miniature bloccate. I controlli testati non sostituiscono la compilazione Android con Gradle/GitHub Actions.
