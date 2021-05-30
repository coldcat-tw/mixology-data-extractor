package idv.coldcat.mixology_data_extractor;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecipeUrlCollector {
	private static final Logger logger = LoggerFactory.getLogger(RecipeUrlCollector.class);

	@Inject
	@Named("DEFAULT_UA")
	private String ua = null;

	public Set<String> collect(String url) throws IOException {
		Set<String> recipeLinks = new LinkedHashSet<>();
		Document doc = Jsoup.connect(url).userAgent(this.ua).get();
		Elements pageBarElement = doc.getElementsByClass("recipe_list_content");
		Elements links = pageBarElement.select("a");
		for (Element link : links) {
			String abshref = link.attr("abs:href");
			logger.info(abshref);
			recipeLinks.add(abshref);
		}
		return recipeLinks;
	}

}
