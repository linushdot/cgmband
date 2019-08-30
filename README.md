## CGM Band

Simple android app which can read CGM values from notifications of the Dexcom G6 app and generate
new notifications based on how the value changes (currently it creates notifications if the value
changes more than 10% compared to the last notification created).

The app can be used together with Mi Fit to display CGM values as notifications on the Mi Band.
Android notification settings should be capable of hiding the notifications of this app from most
parts of the system, while Mi Fit can forward them.