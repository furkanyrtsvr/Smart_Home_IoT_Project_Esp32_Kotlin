1.Google firebase üzerinden projenizi oluşturunuz

2."\GitHub\Smart_Home_IoT_Project_Esp32_Kotlin\Kotlin_Android_Studio\Smart_Home\app\google-services.json" Bu dosyayı kendi firebase bilgilerinize göre düzenleyiniz YA DA daha kolay bir yöntem olan Android Studio üzerinden projenizi firebase e baglanıyız

3.Anlık konumunuzdaki Sıcaklık Bilgileri için:
"\GitHub\Smart_Home_IoT_Project_Esp32_Kotlin\Kotlin_Android_Studio\Smart_Home\app\src\main\java\com\example\smart_home\ui\home\HomeFragment.kt"   Bu dosyanın 106. satırında openweathermap.org sitesinden almış olduğunuz kendi tokenınızı kullanınız.

4."C\GitHub\Smart_Home_IoT_Project_Esp32_Kotlin\Esp32_MicroPython\Smart_Home_main.py" Dosyasında
SSID = "YourWifiName"
PASSWORD = "YourWifiPassword"
FIREBASE_URL = "https://YourProjectid.firebaseio.com/"   Ag adı/Ag sifresi/firebase proje id  niz gibi bilgiler ile düzenleyiniz.

5.Gerekli kütüphaneleri micropython üzerinden ekleyiniz. (Thonny ide)

6.Firebase json dosyasini firebase real time database e importlayabilirsiniz.


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


1.Create your project on Google Firebase.

2.Edit the file located at \GitHub\Smart_Home_IoT_Project_Esp32_Kotlin\Kotlin_Android_Studio\Smart_Home\app\google-services.json according to your own Firebase project information, OR alternatively, connect your project to Firebase directly through Android Studio for an easier method.

3.For real-time temperature data based on your current location:
In the file \GitHub\Smart_Home_IoT_Project_Esp32_Kotlin\Kotlin_Android_Studio\Smart_Home\app\src\main\java\com\example\smart_home\ui\home\HomeFragment.kt, update line 106 with your own API token obtained from openweathermap.org.

4.In the file C\GitHub\Smart_Home_IoT_Project_Esp32_Kotlin\Esp32_MicroPython\Smart_Home_main.py, update the following lines with your Wi-Fi name, Wi-Fi password, and Firebase project URL:

SSID = "YourWifiName"
PASSWORD = "YourWifiPassword"
FIREBASE_URL = "https://YourProjectid.firebaseio.com/"

Edit it with your WIFI name, WIFI password, and Firebase project ID.

5.Install the required MicroPython libraries (Thonny IDE).

6.You can import Firebase json file to Firebase real time database.

