Change Log
==========

Version 1.1.0
-------------

## Dependencies:
- srt 1.4.3
- openssl 1.1.1k

## API changes:
- You don't have to call Srt.startUp(), it is called when you access to an SRT class.
- Srt() is now static: directly use Srt object. Same for Time() and Error(), but it doesn't affect API usage.
- In case an error happened, connectionTime/setRejectReason/peerName/inetAddress/port/sockName/localAddress/localPort return an exception instead of a null
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

