Change Log
==========

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

