Change Log
==========

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

