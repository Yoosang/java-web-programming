package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;

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
			
			processRequestLine(line);
			
			//set header
			while(!"".equals(line = br.readLine())) {
				log.debug("header : {}", line);
				String[] headerToken = line.split(":");
				header.put(headerToken[0].trim(), headerToken[1].trim());
			}
			
			if("POST".equals(method)) {
				String body = IOUtils.readData(br, Integer.parseInt(header.get("Content-Length")));
				this.params = HttpRequestUtils.parseQueryString(body); 
			}
		}
		catch(IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void processRequestLine(String requestLine) {
		log.debug("request line : {}", requestLine);
		String[] tokens = requestLine.split(" ");
		this.method = tokens[0];
		
		if("POST".equals(method)) {
			this.path = tokens[1];
			return;
		}
		
		int index = tokens[1].indexOf("?");
		if(index == -1) {
			this.path = tokens[1];
		}
		else {
			this.path = tokens[1].substring(0, index);
			this.params = HttpRequestUtils.parseQueryString(tokens[1].substring(index+1));
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
