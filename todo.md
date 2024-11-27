# Things to do

## Webserver

- Add method to registration
- (needs matching info ^) Add wildcard paths support
- Add events to server
- Add FixedLengthOutputStream to ensure no more than the correct number of byte is written to the response body
- incorporate newContext with onServiceHandshake into a onHandshakeRequest method, that either accepts, with a protocol and a context, or refuses, with an http response to send the client
- give StaticFileHandler a config to choose what to do in case of miss and a way to read from resources instead of reading from file

## Application

- consider holding session info in memory and only persisting updates
- application configuration