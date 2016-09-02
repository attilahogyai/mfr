package org.mfr.web.action;

import java.io.IOException;

import org.mfr.util.HttpHelper;
import org.mfr.util.ZkUtil;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.impl.InputElement;

public class AbstractSelectorComposer extends SelectorComposer<Component> {
	private static final long serialVersionUID = 1020345520370304262L;
	
	public void checkAdmin(){
		if(!HttpHelper.getUser().isAdmin()) {
			try {
				Executions.forward("/");
			} catch (IOException e) {

			}
		}
	}
	public boolean checkMandatory(InputElement... input){
		for (int i = 0; i < input.length; i++) {
			if(input[i] instanceof Textbox){
				if(!input[i].isValid() || input[i].getText().length()==0){
					ZkUtil.messageBoxWarning("please.fill.field",Labels.getLabel(input[i].getId()));
					return false;
				}
			}
		}
		return true;
	}
}
