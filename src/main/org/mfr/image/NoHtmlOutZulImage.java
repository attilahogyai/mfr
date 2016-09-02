package org.mfr.image;

import java.io.IOException;
import java.io.Writer;

import org.zkoss.zk.au.AuResponse;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.sys.ContentRenderer;

public class NoHtmlOutZulImage extends HtmlOutZulImage{
	private static final long serialVersionUID = 1L;

	@Override
	protected void replace(Component comp, boolean bFellow, boolean bListener,
			boolean bChildren) {
		// TODO Auto-generated method stub
		super.replace(comp, bFellow, bListener, bChildren);
	}
	@Override
	protected void response(AuResponse response) {
		// TODO Auto-generated method stub
		super.response(response);
	}
	@Override
	protected void response(String key, AuResponse response) {
		// TODO Auto-generated method stub
		super.response(key, response);
	}
	@Override
	public void redraw(Writer out) throws IOException {
		// TODO Auto-generated method stub
	}
	@Override
	protected void redrawChildren(Writer out) throws IOException {
		// TODO Auto-generated method stub
	}
	@Override
	protected void render(ContentRenderer renderer, String name, String value)
			throws IOException {
		// TODO Auto-generated method stub
	}
	@Override
	protected void render(ContentRenderer renderer, String name, Object value)
			throws IOException {
		// TODO Auto-generated method stub
	}
	@Override
	protected void render(ContentRenderer renderer, String name, boolean value)
			throws IOException {
		// TODO Auto-generated method stub
	}

}
