# Caregiver
## Building the app

The Android SDK is necessary to build the app. We recommend opening the root "Caregiver" folder in Android Studio 2.1.1 in order to maintain consistent build settings. Additionally, you *must* be using JDK 1.7.x for the project SDK in order for the backend to function properly.

The project has been tested extensively and is guaranteed to build if you follow the instructions above. We cannot guarantee results if any deviations are made. 

## Using the App

From the initial screen, you must first create a Caregiver account. Once you have entered your id/password and put down the name of your patient, you will be brought to the Caregiver dashboard. Here, you can set a daily check-in time and add medication alerts. To demonstrate functionality of the app, set your medication alerts and check-in times for a couple of minutes in the future.

Then, get out a second phone, install the APK, and sign in as a Patient by putting in the same user ID that you put down in initial set-up. (Make sure that you leave the app running on the other phone.) Once you've confirmed the patient name, you will be brought to the Patient dashboard, where you can view the medication alerts set up on the caregiver side. You can also press the HELP button to send an alert to the caregiver side. 

You can see the fall detection functionality by falling while the patient app is running (does not need to be in the foreground as long as the service is running).

On the patient side, an alarm will go off when it is time to take a medication, or time to check in. Once you have acknowledged the alert on the patient side, it will send an alert notification to the caregiver side

** Note: this app operates under the assumption of constant internet access. It will not function properly without internet access
