package nl.xup.prefs.demo.basic;

import java.util.prefs.Preferences;

public class Start {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Get the node with our preferences.
		Preferences myNode = Preferences.systemNodeForPackage( Start.class );

		// Read my preferences
		String color = myNode.get( "color", "none" );
		
		System.out.printf( "==> color: %s \n", color );
	}

}
