package nl.xup.prefs.memory;

import java.io.InputStream;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for the in memory Preferences implementation.
 * 
 * No special configuration is needed. In memory preferences start out with
 * a blank preferences tree. This implementation has no store and retrieve
 * of preferences of its own. However, it is possible to use the standard 
 * import and export of preferences:
 * 
 * Preferences.importPreferences( instream );
 * Preferences.systemRoot().exportSubtree( outstream );
 */
public class MemoryPreferencesFactory implements PreferencesFactory {

    // ----------------------------------------------------------------------
    // Class Attributes
    // ----------------------------------------------------------------------
	
	private static final Logger log = LoggerFactory.getLogger( MemoryPreferencesFactory.class );
	
	private static final String SP_IMPORT_FILE = "nl.xup.prefs.memory.import";
	private static final String DEFAULT_IMPORT_FILE = "memory.prefs.xml";
	
    // ----------------------------------------------------------------------
    // Object Attributes
    // ----------------------------------------------------------------------

	private final Preferences _systemRoot = new MemoryPreferences( false );
	private final Preferences _userRoot = new MemoryPreferences( true );
	private boolean initialized = false;

    // ----------------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------------
  
	public MemoryPreferencesFactory()
	{
		super();
	}

	// ----------------------------------------------------------------------
	// Interface implementation - PreferencesFactory
	// ----------------------------------------------------------------------
  
	public Preferences systemRoot()
	{
		if ( !initialized ) {
			initialImport();
		}
		
		return _systemRoot;
	}

	public Preferences userRoot()
	{
		if ( !initialized ) {
			initialImport();
		}
		
		return _userRoot;
	}

	// ----------------------------------------------------------------------
	// Private methods
	// ----------------------------------------------------------------------
	
	private void initialImport() {
		// Whatever happens, don't initialize again.
		initialized = true;
		
		// Get import file to be used.
		String importFile = System.getProperty( SP_IMPORT_FILE, DEFAULT_IMPORT_FILE );
		InputStream is = getClass().getClassLoader().getResourceAsStream( importFile );
		
		// If it exists, import it
		if ( is != null ) {
			log.info( "Initially importing: {} ", importFile );
			try {
				Preferences.importPreferences(is);
			} catch (Exception e) {
				log.error( "Initial import failed!", e );
			}
		} else {
			log.info( "Initial import '{}' does not exist, import skipped!", importFile );
		}
	}
}
