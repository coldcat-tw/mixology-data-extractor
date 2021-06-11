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

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Injector;

import idv.coldcat.mixology_data_extractor.RecipeDataCollector.RecipeData;

public class DataCollectionService {
	private static final Logger logger = LoggerFactory.getLogger(DataCollectionService.class);

	private Injector injector = null;

	public DataCollectionService(Injector injector) {
		super();
		this.injector = injector;
	}

	void collectRecipeListLinks(String outputPath) throws IOException {
		RecipeListUrlCollector recipeListUrlCollector = injector.getInstance(RecipeListUrlCollector.class);
		String url = null;
		Set<String> recipeListLinks = new LinkedHashSet<>();
		boolean hasNext = false;
		do {
			Set<String> result = recipeListUrlCollector.collect(url);
			recipeListLinks.addAll(result);
			if (recipeListUrlCollector.getNextUrl() != null) {
				url = recipeListUrlCollector.getNextUrl();
				hasNext = true;
				randomSleep();
			} else {
				hasNext = false;
			}
		} while (hasNext);
		String outputFilePath = outputPath + "/output/recipeListLinks.txt";
		outputResult(outputFilePath, recipeListLinks);

		logger.info(recipeListLinks.toString());
	}

	void collectRecipeLinks(String targetPath) throws IOException {
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

	void collectRecipeData(String targetPath) throws IOException {
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

}
