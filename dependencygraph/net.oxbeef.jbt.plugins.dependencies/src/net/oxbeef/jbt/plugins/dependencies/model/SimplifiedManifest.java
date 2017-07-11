package net.oxbeef.jbt.plugins.dependencies.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class SimplifiedManifest {
	private boolean isValid = true;
	private String pluginName = null;
	private ArrayList<String> dependencies;

	public SimplifiedManifest(File f) throws IOException {
		Manifest mf = new Manifest(new FileInputStream(f));
		Map<String, Attributes> map = mf.getEntries();
		Attributes main = mf.getMainAttributes();
		String a = main.getValue("Bundle-SymbolicName");
		pluginName = a == null ? null : !a.contains(";") ? a.trim() : a.substring(0, a.indexOf(";")).trim();
		String req = main.getValue("Require-Bundle");
		if (req == null) {
			dependencies = new ArrayList<String>();
			return;
		}
		String req2 = req.trim().replaceAll("\\\"[^\\\"]*\\\"", "\"\"");
		String req3 = req2.replaceAll(";[^,]*,", ",");
		String[] asDeps = req3.split(",");
		if (asDeps[asDeps.length - 1].contains(";")) {
			asDeps[asDeps.length - 1] = asDeps[asDeps.length - 1].substring(0,
					asDeps[asDeps.length - 1].indexOf(";"));
		}
		ArrayList<String> depsList = new ArrayList<String>();
		depsList.addAll(Arrays.asList(asDeps));
		dependencies = depsList;
		if( depsList.contains("4.0.0)")) {
			System.out.println("BREAK");
		}
		System.out.println(a);
	}

	public String getPluginName() {
		return pluginName;
	}

	public ArrayList<String> getDependencies() {
		return dependencies;
	}

	public boolean isValid() {
		return isValid;
	}
}