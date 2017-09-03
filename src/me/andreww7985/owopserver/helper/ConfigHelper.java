package me.andreww7985.owopserver.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class ConfigHelper {
	private final Properties props = new Properties();
	private final String fileName;
	private final String comment;

	public ConfigHelper(final String fileName, final boolean createIfNonExistant, final String comment)
			throws FileNotFoundException, IOException {
		this.fileName = fileName;
		this.comment = comment;
		try {
			props.load(new BufferedReader(new FileReader(fileName)));
		} catch(final FileNotFoundException e) {
			if (createIfNonExistant) {
				writeProps();
			} else {
				throw e;
			}
		}
	}
	
	public void setDefaultProperty(final String key, final String value) {
		if (!props.containsKey(key)) {
			props.setProperty(key, value);
		}
	}

	public void writeProps() throws IOException {
		props.store(new BufferedWriter(new FileWriter(fileName)), comment);
	}
	
	public Properties getProperties() {
		return props;
	}
}
