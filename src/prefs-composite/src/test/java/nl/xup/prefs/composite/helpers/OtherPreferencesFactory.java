package nl.xup.prefs.composite.helpers;

import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 * This preference factory is solely used for testing and is actually
 * just a copy of the MemoryPreferencesFactory.
 */
public class OtherPreferencesFactory implements PreferencesFactory {

    // ----------------------------------------------------------------------
    // Attributes
    // ----------------------------------------------------------------------

	private final Preferences _systemRoot = new OtherPreferences( false );
	private final Preferences _userRoot = new OtherPreferences( true );

    // ----------------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------------
  
	public OtherPreferencesFactory()
	{
		super();
	}

	// ----------------------------------------------------------------------
	// Interface implementation - PreferencesFactory
	// ----------------------------------------------------------------------
  
	public Preferences systemRoot()
	{
		return _systemRoot;
	}

	public Preferences userRoot()
	{
		return _userRoot;
	}
}
