# ESP32-to-FireBase-Soil-Moisture-Sensor
This is an IoT project that incorporates electronic devices, real-time databases, networking and coding. In my project I use an ESP32 as my microcontroller to detect the soil moisture of my plant. This data is then transmitted over my home router to a Firebase RTDB. Finally, my kotlin app reads the data from the RTDB.

## 📌Features
• Wifi Connectivity - ESP32 is used for internet access
• Sensor data collection - reading in moisture levels from capacative moisture sensor.
• Data uploads to a RTDB - Pushes sensor readings to RTDB and handles Wi-Fi reconnections/Firebase disconnects
• Data Security - Uses Firebase Authenticatin (API Key)
• Data Retrieval - Kotlin app pulls readings from firebase and displays on the UI.

## 📌Tech Stack
### Device (ESP32)
• ESP32 microcontroller
• Arduino framework (C/C++)
• Firebase-ESP-Client library
• Wi-Fi + sensor libraries

### Cloud (Backend)
• Firebase Realtime Database (RTDB)
• Firebase Authentication (optional)

### Mobile App (Frontend)
• Android (Kotlin)
• Firebase Android SDK (RTDB, Auth)

### Tools
• Arduino IDE
• Android Studio

## 📌Tips on Getting Started
### ESP32 Set-Up
• I got my ESP32's from the following link - https://www.amazon.ie/Diymore-Breakout-Development-Bluetooth-Terminal/dp/B0CLD28SHQ
• The above ESP32's come with a breakout board which allows for neater and easier wiring.
• For my soil moisture sensor, I used a capacitive soil moisture sensor which also had inverted values. It is best to test your unique soil moisture sensors and figure out it's max and min values through the Serial Monitor. For this, I simple got the reading when my sensor was 100% dry for the min, and the reading for when placed in water/extremely wet soil for the max value. 
• Some sensors, like mine - can be inverted. Meaning the max will be a low value and the min will be a high value.

<img width="450" height="100" alt="image" src="https://github.com/user-attachments/assets/51d340e7-7864-4030-a82e-8530f1467006" />

### Google Firebase
• I made my RTDB for free at the following link - https://firebase.google.com/
• For the ESP32 code you will need API Key and the Database URL from the RTDB you create. Also, this project enables annonymous sign-in to allow the user to retrieve data from the Firebase. 
• Once everything is set up, you should be able to veiw the readings on the FireBase site.

<img width="300" height="150" alt="image" src="https://github.com/user-attachments/assets/34eacf77-d463-4eaf-8cf3-1347d207e623" />

### Anroid Studio 
Currently, the minimum soil moisture allowed is hard coded into the kotlin code on Android Studio. If you would like to adjust the min value, change the Moisture_Threshold vaariable.

<img width="500" height="70" alt="image" src="https://github.com/user-attachments/assets/a03d6ff0-45f2-41c0-9d6a-f9c085f6ab2b" />


## 📌Future Improvemnts
To improve this project further I plan to focus on the user end. A few ideas are:
• Not hard coding the minimum soil moisture value, and allowing the user to change this by using the GUI.
• Adding an automated system. Once notified of low-moisture the user can press a button to water the plant.
• Add another activity to show plant watering stats.
• Improve the .xml file to make a more attractive user interface.
