[![](https://jitpack.io/v/ThibaultBee/srtdroid.svg)](https://jitpack.io/#ThibaultBee/srtdroid)

# srtdroid: Secure Reliable Transport (SRT) Protocol for Android

Low level API for SRT library on Android. SRT is an open source transport technology that optimizes streaming performance across unpredictable networks. More information on [SRT](https://github.com/Haivision/srt).

srtdroid is a binder/wrapper built on [SRT](https://github.com/Haivision/srt). It is a not a new implementation of SRT protocol.

For a **live streaming SDK** based on [SRT](https://github.com/Haivision/srt), check [StreamPack](https://github.com/ThibaultBee/StreamPack).

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
    implementation 'com.github.ThibaultBee:srtdroid:1.2.0'
}
```

## Samples

srtdroid comes with 2 sample apps:
* [examples](#examples)
* [chat](#chat)

On the server side, configure local IP to `0.0.0.0`.
On the client side, configure remote IP to server device IP.
The port must be the same value for both.

### [examples](./examples)

Examples are the srtdroid counterpart of [SRT examples folder](https://github.com/Haivision/srt/tree/master/examples).

### [chat](./chat)

A SRT Instant Messaging (IM).

## API

As srtdroid is a simple wrapper, it provides a minimalist [API documentation](https://thibaultbee.github.io/srtdroid/dokka/lib). For an extensive SRT API documentation refers to the official [SRT API documentation](https://github.com/Haivision/srt/blob/master/docs/API.md).

### Send/recv

Srt `send`, `sendMsg` and `sendMsg` are called `send()` in srtdroid. Alternatively, you can use the `OutputStream` API.
Srt `recv`, `recvdMsg` are called `recv()` in srtdroid. Alternatively, you can use the `InputStream` API.

## Permissions

You need to add the `INTERNET` permission in your AndroidManifest.xml:
```xml
    <uses-permission android:name="android.permission.INTERNET" />
```

To use, `sendFile` and `recvFile`, you also need to add `READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE`:
```xml
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
For Android > 6.0, you have to request permissions at runtime.

## Build

Srtdroid downloads and builds [SRT](https://github.com/Haivision/srt) and [OpenSSL](https://www.openssl.org) (ssl and crypto) libraries. The first compilation will take a while.

### Linux

You have to install `make`:
```bash
sudo apt-get install build-essential
```

### Windows

Srtdroid does not build on Windows because OpenSSL is really tricky to compile on Windows.

### macOS

Not tested.

## Licence

    Copyright 2021 Thibault B.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
