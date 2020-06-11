package bei7473p5254d69jcuat.tenyu.communication.local;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;

import org.apache.http.*;
import org.apache.http.client.utils.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.*;
import com.sun.net.httpserver.*;

import glb.*;
import glb.Glb.*;

/**
 * @author exceptiontenyu@gmail.com
 *
 */
public class LocalIpc implements GlbMemberDynamicState {
	private HttpServer server;
	//private JsonRpcServer rpcServer = new JsonRpcServer();
	//private LocalIpcAPI api = new LocalIpcAPI();

	/**
	 * @return	サーバが使用しているポート。サーバが起動していない場合-1。
	 */
	public int getPort() {
		if (server == null)
			return -1;
		return server.getAddress().getPort();
	}

	public void start() {
		//int threadCount = 2;
		//ExecutorService httpThreadPool = Executors
		//		.newFixedThreadPool(threadCount);
		if (server == null)
			try {
				server = HttpServer.create(new InetSocketAddress(0), 0);
				HttpContext ctx = server.createContext(
						"/" + Glb.getConst().getAppName(),
						new LocalIpcHandler());
				server.setExecutor(Glb.getExecutor());
			} catch (IOException e) {
				Glb.getLogger().error("", e);
			}
		server.start();
	}

	public void stop() {
		if (server != null)
			server.stop(2);
	}

	private class LocalIpcHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			try {
				//accepts localhost only.
				if (!t.getRemoteAddress().getAddress().isLoopbackAddress()) {
					return;
				}

				String method = t.getRequestMethod();
				String som = null;
				switch (method) {
				case "GET":
					//URL引数から読み取る
					String query = t.getRequestURI().getQuery();
					List<NameValuePair> l = URLEncodedUtils.parse(
							t.getRequestURI(), Glb.getConst().getCharsetNio());
					for (NameValuePair e : l) {
						if (e.getName().equals("som")) {
							som = e.getValue();
						}
					}
					break;
				case "POST":
					//POSTされたデータを読み取る
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(t.getRequestBody(),
									StandardCharsets.UTF_8));
					StringBuilder builder = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
					som = builder.toString();

					break;
				default:
				}
				Glb.debug(som);
				if(som == null || som.length() == 0)
					throw new IllegalStateException("som is empty.");

				//デシリアライズ

				//検証

				//実行
				String resRison = "";//TODO

				//String res = rpcServer.handle(rison, api);

				//返信
				response(resRison, t);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			} finally {
				t.close();
			}
		}
	}

	public static Map<String, Object> decode(String json)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper om = new ObjectMapper();
		return om.readValue(json, new TypeReference<HashMap<String, Object>>() {
		});
	}

	public static String encode(Object o) throws IOException {
		ObjectMapper om = new ObjectMapper();
		om.enable(SerializationFeature.INDENT_OUTPUT);
		return om.writeValueAsString(o);
	}

	private void response(String json, HttpExchange t) throws IOException {
		t.sendResponseHeaders(200, json.length());
		OutputStream os = t.getResponseBody();
		os.write(json.getBytes());
		os.close();
	}

}
