package org.mfr.image;

import java.io.File;

public class ImageFile extends File {
	private static final long serialVersionUID = 3452285177454251063L;
	public ImageFile(String parent) {
		super(parent);
	}
	public void close(){
		try {
			this.finalize();
			System.gc();
		} catch (Throwable e) {
		}
	}
}
