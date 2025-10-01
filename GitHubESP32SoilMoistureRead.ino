/* 
  ESP32 Moisture Sensor to Firebase RTDB ( Realtime Database)
  Author : Madison [MaddieMistCodes]
  Date : September 2025
  Description : This project reads soil moisture using an inverted capacitive soil sensor. It reads data from an analog pin on the ESP32.
                Moisture Data is sent every 5 seconds to a specified path on the Firebase RTDB. In this set-up, the ESP32 operates as a WiFi
                Client connected to a local router
  Dependencies : 
      _ Firebase ESP Client by Mobizt
      _ ESP32 board support via Arduino IDE

  Please note portions of this code were inspired by Joed Goh's ESP32 - Firebase project, https://www.youtube.com/watch?v=aO92B-K4TnQ&list=PLVTsfY7Kr9qjVkq8aJmTXbiiy5h41ayRL&index=6 
*/

#include <WiFi.h> 
#include <Firebase_ESP_Client.h>  // Official Firebase ESP Client library

// Helper functions for token generation and debug info
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

// Wi-Fi credentials
#define WIFI_SSID "INSERT YOUR WIFI SSID HERE"
#define WIFI_PASSWORD "INSERT YOUR WIFI PASSWORD HERE"

// Firebase project credentials
// Web API Key from Firebase Console - authenticates device FB
#define API_KEY "INSERT YOUR FB API KEY HERE"
// Points to FB RTDB endpoint
#define DATABASE_URL "INSERT YOUR FB RTDB URL HERE" 

// Firebase objects
// Handles data transactions
FirebaseData fbdo;
// Manages authentication
FirebaseAuth auth;
// Holds FB config settings
FirebaseConfig config;

// Timers and flags
// Track when data was last sent
unsigned long sendDataPrevMillis = 0;
// Indicate if anonymous sign-in is succesful
bool signUpOk = false;

// Moisture sensor variables
// Pin used on ESP32
int soilPin = 32;
// Reading value
int soilVal;
// Mapped moisture value
int moistureVal;

// Calibration values - inverse sensor
const int drySoil = 3000;
const int wetSoil = 1000;

void setup() {
  Serial.begin(115200);

  // Connect to Wi-Fi
  // Initiate wifi connection
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to WiFi");
  // While ESP32 is not connected display ...
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(300);
  }
  // Now connected - indicate to user + print IP address
  Serial.print("\nConnected with IP: ");
  Serial.println(WiFi.localIP());

  // Configure Firebase
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;

  // Sign in anonymously (email and password left blank)
  // & is to pass a reference to that variable - not a copy. Can access original object directly - saves memory.
  if (Firebase.signUp(&config, &auth, "", "")) {
    Serial.println("Anonymous sign-in successful");
    signUpOk = true;
  } else {
    Serial.printf("Sign-up failed: %s\n", config.signer.signupError.message.c_str());
  }

  // Listener for changes in FB authentication token. Tokens allow FB and ESP32 to communicate securely.
  config.token_status_callback = tokenStatusCallback;
  // Ensure FB has relevant information to run smoothly through a token change.
  // auth gets updated with token and config may update internal state.
  Firebase.begin(&config, &auth);
  // If ESP32 disconnects from WiFi - will automatically attempt to reconnect
  Firebase.reconnectWiFi(true);
}

void loop() {
  // Every 5 seconds, send moisture data to Firebase
  // Millis() returns number of milliseconds since the board was powered on
  // || sendDataPrevMillis == 0 is to ensure first loop runs - after first loop sendDataPrevMillis gains a value
  if (Firebase.ready() && signUpOk && (millis() - sendDataPrevMillis > 5000 || sendDataPrevMillis == 0)) {
    sendDataPrevMillis = millis(); //To store time when data was last sent

    // Read and map moisture value
    soilVal = analogRead(soilPin);
    moistureVal = constrain(map(soilVal, drySoil, wetSoil, 0, 100), 0, 100);

    // Send data to Firebase RTDB
    // If int moistureVal is written to path successfully - print on serial monitor
    // fbdo will handle the request and updating results
    if (Firebase.RTDB.setInt(&fbdo, "/sensor/moisture", moistureVal)) {
      Serial.println();
      Serial.printf("Moisture: %d%%\n", moistureVal);
      Serial.println("Data saved to: " + fbdo.dataPath());
    } else {
      // Else indicate to user error has occurred
      Serial.println("Firebase write failed: " + fbdo.errorReason());
    }
  }
}

