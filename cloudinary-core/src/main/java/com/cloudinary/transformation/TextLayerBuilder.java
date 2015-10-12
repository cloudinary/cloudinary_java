package com.cloudinary.transformation;

import java.util.ArrayList;

import com.cloudinary.SmartUrlEncoder;
import com.cloudinary.utils.StringUtils;

public class TextLayerBuilder extends AbstractLayerBuilder<TextLayerBuilder> {
	protected String resourceType = "text";
	protected String fontFamily = null;
	protected Integer fontSize = null;
	protected String fontWeight = null;
	protected String fontStyle = null;
	protected String textDecoration = null;
	protected String textAlign = null;
	protected String stroke = null;
	protected String letterSpacing = null;
	protected String text = null;

	@Override
	TextLayerBuilder self() {
		return this;
	}

	public TextLayerBuilder resourceType(String resourceType) {
		throw new UnsupportedOperationException("Cannot modify resourceType for text layers");
	}

	public TextLayerBuilder type(String type) {
		throw new UnsupportedOperationException("Cannot modify type for text layers");
	}

	public TextLayerBuilder format(String format) {
		throw new UnsupportedOperationException("Cannot modify format for text layers");
	}

	public TextLayerBuilder fontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
		return self();
	}

	public TextLayerBuilder fontSize(int fontSize) {
		this.fontSize = fontSize;
		return self();
	}

	public TextLayerBuilder fontWeight(String fontWeight) {
		this.fontWeight = fontWeight;
		return self();
	}

	public TextLayerBuilder fontStyle(String fontStyle) {
		this.fontStyle = fontStyle;
		return self();
	}

	public TextLayerBuilder textDecoration(String textDecoration) {
		this.textDecoration = textDecoration;
		return self();
	}

	public TextLayerBuilder textAlign(String textAlign) {
		this.textAlign = textAlign;
		return self();
	}

	public TextLayerBuilder stroke(String stroke) {
		this.stroke = stroke;
		return self();
	}

	public TextLayerBuilder letterSpacing(String letterSpacing) {
		this.letterSpacing = letterSpacing;
		return self();
	}

	public TextLayerBuilder text(String text) {
		this.text = SmartUrlEncoder.encode(text).replace("%2C", "%E2%80%9A").replace("/", "%E2%81%84");
		return self();
	}

	@Override
	public String toString() {
		if (this.publicId == null && this.text == null) {
			throw new IllegalArgumentException("Must supply either text or public_id.");
		}

		ArrayList<String> components = new ArrayList<String>();
		components.add(this.resourceType);

		String styleIdentifier = textStyleIdentifier();
		if (styleIdentifier != null) {
			components.add(styleIdentifier);
		}

		if (this.publicId != null) {
			components.add(this.formattedPublicId());
		}

		if (this.text != null) {
			components.add(this.text);
		}

		return StringUtils.join(components, ":");
	}

	protected String textStyleIdentifier() {
		ArrayList<String> components = new ArrayList<String>();

		if (this.fontWeight != null && !this.fontWeight.equals("normal"))
			components.add(this.fontWeight);
		if (this.fontStyle != null && !this.fontStyle.equals("normal"))
			components.add(this.fontStyle);
		if (this.textDecoration != null && !this.textDecoration.equals("none"))
			components.add(this.textDecoration);
		if (this.textAlign != null)
			components.add(this.textAlign);
		if (this.stroke != null && !this.stroke.equals("none"))
			components.add(this.stroke);
		if (this.letterSpacing != null)
			components.add("letter_spacing_" + this.letterSpacing);

		if (this.fontFamily == null && this.fontSize == null && components.isEmpty()) {
			return null;
		}

		if (this.fontFamily == null) {
			throw new IllegalArgumentException("Must supply fontFamily.");
		}

		if (this.fontSize == null) {
			throw new IllegalArgumentException("Must supply fontSize.");
		}

		components.add(0, Integer.toString(this.fontSize));
		components.add(0, this.fontFamily);

		return StringUtils.join(components, "_");

	}
}
