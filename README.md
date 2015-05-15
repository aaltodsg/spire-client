# SPIRE Mobile Client

[SPIRE = Smart Parking for Intelligent Real-Estate](https://www.hiit.fi/spire), a research project
running between 2013-2014. This repository contains the mobile client
with the following capabilities:
* Google+ account authentication
* Show a map surface with scroll and zoom capabilities. Initial
position obtained from server.
* Fetch and display parking area status information from server.
* Support live updates of parking area statuses in current map view
  with push messaging.
* Search for map destinations.
* Launch Google Maps navigation to destinations or parking areas.
* Save parking areas and destinations to a list of favorites.
* Display list of favorites with color-coded parking area statuses.
* Report parking lot status based on observations.
* Form geofences around displayed parking areas.
* Report parking area entries and exits to server.
* Inside parking areas, report activity (ON_FOOT, ON_BICYCLE,
IN_VEHICLE) to server
* Inside parking areas, report GPS coordinates to server.
* Display notification to end-user to report parking area status, as
instructed by the server.
* Send end-user feedback as email to a pre-determined address.
* Display survey notification to end-user as instructed by a
  push-message from the server

The corresponding [SPIRE server](https://github.com/aaltodsg/spire-server) code to carry out the server operations is available.

# Status of work

The project has finished and the code in the repository is made
available as-is. No development plans, no support and absolutely no
guarantee of the code being suitable for any purpose.

# Client compilation

At the time the code was created, the package could only be compiled
with IntelliJ IDEA (not AndroidStudio, not Eclipse). The situation may
be different now. Unfortunately no package manager was used, so
configuring the libraries will require some effort.

Some documentation for the variables and API keys to set is made
available in "SPIRE_MobileClientDescriptionpub.rtf" chapter 5.

# Documentation

Further information is available in the "SPIRE_MobileClientDescriptionpub.rtf"
document in the repository root.

A
[conference publication](http://www.cs.hut.fi/~mjrinne/papers/its-europe2014/Mobile%20crowdsensing%20of%20parking%20space%20using%20geofencing%20and%20activity%20recognition%20-%20ITSEur2014.pdf)
and an accompanying
[presentation](http://www.cs.hut.fi/~mjrinne/papers/its-europe2014/Presentation%20-%20Mobile%20crowdsensing%20of%20parking%20space%20using%20geofencing%20and%20activity%20recognition%20-%20ITSEur2014.pdf)
are also available.
