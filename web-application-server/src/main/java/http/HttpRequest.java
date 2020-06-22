package http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequest {	
	private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
	private Map<String, String> header = new HashMap<>();
	private Map<String, String> params = new HashMap<>();
	private String method;
	private String path;

	public HttpRequest (InputStream in) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
			String line = br.readLine();
			if(line == null) {
				return;
			}
			
			String[] token = line.split(" ");
			
			//set method
			this.method = token[0];
			
			//set header
			while(!"".equals(line = br.readLine())) {
				String[] headerToken = line.split(": ");
				header.put(headerToken[0], headerToken[1]);
			}
			
			//set path and params
			String paramStr;
			if(token[1].contains("?")) {
				paramStr = token[1].substring(token[1].indexOf("?") + 1,token[1].length());
				token[1] = token[1].substring(0, token[1].indexOf("?"));
			}
			else {
				paramStr = util.IOUtils.readData(br, Integer.parseInt(header.get("Content-Length")));
			}
			this.path = token[1];
			this.params = util.HttpRequestUtils.parseQueryString(paramStr);
			
		}
		catch(Exception e) {
			log.error(e.getMessage());
		}
	}

	public String getMethod() {
		return this.method;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public String getHeader(String key) {
		return this.header.get(key);
	}
	
	public String getParameter(String key) {
		return this.params.get(key);
	}
	
}
