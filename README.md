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

## New in Show and Tell 2
-  Transitioned to phone authentication instead of email-based account authentication (as some email providers filtered the verification email as spam). This also reduces onboarding friction, as there is now only one button ("SIGN IN") and the application automatically detects if an account exists for the phone or not.
-  Utilize Firestore to store cloud-based data and also retrieve live updates inside the app utilizing Coroutines, LiveDatas, and Flows.
-  Mapping capabilities for rendering washroom landmarks on the map for users to navigate to.
-  Integrate an admin mode that allows the university to view advanced information about washrooms, effectively resulting in a washroom management system (WIP)


## Notes
Several important points to consider:
- Documentation is currently a WIP (Focus for the first show and tell was on implementing base logic. Once the code is refined, it will then be fully documented)
- A toolbar layout will soon be added to change between fragment "tabs", thus eliminating the current floating action button workflow.
- A profile page will be added that allows users to view information about their account (e.g. number of reviews).
- Filtering utilizing cloud data is currently a WIP
- UI will be polished to resemble a sleek social app.
- Potentially more components to be added to user reviews (e.g. soap, towel availability)
