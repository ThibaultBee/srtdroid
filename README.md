# Android SRT Wrapper

Low level API for SRT library on Android. SRT is an open source transport technology that optimizes streaming performance across unpredictable networks. More information on https://github.com/Haivision/srt.

## API
As Android SRT Wrapper is a wrapper (obvioulsy) for API documentation refers to [SRT API documentation](https://github.com/Haivision/srt/blob/master/docs/API.md).
You must create a [Srt object](https://github.com/ThibaultBee/SRTWrapper/blob/master/lib/src/main/java/com/github/thibaultbee/srtwrapper/Srt.kt) before calling any other API. It will load SRT, ssl and crypto libraries.
You need to add the INTERNET permission in your AndroidManifest.xml:
	<uses-permission android:name="android.permission.INTERNET" />

## Build step
As it downloads and builds SRT, ssl and crypto libraries, first compilation will take a while.
