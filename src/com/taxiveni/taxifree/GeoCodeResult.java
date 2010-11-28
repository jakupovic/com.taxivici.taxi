package com.taxiveni.taxifree;

public class GeoCodeResult {
	
	public String line1;
	public String line2;
	public String line3;
	public String line4;
	
	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder();
		builder.append("Location:");
		
		if (line1!=null)
			builder.append("-"+line1);
		if (line2!=null)
			builder.append("-"+line2);
		if (line3!=null)
			builder.append("-"+line3);
		if (line4!=null)
			builder.append("-"+line4);
		
		return builder.toString();
		
	}
	
}
