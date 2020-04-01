# Android SRT Wrapper

Low level API for SRT library on Android. SRT is an open source transport technology that optimizes streaming performance across unpredictable networks. More information on https://github.com/Haivision/srt.

## Repository

Get SRT Wrapper lastest artifacts on [jitpack.io](https://jitpack.io/#ThibaultBee/SRTWrapper)

## Sample

Check the SRT chat application in chat folder.
You need 2 Android devices: one client and one server.
On the server side, configure listening IP to "0.0.0.0"
On the client side, configure connecting IP to server IP.
The port must be set to the same value for both.

## API

As Android SRT Wrapper is a wrapper (obvioulsy) for API documentation refers to [SRT API documentation](https://github.com/Haivision/srt/blob/master/docs/API.md).
You must create a [Srt object](https://github.com/ThibaultBee/SRTWrapper/blob/master/lib/src/main/java/com/github/thibaultbee/srtwrapper/Srt.kt) before calling any other API. It will load SRT, ssl and crypto libraries.

## Permission

You need to add the INTERNET permission in your AndroidManifest.xml:
```xml
	<uses-permission android:name="android.permission.INTERNET" />
```

## Build

As it downloads and builds SRT, OpenSSK (ssl and crypto) libraries, first compilation will take a while.
Android SRT Wrapper builds on a Linux. As OpenSSL requires GNU make, it might be tricky to build this project on Windows.
