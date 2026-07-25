# Downloader App — نسخة MVP

كود أولي (Kotlin + Jetpack Compose) لمشروع مدير التنزيلات والمتصفح، مبني بنفس تقسيم الوثيقة
النهائية: `domain` → `data` → `ui`، عبر Repository واحد بين UseCases وData.

## كيف تفتحه

1. افتح المجلد كمشروع Android Studio (Giraffe أو أحدث).
2. Gradle sync سيجلب التبعيات تلقائيًا (Compose, Room, Navigation, Coroutines).
3. شغّله على جهاز/محاكاة بـ minSdk 26 فأعلى.

## قرارات تبسيط متعمدة عن الوثيقة الأصلية (يجب معرفتها)

هذه الفجوات مقصودة لتسريع MVP، بناءً على النقد السابق، وليست نسيانًا:

1. **محرك تنزيل واحد فقط** (`SystemDownloadEngine` فوق `DownloadManager` النظامي).
   `ManagedDownloadEngine` و`DownloadEngineSelector` غير موجودين بعد؛ الواجهة `DownloadEngine`
   جاهزة لإضافتهما لاحقًا دون كسر `DownloadRepositoryImpl`.

2. **لا يوجد LinkResolver / MediaExtractor حقيقي.** الرابط يُستخدم كما هو (`resolvedUrl == sourceUrl`)،
   واسم الملف/النوع يُستنتج من الرابط مباشرة عبر `FileUtils` (مكان مؤقت في `util/`).
   هذا يعني: **لا يوجد دعم لصفحات فيديو (YouTube ونحوه) في هذه النسخة**، فقط روابط ملفات مباشرة.

3. **لا يوجد DownloadScheduler فعلي.** لا تحقق من Wi-Fi فقط، ولا حد أقصى للتنزيلات المتزامنة،
   ولا تأجيل عند بطارية منخفضة. التنزيل يبدأ فورًا عبر DownloadManager.

4. **لا يوجد Pause/Resume حقيقي.** `DownloadManager` لا يدعم إيقافًا مؤقتًا يتحكم به المستخدم مباشرة؛
   حالة `PAUSED` موجودة في الموديل لكن غير مفعّلة فعليًا في هذه النسخة.

5. **FileScanner مبسط جدًا.** لا صور مصغرة حقيقية، فقط أيقونة حسب `mimeType` في شاشة المكتبة.

6. **Scoped Storage:** الكود يعتمد على `setDestinationInExternalPublicDir` عبر DownloadManager،
   وهو متوافق مع Scoped Storage افتراضيًا لأن DownloadManager نفسه يتولى الأذونات على Android 10+.
   لم تُختبر هذه النقطة فعليًا على جهاز حقيقي بعد — يُنصح بالتحقق أول تشغيل.

7. **DI يدوي بسيط (`AppContainer`) بدل Hilt.** قرار مقصود لتقليل التعقيد في MVP، مع الحفاظ على
   نفس شكل الاعتماد على الواجهات، بحيث يسهل استبداله بـ Hilt لاحقًا دون تغيير domain/ui.

8. **لا توجد شاشة إعدادات** في هذه النسخة (راجع `ui/settings/SETTINGS_RESERVED.md`).

9. **لم يُختبر البناء فعليًا** (لا تتوفر بيئة Android SDK/Gradle كاملة في بيئة التوليد هذه)،
   لذلك قد تحتاج لتصحيحات بسيطة عند أول `gradle build` — الأخطاء الأرجح هي فروقات نسخ مكتبات
   Compose BOM / Room / KSP لو غيّرتها.

## نقاط تستحق انتباهك أول تشغيل

- تأكد أن نسخة Android Studio تدعم Kotlin 1.9.24 و KSP المطابق.
- إذا فشل تنزيل فعلي، راجع Logcat لفلتر `DownloadManager` لمعرفة `COLUMN_REASON`.
- عند اختبار المشاركة (Share)، جرّب مشاركة رابط من متصفح آخر أو من تطبيق ملاحظات نصي.

## الخطوات التالية المقترحة (حسب خطة الوثيقة)

راجع "المرحلة الثانية" في الوثيقة: LinkResolver الحقيقي، اختيار الجودة، تحسين الاستئناف،
صور مصغرة فعلية، بحث داخل المكتبة.
