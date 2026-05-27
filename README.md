# Kelime Quiz

![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Android SDK 34](https://img.shields.io/badge/Android%20SDK-34-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Room Database](https://img.shields.io/badge/Room-Database-003B57?style=for-the-badge&logo=sqlite&logoColor=white)

**Kelime Quiz**, 6 tekrar prensibine dayalı bir İngilizce kelime öğrenme Android uygulamasıdır. Kullanıcıya özel kelime havuzu, SRS tabanlı quiz sistemi, aktivite raporları, Wordle mini oyunu ve LLM destekli hikaye üretimi tek projede birleştirilmiştir.

## Kısa Özet

Uygulama, İngilizce kelimeleri sadece listeleyen bir yapı değil, tekrar ve ölçme mantığıyla öğreten bir sistem olarak tasarlandı. Her kullanıcı kendi hesabı ile giriş yapar, kendi kelimelerini yönetir ve ilerlemesi ayrı tutulur.

Projede ana hedefler:

- Kullanıcı kayıt, giriş ve şifre sıfırlama akışını sağlamak
- Kullanıcıya özel kelime havuzu oluşturmak
- Kelimeleri örnek cümle, görsel ve kategori ile desteklemek
- Quiz modülünde 6 tekrar prensibini uygulamak
- Öğrenme ilerlemesini raporlamak
- Wordle ve LLM tabanlı mini oyunlarla etkileşimi artırmak

## Özellikler

- Kullanıcı kayıt olma
- Kullanıcı giriş yapma
- Şifre sıfırlama
- Kullanıcıya özel kelime havuzu
- Kelime ekleme, silme ve detay görüntüleme
- Kelimelere örnek cümle ve görsel ekleme
- Quiz başlatma ve SRS mantığı ile tekrar
- Doğru / yanlış cevaba göre kelime seviyesini güncelleme
- Haftalık, aylık ve toplam aktivite raporu
- Kelime havuzu için dışa aktarılabilir analiz raporu
- Wordle mini oyunu
- Word Chain üzerinden LLM hikaye ve görsel üretimi
- Açık / koyu tema desteği

## Teknoloji

- **Java 17**
- **Android SDK 34**
- **Room Database**
- **Material Components**
- **Glide**
- **JUnit**
- **Bouncy Castle**

## Mimari Yapı

Proje katmanlı bir yapıya yakın tutuldu. Amaç ekran kodlarını veri mantığından ayırmak ve iş kurallarını okunabilir parçalara bölmek.

```text
kelime-quiz/
  app/src/main/java/com/samil/kelimequiz/
    data/
      local/        Room database, DAO ve entity sınıfları
      remote/       LLM istemcisi
      repository/   Veri erişimi ve iş mantığı
    domain/
      model/        Uygulama içi modeller
      service/      SRS ve rapor üretim servisleri
    ui/
      auth/         Giriş, kayıt, şifre sıfırlama
      main/         Ana ekran
      profile/      Ayarlar ve yönlendirmeler
      quiz/         Quiz ekranı
      report/       Aktivite raporu ekranı
      word/         Kelime ekleme ve kelime havuzu
      wordchain/    LLM hikaye üretimi
      wordle/       Wordle mini oyunu
    util/           Session, tema, navigation ve executor yardımcıları
```

## Modüller

### Auth

- Kullanıcı kayıt
- Giriş
- Şifre sıfırlama

### Kelime Havuzu

- Kullanıcı kelime ekleyebilir
- Her kelime için İngilizce karşılık, Türkçe anlam, kategori, görsel ve örnek cümleler tutulur
- Kelimeler öğrenilme seviyesine göre listelenebilir
- Kelime havuzu için analiz raporu üretilebilir

### Quiz

- Quiz soruları tekrar zamanı gelen kelimelerden seçilir
- Yeni kelimeler eksik soruları tamamlar
- Doğru cevapta kelime seviyesi artar
- Yanlış cevapta süreç sıfırlanır
- 6. seviyede kelime öğrenilmiş kabul edilir

### Aktivite Raporu

- Günlük, haftalık, aylık ve toplam raporlar
- Günlük toplam aktivite hesabı:
  - çözülen quiz soruları
  - tamamlanan Wordle
  - oluşturulan LLM hikayeleri
- Haftalık ve aylık raporlar aynı mantığın dönem toplamı ve ortalama değeri üzerinden hazırlanır

### Wordle

- Kullanıcının kelime havuzundan seçilen kelime ile oynanır
- Harf doğru yerdeyse yeşil, kelime içinde ama yanlış yerdeyse sarı, yoksa gri gösterilir

### Word Chain

- Kullanıcının kelime havuzundan seçilen 5 kelime ile hikaye üretilir
- Ardından hikayeye uygun görsel oluşturulur

## Veritabanı

Room tarafında temel tablolar:

- `UserEntity` - kullanıcı bilgileri
- `WordEntity` - kelime bilgileri
- `WordSampleEntity` - örnek cümleler
- `QuizProgressEntity` - öğrenilme seviyesi ve tekrar tarihi
- `QuizResultEntity` - quiz sonuçları
- `ActivityLogEntity` - aktivite kayıtları

## Ekranlar

- `LoginActivity`
- `RegisterActivity`
- `ForgotPasswordActivity`
- `MainActivity`
- `AddWordActivity`
- `WordPoolActivity`
- `QuizActivity`
- `WeeklyReportActivity`
- `ProfileActivity`
- `WordleActivity`
- `WordChainActivity`

## Kurulum

### Android Studio

1. Projeyi açın.
2. `kelime-quiz` klasörünü proje kökü olarak seçin.
3. Gradle senkronizasyonunun tamamlanmasını bekleyin.
4. Uygulamayı emulator veya gerçek cihazda çalıştırın.


## Geliştirme Notları

- Veritabanı işlemleri DAO üzerinden yürütülür
- Şifreler düz metin saklanmaz
- Tema ve oturum yönetimi yardımcı sınıflara ayrılmıştır
- Ağ ve veritabanı işlemleri ana thread dışında çalıştırılır
- Raporlar kullanıcı bazlı ilerleme mantığına göre hazırlanır

## Lisans

Bu proje dönem projesi olarak geliştirilmiştir.
