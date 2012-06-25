EncPassChanger
==============

This is an application for Android 4.0.3+, which allows to set disk encryption password different from lock screen password.
Stock Android 4 firmware doesn't allow setting different screen lock and disk encryption passwords,
and that either forces you to use weak password, or to type long secure password each time you unlock
the screen of device.
This application uses standard VDC calls to validate and set new password. I have tested it on
my own device (Samsung Galaxy Nexus). However, please backup you data before using this app!
If you forget your password, you will NEVER recover your data!

This application may also work on Android 3, however I'm not able to test this,
so I have set minimum API level to 15 (Android 4.0.3).
