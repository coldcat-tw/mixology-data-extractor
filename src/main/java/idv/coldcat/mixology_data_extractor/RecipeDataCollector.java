package idv.coldcat.mixology_data_extractor;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecipeDataCollector {
	private static final Logger logger = LoggerFactory.getLogger(RecipeDataCollector.class);

	@Inject
	@Named("DEFAULT_UA")
	private String ua = null;

	public RecipeData collect(String url) throws IOException {
		Document doc = Jsoup.connect(url).userAgent(this.ua).get();
		RecipeData data = new RecipeData();
		processId(url, data);
		processName(doc, data);
		processEngName(doc, data);
		processIntroduction(doc, data);
		processIngredientsAndSteps(doc, data);
		logger.info("Got recipe data;" + data.getId());

		return data;
	}

	private void processIngredientsAndSteps(Document doc, RecipeData data) {
		Elements ingredientsElem = doc.getElementsByClass("recipe_detail_ingred");
		for (Element elem : ingredientsElem) {
			Elements detailContentElements = elem.getElementsByClass("recipe_detail_content");
			if (detailContentElements.size() > 0) {
				Elements notThElements = elem.select("tr:not(:has(th))");
				Elements filteredElements = notThElements.not("tr:last-child");
				String unescapeEntities = filteredElements.text().replaceAll("\\&nbsp", "");
				data.setIngredients(unescapeEntities);
				
				// process steps
				processSteps(data, detailContentElements);
			}
		}
	}

	private void processSteps(RecipeData data, Elements detailContentElements) {
		Elements stepsElements = detailContentElements.select("div:first-child");
		List<TextNode> stepsContentList = stepsElements.textNodes();
		StringBuffer sb2 = new StringBuffer();
		for (TextNode s : stepsContentList) {
			sb2.append(s.text()).append("\n");
		}
		data.setSteps(sb2.toString().trim());
	}

	private void processIntroduction(Document doc, RecipeData data) {
		Element introElem = doc.getElementById("CT1_RecipeIntro");
		if (introElem != null) {
			data.setIntro(introElem.wholeText());
		}
	}

	private void processEngName(Document doc, RecipeData data) {
		Element engNameElem = doc.getElementById("CT1_RecipeEName");
		if (engNameElem != null) {
			data.setEngName(engNameElem.ownText());
		}
	}

	private void processName(Document doc, RecipeData data) {
		Element nameElem = doc.getElementById("CT1_RecipeCName");
		if (nameElem != null) {
			data.setName(nameElem.ownText());
		}
	}

	private void processId(String url, RecipeData data) {
		String[] split = url.split("=");
		String id = split[1];
		data.setId(id);
	}
	
	public class RecipeData {
		private String id;
		private String name;
		private String engName;
		private String intro;
		private String ingredients;
		private String steps;

		public RecipeData() {}
		
		public RecipeData(String id, String name, String engName, String intro, String ingredients, String steps) {
			this.setId(id);
			this.name = name;
			this.engName = engName;
			this.intro = intro;
			this.ingredients = ingredients;
			this.steps = steps;
		}
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getEngName() {
			return engName;
		}

		public void setEngName(String engName) {
			this.engName = engName;
		}

		public String getIntro() {
			return intro;
		}

		public void setIntro(String intro) {
			this.intro = intro;
		}

		public String getIngredients() {
			return ingredients;
		}

		public void setIngredients(String ingredients) {
			this.ingredients = ingredients;
		}

		public String getSteps() {
			return steps;
		}

		public void setSteps(String steps) {
			this.steps = steps;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		@Override
		public String toString() {
			return "RecipeData [id=" + id + ", name=" + name + ", engName=" + engName + ", intro=" + intro
					+ ", ingredients=" + ingredients + ", steps=" + steps + "]";
		}

	}
}
