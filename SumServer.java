import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class SumServer { // main server class

  private static final String HOST = "localhost";
  private static final int PORT = 5001;

  public static void main(String[] args) throws IOException {
    System.out.printf("Starting server on port %d...\n", PORT);
    HttpServer server = HttpServer.create(new InetSocketAddress(HOST, PORT), 0); // Init server
    server.createContext("/sum", new SumHandler()); // Setup handler
    server.start(); // start listening
  }
}

class SumHandler implements HttpHandler { // request handler class

  private static final int STATUS_OK = 200;
  private static final int STATUS_BAD_REQUEST = 400;
  private static final int STATUS_METHOD_NOT_ALLOWED = 405;

  @Override
  public void handle(HttpExchange he) throws IOException {
    try {
      final Headers headers = he.getResponseHeaders();
      final String requestMethod = he.getRequestMethod().toUpperCase();

      headers.set("Content-type", "application/json");
      headers.add("Access-Control-Allow-Origin", "*");

      if (requestMethod.equals("POST")) { // only allow POST method
				String reqBody = new BufferedReader(new InputStreamReader(he.getRequestBody()))
          .lines().collect(Collectors.joining("\n"));

        System.out.printf("Received %s request with data: %s\n", requestMethod, reqBody);

        JSONObject reqJSON = new JSONObject(reqBody);
        int a = reqJSON.getInt("a"); // get a from JSON input
        int b = reqJSON.getInt("b"); // get b from JSON input

        JSONObject respJSON = new JSONObject();
        respJSON.put("sum", a+b); // sum = a + b

				String resp = respJSON.toString();
        System.out.printf("Returning response: %s\n", resp);
        he.sendResponseHeaders(STATUS_OK, resp.length());
        OutputStream os = he.getResponseBody();
        os.write(resp.getBytes());
        os.close();
      } else { // return 405 if not POST method
        System.out.printf("Received %s request. Returning METHOD NOT ALLOWED\n", requestMethod);
        headers.set("Allow", "POST");
        he.sendResponseHeaders(STATUS_METHOD_NOT_ALLOWED, -1);
      }
    } catch (JSONException e) { // return 400 if there is parsing error
      System.out.printf("Returning BAD REQUEST error: %s\n", e);
      he.sendResponseHeaders(STATUS_BAD_REQUEST, e.toString().length());
      OutputStream os = he.getResponseBody();
      os.write(e.toString().getBytes());
      os.close();
    }
    finally {
      he.close();
    }
  }
}
