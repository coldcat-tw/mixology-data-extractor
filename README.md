# mixology-data-extractor
用來擷取 [Mixology 酒譜](https://mixology.com.tw/Recipe.aspx) 資料的 command line 工具。

## 編譯方式 ##
本工具使用 maven 協助構建與管理套件，請在 `git clone` 之後，在專案根目錄執行以下指令進行編譯及打包 jar 檔

```
mvn clean install
mvn package
```

## 執行方式 ##

```
usage: app -c <COMMAND> <OUTPUT_PATH> [-h]
Mixology recipe data extractor commands
 -c,--command <COMMAND> <OUTPUT_PATH>   Commands of Mixology data extractor.
                                        Available commands are
                                        get_recipe_list_link
                                        get_recipe_link
                                        get_recipe_data
 -h,--help                              help

```


### 擷取酒譜列表清單網址 ###
擷取酒譜列表網址並儲存到 `<OUTPUT_PATH>/output/recipeListLinks.txt`

```
java -cp mixology-data-extractor-0.0.1-SNAPSHOT.jar idv.coldcat.mixology_data_extractor.App -c "get_recipe_list_link" "/folder/to/use"
```

### 擷取各別酒譜網址 ###
根據酒譜列表網址，解析酒譜列表清單網頁後，取得各別酒譜網址並儲存到 `<OUTPUT_PATH>/output/recipeLinks.txt` 

```
java -cp mixology-data-extractor-0.0.1-SNAPSHOT.jar idv.coldcat.mixology_data_extractor.App -c "get_recipe_link" "/folder/contains/recipeListLinks"
```

### 擷取各別酒譜內容 ###
根據各別酒譜網址，解析酒譜內容網頁擷取所需資訊後，儲存至 `<OUTPUT_PATH>/output` (JSON 格式)

```
java -cp mixology-data-extractor-0.0.1-SNAPSHOT.jar idv.coldcat.mixology_data_extractor.App -c "get_recipe_data" "/folder/contains/recipeLinks"
```
