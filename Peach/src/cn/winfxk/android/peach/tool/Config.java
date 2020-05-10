package cn.winfxk.android.peach.tool;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Winfxk
 */
public class Config {
	private String Text = "";
	private Map<String, Object> map = new HashMap<>();
	private Map<String, Object> isMap;
	private File file;
	private Yaml yaml;

	public File getFile() {
		return file;
	}

	public Config(File file) {
		this(file, new HashMap<String, Object>());
	}

	public Config(File file, Map<String, Object> map) {
		this.file = file;
		DumperOptions dumperOptions = new DumperOptions();
		dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		isMap = map;
		yaml = new Yaml(dumperOptions);
		try {
			load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean exists(String Key) {
		return map.containsKey(Key);
	}

	public Map<String, Object> getAll() {
		return map;
	}

	public String getContent() {
		return Text;
	}

	@Override
	public String toString() {
		return "File: " + file.getAbsolutePath() + "\nExists: " + file.exists() + "\n\n" + yaml.dump(map);
	}

	public boolean reload(HashMap<String, Object> map) {
		try {
			Utils.writeFile(file, yaml.dump(map));
			this.map = yaml.loadAs(Text = Utils.readFile(file), Map.class);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean reload() {
		try {
			load();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public Config setAll(Map<String, Object> map) {
		this.map = map;
		return this;
	}

	public Config set(String Key, Object obj) {
		map.put(Key, obj);
		return this;
	}

	public boolean getBoolean(String Key) {
		return getBoolean(Key, false);
	}

	public boolean getBoolean(String Key, boolean Default) {
		return Tool.ObjToBool(get(Key, Default), Default);
	}

	public int getInt(String Key) {
		return getInt(Key, 0);
	}

	public int getInt(String Key, int Default) {
		String string = getString(Key, null);
		if (string == null || string.isEmpty() || !Tool.isInteger(string))
			return Default;
		return Tool.ObjToInt(string, Default);
	}

	public String getString(String Key) {
		return getString(Key, null);
	}

	public String getString(String Key, String Default) {
		if (!map.containsKey(Key))
			return Default;
		Object object = map.get(Key);
		try {
			return (String) object;
		} catch (Exception e) {
			return String.valueOf(object);
		}
	}

	public Object get(String Key) {
		return get(Key, null);
	}

	public Object get(String Key, Object Default) {
		if (!map.containsKey(Key))
			return Default;
		return map.get(Key);
	}

	private void load() throws Exception {
		if (!file.exists())
			if (!save(isMap))
				throw new Exception("无法初始化配置文件！");
		map = yaml.loadAs(Text = Utils.readFile(file), Map.class);
	}

	public boolean save() {
		return save(map);
	}

	public boolean save(Map<String, Object> map) {
		Text = yaml.dump(map);
		try {
			Utils.writeFile(file, Text);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
