package idv.coldcat.mixology_data_extractor;

public interface AppCommandVisitor<IN, OUT> {

	OUT visitGetRecipeListLink(IN in);

	OUT visitGetRecipeLink(IN in);

	OUT visitGetRecipeData(IN in);

}
