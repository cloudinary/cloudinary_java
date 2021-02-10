package com.cloudinary.transformation;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cloudinary.SmartUrlEncoder;
import com.cloudinary.utils.StringUtils;

public class TextLayer extends AbstractLayer<TextLayer> {
    protected String resourceType = "text";
    protected String fontFamily = null;
    protected Integer fontSize = null;
    protected String fontWeight = null;
    protected String fontStyle = null;
    protected String fontAntialiasing = null;
    protected String fontHinting=null;
    protected String textDecoration = null;
    protected String textAlign = null;
    protected String stroke = null;
    protected String letterSpacing = null;
    protected Integer lineSpacing = null;
    protected String text = null;
    protected Object textStyle = null;

    @Override
    TextLayer getThis() {
        return this;
    }

    public TextLayer resourceType(String resourceType) {
        throw new UnsupportedOperationException("Cannot modify resourceType for text layers");
    }

    public TextLayer type(String type) {
        throw new UnsupportedOperationException("Cannot modify type for text layers");
    }

    public TextLayer format(String format) {
        throw new UnsupportedOperationException("Cannot modify format for text layers");
    }

    public TextLayer fontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        return getThis();
    }

    public TextLayer fontAntialiasing(String fontAntialiasing) {
        this.fontAntialiasing = fontAntialiasing;
        return getThis();
    }

    public TextLayer fontHinting(String fontHinting) {
        this.fontHinting = fontHinting;
        return getThis();
    }


    public TextLayer fontSize(int fontSize) {
        this.fontSize = fontSize;
        return getThis();
    }

    public TextLayer fontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
        return getThis();
    }

    public TextLayer fontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
        return getThis();
    }

    public TextLayer textDecoration(String textDecoration) {
        this.textDecoration = textDecoration;
        return getThis();
    }

    public TextLayer textAlign(String textAlign) {
        this.textAlign = textAlign;
        return getThis();
    }

    public TextLayer stroke(String stroke) {
        this.stroke = stroke;
        return getThis();
    }

    public TextLayer letterSpacing(String letterSpacing) {
        this.letterSpacing = letterSpacing;
        return getThis();
    }

    public TextLayer letterSpacing(int letterSpacing) {
        this.letterSpacing = String.valueOf(letterSpacing);
        return getThis();
    }

    public TextLayer lineSpacing(Integer lineSpacing) {
        this.lineSpacing = lineSpacing;
        return getThis();
    }

    public TextLayer text(String text) {
        String part;
        StringBuffer result = new StringBuffer();
        // Don't encode interpolation expressions e.g. $(variable)
        Matcher m = Pattern.compile("\\$\\([a-zA-Z]\\w+\\)").matcher(text);
        int start = 0;
        while (m.find()) {
            part = text.substring(start, m.start());
            part = SmartUrlEncoder.encode(part);
            result.append(part); // append encoded pre-match
            result.append(m.group()); // append match
            start = m.end();
        }
        result.append(SmartUrlEncoder.encode(text.substring(start)));
        this.text = result.toString().replace("%2C", "%252C").replace("/", "%252F");
        return getThis();
    }

    /**
     * Sets a text style identifier,
     * Note: If this is used, all other style attributes are ignored in favor of this identifier
     * @param textStyleIdentifier A variable string or an explicit style (e.g. "Arial_17_bold_antialias_best")
     * @return Itself for chaining
     */
    public TextLayer textStyle(String textStyleIdentifier) {
        this.textStyle = textStyleIdentifier;
        return getThis();
    }

    /**
     * Sets a text style identifier using an expression.
     * Note: If this is used, all other style attributes are ignored in favor of this identifier
     * @param textStyleIdentifier An expression instance referencing the style.
     * @return Itself for chaining
     */
    public TextLayer textStyle(Expression textStyleIdentifier) {
        this.textStyle = textStyleIdentifier;
        return getThis();
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
        // Note: if a text-style argument is provided as a whole, it overrides everything else, no mix and match.
        if (StringUtils.isNotBlank(this.textStyle)) {
            return textStyle.toString();
        }

        ArrayList<String> components = new ArrayList<String>();

        if (StringUtils.isNotBlank(this.fontWeight) && !this.fontWeight.equals("normal"))
            components.add(this.fontWeight);
        if (StringUtils.isNotBlank(this.fontStyle) && !this.fontStyle.equals("normal"))
            components.add(this.fontStyle);
        if (StringUtils.isNotBlank(this.fontAntialiasing))
            components.add("antialias_"+this.fontAntialiasing);
        if (StringUtils.isNotBlank(this.fontHinting))
            components.add("hinting_"+this.fontHinting);
        if (StringUtils.isNotBlank(this.textDecoration) && !this.textDecoration.equals("none"))
            components.add(this.textDecoration);
        if (StringUtils.isNotBlank(this.textAlign))
            components.add(this.textAlign);
        if (StringUtils.isNotBlank(this.stroke) && !this.stroke.equals("none"))
            components.add(this.stroke);
        if (StringUtils.isNotBlank(this.letterSpacing))
            components.add("letter_spacing_" + this.letterSpacing);
        if (this.lineSpacing != null)
            components.add("line_spacing_" + this.lineSpacing.toString());

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
