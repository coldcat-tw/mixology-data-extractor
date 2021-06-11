package idv.coldcat.mixology_data_extractor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;

import idv.coldcat.mixology_data_extractor.RecipeDataCollector.RecipeData;

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
		String outputFilePath = outputPath + "/output/recipeListLinks.txt";
		outputResult(outputFilePath, recipeListLinks);

		logger.info(recipeListLinks.toString());
	}

	private void collectRecipeLinks(String targetPath) throws IOException {
		Injector injector = Guice.createInjector(new ConfigurationModule("mixology-data-extractor"));
		RecipeUrlCollector recipeUrlCollector = injector.getInstance(RecipeUrlCollector.class);

		String recipeListLinksFilePath = targetPath + "/output/recipeListLinks.txt";
		Path path = Paths.get(recipeListLinksFilePath);
		boolean exists = Files.exists(path);
		logger.info("recipeListLinksFilePath;" + recipeListLinksFilePath + ";exists;" + exists);
		if (exists) {
			String content = FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
			Set<String> recipeLinks = new LinkedHashSet<>();
			for (String line : content.split("\n")) {
				logger.info("processing " + line);
				Set<String> result = recipeUrlCollector.collect(line);
				recipeLinks.addAll(result);
				randomSleep();
			}
			String outputFilePath = targetPath + "/output/recipeLinks.txt";
			outputResult(outputFilePath, recipeLinks);
		}
	}

	private void collectRecipeData(String targetPath) throws IOException {
		Injector injector = Guice.createInjector(new ConfigurationModule("mixology-data-extractor"));
		RecipeDataCollector recipeDataCollector = injector.getInstance(RecipeDataCollector.class);

		String recipeLinksFilePath = targetPath + "/output/recipeLinks.txt";
		Path path = Paths.get(recipeLinksFilePath);
		boolean exists = Files.exists(path);
		logger.info("recipeLinksFilePath;" + recipeLinksFilePath + ";exists;" + exists);
		if (exists) {
			String content = FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
			List<RecipeData> recipeDataList = new ArrayList<>();
			for (String line : content.split("\n")) {
				logger.info("processing " + line);
				RecipeData data = recipeDataCollector.collect(line);
				recipeDataList.add(data);
				randomSleep();
			}
			Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
			for (RecipeData data : recipeDataList) {
				String s = gson.toJson(data);
				String outputFilePath = targetPath + "/output/" + data.getId() + ".json";
				List<String> list = new ArrayList<>();
				list.add(s);
				outputResult(outputFilePath, list);
			}
		}
	}

	private void outputResult(String targetPath, Collection<String> result) throws IOException {
		final File f = new File(targetPath);
		StringBuffer sb = new StringBuffer();
		for (String s : result) {
			sb.append(s).append("\n");
		}
		FileUtils.writeStringToFile(f, sb.toString().trim(), StandardCharsets.UTF_8);
	}

	private int randomSleep() {
		return randomSleep(6, 2);
	}

	private int randomSleep(int upper, int lower) {
		Random random = new Random();
		int randomNumber = random.nextInt(upper - lower) + lower;
		try {
			logger.info("sleep " + randomNumber + " sec");
			Thread.sleep(randomNumber * 1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			e.printStackTrace();
		}
		return randomNumber;
	}

	private static Option helpOpt = Option.builder("h").longOpt("help").desc("help").build();
	private static Option cmdOpt = Option.builder("c").longOpt("command").hasArgs().argName("COMMAND> <OUTPUT_PATH").optionalArg(true).required()
			.desc("Commands of Mixology data extractor.\nAvailable commands are\nget_recipe_list_link\nget_recipe_link\nget_recipe_data").build();

	public static void main(String[] args) {

		App app = new App();
		try {
			CommandLine helpCmd = getHelpCommandLine(args);
			if (helpCmd != null) {
				printHelp();
			} else {
				Options options = new Options();
				options.addOption(cmdOpt);
				CommandLineParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(options, args);
				String[] commandValues = cmd.getOptionValues("c");
				String cmdName = commandValues[0];
				String targetPath = commandValues[1];

				switch (AppCommand.valueOf(cmdName)) {
				case get_recipe_list_link:
					app.collectRecipeListLinks(targetPath);
					break;
				case get_recipe_link:
					app.collectRecipeLinks(targetPath);
					break;
				case get_recipe_data:
					app.collectRecipeData(targetPath);
					break;
				default:
					break;
				}
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (ParseException e) {
			logger.error(e.toString());
			printHelp();
		}

	}

	private static void printHelp() {
		Options options = new Options();
		options.addOption(cmdOpt);
		options.addOption(helpOpt);

		HelpFormatter hFormatter = new HelpFormatter();
		hFormatter.printHelp(200, "app", "Mixology recipe data extractor commands", options, "", true);
	}

	private static CommandLine getHelpCommandLine(String[] args) {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmdLine;
		try {
			cmdLine = parser.parse(new Options().addOption(helpOpt), args);
		} catch (ParseException e) {
			cmdLine = null;
		}
		return cmdLine;
	}
}
