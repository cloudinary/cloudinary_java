package com.cloudinary.strategies;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.cloudinary.utils.StringUtils;

public class StrategyLoader {

	@SuppressWarnings("unchecked")
	public static <T> T load(String className) {
		T result = null;
		try {
			Class<?> clazz = Class.forName(className);
			result = (T) clazz.newInstance();
		} catch (Exception e) {
		}
		return result;
	}

	public static <T> T find(List<String> strategies) {
		for (int i = 0; i < strategies.size(); i++) {
			T strategy = load(strategies.get(i));
			if (strategy != null) {
				return strategy;
			}
		}
		return null;
		
	}

	public boolean exists(List<String> strategies) {
		return find(strategies) != null;
	}
	
}
