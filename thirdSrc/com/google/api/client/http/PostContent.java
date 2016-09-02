package com.google.api.client.http;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpRequest;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.params.HttpParams;

public class PostContent extends AbstractHttpContent{
	private String content;
	public PostContent(String content){
		super("application/x-www-form-urlencoded");
		this.content=content;
	}
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(content.getBytes());
	}

}
