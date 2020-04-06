# Android SRT Wrapper

Low level API for SRT library on Android. SRT is an open source transport technology that optimizes streaming performance across unpredictable networks. More information on https://github.com/Haivision/srt.

## Repository

Get SRT Wrapper lastest artifacts on [jitpack.io](https://jitpack.io/#ThibaultBee/SRTWrapper)

## Samples

To test Android SR Wrapper, you will need 2 Android devices: one client and one server.
On the server side, configure listening IP to "0.0.0.0" or to server ethernet IP.
On the client side, configure connecting IP to server IP.
The port must be the same value for both.

### Examples

In examples folder.
Examples are the SRT Android Wrapper counterpart of [SRT examples folder](https://github.com/Haivision/srt/tree/master/examples).
You will find both client and server examples.

### Chat

In chat folder.
Chat is an SRT utility to send and receive messages. It provides both a client and a server.

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
