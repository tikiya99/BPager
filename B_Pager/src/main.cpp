#include <Arduino.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <BluetoothSerial.h>

#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1
#define OLED_ADDR 0x3C

Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);
BluetoothSerial SerialBT;

String currentMsg = "";
String previousMsg = "";

void setup() {
  Serial.begin(115200);

  SerialBT.begin("BPager");
  Serial.println("The device started, now you can pair it with bluetooth!");

  if (!display.begin(SSD1306_SWITCHCAPVCC, OLED_ADDR)) {
    Serial.println(F("SSD1306 allocation failed"));
    for (;;);
  }

  display.clearDisplay();
  display.setTextSize(2);
  display.setTextColor(SSD1306_WHITE);
  display.setCursor(0, 0);
  display.println("BPager");
  display.setTextSize(1);
  display.println("Ready to Pair");
  display.display();
}

void loop() {
  if (SerialBT.available()) {
    previousMsg = currentMsg;

    currentMsg = SerialBT.readStringUntil('\n');
    currentMsg.trim();

    Serial.print("Received: ");
    Serial.println(currentMsg);

    display.clearDisplay();
    display.setTextSize(1);
    display.setCursor(0, 0);
    display.println(currentMsg);
    display.setTextSize(1);
    display.println(previousMsg);

    display.display();
  }
}
