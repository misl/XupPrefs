package nl.xup.prefs.memory;

import java.util.HashMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ItemListener;
import com.hazelcast.core.EntryEvent.EntryEventType;


/**
 * Distributed preferences implementation that keeps all preferences 
 * in memory across multiple JVM instances.
 * 
 * In memory preferences start out with a blank preferences tree. This 
 * implementation has no store and retrieve of preferences of its own. 
 * However, it is possible to use the standard import and export 
 * facilities of preferences:
 * 
 * Preferences.importPreferences( instream );
 * Preferences.systemRoot().exportSubtree( outstream );
 */
public class DistributedMemoryPreferences extends AbstractPreferences
	implements ItemListener<String>, EntryListener<String,String> {
	// ----------------------------------------------------------------------
    // Class fields
    // ----------------------------------------------------------------------
	
	private static final Logger logger = LoggerFactory.getLogger(DistributedMemoryPreferences.class);

    // ----------------------------------------------------------------------
    // Object fields
    // ----------------------------------------------------------------------
    
    private final IMap<String,String> _sharedPreferences;
    private final ISet<String> _sharedChildNames;
    private final HashMap<String,Preferences> _nodes = new HashMap<String,Preferences>();
    private final HashMap<String,String> _externalUpdates = new HashMap<String, String>();
    
    private boolean _usernode = false;
    private boolean _removed = false;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------
  
    /**
     * Construct root <code>MyPreferences</code> instance, construct 
     * user root if userNode is true, system root otherwise
     */
    DistributedMemoryPreferences(boolean userNode) {
        this(userNode, null, "");
    }
  
    /**
     * Construct a preferences node using given parent and given name 
     */
    private DistributedMemoryPreferences(DistributedMemoryPreferences parent, String name) {
        this(parent._usernode, parent, name);
    }
    
    /**
     * TODO: misl
     * @param userNode
     * @param parent
     * @param name
     */
    private DistributedMemoryPreferences(boolean userNode, DistributedMemoryPreferences parent, String name) {
        super(parent, name);

        _usernode = userNode;
        newNode = true;

        String sharedPreferencesMapName = getPreferencesMapName();
        String sharedChildNamesSetName = getNodeNamesSetName();
        
//        logger.debug("Created new node: (2 more lines)");
//        logger.debug("Distributed map with name value pairs: '{}'", sharedPreferencesMapName );
//        logger.debug("Distributed set with childnames: '{}'", sharedChildNamesSetName );
        
        _sharedPreferences = Hazelcast.getMap( sharedPreferencesMapName );
        _sharedPreferences.addEntryListener( this, true );
        _sharedChildNames = Hazelcast.getSet( sharedChildNamesSetName );
        _sharedChildNames.addItemListener( this, true );
    }

    // ----------------------------------------------------------------------
    // SPI implementation - AbstractPreferences
    // ----------------------------------------------------------------------
  
    @Override
	protected String[] childrenNamesSpi() throws BackingStoreException {
      int numberOfChildren = _sharedChildNames.size();
      return (String[]) _sharedChildNames.toArray( new String[numberOfChildren] );
    }

    @Override
	protected AbstractPreferences childSpi(String name) {
      // If the key does not yet exists create a new node.
      if ( !_nodes.containsKey( name ) ) {
          Preferences node = new DistributedMemoryPreferences( this, name );
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
        if ( _sharedPreferences.containsKey( key ) ) {
            // Yes, then we have to return it.
            result = (String) _sharedPreferences.get( key );
        }

        return result;
    }

    @Override
	protected String[] keysSpi() throws BackingStoreException {
        int numberOfKeys = _sharedChildNames.size();
        return (String[]) _sharedPreferences.keySet().toArray( new String[numberOfKeys] );
    }

    @Override
	protected void putSpi(String name, String value) {
    	// Only if the put came from within we have to 
    	// distribute it. If it came from the outside
    	// we can safely ignore it, since it is already
    	// stored in the distributed map.
    	boolean externalUpdate = false;
    	if ( _externalUpdates.containsKey( name ) &&
    			_externalUpdates.get( name ).equals( value ) ) {
    		externalUpdate = true;
    		_externalUpdates.remove( name );
    	}

    	if ( !externalUpdate ) {
    		// Not an external update, so go ahead and
    		// distribute it.
    		_sharedPreferences.put( name, value );
    	}
    }

    @Override
	protected void removeNodeSpi() throws BackingStoreException {
    	// Mark this node as being removed.
    	_removed = true;
    
    	// Remove listener we have set earlier.
//        _sharedChildNames.removeItemListener( this );
//        _sharedPreferences.removeEntryListener( this );

        // Childnode have already been removed according to spec.
        _nodes.clear(); 

        DistributedMemoryPreferences pref = (DistributedMemoryPreferences) parent();
        pref.removeChildNode( this.name() );
    }

    @Override
	protected void removeSpi(String key) {
        // Does a preference with the given key exist.
        if ( _sharedPreferences.containsKey( key ) ) {
            // Yes, then we have to remove it.
            _sharedPreferences.remove( key );
        }
    }

    @Override
	protected void syncSpi() throws BackingStoreException {
        // No sync needed since the data is synced automatically
    	// by using listeners on the distributed data.
    }

    // ----------------------------------------------------------------------
    // Overridden methods - AbstractPreferences
    // ----------------------------------------------------------------------
      
    /* (non-Javadoc)
     * This method is overridden to ensure MemoryPreferences can be 
     * used even if it is not the default configured preferences 
     * implementation.
     * @see java.util.prefs.AbstractPreferences#isUserNode()
     */
    @Override
    public boolean isUserNode() {
    	return _usernode;
    }

    // ----------------------------------------------------------------------
    // ItemListener implementation.
    // ----------------------------------------------------------------------

    public void itemAdded( String nodeName ) {
    	// A new preference node was created on another client of the 
    	// distributed set. Add the node here and fire the node added
    	// event.
    	// This is easily achieved by pretending to create a new node.
    	logger.info( "Distributed Event: Node added '{}' to '{}'", 
    			nodeName, this.absolutePath() );
    	this.node( nodeName );
    }
    
    public void itemRemoved(String nodeName) {
    	// An existing node was removed on another client of the 
    	// distribute set. Remove the node in this client and fire the
    	// node removed event.
    	// This is easily achieved by getting the particular node and
    	// removing it for this client as well.
    	if ( _nodes.containsKey( nodeName ) ) {
        	logger.info( "Distributed Event: Node removed '{}' from '{}'", 
        			nodeName, this.absolutePath() );
    		Preferences preferencesNode = _nodes.get( nodeName );
    		try {
        		preferencesNode.removeNode();
			} catch (BackingStoreException e) {
				// ignore.
			}
    	}
    }
    
    // ----------------------------------------------------------------------
    // EntryListener implementation.
    // ----------------------------------------------------------------------
        
    public void entryAdded(EntryEvent<String,String> event) {
    	// A value for this node was added on another client of the
    	// distributed map. Add the name and value to this node as well
    	// and by doing so fire the necessary events.
    	if ( event.getEventType() == EntryEventType.ADDED ) {
        	logger.info( "Distributed Event: Preference added ({} -> {}) to node '{}'", 
        			new Object[]{ event.getKey(), event.getValue(), this.absolutePath()} );
        	_externalUpdates.put( event.getKey(), event.getValue() );
    		put( (String)event.getKey(), (String)event.getValue() );
    	}
    }
    
    public void entryRemoved(EntryEvent<String,String> event) {
    	// A name-value pair for this node was removed on another client of 
    	// the distributed map. Add the name and value to this node as well
    	// and by doing so fire the necessary events.
    	if ( event.getEventType() == EntryEventType.REMOVED ) {
        	logger.info( "Distributed Event: Preference removed ({}) from node '{}'", 
        			event.getKey(), this.absolutePath() );
    		remove( event.getKey() );
    	}
    }
    
    public void entryUpdated(EntryEvent<String,String> event) {
    	// A value for this node was changed on another client of the
    	// distributed map. Add the name and value to this node as well
    	// and by doing so fire the necessary events.
    	if ( event.getEventType() == EntryEventType.UPDATED ) {
        	logger.info( "Distributed Event: Preference updated ({} -> {}) on node '{}'", 
        			new Object[]{ event.getKey(), event.getValue(), this.absolutePath()} );
        	_externalUpdates.put( event.getKey(), event.getValue() );
    		put( event.getKey(), event.getValue() );
    	}
    }
    
    public void entryEvicted(EntryEvent event) {
    	// Eviction if for caching purposes where LRU or LFU can be thrown
    	// out of the cache. We do not use that feature. If an event like
    	// this does happen it is a configuration issue.
    	logger.warn( "Distributed eviction event: check your configuration this event should not have happened!" );    	
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
        // Remove the child from distributed data as well.
        if ( _sharedChildNames.contains( nodeName ) ) {
        	// Yes, it is still there. Remove it as well.
        	_sharedChildNames.remove( nodeName );
        }
    }
    
    private String getPreferencesMapName() {
    	return (_usernode ? "user" : "system" ) + ":" +
    			"prefs:" + absolutePath();
    }

    private String getNodeNamesSetName() {
    	return (_usernode ? "user" : "system" ) + ":" +
    			"nodes:" + absolutePath();
    }
}
