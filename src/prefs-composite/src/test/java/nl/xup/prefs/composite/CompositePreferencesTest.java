/**
 * 
 */
package nl.xup.prefs.composite;

import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import junit.framework.TestCase;
import nl.xup.prefs.composite.CompositePreferences;
import nl.xup.prefs.composite.CompositePreferencesFactory;
import nl.xup.prefs.composite.CompositeRule;
import nl.xup.prefs.composite.helpers.OtherPreferences;
import nl.xup.prefs.composite.helpers.OtherPreferencesFactory;
import nl.xup.prefs.memory.MemoryPreferences;
import nl.xup.prefs.memory.MemoryPreferencesFactory;

/**
 * @author Minto van der Sluis
 *
 */
public class CompositePreferencesTest extends TestCase {

    // ----------------------------------------------------------------------
    // Attributes
    // ----------------------------------------------------------------------

	private CompositeRule _rule = null;
	private CompositeRule _fallback = null;
	private CompositePreferencesFactory _factory = null;
	private NodeChangeListener _nodeListener = null;
	private NodeChangeEvent _nodeEvent = null;
	private PreferenceChangeListener _prefListener = null;
	private PreferenceChangeEvent _prefEvent = null;
	
    // ----------------------------------------------------------------------
    // TestCase Overrides
    // ----------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
        // We need at least 1 rule.
		_rule = new CompositeRule( OtherPreferencesFactory.class, "^/special.*" );
		_fallback = new CompositeRule( MemoryPreferencesFactory.class );
        CompositePreferencesFactory.addRule( _rule );
        CompositePreferencesFactory.addRule( _fallback );

        // First get us a factory instance.
        _factory = new CompositePreferencesFactory();

    	// Create a node listen.
        _nodeListener = new NodeChangeListener() {
    		public void childAdded(NodeChangeEvent arg0) {
    			_nodeEvent = arg0;
    		}
    		
    		public void childRemoved(NodeChangeEvent arg0) {
    			_nodeEvent = arg0;
    		}
    	};
    	// Create a preference listener.
    	_prefListener = new PreferenceChangeListener() {
    		public void preferenceChange(PreferenceChangeEvent arg0) {
    			_prefEvent = arg0;
    		}
    	};

    	super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		_factory.dispose();
		_factory = null;
		_rule = null;
		_fallback = null;
		_nodeListener = null;
		_nodeEvent = null;
		_prefListener = null;
		_prefEvent = null;
		
		super.tearDown();
	}
	
    // ----------------------------------------------------------------------
    // test Preferences interface
    // ----------------------------------------------------------------------

    public void testFromScratch() {
        // Get the root node from that factory.
        Preferences rootSystem = _factory.systemRoot();
        Preferences rootUser = _factory.userRoot();
        
        // In a from scratch situation there is no data yet. So asking 
        // for nodenames should result in an empty list/array.
        try {
            // Check for child nodes
            String[] results = rootSystem.childrenNames();
            assertEquals( 0, results.length );
            results = rootUser.childrenNames();
            assertEquals( 0, results.length );
            // Check for keys under the root node
            results = rootSystem.keys();
            assertEquals( 0, results.length );
            results = rootUser.keys();
            assertEquals( 0, results.length );
            
            // Both flush and sync are supported but no-op.
            rootSystem.flush();
            rootSystem.sync();
            rootUser.flush();
            rootUser.sync();
        } catch ( BackingStoreException bse ) {
            assertTrue( "No BackingStoreException expected.", false );
        }

        // Clean up the factory.
        _factory.dispose();
    }
    
    public void testSPIInterface() {
        String[] results;
        
        // Get the root node from that factory.
        Preferences rootSystem = _factory.systemRoot();
        Preferences rootUser = _factory.userRoot();
        
        Preferences childNodeSystem = null;
        Preferences childNodeUser = null;
        
        // Add a node
        try {
            // Add a node to the system root. This node should not be available 
            // in the user root.
            childNodeSystem = rootSystem.node( "node1" );
            results = childNodeSystem.childrenNames();
            assertEquals( 0, results.length );
            
            // Check for child nodes under root
            results = rootSystem.childrenNames();
            assertEquals( 1, results.length );
            assertEquals( "node1", results[0] );
            results = rootUser.childrenNames();
            assertEquals( 0, results.length );

            // Add a node to the user root. This node should not be available 
            // in the system root.
            childNodeUser = rootUser.node( "node2" );
            results = childNodeUser.childrenNames();
            assertEquals( 0, results.length );
            
            // Check for child nodes under root
            results = rootSystem.childrenNames();
            assertEquals( 1, results.length );
            assertEquals( "node1", results[0] );
            results = rootUser.childrenNames();
            assertEquals( 1, results.length );
            assertEquals( "node2", results[0] );
        } catch ( BackingStoreException bse ) {
            // This exception was not expected.
            assertTrue( false );
        }

        // Add a key
        try {
            // Add a key to the system node created previously. 
            childNodeSystem.put( "key1", "value1" );
            
            // Check for keys under the child node
            results = childNodeSystem.keys();
            assertEquals( 1, results.length );
            assertEquals( "key1", results[0] );
            assertEquals( "value1", childNodeSystem.get( "key1", "" ) );
            assertEquals( "", childNodeSystem.get( "key2", "" ) );

            // Add a key to the user node created previously. 
            childNodeUser.put( "key2", "value2" );
            
            // Check for keys under the child node
            results = childNodeUser.keys();
            assertEquals( 1, results.length );
            assertEquals( "key2", results[0] );
            assertEquals( "", childNodeUser.get( "key1", "" ) );
            assertEquals( "value2", childNodeUser.get( "key2", "" ) );
        } catch ( BackingStoreException bse ) {
            // This exception was not expected.
            assertTrue( false );
        }

        // Remove a key
        try {
            // Remove the previously added key. 
            childNodeSystem.remove( "key1" );
            
            // Check for keys under the child node
            results = childNodeSystem.keys();
            assertEquals( 0, results.length );
            assertEquals( "", childNodeSystem.get( "key1", "" ) );

            // Remove the previously added key. 
            childNodeUser.remove( "key2" );
            
            // Check for keys under the child node
            results = childNodeUser.keys();
            assertEquals( 0, results.length );
            assertEquals( "", childNodeUser.get( "key2", "" ) );
        } catch ( BackingStoreException bse ) {
            // This exception was not expected.
            assertTrue( false );
        }
        
        // Remove a node
        try {
            // Remove the previously added system node.
            childNodeSystem.removeNode();
            
            // Check for child nodes
            results = rootSystem.childrenNames();
            assertEquals( 0, results.length );

            // Remove the previously added user node.
            childNodeUser.removeNode();
            
            // Check for child nodes
            results = rootUser.childrenNames();
            assertEquals( 0, results.length );
        } catch ( BackingStoreException bse ) {
            // This exception was not expected.
            assertTrue( false );
        }

        // Clean up the factory.
        _factory.dispose();
    }

    
    // ----------------------------------------------------------------------
	// test equals and hashcode
	// ----------------------------------------------------------------------

	public void testEquals() {
		CompositePreferences p1 = new CompositePreferences( _factory, true );
		CompositePreferences p2 = new CompositePreferences( _factory, true );
		CompositePreferences p3 = new CompositePreferences( _factory, false );
		
		assertEquals( false, p1.equals(null) );
		assertEquals( false, p1.equals(new Integer(2)) );
		assertEquals( true, p1.equals(p1) );
		assertEquals( true, p1.equals(p2) );
		assertEquals( false, p1.equals(p3) );
	}

	public void testHashcode() {
		CompositePreferences p1 = new CompositePreferences( _factory, true );
		CompositePreferences p2 = new CompositePreferences( _factory, true );
		CompositePreferences p3 = new CompositePreferences( _factory, false );
		
		assertEquals( true, p1.hashCode() == p1.hashCode());
		assertEquals( true, p1.hashCode() == p2.hashCode());
		assertEquals( false, p1.hashCode() == p3.hashCode());
	}

    // ----------------------------------------------------------------------
    // test Public interface
    // ----------------------------------------------------------------------

	public void testGetLeafPreferences() {
		CompositePreferences p1 = (CompositePreferences)_factory.userRoot();
		CompositePreferences p2 = (CompositePreferences)_factory.systemRoot();
		CompositePreferences p3 = (CompositePreferences)p1.node( "level1/level2" );
		CompositePreferences p4 = (CompositePreferences)p2.node( "level1/level2" );
		CompositePreferences p5 = (CompositePreferences)p1.node( "special/nextlevel" );
		CompositePreferences p6 = (CompositePreferences)p2.node( "special/nextlevel" );
		
		assertTrue( p1.getLeafPreferences() instanceof MemoryPreferences );
		assertTrue( p2.getLeafPreferences() instanceof MemoryPreferences );
		assertTrue( p3.getLeafPreferences() instanceof MemoryPreferences );
		assertTrue( p4.getLeafPreferences() instanceof MemoryPreferences );
		assertTrue( p5.getLeafPreferences() instanceof OtherPreferences );
		assertTrue( p6.getLeafPreferences() instanceof OtherPreferences );
	}
	
    // ----------------------------------------------------------------------
    // test Listeners
    // ----------------------------------------------------------------------

	public void testNodeChangeListenerUser() {
		// Test adding a node to user root
		CompositePreferences node = (CompositePreferences)_factory.userRoot();
		node.addNodeChangeListener( _nodeListener );
		
		// Create a new node.
		Preferences leafNode = node.getLeafPreferences();
		Preferences newLeafNode = leafNode.node( "newnode" );
		newLeafNode.put( "name", "value" );
		
		// Wait a little for the event to be propagated.
	    try {
	    	leafNode.flush();
	        synchronized (this) {
	          wait( 1000 );
	        }
		} catch (Exception e) {
		    // ignore
		}
		
		// Check the event.
		assertNotNull( _nodeEvent );
		assertTrue( _nodeEvent.getParent().equals( node ) );
		Preferences childNode = _nodeEvent.getChild(); 
		assertTrue( childNode instanceof CompositePreferences );
		leafNode = ((CompositePreferences)childNode).getLeafPreferences(); 
		assertTrue( leafNode.equals( newLeafNode ) );
		
		// Make sure the node is loaded into the childcache.
		node.node( "newnode" );
		
		// Now check if removing works as well
		try {
			newLeafNode.removeNode();
		} catch ( BackingStoreException bse ) {
			// Not expected.
			assertFalse( true );
		}
		
		// Wait a little for the event to be propagated.
	    try {
	    	leafNode.flush();
	        synchronized (this) {
	          wait( 1000 );
	        }
		} catch (Exception e) {
		    // ignore
		}
		
		// Check the event.
		assertNotNull( _nodeEvent );
		assertTrue( _nodeEvent.getParent().equals( node ) );
		childNode = _nodeEvent.getChild(); 
		assertTrue( childNode instanceof CompositePreferences );
		leafNode = ((CompositePreferences)childNode).getLeafPreferences(); 
		assertTrue( leafNode.equals( newLeafNode ) );

		node.removeNodeChangeListener( _nodeListener );
	}

	public void testNodeChangeListenerSystem() {
		// Test adding a node to system root
		CompositePreferences node = (CompositePreferences)_factory.systemRoot();
		node.addNodeChangeListener( _nodeListener );

		// Create a new node.
		Preferences leafNode = node.getLeafPreferences();
		Preferences newLeafNode = leafNode.node( "newnode" );
		
		// Wait a little for the event to be propagated.
	    try {
	    	leafNode.flush();
	        synchronized (this) {
	          wait( 1000 );
	        }
		} catch (Exception e) {
		    // ignore
		}
		
		// Check the event.
		assertNotNull( _nodeEvent );
		assertTrue( _nodeEvent.getParent().equals( node ) );
		Preferences childNode = _nodeEvent.getChild(); 
		assertTrue( childNode instanceof CompositePreferences );
		leafNode = ((CompositePreferences)childNode).getLeafPreferences(); 
		assertTrue( leafNode.equals( newLeafNode ) );

		// Make sure the node is loaded into the childcache.
		node.node( "newnode" );
		
		// Now check if removing works as well
		try {
			newLeafNode.removeNode();
		} catch ( BackingStoreException bse ) {
			// Not expected.
			assertFalse( true );
		}
		
		// Wait a little for the event to be propagated.
	    try {
	    	leafNode.flush();
	        synchronized (this) {
	          wait( 1000 );
	        }
		} catch (Exception e) {
		    // ignore
		}
		
		// Check the event.
		assertNotNull( _nodeEvent );
		assertTrue( _nodeEvent.getParent().equals( node ) );
		childNode = _nodeEvent.getChild(); 
		assertTrue( childNode instanceof CompositePreferences );
		leafNode = ((CompositePreferences)childNode).getLeafPreferences(); 
		assertTrue( leafNode.equals( newLeafNode ) );

		node.removeNodeChangeListener( _nodeListener );
	}

	public void testPreferenceChangeListener() {
		// Test adding a node to user root
		CompositePreferences node = (CompositePreferences)_factory.userRoot().node( "level1/level2" );
		node.addPreferenceChangeListener( _prefListener );
		
		// Create a new node.
		Preferences leafNode = node.getLeafPreferences();
		leafNode.put( "name", "value" );
		
		// Wait a little for the event to be propagated.
	    try {
	    	leafNode.flush();
	        synchronized (this) {
	          wait( 1000 );
	        }
		} catch (Exception e) {
		    // ignore
		}
		
		// Check the event.
		assertNotNull( _prefEvent );
		assertEquals( "name", _prefEvent.getKey() );
		assertEquals( "value", _prefEvent.getNewValue() );
		assertEquals( node, _prefEvent.getNode() );
		assertTrue( _prefEvent.getNode() instanceof CompositePreferences );
		Preferences maskedNode = ((CompositePreferences) _prefEvent.getNode()).getLeafPreferences(); 
		assertTrue( maskedNode.equals( leafNode ) );
		
		node.removePreferenceChangeListener( _prefListener );

		// Test adding a node to system root
		_prefEvent = null;
		node = (CompositePreferences)_factory.systemRoot().node( "levelA/levelB" );
		node.addPreferenceChangeListener( _prefListener );
		
		// Create a new node.
		leafNode = node.getLeafPreferences();
		leafNode.put( "name", "value" );
		
		// Wait a little for the event to be propagated.
	    try {
	    	leafNode.flush();
	        synchronized (this) {
	          wait( 1000 );
	        }
		} catch (Exception e) {
		    // ignore
		}
		
		// Check the event.
		assertNotNull( _prefEvent );
		assertEquals( "name", _prefEvent.getKey() );
		assertEquals( "value", _prefEvent.getNewValue() );
		assertEquals( node, _prefEvent.getNode() );
		assertTrue( _prefEvent.getNode() instanceof CompositePreferences );
		maskedNode = ((CompositePreferences) _prefEvent.getNode()).getLeafPreferences(); 
		assertTrue( maskedNode.equals( leafNode ) );
		
		node.removePreferenceChangeListener( _prefListener );
	}
}
