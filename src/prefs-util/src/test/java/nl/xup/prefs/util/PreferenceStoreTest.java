/**
 * 
 */
package nl.xup.prefs.util;

import java.util.prefs.Preferences;

import junit.framework.TestCase;
import nl.xup.prefs.util.IConfigurationChangeListener;
import nl.xup.prefs.util.PreferencesStore;

/**
 * @author Minto van der Sluis
 */
public class PreferenceStoreTest extends TestCase {

  int eventCounter = 0;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // Teller resetten.
    eventCounter = 0;
  }

  /**
   * Test PreferenceStore met minimale configuratie.
   */
  public void testMinimaal() {
    PreferencesTestHelper.laadPreferences( this, "nl/xup/prefs/util/store/minimaal-prefs.xml" );

    PreferenceStoreTestHelper obj = new PreferenceStoreTestHelper();

    assertEquals( "", obj.getString() );
    assertEquals( new Integer( 0 ), obj.getInteger() );
    assertEquals( false, obj.getBoolean() );
  }

  /**
   * Test PreferenceStore met normale configuratie.
   */
  public void testNormaal() {
    PreferencesTestHelper.laadPreferences( this, "nl/xup/prefs/util/store/normaal-prefs.xml" );

    PreferenceStoreTestHelper obj = new PreferenceStoreTestHelper();

    assertEquals( "a", obj.getString() );
    assertEquals( new Integer( 1 ), obj.getInteger() );
    assertEquals( true, obj.getBoolean() );
  }

  /**
   * Test PreferenceStore met normale configuratie.
   */
  public void testGeen() {
    PreferencesTestHelper.laadPreferences( this, "nl/xup/prefs/util/store/geen-prefs.xml" );

    PreferenceStoreTestHelper obj = new PreferenceStoreTestHelper();

    assertEquals( "", obj.getString() );
    assertEquals( new Integer( 0 ), obj.getInteger() );
    assertEquals( false, obj.getBoolean() );
  }

  /**
   * Test PreferenceStore het zetten van preferences.
   */
  public void testZetten() {
    PreferencesTestHelper.laadPreferences( this, "nl/xup/prefs/util/store/normaal-prefs.xml" );

    PreferenceStoreTestHelper obj = new PreferenceStoreTestHelper();

    assertEquals( "a", obj.getString() );
    assertEquals( new Integer( 1 ), obj.getInteger() );
    assertEquals( true, obj.getBoolean() );

    // Nu andere waarden zetten.
    obj.setString( "oeps" );
    obj.setInteger( new Integer( 6 ) );
    obj.setBoolean( false );

    // Zelfde object laten uitlezen.
    assertEquals( "oeps", obj.getString() );
    assertEquals( new Integer( 6 ), obj.getInteger() );
    assertEquals( false, obj.getBoolean() );

    try {
      // Om zeker te zijn dat alle listeners zijn uitgevoerd (aparte
      // thread) eventjes wachten.
      synchronized( this ) {
        wait( 1000 );
      }
    } catch( Exception e ) {
      // ignore
    }

    // Ander object laten uitlezen om te controleren of de
    // preferences ook daadwerkelijk in de backing store zijn
    // gezet.
    obj = new PreferenceStoreTestHelper();
    assertEquals( "oeps", obj.getString() );
    assertEquals( new Integer( 6 ), obj.getInteger() );
    assertEquals( false, obj.getBoolean() );
  }

  /**
   * Test PreferenceStore Hot Modifiability
   */
  public void testHotMod() {
    // Omdat het geheel lazy werkt, wordt pas gereageerd op wijzigingen
    // wannneer de preferences reeds zijn uitgelezen. Daarom moeten we ze
    // eerst een keer uitlezen alvorens ze te wijzigen.
    PreferencesTestHelper.laadPreferences( this, "nl/xup/prefs/util/store/normaal-prefs.xml" );
    PreferenceStoreTestHelper obj = new PreferenceStoreTestHelper();
    assertEquals( "a", obj.getString() );
    assertEquals( new Integer( 1 ), obj.getInteger() );
    assertEquals( true, obj.getBoolean() );

    // Nu settings wijzigingen.
    Preferences node = Preferences.systemRoot().node( "/level1/level2" );
    node.put( "string", "aap" );
    node.put( "integer", "33" );
    node.put( "boolean", "false" );

    try {
      node.flush();
      // Om zeker te zijn dat alle listeners zijn uitgevoerd (aparte
      // thread) eventjes wachten.
      synchronized( this ) {
        wait( 1000 );
      }
    } catch( Exception e ) {
      // ignore
    }

    // Resultaat testen.
    assertEquals( "aap", obj.getString() );
    assertEquals( new Integer( 33 ), obj.getInteger() );
    assertEquals( false, obj.getBoolean() );
  }

  /**
   * Test PreferenceStore Hot Modifiability
   */
  public void testEventHandler() {
    // Omdat het geheel lazy werkt, wordt pas gereageerd op wijzigingen
    // wannneer de preferences reeds zijn uitgelezen. Daarom moeten we ze
    // eerst een keer uitlezen alvorens ze te wijzigen.
    PreferencesTestHelper.laadPreferences( this, "nl/xup/prefs/util/store/normaal-prefs.xml" );
    PreferenceStoreTestHelper obj = new PreferenceStoreTestHelper();
    assertEquals( "a", obj.getString() );
    assertEquals( new Integer( 1 ), obj.getInteger() );
    assertEquals( true, obj.getBoolean() );

    // Listener aanmelden
    IConfigurationChangeListener listener = new IConfigurationChangeListener() {
      public void onConfigurationChanged( Class<? extends PreferencesStore> clazz ) {
        // Aantal events tellen.
        eventCounter++;
      }
    };
    obj.addListener( listener );

    // Nu settings wijzigingen.
    Preferences node = Preferences.systemRoot().node( "/level1/level2" );
    // Alle 3 settings aanpassen
    node.put( "string", "aap" );
    node.put( "integer", "33" );
    node.put( "boolean", "false" );

    try {
      node.flush();
      // Om zeker te zijn dat alle listeners zijn uitgevoerd (aparte
      // thread) eventjes wachten.
      synchronized( this ) {
        // Iets langer wachten aangezien het event vertraagd afgaan om
        // de interne event te bundelen.
        wait( 2000 );
      }
    } catch( Exception e ) {
      // ignore
    }
    obj.removeListener( listener );

    // Resultaat testen. 3 wijzigingen moeten binnenkomen als 1 event.
    assertEquals( 1, eventCounter );
  }
}