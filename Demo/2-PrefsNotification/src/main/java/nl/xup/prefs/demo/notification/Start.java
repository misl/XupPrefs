package nl.xup.prefs.demo.notification;

import java.io.Console;
import java.util.Scanner;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

public class Start implements PreferenceChangeListener {

    // ----------------------------------------------------------------------
    // Class Attributes
    // ----------------------------------------------------------------------

	private static final String KEY_COLOR = "color";
	
	private static boolean stopRequested = false;
	private static Preferences myNode;

	// ----------------------------------------------------------------------
	// Interface implementation - PreferenceChangeListener
	// ----------------------------------------------------------------------
	
	public void preferenceChange( PreferenceChangeEvent event ) {
		System.out.printf( "\n==> key '%s' changed to '%s' \n", 
				event.getKey(), event.getNewValue() );
	};
	
	// ----------------------------------------------------------------------
	// Private methods
	// ----------------------------------------------------------------------

	private static void printColor() {
		// Read my preferences
		String color = myNode.get( KEY_COLOR, "none" );

		System.out.printf( "==> color: %s \n", color );
	}
	
	private static void processColor( String color ) {
		System.out.printf( "==> processing: %s \n", color );
		
		myNode.put( KEY_COLOR, color );
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Console console = System.console();
		if ( console != null ) {
			System.out.println( "Demo started:" );
	
			// Get the node with our preferences.
			myNode = Preferences.systemNodeForPackage( Start.class );
			myNode.addPreferenceChangeListener( new Start() );
	
			// Prepare console operation.
			Scanner scanner = new Scanner( console.reader() );
			
			// Loop until stopped
			while( !stopRequested ) {
				console.printf( "Give new color: " );
				String command = scanner.next();
				
				if ( "quit".equalsIgnoreCase( command ) ) {
					stopRequested = true;
				} else if ( "print".equalsIgnoreCase( command ) ) {
					printColor();
				} else {
					processColor( command );
				}
			}
	
			System.out.println( "Demo is done:" );
		} else {
			System.out.println( "Demo requires console! Restart from command-line" );
		}
	}

}
