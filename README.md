# eBulletin

To switch from the "Android" version to the "non-Android" version, simply go to 
*QuickstartPreferences* and change *isAndroid* --> *false* 
or vice versa

It is nessisary for now because this project uses Google Cloud Messaging (GCM) for its notifications
a phone that can run .apk but is not a true android phone cannot use GCM

I hope to swap this out with Socket.io later
