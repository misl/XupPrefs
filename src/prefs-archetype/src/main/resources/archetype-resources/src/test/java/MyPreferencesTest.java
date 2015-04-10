package ${groupId}.${artifactId};

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Unit test for simple App.
 */
public class MyPreferencesTest extends PreferencesTestBase {

	// ----------------------------------------------------------------------
	// Class fields
	// ----------------------------------------------------------------------
	private static final String PREFS_FACTORY_KEY = "java.util.prefs.PreferencesFactory";

	// ----------------------------------------------------------------------
	// Object fields
	// ----------------------------------------------------------------------

	// Remember the previous preferences factory, so it can be restored.
    private String previousFactory;
	
	// ----------------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------------

	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MyPreferencesTest( String testName )
    {
        super( testName );
    }

    // ----------------------------------------------------------------------
    // TestCase overrides
    // ----------------------------------------------------------------------

    protected void setUp() throws Exception {
    	// Store the existing preference factory, so it can be
    	// restored later.
    	previousFactory = System.getProperty( PREFS_FACTORY_KEY );

		// Make sure the correct preferences factory is used
		// for all tests
		System.setProperty( PREFS_FACTORY_KEY, 
				"${groupId}.${artifactId}.MyPreferencesFactory");
		
		// Make sure the preferences factory is properly initialized.
		// Either through setting some system properties or by injecting
		// the necessary configuration statically into the factory.
		// TODO: configure factory.
    }

    protected void tearDown() throws Exception {
    	// Restore the old preference factory if there is one.
        if (previousFactory != null)
        {
            System.setProperty( PREFS_FACTORY_KEY, previousFactory);
        }
    }

    // ----------------------------------------------------------------------
    // Tests
    // ----------------------------------------------------------------------

    /**
     * Test if preferences of the correct type are returned.
     */
	public void testPutGet() throws BackingStoreException {
		Preferences uroot = Preferences.userRoot();
		Preferences sroot = Preferences.systemRoot();        

        uroot.put("ukey1", "value1");
		assertEquals("value1", uroot.get("ukey1", null));
		String[] names = uroot.keys();
		assertEquals(1, names.length);

		uroot.put("ukey2", "value3");
		assertEquals("value3", uroot.get("ukey2", null));
		uroot.put("\u4e2d key1", "\u4e2d value1");
		assertEquals("\u4e2d value1", uroot.get("\u4e2d key1", null));
		names = uroot.keys();
		assertEquals(3, names.length);

		uroot.flush();
		uroot.clear();
		names = uroot.keys();
		assertEquals(0, names.length);

		sroot.put("skey1", "value1");
		assertEquals("value1", sroot.get("skey1", null));
		sroot.put("\u4e2d key1", "\u4e2d value1");
		assertEquals("\u4e2d value1", sroot.get("\u4e2d key1", null));
	}

	/**
	 * Test the security features of the preferences implementation.
	 */
	public void testSecurity() {
		// TODO: This is implemetation specific, so you should come
		//       up with your own tests.
	}
}
