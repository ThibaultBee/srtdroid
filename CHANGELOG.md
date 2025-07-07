Changelog
==========

Version 1.9.2
-------------

## Dependencies:

- srt 1.5.4
- openssl 3.5.1

## Bug fixes:

- Add support for 16 KB page size

## Features:

- Move Maven packages to new Maven Central portal

Version 1.9.1
-------------

## Dependencies:

- srt 1.5.4
- openssl 3.0.9

## Bug fixes:

- Fix the exception when `CoroutineSrtSocket` is closed during a connection

Version 1.9.0
-------------

## Dependencies:

- srt 1.5.4
- openssl 3.0.9

Version 1.8.5
-------------

## Dependencies:

- srt 1.5.3
- openssl 3.0.9

## Bug fixes:

- ktx: close socket when connection fails to avoid resource leak

Version 1.8.4
-------------

## Dependencies:

- srt 1.5.3
- openssl 3.0.9

## Refactor:

- Put the native libraries in a single fat library to avoid conflicts with other libraries

Version 1.8.3
-------------

## Dependencies:

- srt 1.5.3
- openssl 3.0.9

## Features:

- Coroutine socket: move extension functions to `CoroutineSrtSocket` file

## Bug fixes:

- Coroutine socket: fix `equals` implementation
- Coroutine socket: fix remote disconnect detection

Version 1.8.2
-------------

## Dependencies:

- srt 1.5.3
- openssl 3.0.9

## Bug fixes:

- Coroutine socket: epoll must not throw an exception when the socket is closed

Version 1.8.1
-------------

## Dependencies:

- srt 1.5.3
- openssl 3.0.9

## Bug fixes:

- Fix `SrtUrl` parsing when stream id
  has [stream id syntax](https://github.com/Haivision/srt/blob/master/docs/features/access-control.md?plain=1)

Version 1.8.0
-------------

From `1.8.0`, packages are available in `io.github.thibaultbee.srtdroid` instead
of `io.github.thibaultbee`.

```gradle
dependencies {
    implementation 'io.github.thibaultbee.srtdroid:srtdroid-core:1.8.0'
    // If you use Kotlin Coroutines, you can use srtdroid-ktx
    implementation 'io.github.thibaultbee.srtdroid:srtdroid-ktx:1.8.0'
}
```

`Socket` class has been renamed `SrtSocket` to avoid confusion with `Socket` from `java.net`. Same
for `Error`, it has been renamed `SrtError`.

## Dependencies:

- srt 1.5.3
- openssl 3.0.9

## Features:

- Add a Kotlin package with socket based on coroutine
- core: add an API to parse ffmpeg like URL. See `SrtUrl` class.
- core: split listener in 2 interfaces: `ClientListener` and `ServerListener`
- core: recv(int) only returns a `ByteArray` instead of a Pair<Int, ByteArray>
- core: implement `Epoll` and `Socket` `equals` and `hashCode`
- Upgrade dependencies (AGP, Kotlin, NDK,...)

## Bug fixes:

- Fix Epoll APIs

Version 1.7.0
-------------

## Dependencies:

- srt 1.5.3
- openssl 3.0.9

## Features:

- Add and test support for Android 34
- Add source and documentation jars to maven central
- Upgrade to Kotlin 1.8
- Upgrade gradle and Android gradle plugin
- Upgrade GitHub Actions to fix warnings

Version 1.6.0
-------------

## Dependencies:

- srt 1.5.2
- openssl 3.0.9

Version 1.5.2
-------------

## Dependencies:

- srt 1.5.1
- openssl 3.0.7

## Features:

- Add support for Android >= 19. Thanks to [@yoobi](https://github.com/yoobi)

Version 1.5.1
-------------

## Dependencies:

- srt 1.5.1
- openssl 3.0.7

## API changes:

- `SockStatus` `NON_EXIST` is now `NONEXIST` to fix a crash when using `NON_EXIST`

Version 1.5.0
-------------

## Dependencies:

- srt 1.5.1
- openssl 3.0.1

Version 1.4.0
-------------

## Dependencies:

- srt 1.5.0
- openssl 3.0.1

Version 1.3.0
-------------

## Dependencies:

- srt 1.4.4
- openssl 3.0.1

## API changes:

- Package has been moved to maven central and renamed from `com.github.thibaultbee`
  to `io.github.thibaultbee`

## Features:

- Rewrites the native part to be more C++ like
- Upgrades Kotlin version to `1.6.10` and android gradle plugin
- Removes `jcenter` as a dependencies repository
- Improves sample: do not run task on main thread

## Other changes:

- Run `build` action on push to project
- Run `docs` action on published release
- Removes `chat` example to simplify maintenance

Version 1.2.0
-------------

## Dependencies:

- srt 1.4.4
- openssl 1.1.1k

## Features:

- New API guide

Version 1.1.0
-------------

## Dependencies:

- srt 1.4.3
- openssl 1.1.1k

## API changes:

- You don't have to call Srt.startUp(), it is called when you access to an SRT class.
- Srt() is now static: directly use Srt object. Same for Time() and Error(), but it doesn't affect
  API usage.
- In case an error happened,
  connectionTime/setRejectReason/peerName/inetAddress/port/sockName/localAddress/localPort return an
  exception instead of a null
- In case an error happened, most Epoll methods return an exception instead of a -1
- Fix a typo: SockStatus NONEXIST become NON_EXIST
- MsgCtrl.boundary is a Boundary type and no longer an Int
- Calls socket.listener instead of socket.socketInterface.

Version 1.0.0
-------------

## Dependencies:

- srt 1.4.2
- openssl 1.1.1h

## API changes:

- use Kotlin getter/setter instead of Java getValue()
- most API returns an exception when srt returns -1

## Features:

- add new API from srt 1.4.2
- add API from Android Socket API

## Bugfixes:

- getSockName(), getPeerName() and accept() return an address

