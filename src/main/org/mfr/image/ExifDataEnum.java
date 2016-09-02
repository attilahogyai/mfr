package org.mfr.image;

public enum ExifDataEnum {
	Filename, ImageWidth, ImageHeight, FNumber, ExposureTime, Iso, FocalLength, Model, ExposureProgram, DateTimeOriginal, ExposureCompensation, MeteringMode, WhiteBalance, LensInfo, LensModel, LensMake, XResolution, YResolution, ResolutionUnit, Orientation;
	public static String[] getKeyArrays() {
		ExifDataEnum valuearray[] = ExifDataEnum.values();
		String[] rarray = new String[valuearray.length];
		for (int i = 0; i < rarray.length; i++) {
			rarray[i] = valuearray[i].toString();
		}
		
		 
		return rarray;
	}
}
