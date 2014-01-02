package com.cloudinary;

import java.util.Collection;
import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;
import java.awt.Rectangle;
public class Coordinates {

	Collection<Rectangle> coordinates = new ArrayList<Rectangle>();
	
	public Coordinates() {
	}

	public Coordinates(Collection<Rectangle> coordinates) {
		this.coordinates = coordinates;
	}

	public void addRect(Rectangle rect) {
		this.coordinates.add(rect);
	}

	
	public Collection<Rectangle> underlaying() {
		return this.coordinates;
	}

	@Override
	public String toString() {
		ArrayList<String> rects = new ArrayList<String>();
		for (Rectangle rect : this.coordinates) {
			rects.add(rect.x + "," + rect.y + "," + rect.width + "," + rect.height);
		}
		return StringUtils.join(rects, "|");
	}

}
