# QA RC4 – ManicApe entro 48 ore

Eseguiti controlli locali il 20/11/2026.

- PASS: parsing XML e sintassi di tutti gli script JavaScript (Node.js).
- PASS: layout XML Android, 42 celle calendario, risorse icone trasparenti (`tools/test_rc3.py`).
- PASS: in browser Chromium con feed YouTube simulato: 4 ore -> visibile; 47 ore -> visibile; 49 ore -> nascosto; timestamp mancante -> nascosto; timestamp futuro -> nascosto (`tools/test_rc4_manicape_48h.py`).
- PASS: smoke test preesistente video, fonti locali e navigazione calendario (`tools/test_rc3_ui.py`).

Da verificare: effettiva accessibilità dei feed YouTube su rete Android, installazione/aggiornamento APK su Samsung One UI, Gradle build, firma APK.
