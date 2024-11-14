# Things to do

## Webserver

- Remove version from response, it's obviously not supposed to be there
- Move request and response serializing and parsing to webserver to make responses of non ResponseBody type possible
- Move context handling to webserver
- (probably stored in context ^) Tell handler what path it was registered on and what path matched 
- (needs matching info ^) Add wildcard paths support
- Add events to server

## Application

- consider holding session info in memory and only persisting updates
