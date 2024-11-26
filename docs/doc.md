# Documentazione software

Il software è composto da due moduli: un modulo contenente la libreria del webserver, e un modulo applicativo che utilizza la libreria a scopo dimostrativo

## Modulo WebServer

Il modulo WebServer è una libreria che permette la creazione di un webserver java

### Dipendenze

Il webserver è scritto in java 17 e utilizza solo una libreria di logging: `slf4j`.

`slf4j` abbreviativo di Simple Logging Facade for Java è una libreria che fornisce un'interfaccia per il logging unica, e permette di utilizzare diverse libreria di logging come backend, permettendo agli utilizzatori della libreria di scegliere la libreria di logging che preferiscono.

### Componenti Principali

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

L'interfaccia `HttpRequestHandler` è l'interfaccia principale per la gestione delle richieste http.
Tutte le classi che gestiscono le richieste http implementano questa interfaccia.

L'interfaccia ha un solo metodo da implementare: `HttpResponse<? extends HttpResponseBody> handle(HttpRequest<? extends Body> r, Context requestContext)` che riceve come argomenti la request http e il RequestContext del webserver, e restituisce la response http 

#### HttpRequest

La classe `HttpRequest` contiene i dati della richiesta http ricevuta dal server. 
Contiene:
- Metodo http
- Path http
- Versione protocollo http
- Headers
- Eventuale Body

#### HttpResponse

La classe `HttpRequest` contiene i dati della richiesta http ricevuta dal server. 
Contiene:
- Status Code http
- Versione protocollo http
- Headers
- Eventuale Body

#### HttpResponseBody

L'interfaccia `HttpResponseBody` contiene i metodi necessari alla serializzazione del body di una response http.

L'interfaccia ha 3 metodi da implementare: 

- `void write(OutputStream out) throws IOException`:
    questo metodo prende come argomento un OutputStream su cui scrivere il body della response
- `Long contentLength()`:
    questo metodo ritorna la lunghezza del body da scrivere nella response o null se la lunghezza non è conosciuta a priori.
    Se si è specificata una lunghezza, il numero di byte scritti dal metodo `write` deve coincidere con quanto specificato.
    Se non si è specificata una lunghezza la response verrà inviata con `Transfer-Encoding: chunked`
- `String contentType()`:
    questo metodo deve ritornare il `content-type` associato alla response.


#### HttpInterceptor

L'interfaccia `HttpInterceptor` è l'interfaccia da implementare per intercettare richieste http.

L'interfaccia permette di preprocessare le request, modificare le response, o fermare una richiesta non valida rispondendo al posto dell'handler.

L'interfaccia ha un solo metodo da implementare `HttpResponse<? extends HttpResponseBody> intercept(HttpRequest<? extends Body> request, Context requestContext, HttpRequestHandler<Body, Context> next)` che prende come argomenti la richiesta http ricevuta, il requestContext http e l'handler che dovrebbe gestire la risposta

#### WebsocketHandler

L'interfaccia `WebsocketHandler` permette di gestire gli eventi legati alle connessioni che usano il protocollo websocket.

L'interfaccia prevede 5 metodi da implementare:

- `WebsocketContext newContext(HttpRequestContext ctx)`: questo metodo viene invocato appena si riceve una richiesta di connessione websocket. Questo metodo serve a creare il context che conterrà lo stato relativo alla connessione websocket corrente, che verrà passato a tutte le chiamate websocket della stessa connessione
- `HandshakeResult onServiceHandshake(String[] availableProtocols, WebsocketContext context)`: questo metodo viene invocato successivamente al metodo newContext, riceve come parametri il context creato e un array di sottoprotocolli websocket richiesti dal client. Questo metodo deve ritornare un `HandshakeResult` che può essere costruito con `HandshakeResult.accept(String protocol)` passando il protocollo selezionato tra quelli ricevuti se si accetta la richiesta di connessione websocket, o con `HandshakeResult.refuse(String message)` passando un messaggio da inviare al client, se non si accetta la richiesta di connessione.
- `void onHandshakeComplete(WebsocketWriter websocketWriter, WebsocketContext context)`: questo metodo viene chiamato una volta che l'handshake websocket è completato. riceve come parametri il context websocket e un istanza di `WebSocketWriter` che permette di inviare messaggi al client che ha completato l'handshake
- `void onMessage(WebsocketMessage msg, WebsocketContext context)`: questo metodo viene chiamato ogni volta che arriva un messaggio dal client. al metodo vengono passati il context websocket e il messaggio ricevuto.
- `void onClose(WebsocketContext context)`: questo metodo viene chiamato quando il client chiude la connessione websocket. Al metodo viene passato il context websocket

### Esempi di utilizzo

#### registrazione di un semplice handler

```java
WebServer srv = Webserver.builder().build();
srv.registerHttpContext("/", (request, context) -> {
    return new HttpResponse(200, new ByteResponseBody("Hello"))
});
srv.start();
```

Questo esempio mostra la creazione di un server che risponde con stato `200` e `Hello` a tutte le chiamate:

`WebServer srv = Webserver.builder().build()` crea un server con le configurazioni di default.

`srv.registerHttpContext("/", (request, context) => { ... })` registra un handler che viene utilizzato per tutti i sottopath del path indicato.

`new HttpResponse(200, new ByteResponseBody("Hello"))` crea una risposta http che ha nel body la stringa `Hello`. Il `ByteResponseBody`, quando costruito con una stringa come parametro mette la stringa nel body e configura il content type della response come `text/plain`.

#### Servire file statici

```java
WebServer srv = Webserver.builder().build();
srv.registerHttpContext("/resources", 
    new StaticFileHandler("/path/to/resources")
);
srv.start();
```

Questo esempio mostra come servire file statici usando la classe `StaticFileHandler`.

```bash
> tree
path
└─ to
   ├─ secret-file
   └─ resources
      ├── index.html
      ├── script.js
      └── style.css
```

Con il codice mostrato e le cartelle qui sopra

- chiamare `http://localhost/resources/index.html` restituisce il file `index.html` con content type `text/html`
- chiamare `http://localhost/resources/script.js` restituisce il file `script.js` con content type `text/javascript`
- chiamare `http://localhost/resources/style.css` restituisce il file `style.css` con content type `text/css`
- chiamare `http://localhost/resources/missing.txt` restituisce un errore `404` dato che il file non esiste
- chiamare `http://localhost/resources/` restituisce il file `index.html` con content type `text/html`, dato che la classe `StaticFileHandler` quando il path punta ad una cartella cerca all'interno un file `index.html` 
- chiamare `http://localhost/resources/../secret-file` restituisce un errore `404` dato che `StaticFileHandler` impedisce di accedere a file quando il percorso contiene delle parti di path con funzioni particolari, come `..` o `~`

#### Registrazione di un interceptor

```java
WebServer srv = Webserver.builder().build();

srv.registerHttpInterceptor((request, context, next) -> {
    request.getHeaders().add("X-Intercepted-At", LocalDateTime.now().toString())
    var response = next.handle(request, context);
    response.getHeaders().add("X-Intercepted", "true");
    return response;

})

srv.registerHttpContext("/", (r, c) => {
    //Interceptor added data to request
    String interceptedAt = r.getHeaders().getFirst("X-Intercepted-At");
    assert interceptedAt != null; 

    new HttpResponse(200, new ByteResponseBody("Hello at " + interceptedAt))
});

srv.start();
```

Questo esempio estende quello precedente mostrando l'utilizzo di un interceptor aggiungendo un header alla request e che arriva all'handler e un header alla response che viene restituita al client.

#### Specificità degli handler

```java
WebServer srv = Webserver.builder().build();
srv.registerHttpContext("/", (request, context) -> {
    return new HttpResponse(200, new ByteResponseBody("Hello"))
});
srv.registerHttpContext("/world", (request, context) -> {
    return new HttpResponse(200, new ByteResponseBody("Hello World and more"))
});
srv.registerHttpHandler("/world", (request, context) -> {
    return new HttpResponse(200, new ByteResponseBody("Hello World"))
});
srv.start();
```

- Una chiamata a `http://localhost/` risponde con `Hello`
- Una chiamata a `http://localhost/other` risponde con `Hello`
- Una chiamata a `http://localhost/world` risponde con `Hello World`
- Una chiamata a `http://localhost/world/etc` risponde con `Hello World and more`

la prima chiamata ha come path `/` che corrisponde al primo handler e viene quindi gestito dal primo handler

la seconda chiamata ha come path `/other` che non corrisponde esattamente a nessun handler, ma essendo un sottopath di `/` viene gestito dal primo handler

la terza chiamata ha come path `/world` che corrisponde sia al secondo che al terzo handler, ma dato che il terzo handler è più specifico per quel path, è quello a gestire la chiamata

la quarta chiamata ha come path `/world/etc` che non corrisponde a nessun handler, ma è un sottopath sia del primo che del secondo handler. Dato che il secondo handler è più specifico è quello che gestirà la chiamata

#### gestire una connessione websocket

```java
class SimpleContext {
    private WebSocketWriter writer;
    private String connectionId;
    private String protocol;
    /* GETTERS AND SETTERS */
}

class WSHandler implements WebsocketHandler<SimpleContext, RequestContext> {

    public SimpleContext newContext(RequestContext ctx) {
        //Volendo è anche possibile prendere delle informazioni dal
        //request context (ad esempio dati sull'autenticazione)
        //e spostarli nel context della connessione
        var ctx = new SimpleContext();
        ctx.setConnectionId(UUID.randomUUID().toString());
        return ctx;
    }

    public HandshakeResult onServiceHandshake(String[] availableProtocols, SimpleContext ctx) {
        if(availableProtocols.length == 0) return HandshakeResult.refuse("No protocol");
        else {
            ctx.setProtocol(availableProtocols[0]);
            return HandshakeResult.accept(ctx.getProtocol());
        }
    }

    public void onHandshakeComplete(WebSocketWriter writer, SimpleContext ctx) {
        //La connessione websocket è completa. Salva il writer per 
        //poter inviare messaggi al client in seguito
        System.out.println("Connected " + ctx.getConnectionId()
                + " using protocol " + ctx.getProtocol);
        ctx.setWriter(writer);
    }

    public void onMessage(WebsocketMessage msg, SimpleContext ctx) {
        //Un messaggio websocket puo contenere test o dati binari
        //In questo esempio gestiamo solo i messaggi testuali
        assert msg.type == MsgType.TEXT; 
        //I messaggi websocket possono essere divisi in chunk.
        //In questo esempio gestiamo messaggi con un singolo chunk
        assert msg.data.length == 1; 
        String message = new String(msg.content[0]);
        System.out.println("Received message from " + ctx.getConnectionId() 
                + ": " + message);
        ctx.getWriter().sendText("Responding to: " + message)
    }

    public void onClose(SimpleContext ctx) {
        System.out.println("Disconnected: " + ctx.getConnectionId());
    }
}
```

```java
WebServer wsSrv = WebServer.builder().build();
wsSrv.registerWebSocketHandler("/", new WSHandler());
```

Questo è un esempio di gestione di una connessione websocket.

In questo esempio ogni volta che un client invia un messaggio, il server stampa a console il messaggio ricevuto e risponde al client con un acnkowledgment del messaggio ricevuto.

Vengono anche stampati in console informazioni sulle connessioni effettuate e sulle connessioni terminate

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
