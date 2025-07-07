# srtdroid: Secure Reliable Transport (SRT) Protocol for Android

Low level API for SRT library on Android. SRT is an open source transport technology that optimizes
streaming performance across unpredictable networks. More information
on [SRT](https://github.com/Haivision/srt).

srtdroid is a binder/wrapper for Android built on [SRT](https://github.com/Haivision/srt). It is a
not a new implementation of SRT protocol.

For a **live streaming SDK** based on [SRT](https://github.com/Haivision/srt), go
to [StreamPack](https://github.com/ThibaultBee/StreamPack).

## Install

Get srtdroid latest artifacts on MavenCentral.

```gradle
dependencies {
    implementation 'io.github.thibaultbee.srtdroid:srtdroid-core:1.9.2'
    // If you use Kotlin Coroutines, you can use srtdroid-ktx
    implementation 'io.github.thibaultbee.srtdroid:srtdroid-ktx:1.9.2'
}
```

## Sample

srtdroid comes with an [examples](https://github.com/ThibaultBee/srtdroid/tree/master/examples)
application. Examples are the srtdroid counterpart
of [SRT examples folder](https://github.com/Haivision/srt/tree/master/examples).

On the server side, configure local IP to `0.0.0.0`. On the client side, configure remote IP to
server device IP. The port must be the same value for both.

## API

As srtdroid is a simple wrapper, it provides a
minimalist [API documentation](https://thibaultbee.github.io/srtdroid/dokka/lib). For an extensive
SRT API documentation refers to the
official [SRT API documentation](https://github.com/Haivision/srt/blob/master/docs/API.md).

### Send/recv

Srt `send`, `sendMsg` and `sendMsg` are called `send()` in srtdroid. Alternatively, you can use
the `OutputStream` API. Srt `recv`, `recvdMsg` are called `recv()` in srtdroid. Alternatively, you
can use the `InputStream` API.

## Permissions

To use, `sendFile` and `recvFile`, you need to add `READ_EXTERNAL_STORAGE` (
or [
`READ_MEDIA_*`](https://developer.android.com/about/versions/13/behavior-changes-13#granular-media-permissions)
if your app targets Android 13 or higher)
and `WRITE_EXTERNAL_STORAGE` to your `AndroidManifest.xml`:

```xml

<manifest>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <!-- If your app targets Android < 13 -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <!-- If your app targets Android >= 13, READ_MEDIA_VIDEO and/or READ_MEDIA_IMAGES and/or READ_MEDIA_AUDIO -->
    <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
</manifest>
```

Then, you have to request permissions at runtime.

## Build

srtdroid downloads and builds [SRT](https://github.com/Haivision/srt)
and [OpenSSL](https://www.openssl.org) (ssl and crypto) libraries. The first compilation will take a
while.

### Linux

You have to install `make`:

```bash
sudo apt-get install build-essential
```

### Windows

srtdroid does not build on Windows because OpenSSL is really tricky to compile on Windows.

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
