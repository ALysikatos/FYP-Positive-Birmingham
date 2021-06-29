# Positive Birmingham
### An app to visit Buildings of Architectural interest in Birmingham.

#### Positive Birmingham was developed in the Android Studio integrated development environment (IDE). Android Studio is Google’s recommended development environment for building apps which use Maps and is designed specifically for Android development.


## Prerequisite before running the app

1. To be able to run Positive Birmingham from your personal computer you will have to download Android Studio.
2. Have an android device in developer and usb debugging mode to run the app on, or download an emulator to emulate an android phone on the computer.
3. To run this applicaton you will need to make an account on Google’s Cloud Platform Console (https://console.cloud.google.com/), to create API key's personal to your computer. API keys are needed to use the Maps SDK for Android to track API requests associated with your project for usage and billing. 
  1. Create one key with Android restrictions - this will be used for Maps.
  2. Create another key with your servers IP as a restriction - this will be used for directions and places API due to them not supporting API Keys with Android restrictions.
  3. Enable the Directions API, Places API, Maps SDK for Android and the Geocoding API.
4. Go to app/res/values/google_maps_api_example.xml and change the file name to 'google_maps_api.xml'. Open this file and copy and paste your API keys into the google_maps_key, and the google_directions_key respectively.

After opening up the project in Android Studio, press the green run button, select a device and the app should run.

## Support

If you have any further issues please refer to the following website: https://developers.google.com/maps/documentation/android-sdk/signup

