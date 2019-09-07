## CGM Band

Simple android app which can read CGM values from notifications of the Dexcom G6 app and generate
new notifications based on how the value changes (currently it creates notifications if the value
changes more than 10% compared to the last notification created).

The app can be used together with Mi Fit to display CGM values as notifications on the Mi Band.
Android notification settings should be capable of hiding the notifications of this app from most
parts of the system, while Mi Fit can forward them.

# Suggested improvements

- Improve support for different version of the Dexcom app (currently only matches one package name and also parsing the notification with the current reading is only tested on one phone/android version)
- Support more configuration options in the UI (currently it only let's you choose "silent hours" and start off the service)
- ...

So far this is really just a stub fitting my personal needs with getting CGM notifications on my Mi Band in a way I find them useful.
