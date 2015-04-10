package ${groupId}.${artifactId};

import java.util.HashMap;
import java.util.Iterator;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 * TODO
 */
public class MyPreferences extends AbstractPreferences {
	// ----------------------------------------------------------------------
	// Class fields
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Object fields
	// ----------------------------------------------------------------------
	
	private HashMap preferences = new HashMap();
	private HashMap nodes = new HashMap();

	// ----------------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------------
  
	/**
	 * Construct root <code>MyPreferences</code> instance, construct 
	 * user root if userNode is true, system root otherwise
	 */
	MyPreferences(boolean userNode) {
		super(null, "");
	}
  
	/**
	 * Construct a preferences node using given parent and given name 
	 */
	private MyPreferences(AbstractPreferences parent, String name) {
		super(parent, name);
	}

	// ----------------------------------------------------------------------
	// SPI implementation - AbstractPreferences
	// ----------------------------------------------------------------------
  
	  protected String[] childrenNamesSpi() throws BackingStoreException {
		  return (String[]) nodes.keySet().toArray( new String[0] );
	  }

	  protected AbstractPreferences childSpi(String name) {
		  // If the key does not yet exists create a new node.
		  if ( !nodes.containsKey( name ) ) {
			  Preferences node = new MyPreferences( this, name );
			  nodes.put( name, node );
		  }
		  
		  // Return the node corresponding to the key.
		  return (AbstractPreferences) nodes.get( name );
	  }

	  protected void flushSpi() throws BackingStoreException {
		  // No need to flush in memory preferences.
	  }

	  protected String getSpi(String key) {
		  String result = null;
		  
		  // Does a preference with the given key exist.
		  if ( preferences.containsKey( key ) ) {
			  // Yes, then we have to return it.
			  result = (String) preferences.get( key );
		  }
		  
		  return result;
	  }

	  protected String[] keysSpi() throws BackingStoreException {
		  return (String[]) preferences.keySet().toArray( new String[0] );
	  }

	  protected void putSpi(String name, String value) {
		  preferences.put( name, value );
	  }

	  protected void removeNodeSpi() throws BackingStoreException {
		  // Remove every childnode.
		  for( Iterator iter=nodes.keySet().iterator(); iter.hasNext(); ) {
			  Preferences pref = (Preferences) nodes.get( iter.next() );
			  pref.removeNode();
		  }
		  nodes.clear();
	  }

	  protected void removeSpi(String key) {
		  // Does a preference with the given key exist.
		  if ( preferences.containsKey( key ) ) {
			  // Yes, then we have to remove it.
			  preferences.remove( key );
		  }
	  }

	  protected void syncSpi() throws BackingStoreException {
		  // No sync needed
	  }
}
