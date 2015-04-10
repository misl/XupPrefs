package nl.xup.prefs.memory;

import java.util.prefs.PreferencesFactory;
import java.util.prefs.Preferences;

/**
 * Factory for the distributed in memory Preferences implementation.
 * 
 * No special configuration is needed. In memory preferences start out with
 * a blank preferences tree. This implementation has no store and retrieve
 * of preferences of its own. However, it is possible to use the standard 
 * import and export functionality of preferences:
 * 
 * Preferences.importPreferences( instream );
 * Preferences.systemRoot().exportSubtree( outstream );
 */
public class DistributedMemoryPreferencesFactory implements PreferencesFactory {

    // ----------------------------------------------------------------------
    // Attributes
    // ----------------------------------------------------------------------

	private final Preferences _systemRoot = new DistributedMemoryPreferences( false );
	private final Preferences _userRoot = new DistributedMemoryPreferences( true );

    // ----------------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------------
  
	public DistributedMemoryPreferencesFactory()
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
