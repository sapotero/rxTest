package sapotero.rxtest.views.adapters.models;

import android.content.Context;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.query.WhereAndOr;
import io.requery.rx.RxScalar;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.db.requery.utils.validation.Validation;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import sapotero.rxtest.views.menu.fields.MainMenuButton;
import sapotero.rxtest.views.menu.fields.MainMenuItem;

public class DocumentTypeItem {
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject RxSharedPreferences settings;
  @Inject Validation validation;

  private final MainMenuItem mainMenuItem;
  private final String user;


  public DocumentTypeItem(Context context, MainMenuItem mainMenuItem, String user) {
    super();
    this.mainMenuItem = mainMenuItem;
    this.user = user;

    EsdApplication.getValidationComponent().inject(this);
  }

  // Главное меню
  public String getName() {



    if (mainMenuItem.getIndex() == 0){

      Integer projects = -1;

      Integer total = dataStore
        .count(RDocumentEntity.class)
        .where( RDocumentEntity.USER.eq( settings.getString("login").get() )   )
//        .and( RDocumentEntity.FILTER.in( MainMenuButton.ButtonStatus.forAllDocuments() ) )
        .and( RDocumentEntity.DOCUMENT_TYPE.in( validation.getSelectedJournals() ) )
        .and( RDocumentEntity.PROCESSED.eq( false ) )
        .get()
        .value();


      if ( validation.hasSigningAndApproval() ){
        projects = dataStore
          .count(RDocumentEntity.class)
          .where( RDocumentEntity.FILTER.in( MainMenuButton.ButtonStatus.getProject() )   )
          .and( RDocumentEntity.USER.eq( settings.getString("login").get() ) )
          .get()
          .value();
      }

      String title;
      if (projects != -1) {
        title = String.format( mainMenuItem.getName(), total, projects);
      } else {
        title = String.format( "Документы %s", total);
      }

      return title;

    } else {
      int count = 0;

      // настройка
      // если включена настройка "Отображать документы без резолюции"
      WhereAndOr<RxScalar<Integer>> query;
      if ( settings.getBoolean("settings_view_type_show_without_project").get()
        || mainMenuItem.getIndex() == 3  // подписание/согласование
        || mainMenuItem.getIndex() == 9  // обработанные
        || mainMenuItem.getIndex() == 10 // избранное
      ){
        query = dataStore
          .count(RDocumentEntity.class)
          .where(RDocumentEntity.USER.eq(settings.getString("login").get()))
          .and(RDocumentEntity.FILTER.ne( Fields.Status.LINK.getValue() ));


      } else {
        query = dataStore
          .count(RDocumentEntity.class)
          .where( RDocumentEntity.USER.eq( settings.getString("login").get() ) )
          .and( RDocumentEntity.WITH_DECISION.eq(true) )
          .and( RDocumentEntity.FILTER.ne( Fields.Status.LINK.getValue() ) );
      }

      if ( mainMenuItem.getCountConditions().length > 0 ){

        for (ConditionBuilder condition : mainMenuItem.getCountConditions() ){
          switch ( condition.getCondition() ){
            case AND:
              query = query.and( condition.getField() );
              break;
            case OR:
              query = query.or( condition.getField() );
              break;
            default:
              break;
          }
        }
      }
      count = query.get().value();

      return String.format( mainMenuItem.getName(), count);
    }

  }

  public MainMenuItem getMainMenuItem(){
    return mainMenuItem;
  }

  public void invalidate() {
    getName();
  }
}
