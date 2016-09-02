package org.mfr.image;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zul.Image;
import org.zkoss.zul.impl.Utils;

public class HtmlOutZulImage extends Image {

	private byte _imgver;
	private org.zkoss.image.Image _image = null;
	private String _src;
	private String url;

	public HtmlOutZulImage() {
		super();
	}

	public HtmlOutZulImage(String src) {
		super(src);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url=url;
	}

	private String getEncodedURL() {
		if (getContent() != null)
			
			return Utils.getDynamicMediaURI( //already encoded
				this, _imgver, "c/" + _image.getName(), _image.getFormat());
	
		final Desktop dt = getDesktop(); //it might not belong to any desktop
		return dt != null ? dt.getExecution()
			.encodeURL(_src != null ? _src: "~./img/spacer.gif"): "";
	}

	@Override
	public void setContent(org.zkoss.image.Image image) {
		super.setContent(image);
		_imgver++;
		_image=image;
		url=getEncodedURL();
	}

	@Override
	public void setSrc(String src) {
		super.setSrc(src);
		_src=src;
	}

}