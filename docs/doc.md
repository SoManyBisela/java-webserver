# Documentazione software

Il software è composto da due moduli: un modulo contenente la libreria del webserver, e un modulo applicativo che utilizza la libreria a scopo dimostrativo

## Modulo WebServer

Il modulo WebServer è una libreria che permette la creazione di un webserver java

### Dipendenze

Il webserver è scritto in java 17 e utilizza solo una libreria di logging: `slf4j`.
`slf4j` abbreviativo di Simple Logging Facade for Java è una libreria che fornisce un'interfaccia per il logging unica, e permette di utilizzare diverse libreria di logging come backend, permettendo agli utilizzatori della libreria di scegliere la libreria di logging che preferiscono.

### Componenti

Di seguito sono elencate le classi principali necessarie all'utilizzo del webserver.

#### WebServer

La classe `WebServer` è la classe principale del webserver, contiene la logica per accettare e gestire le connessioni html e websocket. Attraverso il webserver è possibile registrare degli handler per gestire le richieste http e websocket e aggiungere degli interceptor che vengono eseuti prima di passare la richiesta all'handler.

I metodi principali sono:
- `void start()` per avviare il server
- `void stop()` per fermare il server
- `void registerHttpContext(String path, HttpRequestHandler<Body, ? super Context> handler)` per registrare un handler http associato ad un path e ai subpath
- `void registerHttpHandler(String path, HttpRequestHandler<Body, ? super Context> handler)` per registrare un handler http associato ad un path specifico
- `void registerInterceptor(HttpInterceptor<Body, Context> interceptor)` per registrare un interceptor che intercetta le richieste
- `void registerWebSocketContext(String path, WebsocketHandler<?, ? super Context> handler)` per registrare un handler websocket associato ad un path e ai subpath
- `void registerWebSocketHandler(String path, WebsocketHandler<?, ? super Context> handler)` per registrare un handler websocket associato ad un path specifico
  
Si instanzia tramite il builder `WebServerBuilder` che permette di configurarne il funzionamento.

#### HttpRequestHandler

#### HttpRequest

#### HttpHttpResponse

#### HttpInterceptor

#### WebsocketHandler

### Utilizzo

#### Configurazione del server

```java

```

### Threading model

Il webserver è basato su un thread principale, che accetta le connessioni, e su una thread pool che esegue i task per gestire le richieste.

![execution-model](diagrams/webserver/execution-model.svg)

Ogni connessione viene gestita da un thread della thread pool, su cui viene eseguito un task che legge le richieste in arrivo sulla connessione e le gestisce tramite gli handler registrati.

Nel caso in cui il server riceva una richiesta di passaggio a websocket, lo stesso thread che si occupava di gestire le richieste http si occupa di gestire i messaggi websocket, chiamando l'handler registrato per i messaggi websocket.

### Design patterns

#### Factory pattern

Il factory pattern permette di creare oggetti senza specificare la classe concreta dell'oggetto che si vuole creare.

È utilizzato in `ServerSocketFactory` e in `RequestContextFactory` per permettere la configurazione del webserver.

![socket context factory](diagrams/webserver/patterns/server-socket-factory.svg)
In `ServerSocketFactory` è utilizzato per permettere la configurazione del socket che il server utilizza per accettare connessioni, permettendo ad esempio di utilizzare una connessione tls invece di una semplice connessione tcp, o potenzialmente di avere il server in ascolto su un unix socket.

![request context factory](diagrams/webserver/patterns/request-context-factory.svg)
In `RequestContextFactory` è utilizzato per permettere di personalizzare il `RequestContext`, permettendo così ad esempio di aggiungere informazioni aggiuntive che vengono passate agli handler.

Un esempio di utilizzo lo si puù trovare nel modulo `SampleApp` dove viene utilizzato per instanziare la classe `ApplicationRequestContext` che contiene informazioni aggiuntive sull'autenticazione.

#### Strategy pattern

Lo strategy pattern permette di incapsulare comportamenti diversi in classi separate e di utilizzarli intercambiabilmente a runtime

![strategy pattern](diagrams/webserver/patterns/handler-strategy.svg)

È utilizzato in `RequestHandler` per permettere di registrare diversi handler per gestire le richieste.

#### Decorator pattern

Il decorator pattern permette di aggiungere o modificare le funzionalità di un oggetto senza modificarne la struttura

![decorator pattern](diagrams/webserver/patterns/input-stream-decorator.svg)

È utilizzato da `FixedLengthInputStream`, `ChunkedOutputStream`, `UnmaskingInputStream`, `HttpInputStream` e `HttpOutputStream`.

- `FixedLengthInputStream` permette di leggere un numero fisso di byte da un input stream, in questo modo passando l'input stream ad un handler ci si può assicurare che non vengano letti byte appartenenti alla richiesta successiva
- `ChunkedInputStream` permette di scrivere il body di una risposta in chunk, senza conoscerne a priori la lunghezza, come definito per le richieste con `Transfer-Encoding: chunked`
- `UnmaskingInputStream` permette di decodificare i dati ricevuti da un client websocket, facendo il bitwise xor dei dati con la maschera ricevuta
- `HttpInputStream` e `HttpOutputStream` aggiungono metodi di utility utili per leggere request e scrivere response http

#### Builder pattern

Il builder pattern permette di costruire un oggetto complesso passo passo, permettendo di configurare l'oggetto in modo flessibile

![builder pattern](diagrams/webserver/patterns/webserver-builder.svg)

È utilizzato per la costruzione del `WebServer` permettendo di configurare il socket e la creazione del contesto delle richieste
