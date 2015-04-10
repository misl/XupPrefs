package nl.xup.prefs.util;

import nl.xup.prefs.util.IConfigurationChangeListener;
import nl.xup.prefs.util.PreferencesStore;

/**
 * @author Minto van der Sluis
 */
public class PreferenceStoreTestHelper extends PreferencesStore {
	
    // ----------------------------------------------------------------------
    // Class fields
    // ----------------------------------------------------------------------
	public static final String STRING_KEY  = "string";
	public static final String INTEGER_KEY = "integer";
	public static final String BOOLEAN_KEY = "boolean";
	
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PreferenceStoreTestHelper() {
    	super( PreferenceStoreTestHelper.class );
    	
    	// Niet het default pad nemen, maar het pad van de opgegeven node.
    	_nodePath = "/level1/level2";
    }

    // ----------------------------------------------------------------------
    // Getters / Setters
    // ----------------------------------------------------------------------
    
    public String getString() {
    	return getPreference( STRING_KEY );
    }

    public void setString( String waarde ) {
    	setPreference( STRING_KEY, waarde );
    }

    public Integer getInteger() {
    	return getPreferenceInteger( INTEGER_KEY );
    }

    public void setInteger( Integer waarde ) {
    	setPreference( INTEGER_KEY, waarde );
    }

    public boolean getBoolean() {
    	return getPreferenceBoolean( BOOLEAN_KEY );
    }

    public void setBoolean( boolean waarde ) {
    	setPreference( BOOLEAN_KEY, waarde );
    }

    // ----------------------------------------------------------------------
    // Interface
    // ----------------------------------------------------------------------

    /**
	 * Listener voor configuratie wijzigingen aanmelden.
	 * @param listener
	 */
	public void addListener( IConfigurationChangeListener listener ) {
		addInternalListener( listener );
	}
	
	/**
	 * Listener voor configuratie wijzigingen afmelden.
	 * @param listener
	 */
	public void removeListener( IConfigurationChangeListener listener ) {
		removeInternalListener( listener );
	}
}
