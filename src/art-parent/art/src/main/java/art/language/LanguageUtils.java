package art.language;

import java.util.Map;
import java.util.TreeMap;

/**
 * Class to retrieve available languages/translations for the application
 *
 * @author Timothy Anyona
 */
public class LanguageUtils {

	/**
	 * Get available application languages. Doesn't include default (English)
	 *
	 * @return map with available application languages. key=locale code,
	 * value=language name
	 */
	public static Map<String, String> getLanguages() {
		//use a treemap so that languages are displayed in alphabetical order (of language codes)
		//don't include default (english)
		//see http://people.w3.org/rishida/names/languages.html for language names
		Map<String, String> languages = new TreeMap<String, String>();

		languages.put("es", "Español"); //spanish
		languages.put("fr", "Français"); //french
		languages.put("hu", "Magyar"); //hungarian
		languages.put("it", "Italiano"); //italian
		languages.put("pt_BR", "Português (Brasil)"); //brazilian portuguese
		languages.put("sw", "Kiswahili"); //swahili
		languages.put("zh_CN", "简体中文"); //simplified chinese
		languages.put("zh_TW", "繁體中文"); //traditional chinese

		return languages;
	}
}
