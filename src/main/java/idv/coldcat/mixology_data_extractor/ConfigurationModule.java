package idv.coldcat.mixology_data_extractor;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class ConfigurationModule extends AbstractModule {
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationModule.class);
	
	private String confName = null;
	
	public ConfigurationModule(String confName) {
		super();
		this.confName = confName;
	}

	@Override
	protected void configure() {
		Properties props = new Properties();
		try {
			props.load(this.getClass().getClassLoader().getResourceAsStream(this.confName + ".properties"));
			Names.bindProperties(binder(), props);
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	

}
