/**
 * Internalization.java
 * Created On 2005, Dec 21, 2005 3:13:24 PM
 * @author E. Rajasekar
 */

package app.astrosoft.util;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import app.astrosoft.consts.DisplayFormat;
import app.astrosoft.consts.Language;
import app.astrosoft.ui.AstroSoft;

public class Internalization {

	private static final Logger log = Logger.getLogger(Internalization.class.getName());
	
	public static String bundleName = "resources.AstrosoftBundle";

	private static Map<Language, ResourceBundle> bundles;

	static {
		loadBundles();
	}

	private static void loadBundles() {
		bundles = new EnumMap<Language, ResourceBundle>(Language.class);
		
		for (Language l : Language.values()) {
			ResourceBundle bundle = ResourceBundle.getBundle(bundleName, Locale.forLanguageTag(l.isoCode()));
			bundles.put(l, bundle);
		}
	}

	private static final java.util.Set<String> reportedMissingKeys = java.util.concurrent.ConcurrentHashMap.newKeySet();
	private static final java.io.File MISSING_LOG_FILE = new java.io.File("logs/missing_translations.log");

	private static void logMissingTranslation(Language language, String key, String fallback) {
		String id = (language != null ? language.name() : "null") + ":" + key;
		if (reportedMissingKeys.add(id)) {
			log.fine("Missing localization for [" + language + "]: " + key + " -> falling back to '" + fallback + "'");
			try {
				java.io.File parent = MISSING_LOG_FILE.getParentFile();
				if (parent != null && !parent.exists()) {
					parent.mkdirs();
				}
				String entry = String.format("%s [%s] key=%s fallback=%s%n",
						java.time.Instant.now().toString(), language, key, fallback);
				java.nio.file.Files.writeString(
						MISSING_LOG_FILE.toPath(),
						entry,
						java.nio.file.StandardOpenOption.CREATE,
						java.nio.file.StandardOpenOption.APPEND);
			} catch (Throwable ignored) {}
		}
	}

	public static String getString(Language language, String key) {
		if (key == null) {
			return "";
		}
		
		ResourceBundle bundle = bundles.get(language);
		boolean langNotEnglish = (language != null && !language.isEnglish());

		String msg = null;
		if (bundle != null) {
			try {
				msg = bundle.getString(key);
			} catch (java.util.MissingResourceException e) {
				// fall through to English fallback
			}
		}

		if (msg == null && langNotEnglish) {
			ResourceBundle enBundle = bundles.get(Language.ENGLISH);
			if (enBundle != null) {
				try {
					msg = enBundle.getString(key);
				} catch (java.util.MissingResourceException ex) {
					logMissingTranslation(language, key, key);
					return key;
				}
			} else {
				logMissingTranslation(language, key, key);
				return key;
			}
			logMissingTranslation(language, key, msg);
		}
		return msg != null ? msg : key;
	}

	public static String getString(String key) {
		return getString(AstroSoft.getPreferences().getLanguage(), key);
	}

	public static String getString(Language language, Enum string) {
		return getString(language, string != null ? string.name() : null);
	}
	
	public static String getString(DisplayFormat format, String... args) {
		if (args == null || args.length == 0) {
			return "";
		}
		switch (format) {
			case FULL_NAME:
			default:
				return getString(args[0]);

			case SYMBOL:
				return args.length > 1 ? getString(args[1]) : getString(args[0]);
		}
	}
}
