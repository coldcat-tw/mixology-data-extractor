package idv.coldcat.mixology_data_extractor;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class RecipeListUrlCollector {
	private static final Logger logger = LoggerFactory.getLogger(RecipeListUrlCollector.class);

	@Inject
	@Named("DEFAULT_UA")
	private String ua = null;

	@Inject
	@Named("MIXOLOGY_BASE_URL")
	private String mixologyBaseUrl = null;

	@Inject
	@Named("RECIPE_URI")
	private String mixologyRecipeUri = null;

	private Pattern pattern;
	private String nextUrl;

	@Inject
	public RecipeListUrlCollector() {
		this.pattern = Pattern.compile("後\\d{1,2}頁");
		this.nextUrl = null;
	}

	public Set<String> collect(String url) throws IOException {
		if (Strings.isNullOrEmpty(url))
			url = this.mixologyBaseUrl + this.mixologyRecipeUri;

		Set<String> recipeListLinks = new LinkedHashSet<>();
		recipeListLinks.add(url);
		Document doc = Jsoup.connect(url).userAgent(this.ua).get();
		Elements pageBarElement = doc.getElementsByClass("page_toolbar");
		Elements links = pageBarElement.select("a");
		for (Element link : links) {
			String urlText = link.ownText();
			String abshref = link.attr("abs:href");
			if (this.pattern.matcher(urlText).matches())
				this.nextUrl = abshref;
			logger.info(abshref + ";" + urlText);
			recipeListLinks.add(abshref);
		}
		return recipeListLinks;
	}

	public String getNextUrl() {
		return nextUrl;
	}

}
