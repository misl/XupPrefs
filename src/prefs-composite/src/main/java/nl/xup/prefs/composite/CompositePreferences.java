package nl.xup.prefs.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 * TODO: doc it
 */
public class CompositePreferences extends AbstractPreferences
		implements PreferenceChangeListener, NodeChangeListener {

    // ----------------------------------------------------------------------
    // Object fields
    // ----------------------------------------------------------------------
    
    private CompositePreferencesFactory _factory;

    private boolean _usernode = false;
    private CompositeRule _rule = null;
    private Preferences _node = null;
    
    private List<PreferenceChangeListener> _prefListeners = new ArrayList<PreferenceChangeListener>();
    private List<NodeChangeListener> _nodeListeners = new ArrayList<NodeChangeListener>();
    
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------
  
    /**
     * Construct root <code>CompositePreferences</code> instance, construct 
     * user root if userNode is true, system root otherwise
     */
    CompositePreferences( CompositePreferencesFactory factory, boolean userNode ) {
        super(null, "");
        
        _factory = factory;
        _usernode = userNode;
        _rule = _factory.findMatchingRule( this.absolutePath() );
        _node = getLeafNode();
    }

    /**
     * Construct a preferences node using given parent and given name 
     */
    private CompositePreferences( CompositePreferences parent, String name ) {
        this(parent, name, null);
    }

    /**
     * Construct a preferences node using given parent, name and underlying
     * leaf node. 
     */
    private CompositePreferences( CompositePreferences parent, String name, Preferences leafNode ) {
        super(parent, name);

        _factory = parent._factory;
        _usernode = parent._usernode;
        _rule = _factory.findMatchingRule( this.absolutePath() );
        
        _node = leafNode;
        // Determine leafNode if not know already.
        if ( _node == null ) {
            _node = getLeafNode();
        }
    }

    // ----------------------------------------------------------------------
    // Equals and Hashcode
    // ----------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj ) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }
        // By now the object must be of the same type as this object is.
        final CompositePreferences node = (CompositePreferences) obj;
        
        // Composite preference nodes are the same if they share the same
        // factory and the same leaf node.
        return _factory.equals( node._factory) && _node.equals( node._node );
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
   	    return ( null == _factory ? 0 : _factory.hashCode() * _node.hashCode() );
    }
    
    // ----------------------------------------------------------------------
    // SPI implementation - AbstractPreferences
    // ----------------------------------------------------------------------
  
    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        // Only ask the leaf for its children if the leaf still exists. We
    	// need to determine this with a work-around otherwise we might get
    	// hit by the "Node has been removed" IllegalStateException.
    	if ( getLeafRoot( _rule.getFactory() ).nodeExists( _node.absolutePath() ) ){
    		return _node.childrenNames();
    	}
    	// leaf node no longer exists, so no children.
    	return new String[0];
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        // Create a new composite node with the given name.
        CompositePreferences child = new CompositePreferences(this, name);
        return child;
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
    	// Since here we wrap multiple preferences implementation, all 
    	// we have to do is pass the flush call to every wrapped
    	// implementation.
    	// NOTE: not only the currently wrapped implementation (factory)
    	//       but all implementations (factories). Subnodes might
    	//       be handled by other implementations and flush should
    	//       flush the full subtree.
        for( PreferencesFactory factory : _factory.getLeafFactories() ) {
            // Get the right root node from this factory.
            Preferences node = getLeafRoot( factory );
            
            // Does the node exist in the current leaf?
            if ( node.nodeExists( this.absolutePath() ) ) {
                // yes, then flush it.
                node.node( this.absolutePath() ).flush();
            }
        }
    }

    @Override
    protected String getSpi(String key) {
        // Pass on the leaf being used.
        return _node.get( key, null );
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException {
        // Ask the leaf for the available keys.
        return _node.keys();
    }

    @Override
    protected void putSpi(String name, String value) {
        // Pass on the leaf being used.
        _node.put( name, value );
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        // When a node should not be in the composite node tree,
    	// it should not be in any of the proxied leafs.
        for( PreferencesFactory factory : _factory.getLeafFactories() ) {
            // Get the right root node from this factory.
            Preferences node = getLeafRoot( factory );

            // Does the node exist in the current leaf?
            if ( node.nodeExists( this.absolutePath() ) ) {
                // yes, then remove it.
                node.node( this.absolutePath() ).removeNode();
            }
        }
    }

    @Override
    protected void removeSpi(String key) {
        // Pass on the leaf being used.
        _node.remove( key );
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
    	// Since here we wrap multiple preferences implementation, all 
    	// we have to do is pass the sync call to every wrapped
    	// implementation.
    	// NOTE: not only the currently wrapped implementation (factory)
    	//       but all implementations (factories). Subnodes might
    	//       be handled by other implementations and sync should
    	//       sync the full subtree.
        for( PreferencesFactory factory : _factory.getLeafFactories() ) {
            // Get the right root node from this factory.
            Preferences node = getLeafRoot( factory );

            // Does the node exist in the current leaf?
            if ( node.nodeExists( this.absolutePath() ) ) {
                // yes, then sync it.
                node.node( this.absolutePath() ).sync();
            }
        }
    }

	// ----------------------------------------------------------------------
	// NodeChangeListener Interface
	// ----------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see java.util.prefs.NodeChangeListener#childAdded(java.util.prefs.NodeChangeEvent)
	 */
	public void childAdded( NodeChangeEvent event ) {
		// Determine child. Since this is a new node event, the child is not
		// yet known. So create a new childnode.
		Preferences child = new CompositePreferences( this, event.getChild().name(), 
				event.getChild() );
		
		// Substitute the node with the composite node.
		NodeChangeEvent newEvent = new NodeChangeEvent( this, child );
		
		// Pass the event on to the original listeners
		for( NodeChangeListener listener : _nodeListeners ) {
			listener.childAdded( newEvent );
		}
	}

	/* (non-Javadoc)
	 * @see java.util.prefs.NodeChangeListener#childRemoved(java.util.prefs.NodeChangeEvent)
	 */
	public void childRemoved( NodeChangeEvent event ) {
		// Determine child. Only existing nodes can be removed.
		Preferences child = new CompositePreferences( this, event.getChild().name(), 
				event.getChild() );
		
		// Substitute the node with the composite node.
		NodeChangeEvent newEvent = new NodeChangeEvent( this, child );
		
		// Pass the event on to the original listeners
		for( NodeChangeListener listener : _nodeListeners ) {
			listener.childRemoved( newEvent );
		}

		// Check if the node still exists in the composite layer. In that 
		// case it has only been removed from the leaf layer and still needs 
		// to be removed from the composite layer.
		try {
			if ( nodeExists( child.name() ) ) {
				// Yes it exists, so get rid of it.
				node( child.name() ).removeNode();
			}
		} catch ( BackingStoreException bse ) {
			// Ignore
		}
	}

	// ----------------------------------------------------------------------
	// PreferenceChangeListener Interface
	// ----------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see java.util.prefs.PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
	 */
	public void preferenceChange( PreferenceChangeEvent event ) {
		// Substitute the node with the composite node.
		PreferenceChangeEvent newEvent = new PreferenceChangeEvent( 
				this, event.getKey(), event.getNewValue() );
		
		// Pass the event on to the original listeners
		for( PreferenceChangeListener listener : _prefListeners ) {
			listener.preferenceChange( newEvent );
		}
	}
	
    // ----------------------------------------------------------------------
    // Override - AbstractPreferences
    // ----------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#flush()
     */
    @Override
    public void flush() throws BackingStoreException {
    	// Normal flush behavior is not needed since it will
    	// traverse down all child nodes recursively and flush
    	// those too. Since we don't have any data ourselves
    	// we only need to flush that data.
    	flushSpi();
    }
    
    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#sync()
     */
    @Override
    public void sync() throws BackingStoreException {
    	// Normal sync behavior is not needed since it will
    	// traverse down all child nodes recursively and sync
    	// those too. Since we don't have any data ourselves
    	// we only need to sync that data.
    	syncSpi();
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#addNodeChangeListener(java.util.prefs.NodeChangeListener)
     */
    @Override
    public void addNodeChangeListener( NodeChangeListener listener ) {
    	boolean isFirstListener = false;
    	
        synchronized(lock) {
        	// Determine if this is going to be our first listener.
        	isFirstListener = (_nodeListeners.size() == 0);
        	
	    	// Add the listener to our list of listeners
        	_nodeListeners.add( listener );
        }
        
    	// On the first listener being added we need to register to the 
    	// underlying leaf node.
        if ( isFirstListener ) {
        	_node.addNodeChangeListener( this );
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#addPreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
     */
    @Override
    public void addPreferenceChangeListener( PreferenceChangeListener listener ) {
    	boolean isFirstListener = false;
    	
        synchronized(lock) {
        	// Determine if this is gonna be our first listener.
        	isFirstListener = (_prefListeners.size() == 0);
        	
	    	// Add the listener to our list of listeners
        	_prefListeners.add( listener );
        }
        
    	// On the first listener being added we need to register to the 
    	// underlying leaf node.
        if ( isFirstListener ) {
            _node.addPreferenceChangeListener( this );
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#removeNodeChangeListener(java.util.prefs.NodeChangeListener)
     */
    @Override
    public void removeNodeChangeListener( NodeChangeListener listener ) {
    	boolean isLastListener = false;
    	
        synchronized(lock) {
        	// Remove the listener from our list of listeners
        	_nodeListeners.remove( listener );

        	// Determine if this is gonna be our first listener.
        	isLastListener = (_nodeListeners.size() == 0);
        }
        
    	// On the last listener being removed we need to unregister from the 
    	// underlying leaf node.
        if ( isLastListener ) {
        	_node.removeNodeChangeListener( this );
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#removePreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
     */
    @Override
    public void removePreferenceChangeListener(PreferenceChangeListener listener) {
    	boolean isLastListener = false;
    	
        synchronized(lock) {
        	// Remove the listener from our list of listeners
        	_prefListeners.remove( listener );

        	// Determine if this is gonna be our first listener.
        	isLastListener = (_prefListeners.size() == 0);
        }
        
    	// On the last listener being removed we need to unregister from the 
    	// underlying leaf node.
        if ( isLastListener ) {
        	_node.removePreferenceChangeListener( this );
        }
    }
        
    /* (non-Javadoc)
     * This method is overridden to ensure CompositePreferences can even be 
     * used even if they are not the default configured preferences 
     * implementation.
     * @see java.util.prefs.AbstractPreferences#isUserNode()
     */
    @Override
    public boolean isUserNode() {
        return _usernode;
    }

    // ----------------------------------------------------------------------
    // Public Interface
    // ----------------------------------------------------------------------
    
    /**
     * Gives access to the underlying preference node.
     * @return the preference node for the underlying leaf.
     */
    public Preferences getLeafPreferences() {
    	return _node;
    }
    
    // ----------------------------------------------------------------------
    // Private methods
    // ----------------------------------------------------------------------
    
    /**
     * Determine the underlying leaf node for the current composite node.
     * @return the preference node for the underlying leaf.
     */
    private Preferences getLeafNode() {
        Preferences leafRoot = getLeafRoot( _rule.getFactory() );
        
        // Determine leaf path to the right node. The rule specifies if the 
        // composite path needs to be substituted.
        String leafPath = _rule.replace( absolutePath() );
        
        // Now determine the right node.
        return leafRoot.node( leafPath );
    }

    /**
     * Determines the root node of the underlying leaf. When the current node
     * is a usernode, userRoot will be returned. Otherwise systemRoot will be
     * retrieved from the factory. Not the composite factory itself is used 
     * but the given factory.
     * @param factory the factory to be used.
     * @return the root preference node for the underlying node. 
     */
    private Preferences getLeafRoot( PreferencesFactory factory ) {
        Preferences leafRoot;
        
        // Determine which root node to use.
        if ( _usernode ) {
            leafRoot = factory.userRoot();
        } else {
            leafRoot = factory.systemRoot();
        }
        
        return leafRoot;
    }
}
