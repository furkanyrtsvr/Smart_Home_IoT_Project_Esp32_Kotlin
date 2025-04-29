from machine import Pin, PWM , ADC , SoftI2C , TouchPad
import network
import urequests
import time
import ntptime
import ujson
import machine
from machine_i2c_lcd import I2cLcd
from dht import DHT11

SSID = "YourWifiName"
PASSWORD = "YourWifiPassword"
FIREBASE_URL = "https://YourProjectid.firebaseio.com/"

led = Pin(2, Pin.OUT)

#rgb
BLUE  = PWM(Pin(27), freq=1000)
GREEN = PWM(Pin(33), freq=1000)
RED   = PWM(Pin(32), freq=1000)
relay = Pin(14, Pin.OUT)
pir   = Pin(13, Pin.IN)         # Bu pir sensoruyle rtc saate göre ya da ldr ışık şiddetine göre led yakabilirsin (role ile)
# Sicaklik - Nem sensoru
dht_sensor = DHT11(Pin(39))


# LDR ADC pin (GPIO34)
ldr = ADC(Pin(34))
ldr.atten(ADC.ATTN_11DB)        # 0-3.3V okuma
ldr.width(ADC.WIDTH_12BIT)      # 12-bit çözünürlük (0-4095 arası değer)

# Gaz sensörü ADC pin (GPIO35)
gas_sensor = ADC(Pin(35))
gas_sensor.atten(ADC.ATTN_11DB)       # 0-3.3V arası okumak için
gas_sensor.width(ADC.WIDTH_12BIT)     # 12-bit çözünürlük (0-4095 arası değer)

# Yangın sensörü ADC pin (GPIO36)
yangin_sensor = ADC(Pin(36))
yangin_sensor.atten(ADC.ATTN_11DB)  # 0-3.6V arası okuma
yangin_sensor.width(ADC.WIDTH_12BIT)  # 10-bit çözünürlük (0-1023)

# Su Baskini sensörü
su_baskini = TouchPad(Pin(32))  # GPIO 32'yi kullanıyoruz


# LCD I2C addres and dimensions tanımı
I2C_ADDR = 0x27
I2C_NUM_ROWS = 2
I2C_NUM_COLS = 16

# I2C pin tanımı
i2c = SoftI2C(sda=Pin(21), scl=Pin(22), freq=400000)

lcd = I2cLcd(i2c, I2C_ADDR, I2C_NUM_ROWS, I2C_NUM_COLS)

# Servo PWM ayarı
sg90 = PWM(Pin(23), freq=50)  # 50Hz


relay.value(1)
RED.duty(0)
GREEN.duty(0)
BLUE.duty(0)

def set_color(r, g, b):
    RED.duty(int(r / 255 * 1023))    #DAC 8bitlik
    GREEN.duty(int(g / 255 * 1023))
    BLUE.duty(int(b / 255 * 1023))

# Açıdan duty hesaplayan fonksiyon
def set_angle(angle):
    # 0 derece için duty 26, 180 derece için duty 123 olsun
    min_duty = 26
    max_duty = 123
    duty = int(min_duty + (max_duty - min_duty) * angle / 180)
    sg90.duty(duty)
    
# Wi-Fi bağlantısı kurma
def connect_wifi():
    wlan = network.WLAN(network.STA_IF)
    wlan.active(True)
    if not wlan.isconnected():
        print("WiFi'ye bağlanılıyor...")
        wlan.connect(SSID, PASSWORD)
        while not wlan.isconnected():
            time.sleep(1)
    print("WiFi Bağlandı:", wlan.ifconfig())

def read_from_firebase(path=""):
    try:
        url = FIREBASE_URL + path + ".json"
        response = urequests.get(url)

        if response.status_code == 200:
            try:
                data = response.json()
                print(f"Firebase'den okundu ({path}): {data}")
                response.close()
                return data
            except ValueError:
                print(f"Firebase'den gelen veri JSON formatında değil ({path}): {response.text}")
                response.close()
                return None
        else:
            print(f"Firebase'den veri çekme başarısız oldu ({path}). Durum Kodu: {response.status_code}")
            response.close()
            return None

    except Exception as e:
        print(f"Bağlantı hatası ({path}): {e}")
        return None


        # Hata olursa 2 saniye bekle ve yeniden dene
        time.sleep(2)

# Firebase'e veri yazma
def write_to_firebase(path, data):
    try:
        url = FIREBASE_URL + path + ".json"
        headers = {"Content-Type": "application/json"}
        response = urequests.put(url, data=ujson.dumps(data), headers=headers)
        response.close()
        print("Firebase'e yazıldı:", path, data)
    except Exception as e:
        print("Firebase yazma hatası:", e)

def measure_network_speed():
    try:
        start_time = time.ticks_ms()
        response = urequests.get("http://speedtest.tele2.net/1MB.zip") # Daha büyük bir dosya indiriliyor
        end_time = time.ticks_ms()
        if response.status_code == 200:
            content_length = int(response.headers.get('Content-Length', 0))
            duration = time.ticks_diff(end_time, start_time) / 1000  # saniye cinsinden süre
            if duration > 0 and content_length > 0:
                speed_bps = (content_length * 8) / duration # bit per second
                speed_Mbps = speed_bps / (1024 * 1024)
                print(f"Ağ hızı: {speed_Mbps:.2f} Mbps")
            else:
                print("İndirme süresi veya dosya boyutu sıfır.")
            response.close()
        else:
            print("Ağ hızı ölçümü başarısız.")
    except Exception as e:
        print(f"Ağ hızı ölçümü hatası: {e}")

def rtc_ntp_senkron():
    try:
        ntptime.settime()  # UTC zamanını al
        print("Zaman alındı!")
        
        # RTC'yi UTC+3 olarak ayarla
        rtc = machine.RTC()
        zaman = rtc.datetime()  # UTC zamanı
        saat = (zaman[4] + 3) % 24  # Saat + 3
        gun_farki = (zaman[4] + 3) // 24  # Saat taşarsa günü artır
        gun = zaman[2] + gun_farki
        
        # Yeni zaman tuple'ı
        yeni_zaman = (zaman[0], zaman[1], gun, zaman[3], saat, zaman[5], zaman[6], zaman[7])
        rtc.datetime(yeni_zaman)  # RTC'yi güncelle
        print("RTC Türkiye Saatine (UTC+3) ayarlandı:", yeni_zaman)
        
    except Exception as e:
        print("Zaman alınamadı veya RTC ayarlanamadı:", e)


# Gün adları listesi (RTC haftanın günü: 0=Pazartesi, ..., 6=Pazar)
gun_isimleri = [
    "pazartesi", "sali", "carsamba",
    "persembe", "cuma", "cumartesi", "pazar"
]


def kontrol_ve_led(gun_durumu,zaman_durumu,relay_station):
    zaman = rtc.datetime()
    gun_index = zaman[3]  # RTC'den haftanın günü (0 = Pazartesi)
    gun_adi = gun_isimleri[gun_index]  # Örneğin: "pazartesi"
    simdiki_saat = zaman[4]
    simdiki_dakika = zaman[5]

    print("Bugün:", gun_adi, "Saat:", simdiki_saat, "Dakika:", simdiki_dakika)

    if gun_durumu and zaman_durumu is not None:
        if gun_adi in gun_durumu and gun_durumu[gun_adi] == 1:
            try:
                bas_saat = int(zaman_durumu.get("startTime_hour1"))
                bas_dakika = int(zaman_durumu.get("startTime_minute1"))
                bit_saat = int(zaman_durumu.get("endTime_hour2"))
                bit_dakika = int(zaman_durumu.get("endTime_minute2"))

                bas_zaman = bas_saat * 60 + bas_dakika
                bit_zaman = bit_saat * 60 + bit_dakika
                simdi_zaman = simdiki_saat * 60 + simdiki_dakika

                if bas_zaman <= simdi_zaman <= bit_zaman:
                    relay.value(relay_station)
                    write_to_firebase("led_status",relay_station)
                    print("Zaman aralığında.")
                    
                else:
                    relay.value(1 - relay_station)
                    write_to_firebase("led_status",1 - relay_station)
                    print("Zaman aralığı dışında.")
                    
            except Exception as e:
                print("Saat verileri okunamadı:", e)
                write_to_firebase("led_status",1 - relay_station)
                relay.off()
        else:
            relay.value(1 - relay_station)
            print("Bugün seçili değil.")
                    

    else:
        print("Firebase'den geçerli gün/zaman durumu alınamadı.")
        
connect_wifi()

#lcd.putstr("It's working :)")

time.sleep(1)

#lcd.clear()

# NTP ile zamanı al ve RTC'yi güncelle
rtc_ntp_senkron()
# RTC'den zamanı oku
rtc = machine.RTC()

measure_network_speed()

pir_last_detect_hour = "first"

while True:
    try:
        # zaman = rtc.datetime()
        # print("Türkiye Saati (UTC+3):", zaman)
        # print("Tarih: {:02d}/{:02d}/{}".format(zaman[2], zaman[1], zaman[0]))
        # print("Saat: {:02d}:{:02d}:{:02d}".format(zaman[4], zaman[5], zaman[6]))
        
        lcd_zaman = rtc.datetime()
        saat   = lcd_zaman[4]  
        dakika = lcd_zaman[5]
        lcd.clear()
        ekran = "%02d:%02d" % (saat, dakika)
        lcd.putstr(ekran)
        
        data = read_from_firebase()
        
        dht_sensor.measure()  # Sensörden ölçüm al
        temp = dht_sensor.temperature()  # Sıcaklık (Celsius)
        hum = dht_sensor.humidity()      # Nem (%)
        lcd.move_to(0, 1)
        lcd.putstr("temp: %02d    hum:  %03d" % (temp, hum))
        
        if data is not None:
            data_day_time = data.get("selectedDays") 
            data_relay_status = data_day_time.get("relay_status")
            data_relay_status_manuel_or_date = data.get("relay_manuel_or_date")
            
            data_day  = data_day_time.get("day") 
            data_time = data_day_time.get("time") 
            
            data_sensor   = data.get("sensor") 
            data_fire = data_sensor.get("fire_sensor")
            data_gas = data_sensor.get("gas_sensor")
            data_water = data_sensor.get("water_sensor")
            data_pir = data_sensor.get("pir_sensor")
            
            
            data_sensor_just_send   = data.get("sensor_just_send")
            
            data_servo = data_sensor_just_send.get("servo")
            data_lcd = data_sensor_just_send.get("lcd")
            data_lcd_manuel = data_sensor_just_send.get("lcd_manuel")
            
            data_rgb = data.get("rgb")
            
            
            
            led_status = data.get("led_status")
            relay_status = data.get("relay_status_manuel")
            r_value = data_rgb.get("r")
            g_value = data_rgb.get("g")
            b_value = data_rgb.get("b")
            set_color(r_value, g_value, b_value)
            
            if led_status == 1:
                led.value(1)
            else:
                led.value(0)
           
            if data_relay_status_manuel_or_date == 0:
                if data_day_time:
                    kontrol_ve_led(data_day,data_time,data_relay_status)

            elif data_relay_status_manuel_or_date == 1:
                if relay_status == 1:
                    relay.value(1)
                else:
                    relay.value(0)

           
            set_angle(data_servo)
            
            ldr_val = ldr.read()
            
            if pir.value() == 1:
               pir_last_detect_hour = "%02d:%02d" % (saat, dakika)

            sensor_data = {
               
                "pir_sensor": pir.value(),
                "pir_last_hour": pir_last_detect_hour,
                "gas_sensor": 1 if gas_sensor.read() >= 1200 else 0,
                "fire_sensor": 1 if yangin_sensor.read() <= 20 else 0,
                "water_sensor": 1 if su_baskini.read() <= 200 else 0,
                "ldr_value": ldr_val,
                "dht_temperature": 2,
                "dht_humidity": 1,
            }

            write_to_firebase("sensor", sensor_data)
                
            if data_lcd_manuel == 1:
                lcd.clear()
                lcd.putstr(data_lcd)
            else:
                lcd.clear()
                ekran = "      %02d:%02d    " % (saat, dakika)
                lcd.putstr(ekran)
                lcd.move_to(0, 1)
                lcd.putstr("temp: %02d    hum:  %03d" % (temp, hum))
                
            
    except Exception as e:
        print("Hata oluştu:", e)

    time.sleep(1)


