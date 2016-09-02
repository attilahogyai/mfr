package org.mfr.util;

import org.zkoss.util.resource.Labels;

public class ExceptionWrapper extends Exception {
	String exceptionLocalizedMessage;
	String textKey;
	private Object[] messageArguments=null;
	
	public ExceptionWrapper(Exception e,String textKey,Object[] messageArguments) {
		super(e);
		exceptionLocalizedMessage=Labels.getLabel(textKey, messageArguments);
		this.messageArguments = messageArguments;
	}

	public Object[] getMessageArguments() {
		return messageArguments;
	}

	public void setMessageArguments(Object[] messageArguments) {
		this.messageArguments = messageArguments;
	}

	public String getExceptionLocalizedMessage() {
		return exceptionLocalizedMessage;
	}
}
