package org.apache.catalina;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.comet.CometEvent;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ErrorReportValve;
import org.mfr.web.PrivateContentAccessManager;

public class MfrErrorReportValve extends ErrorReportValve {

	@Override
	protected void report(Request request, Response response, Throwable arg2) {
		try {
			if(arg2!=null){
				RequestWrapper.httpRequest.set(request);
				request.getRequestDispatcher("/error500.zul").forward(request, response);
				System.out.println("error report : "+arg2.getMessage());
			}
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void invoke(Request request, Response response) throws IOException,
			ServletException {
		RequestWrapper.httpRequest.set(request);
		super.invoke(request, response);
	}

}
