package tmp;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;

public class Jacalindo {

    public static void main(String[] args) throws IOException, InterruptedException {
        var canset = new Semaphore(1);
        var canget = new Semaphore(0);
        String[] cbs = new String[1];
        Thread t = getThread(canget, cbs, canset);
        try(FileReader in = new FileReader("docs/doc.md");) {
            BufferedReader bufferedReader = new BufferedReader(in);
            String s;
            StringBuilder codeblock = null;
            while((s = bufferedReader.readLine()) != null) {
                if(s.startsWith("```java")) {
                    codeblock = new StringBuilder();
                } else if (codeblock != null) {
                    if(s.startsWith("```")) {
                        canset.acquire();
                        cbs[0] = codeblock.toString();
                        canget.release();
                        codeblock = null;
                    } else {
                        codeblock.append(s).append("\n");
                    }
                }
            }
        }
        t.interrupt();
    }

    private static Thread getThread(Semaphore canget, String[] cbs, Semaphore canset) {
        Thread t = new Thread(() -> {
            int cb = 0;
            try {
                while(!Thread.interrupted()) {
                    canget.acquire();
                    String code = cbs[0];
                    canset.release();
                    String classname = "cb" + cb++;
                    String f = "package tmp;\n" +
                            "import com.simonebasile.http.handlers.*;\n" +
                            "import java.util.UUID;\n" +
                            "import java.io.IOException;\n" +
                            "import com.simonebasile.http.message.HttpResponse;\n" +
                            "import com.simonebasile.http.response.ByteResponseBody;\n" +
                            "import com.simonebasile.http.server.WebServer;\n" +
                            "import com.simonebasile.http.handlers.StaticFileHandler;\n" +
                            "import java.time.LocalDateTime;\n" +
                            "public class " + classname + " {\n" +
                            "public static void main(String[] args) {\n" +
                            code + "\n" +
                            "}\n" +
                            "}";
                    try (FileOutputStream fileOutputStream = new FileOutputStream("WebServer/src/main/java/tmp/" + classname + ".java");){
                        fileOutputStream.write(f.getBytes(StandardCharsets.UTF_8));
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Interrupted");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
        return t;
    }
}
