package idv.coldcat.mixology_data_extractor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class App {
	private static final Logger logger = LoggerFactory.getLogger(App.class);

	private void collectRecipeListLinks(String outputPath) throws IOException {
		Injector injector = Guice.createInjector(new ConfigurationModule("mixology-data-extractor"));
		String url = null;
		Set<String> recipeListLinks = new LinkedHashSet<>();
		boolean hasNext = false;
		do {
			RecipeListUrlCollector recipeListUrlCollector = injector.getInstance(RecipeListUrlCollector.class);
			Set<String> result = recipeListUrlCollector.collect(url);
			recipeListLinks.addAll(result);
			if (recipeListUrlCollector.getNextUrl() != null) {
				url = recipeListUrlCollector.getNextUrl();
				hasNext = true;
			} else {
				hasNext = false;
			}
		} while (hasNext);
		logger.info(outputPath);
		if (!Strings.isNullOrEmpty(outputPath)) {
			final File f = new File(outputPath + "/output/recipeListLinks.txt");
	        FileUtils.writeStringToFile(f, recipeListLinks.toString(), StandardCharsets.UTF_8);
		}

		logger.info(recipeListLinks.toString());
	}

	public static void main(String[] args) {

		App app = new App();
		try {
			Option option = Option.builder("c").longOpt("command").hasArgs().required()
					.desc("Admin command to execute").build();
			Options options = new Options();
			options.addOption(option);
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);
			String[] commandValues = cmd.getOptionValues("c");
			String cmdName = commandValues[0];
			if ("get_recipe_list_link".equals(cmdName)) {
				logger.info(cmdName);
				String targetPath = commandValues[1];
				logger.info(targetPath);
				app.collectRecipeListLinks(targetPath);
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ParseException e) {
			logger.error(e.toString());
		}

	}
}
