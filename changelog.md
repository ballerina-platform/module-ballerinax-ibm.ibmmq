# Change Log
This file contains all the notable changes done to the Ballerina IBM MQ package through the releases.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added 

- [Introduce support to provide `MQPutMessageOptions` when invoking `put` operation on `ibmmq:Queue` or `ibmmq:Topic`](https://github.com/ballerina-platform/ballerina-library/issues/6966)

## [1.1.0] - 2024-08-28

### Added

- [Add Support for Retrieving Messages from IBM MQ by Matching Correlation ID and Message ID](https://github.com/ballerina-platform/ballerina-library/issues/6918)

## [1.0.0] - 2024-07-08

### Changed

- [Decouple IBM MQ java client jar from the IBM MQ connector](https://github.com/ballerina-platform/ballerina-library/issues/6287)

## [0.1.3] - 2023-12-04

### Fixes

- [When decoding IBM MQ headers from the received IBM MQ message Ballerina IBM MQ connector accidentally passes through to the message payload](https://github.com/ballerina-platform/ballerina-library/issues/5819)

## [0.1.2] - 2023-11-15

### Added

- [Added support `MQIIH` headers in `ibmmq:Message`](https://github.com/ballerina-platform/ballerina-standard-library/issues/5730)

## [0.1.1] - 2023-11-14

### Added

- [Added support `MQRFH2` headers in `ibmmq:Message`](https://github.com/ballerina-platform/ballerina-standard-library/issues/5730)
- [Incorporate secure-socket configuration for ballerina IBM MQ connector](https://github.com/ballerina-platform/ballerina-library/issues/5741)

## [0.1.0] - 2023-10-31

### Added
- [Introduce ballerina `ibm.ibmmq` package](https://github.com/ballerina-platform/ballerina-standard-library/issues/5084)
