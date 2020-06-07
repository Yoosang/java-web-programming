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
			this.method = token[0];
			splitUrlParams(token[1]);
			
			while(!"".equals(line)) {
				line = br.readLine();
				String[] headerToken = line.split(": ");
				header.put(headerToken[0], headerToken[1]);
			}
		}
		catch(Exception e) {
			log.error(e.getMessage());
		}
	}
		
	private void splitUrlParams(String str) {
		String[] splitedStr = str.split("\\?");
		this.path = splitedStr[0];
		params = util.HttpRequestUtils.parseQueryString(splitedStr[1]);
	}
	
	public String getMethod() {
		return this.method;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public String getHeader(String key) {
		return header.get(key);
	}
	
	public String getParameter(String key) {
		return params.get(key);
	}
	
}
