package org.mfr.web;

import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.util.WebAppInit;
import org.zkoss.zul.Messagebox;

public class AppInit implements WebAppInit {

	@Override
	public void init(WebApp arg0) throws Exception {
		Messagebox.setTemplate("template/messagebox.zul");

	}

}
