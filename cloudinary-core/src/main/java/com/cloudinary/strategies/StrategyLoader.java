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

	
	@Deprecated
	public static ArrayList<String> getClassNamesFromPackage(String packageName) throws IOException, URISyntaxException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL packageURL;
		ArrayList<String> names = new ArrayList<String>();
		

		packageName = packageName.replace(".", "/");
		packageURL = classLoader.getResource(packageName);
		
		if (packageURL.getProtocol().equals("jar")) {
			String jarFileName;
			JarFile jf;
			Enumeration<JarEntry> jarEntries;
			String entryName;

			// build jar file name, then loop through zipped entries
			jarFileName = URLDecoder.decode(packageURL.getFile(), "UTF-8");
			jarFileName = jarFileName.substring(5, jarFileName.indexOf("!"));
			System.out.println(">" + jarFileName);
			jf = new JarFile(jarFileName);
			jarEntries = jf.entries();
			while (jarEntries.hasMoreElements()) {
				entryName = jarEntries.nextElement().getName();
				if (entryName.startsWith(packageName) && entryName.length() > packageName.length() + 5) {
					entryName = entryName.substring(packageName.length(), entryName.lastIndexOf('.'));
					names.add(entryName);
				}
			}

			// loop through files in classpath
		} else {
			URI uri = new URI(packageURL.toString());
			File folder = new File(uri.getPath());
			// won't work with path which contains blank (%20)
			// File folder = new File(packageURL.getFile());
			File[] contenuti = folder.listFiles();
			System.out.println("files:");
			System.out.println(StringUtils.join(contenuti, "\n"));
			System.out.println("-- -- --");
			String entryName;
			for (File actual : contenuti) {
				entryName = actual.getName();
				System.out.println(entryName);
				entryName = entryName.substring(0, entryName.lastIndexOf('.'));
				names.add(entryName);
			}
		}
		return names;
	}

}
