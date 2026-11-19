# A.M.B. v2.5.2 patch manifest

Base: **A.M.B. 2.5.1** (not 2.5). Update these paths in the root of your existing Android repository:

- `app/build.gradle` — versionCode 252, versionName 2.5.2
- `app/src/main/java/com/apexmotoriblaj/amb/ArticleActivity.java` — native WebView reader and manual consent
- `app/src/main/java/com/apexmotoriblaj/amb/AdBlock.java` — conservative domain filtering without cookie-dialog injection
- `app/src/main/assets/index.html` — 2.5.2 app version in settings/UI
- `tools/verify_project.py` — version assertion
- `tools/test_reader_252.py` — reader consent + scroll regression tests (desktop simulation)
- `GUIDA_PATCH_2_5_2_IT_EN.md`
- `RELEASE_2_5_2_IT_EN.md`
- `PATCH_MANIFEST_2_5_2.md`

Static and browser fixture checks have been run. Android Gradle APK build and device integration are not verified here.
