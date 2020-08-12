package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import jdk.nashorn.internal.ir.RuntimeNode.Request;
import model.User;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
        	HttpRequest request = new HttpRequest(in);
        	HttpResponse response = new HttpResponse(out);
        	String path = request.getPath();
        	
        	if("/user/create".equals(path)) {
        		User user = new User(
        				request.getParameter("userId"),
        				request.getParameter("password"),
        				request.getParameter("name"),
        				request.getParameter("email"));
        		log.debug("user: {}", user);
        		DataBase.addUser(user);
        		response.sendRedirect("/index.html");
        	}
        	else if("/user/login".equals(path)) {
        		User user = DataBase.findUserById(request.getParameter("userId"));
        		if(user != null) {
        			if(user.login(request.getParameter("password"))) {
        				response.addHeader("Set-Cookie", "logined=true");
        				response.sendRedirect("/index.html");
        			}
        			else {
        				response.sendRedirect("/user/logined_failed.html");
        			}
        		}
        		else {
        			response.sendRedirect("/user/logined_failed.html");
        		}
        	}
        	
        	BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
        	DataOutputStream dos = new DataOutputStream(out);
        	String line = br.readLine();	
        	if(line == null) {
        		return;
        	}
        	String[] token = line.split(" ");
        	String url = token[1]; 
        	log.debug("request url : {}", url);		//url debug
        	boolean logined = false;
        	
        	Map<String, String> headers = new HashMap<String, String>();
    		while(!"".equals(line)) {
    			//log.debug("HTTP Header : {}", line); 
    			line = br.readLine(); 
    			String[] headerTokens = line.split(": ");
    			if(headerTokens.length == 2) {
    				headers.put(headerTokens[0], headerTokens[1]);
    			}
    			if(line.contains("Cookie")) {
    				logined=true;
    			}
    		}
    		log.debug("Content-Length : {}", headers.get("Content-Length"));
        	
        	if(url.startsWith("/user/create")) {
        		String reqBody = util.IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
        		log.debug("request body : {}", reqBody);
        		Map<String, String> userInfo = util.HttpRequestUtils.parseQueryString(reqBody);
            	User newUser = new User(userInfo.get("userId") , userInfo.get("password"), userInfo.get("name"), userInfo.get("email"));
            	DataBase.addUser(newUser);
            	log.debug("User info : {}", newUser.toString());
            	response302Header(dos);
        	}
        	else if(url.equals("/user/login")) {
        		String reqBody = util.IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
        		log.debug("request body : {}", reqBody);
        		Map<String, String> userInfo = util.HttpRequestUtils.parseQueryString(reqBody);
        		log.debug("User Id : {}, User Password : {}", userInfo.get("userId"), userInfo.get("password"));
        		User loginUser = DataBase.findUserById(userInfo.get("userId"));
        		
        		if(loginUser == null) {
        			log.debug("User not found!");
        			response302HeaderWithCookie(dos, "logined=false");
        		}
        		else if(!loginUser.getPassword().equals(userInfo.get("password"))) {
        			log.debug("Password Mismatch!");
        			response302HeaderWithCookie(dos, "logined=false");
        		}
        		else {
        			log.debug("Login Success");
        			response302HeaderWithCookie(dos, "logined=true");
        		}
        	}
        	else if(url.equals("/user/list")) {
        		Collection<User> users = DataBase.findAll();
                StringBuilder sb = new StringBuilder();
                sb.append("<table border='1'>");
                for (User user : users) {
                    sb.append("<tr>");
                    sb.append("<td>" + user.getUserId() + "</td>");
                    sb.append("<td>" + user.getName() + "</td>");
                    sb.append("<td>" + user.getEmail() + "</td>");
                    sb.append("</tr>");
                }
                sb.append("</table>");
                byte[] body = sb.toString().getBytes();
                response200Header(dos, body.length);
                responseBody(dos, body);
        	}
        	else if(url.endsWith(".css")) {
        		byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            	response200HeaderWithCss(dos, body.length);
                responseBody(dos, body);
        	}
        	else {
            	byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            	response200Header(dos, body.length);
                responseBody(dos, body);
        	}
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    

    private void response302HeaderWithCookie(DataOutputStream dos, String cookie) {
    	try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            if("logined=false".equals(cookie)) {
            	dos.writeBytes("Location: login_failed.html\r\n");
            }
            else {
            	dos.writeBytes("Location: /index.html\r\n");
            }
            dos.writeBytes("Set Cookie: " + cookie + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response200HeaderWithCss(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response302Header(DataOutputStream dos) {
    	try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: /index.html\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
