package idv.coldcat.mixology_data_extractor;

public enum AppCommand {
	get_recipe_list_link {
		@Override
		public <IN, OUT> OUT accept(AppCommandVisitor<IN, OUT> visitor, IN in) {
			return visitor.visitGetRecipeListLink(in);
		}
	},
	get_recipe_link {
		@Override
		public <IN, OUT> OUT accept(AppCommandVisitor<IN, OUT> visitor, IN in) {
			return visitor.visitGetRecipeLink(in);
		}
	},
	get_recipe_data {
		@Override
		public <IN, OUT> OUT accept(AppCommandVisitor<IN, OUT> visitor, IN in) {
			return visitor.visitGetRecipeData(in);
		}
	};

	public abstract <IN, OUT> OUT accept(AppCommandVisitor<IN, OUT> visitor, IN in);

}
