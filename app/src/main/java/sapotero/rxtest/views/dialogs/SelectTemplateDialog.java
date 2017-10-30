package sapotero.rxtest.views.dialogs;

import android.content.Context;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RTemplateEntity;
import sapotero.rxtest.utils.ISettings;

public class SelectTemplateDialog {

  public static final String DECISION = "decision";
  public static final String REJECTION = "rejection";

  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject ISettings settings;

  private Context context;
  private Callback callback;
  private String oldText;
  private MaterialDialog.Builder dialogBuilder;

  private boolean noTemplates = false;
  private boolean isDecision;

  public interface Callback {
    void onSelectTemplate(String template, boolean cancel, String oldText);
  }

  public SelectTemplateDialog(Context context, Callback callback, String type, String oldText) {
    this.context = context;
    this.callback = callback;
    this.oldText = oldText;

    EsdApplication.getDataComponent().inject( this );

    ArrayList<String> items = new ArrayList<>();

    List<RTemplateEntity> templates = dataStore
      .select( RTemplateEntity.class )
      .where( RTemplateEntity.USER.eq( settings.getLogin() ))
      .and( RTemplateEntity.TYPE.eq(type) )
      .get().toList();

    if (templates.size() > 0) {
      noTemplates = false;
      for (RTemplateEntity tmp : templates){
        items.add( tmp.getTitle() );
      }
    } else {
      noTemplates = true;
    }

    isDecision = Objects.equals( type, DECISION );

    dialogBuilder = new MaterialDialog.Builder( context )
      .title( isDecision ? R.string.dialog_template_title : R.string.drawer_item_settings_templates_off )
      .cancelable( false )
      .autoDismiss( true )
      .positiveText(R.string.dialog_oshs_close)
      .onPositive((dialog1, which) -> sendCallback( "", true ))
      .items( items )
      .itemsCallback((dialog1, view, which, text) -> sendCallback( text.toString(), false ));
  }

  private void sendCallback(String template, boolean cancel) {
    if ( callback != null ) {
      callback.onSelectTemplate( template, cancel, oldText );
    }
    clearReferences();
  }

  // Returns true if dialog is shown
  public boolean show() {
    boolean isShown = false;

    if ( noTemplates ) {
      // resolved https://tasks.n-core.ru/browse/MPSED-2290
      // если нет шаблонов, то не давать открывать модалку шаблонов и выводить предупреждение: "Шаблоны резолюции отсутствуют"
      String message = context.getString( isDecision ? R.string.dialog_decision_template_empty : R.string.dialog_rejection_template_empty );
      Toast.makeText( context, message, Toast.LENGTH_SHORT ).show();
      isShown = false;

    } else if (dialogBuilder != null) {
      dialogBuilder.show();
      isShown = true;
    }

    return isShown;
  }

  private void clearReferences() {
    dialogBuilder = null;
  }
}
