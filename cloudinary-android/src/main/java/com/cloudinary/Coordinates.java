package com.cloudinary;

import java.util.ArrayList;
import java.util.Collection;

import android.graphics.Rect;
import android.text.TextUtils;
public class Coordinates {

	Collection<Rect> coordinates = new ArrayList<Rect>();
	
	public Coordinates() {
	}

	public Coordinates(Collection<Rect> coordinates) {
		this.coordinates = coordinates;
	}

	public void addRect(Rect rect) {
		this.coordinates.add(rect);
	}

	
	public Collection<Rect> underlaying() {
		return this.coordinates;
	}

	@Override
	public String toString() {
		ArrayList<String> rects = new ArrayList<String>();
		for (Rect rect : this.coordinates) {
			rects.add(rect.left + "," + rect.top + "," + rect.width() + "," + rect.height());
		}
		return TextUtils.join("|", rects);
	}

}
