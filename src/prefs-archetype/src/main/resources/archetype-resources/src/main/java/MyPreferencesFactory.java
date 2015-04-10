package ${groupId}.${artifactId};

import java.util.prefs.PreferencesFactory;
import java.util.prefs.Preferences;

/**
 * TODO
 */
public class MyPreferencesFactory implements PreferencesFactory {
	// ----------------------------------------------------------------------
	// Class fields
	// ----------------------------------------------------------------------
	private static final Preferences _systemRoot = new MyPreferences( false );
	private static final Preferences _userRoot = new MyPreferences( true );

	// ----------------------------------------------------------------------
	// Static initializer
	// ----------------------------------------------------------------------
	static 
	{
		// Retrieve system properties needed for configuration.
		// TODO
	}

	// ----------------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------------
  
	public MyPreferencesFactory()
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
