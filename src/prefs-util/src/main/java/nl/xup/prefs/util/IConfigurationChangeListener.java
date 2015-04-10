package nl.xup.prefs.util;

/**
 * Object die wijzigingen in de configuratie bij wensen te houden, dienen
 * dit interface te implementeren. Tevens dienen zij zich als listener aan te 
 * melden bij een afgeleide van <code>PreferencesStore</code>. 
 * <p>
 * Patterns: Observer
 * </p>
 * 
 * @author Minto van der Sluis
 */
public interface IConfigurationChangeListener {

	/**
	 * Geeft aan dat de configuratie van de meegekregen class is gewijzigd.
	 * @param clazz de class wiens configuratie is gewijzigd.
	 */
	void onConfigurationChanged( Class<? extends PreferencesStore> clazz );
}
