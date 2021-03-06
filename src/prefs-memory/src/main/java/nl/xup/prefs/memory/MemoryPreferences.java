package nl.xup.prefs.memory;

import java.util.HashMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 * Preferences implementation that keeps all preferences in memory.
 * 
 * In memory preferences start out with a blank preferences tree. This 
 * implementation has no store and retrieve of preferences of its own. 
 * However, it is possible to use the standard import and export 
 * facilities of preferences:
 * 
 * Preferences.importPreferences( instream );
 * Preferences.systemRoot().exportSubtree( outstream );

 */
public class MemoryPreferences extends AbstractPreferences {
	// ----------------------------------------------------------------------
    // Class fields
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // Object fields
    // ----------------------------------------------------------------------
    
    private final HashMap<String,String> _preferences = new HashMap<String,String>();
    private final HashMap<String,Preferences> _nodes = new HashMap<String,Preferences>();
    
    private boolean _usernode = false;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------
  
    /**
     * Construct root <code>MyPreferences</code> instance, construct 
     * user root if userNode is true, system root otherwise
     */
    MemoryPreferences(boolean userNode) {
        super(null, "");
        
        _usernode = userNode;
        newNode = true;
    }
  
    /**
     * Construct a preferences node using given parent and given name 
     */
    private MemoryPreferences(MemoryPreferences parent, String name) {
        super(parent, name);

        _usernode = parent._usernode;
        newNode = true;
    }

    // ----------------------------------------------------------------------
    // SPI implementation - AbstractPreferences
    // ----------------------------------------------------------------------
  
    @Override
	protected String[] childrenNamesSpi() throws BackingStoreException {
      return (String[]) _nodes.keySet().toArray( new String[0] );
    }

    @Override
	protected AbstractPreferences childSpi(String name) {
      // If the key does not yet exists create a new node.
      if ( !_nodes.containsKey( name ) ) {
          Preferences node = new MemoryPreferences( this, name );
          _nodes.put( name, node );
      }
          
      // Return the node corresponding to the key.
      return (AbstractPreferences) _nodes.get( name );
    }

    @Override
	protected void flushSpi() throws BackingStoreException {
        // No need to flush in memory preferences.
    }

    @Override
	protected String getSpi(String key) {
        String result = null;

        // Does a preference with the given key exist.
        if ( _preferences.containsKey( key ) ) {
            // Yes, then we have to return it.
            result = (String) _preferences.get( key );
        }

        return result;
    }

    @Override
	protected String[] keysSpi() throws BackingStoreException {
        return (String[]) _preferences.keySet().toArray( new String[0] );
    }

    @Override
	protected void putSpi(String name, String value) {
        _preferences.put( name, value );
    }

    @Override
	protected void removeNodeSpi() throws BackingStoreException {
        // Childnode have already been removed according to spec.
        _nodes.clear(); 
        _preferences.clear();

        MemoryPreferences pref = (MemoryPreferences) parent();
        pref.removeChildNode( this.name() );
    }

    @Override
	protected void removeSpi(String key) {
        // Does a preference with the given key exist.
        if ( _preferences.containsKey( key ) ) {
            // Yes, then we have to remove it.
            _preferences.remove( key );
        }
    }

    @Override
	protected void syncSpi() throws BackingStoreException {
        // No sync needed
    }

    // ----------------------------------------------------------------------
    // Overridden methods - AbstractPreferences
    // ----------------------------------------------------------------------
      
    /* (non-Javadoc)
     * This method is overridden to ensure MemoryPreferences can even be 
     * used even if they are not the default configured preferences 
     * implementation.
     * @see java.util.prefs.AbstractPreferences#isUserNode()
     */
    @Override
    public boolean isUserNode() {
    	return _usernode;
    }
    
    // ----------------------------------------------------------------------
    // Private methods.
    // ----------------------------------------------------------------------
    
    private void removeChildNode( String nodeName ) {
        // Does a node with the given name exist.
        if ( _nodes.containsKey( nodeName ) ) {
            // Yes, then we have to remove it.
            _nodes.remove( nodeName );
        }
    }
}
