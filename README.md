# Positive Birmingham
## An app to visit Buildings of Architectural interest in Birmingham.

### Positive Birmingham was developed in the Android Studio integrated development environment (IDE). Android Studio is Google’s recommended development environment for building apps which use Maps and is designed specifically for Android development.
To be able to run Positive Birmingham from your personal computer you will have to download Android Studio, and either have an android device to run the app on or download an emulator to emulate an android phone on the computer. 

To run this applicaton you will need to make an account on Google’s Cloud Platform Console (https://console.cloud.google.com/), to create API key's personal to your computer. API keys are needed to use the Maps SDK for Android to track API requests associated with your project for usage and billing. 
Create one key with Android restrictions - this will be used for Maps.
Create another key with your servers IP as a restriction - this will be used for directions and places API due to them not supporting API Keys with Android restrictions.

Go to app/res/values/google_maps_api_example.xml and change the file name to 'google_maps_api.xml'. Open this file and copy and paste your API keys into the google_maps_key, and the google_directions_key respectively.

After opening up the project in Android Studio, press the green run button, select a device and the app should run.

## Support

If you have any further issues please refer to the following website: https://developers.google.com/maps/documentation/android-sdk/signup

