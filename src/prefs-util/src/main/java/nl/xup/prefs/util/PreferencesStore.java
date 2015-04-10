package nl.xup.prefs.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class voor het uitlezen van preference instellingen. Hierbij worden
 * reeds ingelezen instellingen automatisch geupdate na verandering van de 
 * preferences. 
 * 
 * @author Minto van der Sluis
 */
abstract public class PreferencesStore {
	// ----------------------------------------------------------------------
	// Class fields
	// ----------------------------------------------------------------------
	private static final Logger logger = LoggerFactory
			.getLogger(PreferencesStore.class);
	
	private static final long VERTRAGING = 1000; // 1 seconde vertraging.

	// ----------------------------------------------------------------------
	// Object fields
	// ----------------------------------------------------------------------
	protected String _nodePath = null;

	private String _nodeNaam = null;
	private boolean _isRegistered = false;
	private final HashMap<String, Object> _prefMap = new HashMap<String, Object>(3);
	
	private final List<IConfigurationChangeListener> _listeners = new ArrayList<IConfigurationChangeListener>();
	private Timer _delayTimer;
	private static Map<String,String> _conversieRegels = new HashMap<String,String>();
	
	static {
		_conversieRegels.put( "JA", "true" );
		_conversieRegels.put( "J", "true" );
		_conversieRegels.put( "YES", "true" );
		_conversieRegels.put( "Y", "true" );
		_conversieRegels.put( "NEE", "false" );
		_conversieRegels.put( "NO", "false" );
		_conversieRegels.put( "N", "false" );
	}	

	// ----------------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------------

	protected PreferencesStore(Class<? extends PreferencesStore> clazz) {
		_nodePath = clazz.getName().replace('.', '/');
	}

	// ----------------------------------------------------------------------
	// Protected Interface
	// ----------------------------------------------------------------------

	/**
	 * Laad een preference waarde. De waarde wordt slecht eenmalig uit de
	 * preferences gehaald en in een interne map opgeslagen vervolgens zal deze
	 * map gebruikt worden voor elk volgend verzoek voor dezelfde preference.
	 * 
	 * @param name
	 *            De naam waaronder de waarde is vastgelegd.
	 * @return De waarde als een string.
	 */
	protected String getPreference(final String name) {
		// Altijd de node opvragen, hierdoor kunnen we goed overweg
		// met wijzigingen in de preference hierarchie.
		Preferences node = getNode();

		// Kennen we de waarde voor deze preference reeds.
		if (_prefMap.containsKey(name)) {
			// Ja, deze dan teruggeven.
			return (String) _prefMap.get(name);
		}

		// Nee, dan moeten we die eerst gaan laden.
		String prefValue = node.get(name, null);

		// Is de gewenste preference aanwezig.
		if (prefValue == null) {
			// Preference schijnbaar niet (goed) gezet. Melding van maken.
			logger.error("Preference '{}' in '{}' niet (goed) geconfigureerd!",
					name, _nodeNaam);

			// Om niet overal op null pointers te hoeven checken geven we
			// in deze situatie maar een lege waarde terug.
			prefValue = "";
		}

		// De gevonden preference in onze cache opnemen.
		_prefMap.put(name, prefValue);

		return prefValue;
	}
	
	/**
	 * Bewaar een preference waarde. 
	 * 
	 * @param name	de naam waaronder de waarde wordt opgeslagen
	 * @param value de op te slagen waarde.
	 */
	protected void setPreference( final String name, final String value ) {
		// Altijd de node opvragen, hierdoor kunnen we goed overweg
		// met wijzigingen in de preference hierarchie.
		Preferences node = getNode();
		
		// Alleen indien de nieuwe waarde anders is dan de vorige waarde
		// moeten we het hele circus doorlopen.
		if ( getPreference( name ).equals( value ) ) {
			// Waarde is niet veranderd, dus we zijn klaar.
			return;
		}

		// De waarde voor de preferences wegschrijven.
		node.put( name, value );
		
		// Eigen administratie hoeven we niet bij te werken, deze
		// wordt automatisch bijgewerkt wanneer de listeners afgaan.
		try{
			node.flush();
		} catch ( Exception e ) {
			// don't care
			logger.warn( "Setting '" + name + "' to '" 
					+ value + "' failed!", e );
		}

		// De gewijzigde preference ook in onze cache opnemen.
		_prefMap.put(name, value);
	}

	/**
	 * Laad een preference waarde. De waarde wordt slecht eenmalig uit de
	 * preferences gehaald en in een interne map opgeslagen vervolgens zal deze
	 * map gebruikt worden voor elk volgend verzoek voor dezelfde preference.
	 * 
	 * @param name
	 *            De naam waaronder de waarde is vastgelegd.
	 * @return De waarde als een Integer.
	 */
	protected Integer getPreferenceInteger(final String name) {
		// Haal de string representatie op.
		String strValue = getPreference(name);
		// Omzetten naar een integer.
		Integer intValue = new Integer(0);
		if (strValue.trim().length() != 0) {
			intValue = new Integer(strValue);
		}
		return intValue;
	}

	/**
	 * Bewaar een preference waarde. 
	 * 
	 * @param name	de naam waaronder de waarde wordt opgeslagen
	 * @param value de op te slagen waarde.
	 */
	protected void setPreference( final String name, final Integer value ) {
		setPreference( name, value.toString() );
	}

	/**
	 * Laad een preference waarde. De waarde wordt slecht eenmalig uit de
	 * preferences gehaald en in een interne map opgeslagen vervolgens zal deze
	 * map gebruikt worden voor elk volgend verzoek voor dezelfde preference.
	 * 
	 * @param name
	 *            De naam waaronder de waarde is vastgelegd.
	 * @return De waarde als een boolean.
	 */
	protected boolean getPreferenceBoolean(final String name) {
		// Haal de string representatie op.
		String strValue = getPreference(name);
		
		// teksten naar boolean waarden transformeren.
		if ( _conversieRegels.containsKey( strValue.toUpperCase() ) ) {
			strValue = _conversieRegels.get( strValue.toUpperCase() );
		}
		
		// Omzetten naar een integer.
		boolean bValue = false;
		if (strValue.trim().length() != 0) {
			bValue = new Boolean(strValue).booleanValue();
		}
		return bValue;
	}

	/**
	 * Bewaar een preference waarde. 
	 * 
	 * @param name	de naam waaronder de waarde wordt opgeslagen
	 * @param value de op te slagen waarde.
	 */
	protected void setPreference( final String name, final boolean value ) {
		setPreference( name, Boolean.toString( value ) );
	}

	/**
	 * TODO
	 * @return
	 */
	protected PreferenceChangeListener getNodeChangeListener() {
		return new PreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent evt) {
				// Zoek of we de key in de value map kennen.
				if (_prefMap.containsKey(evt.getKey())) {
					// De nieuwe waarde opnemen.
					onPreferenceChanged( evt.getKey(), evt.getNewValue() );
				} else {
					// Onbekende keys of nog niet gebruikte keys worden
					// genegeerd.
				}
			}
		};
	}

	/**
	 * TODO
	 * @return
	 */
	protected NodeChangeListener getNodeDeleteListener() {
		return new NodeChangeListener() {
			public void childAdded(NodeChangeEvent evt) {
				// ignore
			}

			public void childRemoved(NodeChangeEvent evt) {
				// Is onze eigen node verwijderd?
				if (evt.getChild().name().equals(_nodeNaam)) {
					// Dan zijn we ook niet langer registered.
					_isRegistered = false;
					// Als de node niet meer bestaat, dan zijn de
					// preferences ook niet langer valide.
					_prefMap.clear();
				}
			}
		};
	}
	
	/**
	 * Handler voor het verwerken van veranderingen in de instellingen.
	 * @param name	de naam van de veranderde preference
	 * @param value	de nieuwe waarde
	 */
	protected void onPreferenceChanged( String name, String value ) {
		_prefMap.put( name, value );
		
		// Zijn er wel listeners?
		if ( _listeners.isEmpty() ) {
			// Geen listeners dan ook niet moeilijk doen ;-)
			return;
		}
		
		// Alle listeners op de hoogte brengen van de wijzigingen. Echter
		// om te voorkomen dat we voor elke kleine wijziging weer alle 
		// listeners bijlangs gaan, bouwen we een kleine vertraging in.
		// Deze vertraging zorgt er voor dat wanneer meerdere preferences
		// vlak na elkaar wijzigen, er toch maar 1 event uit gaat.
		
		synchronized (_listeners) {
			// Is er reeds een timer gestart?
			if ( _delayTimer != null ) {
				// Deze timer stoppen en een nieuwe aanmaken om weer
				// met een verse drempel te starten voor eventueel nog
				// komende configuratie wijzingen.
				_delayTimer.cancel();
				_delayTimer = null;
			}
			
			// Timer aanmaken met een opdracht (Task) om de events te versturen.
			TimerTask eventSenderTask = new TimerTask() {
				@Override
				public void run() {
					// Alle listeners bijlangs gaan en het event versturen.
					for (IConfigurationChangeListener listener : _listeners ) {
						listener.onConfigurationChanged( PreferencesStore.this.getClass() );
					}
					
					// Events zijn reeds verstuurd, de timer is nu niet langer nodig.
					_delayTimer.cancel();
					_delayTimer = null;
				}
			};
			_delayTimer = new Timer();
			_delayTimer.schedule( eventSenderTask, VERTRAGING );
		}	
	}
	
	/**
	 * Listener voor configuratie wijzigingen aanmelden.
	 * @param listener
	 */
	protected void addInternalListener( IConfigurationChangeListener listener ) {
		_listeners.add( listener );
	}
	
	/**
	 * Listener voor configuratie wijzigingen afmelden.
	 * @param listener
	 */
	protected void removeInternalListener( IConfigurationChangeListener listener ) {
		_listeners.remove( listener );
	}

	// ----------------------------------------------------------------------
	// Private methods
	// ----------------------------------------------------------------------

	/**
	 * Haalt de preference node op waar zich de configuratie bevindt. Tevens 
	 * wordt een listener op deze node geplaatst wanneer dat nog niet was 
	 * gedaan (dus eenmalig).
	 * 
	 * @return Preferences object
	 */
	private Preferences getNode() {
		// Bepaal de te gebruiken node.
		Preferences node = Preferences.systemRoot().node(_nodePath);

		// Zet een preference change listener op deze node als dat nog niet
		// eerder is gedaan.
		if (!_isRegistered) {
			// Onthoud de node naam tbv de listeners.
			_nodeNaam = node.name();

			// We gaan twee listeners zetten. Een om veranderingen
			// in de preferences op te vangen (value change).
			node.addPreferenceChangeListener( getNodeChangeListener() );
			// En een om node veranderingen op te vangen (node removal).
			node.parent().addNodeChangeListener( getNodeDeleteListener() );
			
			_isRegistered = true;
		}

		return node;
	}

}
