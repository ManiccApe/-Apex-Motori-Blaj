# A.M.B. 2.6.1 RC4 – ManicApe: solo nuovi upload (48 ore)

**Novità**

- Il canale **ManicApe** (`https://www.youtube.com/@maniccape`) viene controllato normalmente in background insieme agli altri creator, ma appare nella scheda **Video** soltanto quando il feed pubblica almeno un video con data verificata entro le **ultime 48 ore**.
- Solo i video ManicApe degli ultimi due giorni vengono mostrati, in primo piano. Video più vecchi, senza data di pubblicazione completa o con data futura vengono esclusi.
- La stessa regola si applica ai video salvati nella cache: i contenuti scaduti non vengono rimostrati dopo aver riaperto l’app. Quando un upload supera le 48 ore viene automaticamente nascosto anche lasciando la sezione aperta.
- Quando non ci sono pubblicazioni ManicApe recenti, sparisce anche la riga del canale dalla gestione dei creator nella scheda Video. Il collegamento in **Impostazioni → Contattami** resta sempre disponibile.
- Tutti gli altri creator mantengono il comportamento precedente e continuano a mostrare i propri video indipendentemente da questa regola.

**Limiti**: il feed pubblico YouTube deve fornire la data completa di pubblicazione; se la data non è verificabile, l'app non mostra ManicApe per non segnalare falsamente un nuovo upload. Alcuni feed potrebbero essere inaccessibili. Google/YouTube gestisce il player e la pubblicità, invariati rispetto a RC3.

**Stato**: pre-release; sorgenti testati tramite controlli JavaScript e test di freschezza. Compilazione APK e test su Android non verificati in questo ambiente.

**GitHub**: tag `v2.6.1-rc4`; release name `A.M.B. 2.6.1 RC4 – ManicApe 48h Feed`; selezionare **Pre-release**. Non pubblicare come versione stabile prima di installazione e test.
