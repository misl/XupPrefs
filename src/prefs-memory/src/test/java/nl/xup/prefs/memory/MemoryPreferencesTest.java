package nl.xup.prefs.memory;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

/**
 * Unittests for MemoryPreferences class
 * 
 * @author Minto van der Sluis
 */
public class MemoryPreferencesTest extends TestCase {

    // ----------------------------------------------------------------------
    // Preferences interface
    // ----------------------------------------------------------------------

    public void testFromScratch() {
        // First get us a factory instance.
        MemoryPreferencesFactory factory = new MemoryPreferencesFactory();
        
        // Get the root node from that factory.
        Preferences rootSystem = factory.systemRoot();
        Preferences rootUser = factory.userRoot();
        
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
    }
    
    public void testSPIInterface() {
        String[] results;
        
        // First get us a factory instance.
        MemoryPreferencesFactory factory = new MemoryPreferencesFactory();
        
        // Get the root node from that factory.
        Preferences rootSystem = factory.systemRoot();
        Preferences rootUser = factory.userRoot();
        
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
    }
}
