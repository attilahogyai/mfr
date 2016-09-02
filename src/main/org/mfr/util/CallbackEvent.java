package org.mfr.util;

import java.util.EventListener;

public interface CallbackEvent extends EventListener{
	public void onEvent(EventData event);
}
