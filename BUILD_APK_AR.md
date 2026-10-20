# إنشاء APK لتطبيق SoftNet CardForge Android

## طريقة البناء من Android Studio
1. افتح مجلد `SoftNetCardForgeAndroid` في Android Studio.
2. تأكد من تثبيت JDK 17 وAndroid SDK Platform 35 وBuild Tools 35.0.0.
3. اضغط **Sync Project with Gradle Files**.
4. اختر **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. بعد نجاح البناء ستجد الملف في:
   `app/build/outputs/apk/debug/app-debug.apk`

## بناء APK تلقائيًا عبر GitHub Actions
يتضمن المشروع ملف workflow في `.github/workflows/build-apk.yml`.
1. أنشئ مستودع GitHub وارفع محتويات مجلد المشروع إليه.
2. افتح تبويب **Actions**، واختر **Build Android APK**، ثم **Run workflow**.
3. بعد اكتمال البناء، افتح تشغيل workflow الناجح ونزّل artifact باسم `SoftNet-CardForge-Android15-debug-apk`.
4. فك ضغط الـ artifact وخذ `app-debug.apk` إلى الهاتف للتثبيت.

## تنبيه
ملف APK الناتج Debug للتجربة، وليس إصدارًا موقّعًا للنشر التجاري في Google Play. التطبيق يتصل بخادم SoftNet CardForge Web V58 الذي يعمل على الكمبيوتر؛ يجب أن يكون الجهازان على نفس الشبكة المحلية. لا تعرّض خادم HTTP إلى الإنترنت العام.
