package idv.coldcat.mixology_data_extractor;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class App {
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	private static Option helpOpt = Option.builder("h").longOpt("help").desc("help").build();
	private static Option cmdOpt = Option.builder("c").longOpt("command").hasArgs().argName("COMMAND> <OUTPUT_PATH")
			.optionalArg(true).required()
			.desc("Commands of Mixology data extractor.\nAvailable commands are\nget_recipe_list_link\nget_recipe_link\nget_recipe_data")
			.build();

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
				
				Injector injector = Guice.createInjector(new ConfigurationModule("mixology-data-extractor"));
				DataCollectionService service = new DataCollectionService(injector);

				switch (AppCommand.valueOf(cmdName)) {
				case get_recipe_list_link:
					service.collectRecipeListLinks(targetPath);
					break;
				case get_recipe_link:
					service.collectRecipeLinks(targetPath);
					break;
				case get_recipe_data:
					service.collectRecipeData(targetPath);
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
