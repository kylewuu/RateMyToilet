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
- Sign in screens has orientation locked to vertical for aesthetics, but the rest of the app can be freely rotated.


## Pitches And Presentation Videos
- [Original Pitch](https://www.youtube.com/watch?v=8LEHrpURMUA)
- [Show and Tell 1](https://www.youtube.com/watch?v=ig3SUulYDys)
- [Show and Tell 2](https://www.youtube.com/watch?v=bEes_D9cRB4)
- [Figma UI Prototype](https://www.figma.com/file/LH57Gz221c4b5S6shuGYpm/Final-Project?node-id=0%3A1&t=vYoF0P7ErklMTadW-0)
- [Final Presentation]()

## Team Effort Breakdown
- Warren - Washroom Map, Washroom Details & Reviews
- Wilson - Washroom List & Adding a New Location to the Map
- Kyle - Firebase Database for Locations & Reviews, Admin Interfaces & Figma UI Prototype
- Nirvon - Firestore Authentication, Profile Page & Tab Layout
- Kyle & Wilson - Diagrams, Slides & Presentations

## Diagrams
MVVM Diagram 

<img src = "https://github.com/kylewuu/RateMyToilet/blob/Wilson-Wu1-patch-5/diagrams/MVVMDiagram.png" width=50% height=50%>

Thread Diagram 

<img src = "https://github.com/kylewuu/RateMyToilet/blob/Wilson-Wu1-patch-5/diagrams/threadDiagram.png" width=50% height=50%>


## References
Please note that other references are also made at the top of the files they were referenced in.
- https://stackoverflow.com/questions/57306302/how-to-detect-if-user-turned-off-location-in-settings
- https://stackoverflow.com/questions/17945498/how-set-listview-not-clickable
- https://stackoverflow.com/questions/70209573/sorting-arraylist-and-returning-an-arraylist-not-a-list-kotlin
- https://stackoverflow.com/questions/21422294/how-to-add-button-in-header
- https://www.android--code.com/2020/03/android-kotlin-listview-add-item.html
- https://stackoverflow.com/questions/14666106/inserting-a-textview-in-the-middle-of-a-imageview-android
- https://www.javatpoint.com/android-custom-listview
- https://stackoverflow.com/questions/35648913/how-to-set-menu-to-toolbar-in-android
- https://stackoverflow.com/questions/9884202/custom-circle-button
- https://stackoverflow.com/questions/65879622/using-kotlin-coroutines-to-update-my-textview-crashes-it
- https://stackoverflow.com/questions/3646415/how-to-create-edittext-with-rounded-corners
- https://stackoverflow.com/questions/8743120/how-to-grey-out-a-button
- https://www.geeksforgeeks.org/ratingbar-in-kotlin/
- https://www.geeksforgeeks.org/cardview-in-android-with-example/#:~:text=CardView%20is%20a%20new%20widget,look%20to%20the%20UI%20design
- https://stackoverflow.com/questions/23925907/slidedown-and-slideup-layout-with-animation
- https://stackoverflow.com/questions/19210187/how-to-create-an-interface-to-get-info-from-a-fragment-to-an-android-activity
- https://medium.com/@tonyshkurenko/work-with-clustermanager-bdf3d70fb0fd
