package ${groupId}.${artifactId};

import java.util.prefs.Preferences;

/**
 * Unit test for simple App.
 */
public class MyPreferencesFactoryTest extends PreferencesTestBase {

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
    public MyPreferencesFactoryTest( String testName )
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
	public void testPreferencesType() 
	{
		Preferences uroot = Preferences.userRoot();
		Preferences sroot = Preferences.systemRoot();        

		// check if system and user preferences object are of the correct type.
		assertTrue( "check systemRoot()", sroot instanceof MyPreferences );
		assertTrue( "check userRoot()", uroot instanceof MyPreferences );
		
		// check if retrieving the preferences roots twice results in the same
		// objects.
		assertEquals( sroot, Preferences.systemRoot() );
		assertEquals( uroot, Preferences.userRoot() );
	}
}
