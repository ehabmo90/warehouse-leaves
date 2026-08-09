# استخراج APK من GitHub

## 1) ارفع المشروع إلى GitHub

- أنشئ Repository جديدًا.
- ارفع محتويات هذا المجلد كما هي.
- لا ترفع ملف `.env` إذا كان موجودًا على جهازك؛ المشروع يحتوي على `.env.example` ويمكن للـ workflow استخدامه.

## 2) شغّل البناء

افتح: **GitHub → Actions → Build APK → Run workflow**.

بعد نجاح العملية: **Artifacts → leaves-debug-apk** ثم نزّل الملف وفك الضغط لتحصل على `app-debug.apk`.

## 3) ملاحظة مهمة

هذه نسخة Debug للاختبار والتثبيت المباشر. للنشر على Google Play نحتاج إعداد Release signing/Keystore منفصل.
