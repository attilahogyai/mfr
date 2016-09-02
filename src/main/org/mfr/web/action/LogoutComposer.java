package org.mfr.web.action;

import org.zkoss.zhtml.A;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;

public class LogoutComposer extends SelectorComposer<A> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4461101876936778213L;

	@Listen("onClick=#logout")
    public void cancel() {
		Executions.getCurrent().getSession().invalidate();
        Executions.sendRedirect("index.zul");
    }
}
