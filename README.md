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

## Notes
Several important points to consider:
- The app currently does not send a verification email since several email providers, including SFU, have blocked outgoing emails from the Firebase domain. As such, the app will utilize phone authentication instead, as it reduces on-boarding friction and the potential for spam/bot accounts. 
- The application of the selected filter to map landmarks is currently a WIP
- While list views are used for now, the app will transition to recycler views for pages with unbounded (and potentially high number of) list items.
- App logic for retrieving and persisting cloud data is currently a WIP
- Documentation is currently a WIP (Focus for the first show and tell was on implementing base logic. Once the code is refined, it will then be fully documented)

