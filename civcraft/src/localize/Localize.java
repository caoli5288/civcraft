package localize;

/*
 * This plugin needs a default_lang.yml file in the jar file. This file includes the default strings.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.main.CivLog;

public class Localize {
	private JavaPlugin plugin;
	public String languageFile;
	
	public Localize(JavaPlugin plugin){
		this.plugin = plugin;
	}
	public Localize(JavaPlugin plugin, String langFile){
		this.plugin = plugin;
		this.setLanguageFile(langFile);
	}
	
	public void setLanguageFile(String langFile){
		if(langFile.equals("") || langFile == null){
			this.languageFile = "default_lang.yml";
		}else{
			this.languageFile = langFile;
		}
		this.reloadLocalizedStrings();
	}
	public String getLanguageFile(){
		return this.languageFile;
	}
	
	public String localizedString(String pathToString){
		Object value = this.getLocalizedStrings().get(pathToString);
		if(value==null)return pathToString;
		else return (String)this.getLocalizedStrings().get(pathToString);
	}
	public void loadDefaultLanguageFiles(){
		String oldLanguageFile = this.getLanguageFile();
		
		this.setLanguageFile("default_lang.yml");
		this.getLocalizedStrings().options().copyDefaults(true);
		this.saveLocalizedStrings();
		
		this.setLanguageFile(oldLanguageFile);
		this.reloadLocalizedStrings();
	}
	
	private FileConfiguration localizedStrings = null;
	private File localizedStringsFile = null;
	
	public void reloadLocalizedStrings() {	
		File localizedStringsFile = new File(plugin.getDataFolder().getPath()+"/localization/"+languageFile);
		Boolean isDefault = (languageFile.equalsIgnoreCase("default_lang.yml"));
		if (isDefault)
		{
			if (languageFile.equalsIgnoreCase("default_lang.yml"))
			{
			CivLog.warning("Configuration file:"+languageFile+" in use. Updating to disk from Jar.");
			try {
				CivSettings.streamResourceToDisk("/localization/"+languageFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
		} else if (!localizedStringsFile.exists()) {
			
			CivLog.warning("Configuration file:"+languageFile+" was missing. You must create this file in plugins/Civcraft/localization/");
			CivLog.warning("Using default_lang.yml");
			this.setLanguageFile("");
			this.loadDefaultLanguageFiles();
			return;
			
		}
	    localizedStrings = YamlConfiguration.loadConfiguration(localizedStringsFile);
		
		CivLog.info("Loading Configuration file:"+languageFile);
		// read the config.yml into memory
		YamlConfiguration cfg = new YamlConfiguration(); 
		try {
			cfg.load(localizedStringsFile);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        localizedStrings.setDefaults(cfg);
		
//	    localizedStringsFile = new File(CivSettings.plugin.getDataFolder().getPath()+"/data/"+languageFile);
//	    if (!localizedStringsFile.exists()) {
//			CivLog.warning("Configuration file: "+languageFile+" was missing. Streaming to disk from Jar.");
//			try {
//				CivSettings.streamResourceToDisk("/data/"+languageFile);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	    localizedStrings = YamlConfiguration.loadConfiguration(localizedStringsFile);
//	 
//	    InputStream defConfigStream = plugin.getResource("default_lang.yml");
//	    if (defConfigStream != null) {
//	    	Reader defConfigReader = new InputStreamReader(defConfigStream);
//	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigReader);
//	        localizedStrings.setDefaults(defConfig);
//	    }
	}
	private FileConfiguration getLocalizedStrings() {
	    if (localizedStrings == null) {
	        reloadLocalizedStrings();
	    }
	    return localizedStrings;
	}
	public void saveLocalizedStrings() {
	    if (localizedStrings == null || localizedStringsFile == null) {
	    return;
	    }
	    try {
	    	localizedStrings.save(localizedStringsFile);
	    } catch (IOException ex) {
	        Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save configuration to " + localizedStringsFile, ex);
	    }
	}
}