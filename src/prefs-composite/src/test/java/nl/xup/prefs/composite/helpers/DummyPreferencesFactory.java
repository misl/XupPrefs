/**
 * 
 */
package nl.xup.prefs.composite.helpers;

import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 * Dummy Preferences Factory implementation which can not be instantiated.
 * This one is used to test exceptions in one of the CompositeRule
 * constructors.
 */
public class DummyPreferencesFactory implements PreferencesFactory {

	// ----------------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------------
  
	private DummyPreferencesFactory()
	{
		super();
	}

	// ----------------------------------------------------------------------
	// Interface implementation - PreferencesFactory
	// ----------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see java.util.prefs.PreferencesFactory#systemRoot()
	 */
	public Preferences systemRoot() {
		return null;
	}

	/* (non-Javadoc)
	 * @see java.util.prefs.PreferencesFactory#userRoot()
	 */
	public Preferences userRoot() {
		return null;
	}

}
