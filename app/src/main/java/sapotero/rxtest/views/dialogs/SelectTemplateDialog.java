package sapotero.rxtest.views.dialogs;

import android.content.Context;

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

  private MaterialDialog.Builder dialogBuilder;

  public interface Callback {
    void onSelectTemplate(String template);
  }

  public SelectTemplateDialog(Context context, Callback callback, String type) {
    EsdApplication.getDataComponent().inject( this );

    ArrayList<String> items = new ArrayList<>();

    List<RTemplateEntity> templates = dataStore
      .select( RTemplateEntity.class )
      .where( RTemplateEntity.USER.eq( settings.getLogin() ))
      .and( RTemplateEntity.TYPE.eq(type) )
      .get().toList();

    if (templates.size() > 0) {
      for (RTemplateEntity tmp : templates){
        items.add( tmp.getTitle() );
      }
    }

    dialogBuilder = new MaterialDialog.Builder( context )
      .title( Objects.equals( type, DECISION ) ? R.string.dialog_template_title : R.string.drawer_item_settings_templates_off )
      .cancelable( false )
      .autoDismiss( true )
      .items( items )
      .itemsCallback((dialog1, view, which, text) -> {
        callback.onSelectTemplate( text.toString() );
        clearReferences();
      });
  }

  public void show() {
    if (dialogBuilder != null) {
      dialogBuilder.show();
    }
  }

  private void clearReferences() {
    dialogBuilder = null;
  }
}
