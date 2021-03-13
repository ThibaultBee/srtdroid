[![](https://jitpack.io/v/ThibaultBee/srtdroid.svg)](https://jitpack.io/#ThibaultBee/srtdroid)

# srtdroid: Secure Reliable Transport (SRT) Protocol for Android

Low level API for SRT library on Android. SRT is an open source transport technology that optimizes streaming performance across unpredictable networks. More information on https://github.com/Haivision/srt.

srtdroid is a binder built on [SRT](https://github.com/Haivision/srt). It is a not a new implementation of SRT protocol.

## Setup

Get srtdroid lastest artifacts on [jitpack.io](https://jitpack.io/#ThibaultBee/srtdroid)

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.ThibaultBee:srtdroid:1.0.0'
}
```


## Samples

To test srtdroid, you will need 2 Android devices: one client and one server.
On the server side, configure listening IP to "0.0.0.0" or to server ethernet IP.
On the client side, configure connecting IP to server IP.
The port must be the same value for both.

### Examples

In examples folder.
Examples are the srtdroid counterpart of [SRT examples folder](https://github.com/Haivision/srt/tree/master/examples).
You will find both client and server examples.

### Chat

In chat folder.
Chat is an SRT utility to send and receive messages. It provides both a client and a server.

## API

As srtdroid is a simple wrapper, it provides a limited [API documentation](https://thibaultbee.github.io/srtdroid/dokka/lib). For an extensive SRT API documentation refers to the official [SRT API documentation](https://github.com/Haivision/srt/blob/master/docs/API.md).
You must create a [Srt object](https://github.com/ThibaultBee/srtdroid/blob/master/lib/src/main/java/com/github/thibaultbee/srtdroid/Srt.kt) before calling any other API. It will load SRT, ssl and crypto libraries.

### Read/write

Srt `send`, `sendMsg` and `sendMsg` are called `send()` in srtdroid. Alternatively, you can use the `OutputStream` API.
Srt `recv`, `recvdMsg` are called `recv()` in srtdroid. Alternatively, you can use the `InputStream` API.

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

## Licence

    Copyright 2021 Thibault Beyou

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
