package nl.xup.prefs;

import java.util.prefs.Preferences;

import nl.xup.prefs.browser.multo.tree.PreferencesTreeProvider;
import nl.xup.prefs.browser.multo.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultNestedTree;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class HomePage extends WebPage {
  private static final long serialVersionUID = 1L;

  public HomePage( final PageParameters parameters ) {
    super( parameters );

    DefaultNestedTree<Preferences> tree = new DefaultNestedTree<Preferences>( "prefsTree", new PreferencesTreeProvider() ) {
      @Override
      protected Component newContentComponent( String id, IModel<Preferences> node ) {
        return new TreeNode(id, this, node);
      }
    };

    add( tree );
  }
}
