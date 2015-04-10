package nl.xup.prefs.browser.multo.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.model.IModel;

public class PreferencesTreeProvider implements ITreeProvider<Preferences>{

  // ----------------------------------------------------------------------
  // Class attributes
  // ----------------------------------------------------------------------

  private static final long serialVersionUID = 1L;

  // ----------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------

  public PreferencesTreeProvider() {
  }
  
  // ----------------------------------------------------------------------
  // Implementing ITreeProvider
  // ----------------------------------------------------------------------

  @Override
  public void detach() {
    // Nothing to do
  }
  
  @Override
  public Iterator<? extends Preferences> getRoots() {
    Preferences node = Preferences.systemRoot();
    return getChildren( node );
  }
  
  @Override
  public boolean hasChildren( Preferences node ) { 
    try {
      return node.childrenNames().length > 0;
    } catch( BackingStoreException e ) {
      // Something went wrong, assume no children.
      return false;
    }
  }
  
  @Override
  public Iterator<? extends Preferences> getChildren( Preferences node ) {
    // Get all child nodes
    List<Preferences> children = new ArrayList<Preferences>();
    try {
      for( String childNodeName : node.childrenNames()) {
        children.add( node.node( childNodeName ) );
      }
    } catch( BackingStoreException e ) {
      // Something went wrong, assume no children.
    }

    return children.iterator();
  }
  
  @Override
  public IModel<Preferences> model( Preferences node ) {
    return new PreferencesModel( node );
  }
}
