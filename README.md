[![](https://jitpack.io/v/ThibaultBee/srtdroid.svg)](https://jitpack.io/#ThibaultBee/srtdroid)

# Android SRT Wrapper

Low level API for SRT library on Android. SRT is an open source transport technology that optimizes streaming performance across unpredictable networks. More information on https://github.com/Haivision/srt.

## Setup

Get SRT Wrapper lastest artifacts on [jitpack.io](https://jitpack.io/#ThibaultBee/srtdroid)

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.ThibaultBee:srtdroid:0.8.2'
}
```


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
You must create a [Srt object](https://github.com/ThibaultBee/srtdroid/blob/master/lib/src/main/java/com/github/thibaultbee/srtdroid/Srt.kt) before calling any other API. It will load SRT, ssl and crypto libraries.

### Read/write

Srt send, sendMsg and sendMsg are called send(). Alternatively, you can use OutputStream.
Srt recv, recvdMsg are called recv(). Alternatively, you can use InputStream.

## Permissions

You need to add the INTERNET permission in your AndroidManifest.xml:
```xml
    <uses-permission android:name="android.permission.INTERNET" />
```

To use, sendFile and recvFile, you might also add READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE
```xml
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
For Android > 6.0, you have to request permissions at runtime.

## Build

As it downloads and builds SRT, OpenSSL (ssl and crypto) libraries, first compilation will take a while.
Android SRT Wrapper is designed to build on a Linux platform. As you requires `make`, you need to install:
```bash
sudo apt-get install build-essential
```

As OpenSSL is really tricky to cross-compile on Windows, you can't build on Windows.
