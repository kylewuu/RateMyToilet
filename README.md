# RateMyToilet
## Team 11:
- Kyle Wu
- Wilson Wu
- Yang Han
- Nirvon Shoa

## Overview
Team 11 has created a location-based mobile application for crowdsourced reviews on university washrooms. RateMyToilet is cloud-based and utilizes Firebase for data storage and user authentication.

## Features
Functionality the team is currently working on include:
- Precise location tracking via LocationManager
- User account registration with Firebase 
- Landmark (washroom) filtering via a pop-up dialog fragment with criteria options
- Compiled user reviews via a list view
- Cloud-based storage of user accounts, reviews, and landmark locations 

## Installation Notes
Due to the Firebase SDK setup and configurations for Firebase Auth, installing the app from the Android Studio project directly onto a phone requires adding in the the SHA certificates for that specific Android Studio. Therefore, installing the application from Android Studio will not allow the user to successfully sign in to the app. 

As an alternative, please use the attached APK. Signing in when the app is installed from the attached APK will work as intended, and there should not be any errors.

If running RateMyToilet from Android Studio is necessary, please comment out the `startActivity(intent)` line in WashroomMapFragment.kt. It should look like this:

```
    private fun loadLaunchScreen() {
        val intent = Intent(activity, LaunchActivity::class.java)
    //        startActivity(intent)
    }
```

This is ***not*** the intended method of running the app because the sign up page will be disabled and the map will appear without any users being logged in. There ***will*** be features that crash the app since there is no user present. Please use the APK if possible and only disable the sign in screen if running in Android Studio is absolutely necessary.

## User Logins
To avoid having your phone number stored into our Firebase, please use the follow test numbers to login:

Admin: 
Number: 604 444 5555
Verification code (same every time): 123456

Normal user:
Number 778 444 5555
Verification code (same every time): 123456

Using an actual phone number also works and you will receive a login code. If you want to try it out just note that your number will be stored in our Firebase.

## Notes
- Please note that fetching items from Firebase can take awhile, especially on a slow connection. The loading speed may have an impact on all 3 pages (Map, List, My Profile). 
- Applying the filter for the map or list will require the app to fetch data from the cloud. Thus, please wait 5-10 seconds to see your updates.
