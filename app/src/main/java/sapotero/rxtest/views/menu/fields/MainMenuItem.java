package sapotero.rxtest.views.menu.fields;

import java.util.ArrayList;
import java.util.Arrays;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.views.menu.builders.ButtonBuilder;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import timber.log.Timber;


enum V2DocumentType{
  INCOMING_DOCUMENTS ("incoming_documents"),
  CITIZEN_REQUESTS   ("citizen_requests"),
  INCOMING_ORDERS    ("incoming_orders"),
  ORDERS             ("orders"),
  ORDERS_DDO         ("orders_ddo"),
  OUTGOING_DOCUMENTS ("outgoing_documents");

  private final String name;

  V2DocumentType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}

public enum MainMenuItem {

  ALL ( 0, "Документы %s / Проекты %s",
    new MainMenuButton[]{
      MainMenuButton.PROJECTS,
      MainMenuButton.PERFORMANCE,
      MainMenuButton.PRIMARY_CONSIDERATION,
      MainMenuButton.VIEWED
    },
    true,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FAVORITES.ne(false) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.ne(true) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.eq( false ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_PROCESSED_FOLDER.eq( false ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_FAVORITES_FOLDER.eq( false ) ),
    },
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_PROCESSED_FOLDER.eq( false ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_FAVORITES_FOLDER.eq( false ) ),
    },
    true, false),

  INCOMING_DOCUMENTS ( 1, "Входящие документы %s", new MainMenuButton[]{
    MainMenuButton.PERFORMANCE,
    MainMenuButton.PRIMARY_CONSIDERATION,
    MainMenuButton.VIEWED
  },
    true,
    new ConditionBuilder[]{
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.UID.like( Fields.Journal.INCOMING_DOCUMENTS.getValue() + "%"  ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.eq( false ) )
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.DOCUMENT_TYPE.eq( V2DocumentType.INCOMING_DOCUMENTS.getName() )  )
    },
    new ConditionBuilder[]{
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.UID.like( Fields.Journal.INCOMING_DOCUMENTS.getValue() + "%"  ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.eq( false ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_PROCESSED_FOLDER.eq( false ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_FAVORITES_FOLDER.eq( false ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.DOCUMENT_TYPE.eq( V2DocumentType.INCOMING_DOCUMENTS.getName() )  )
    },
    false, false),

  CITIZEN_REQUESTS ( 2, "Обращения граждан %s", new MainMenuButton[]{
    MainMenuButton.PERFORMANCE,
    MainMenuButton.PRIMARY_CONSIDERATION,
    MainMenuButton.VIEWED
  },
    true,
    new ConditionBuilder[]{
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.UID.like( Fields.Journal.CITIZEN_REQUESTS.getValue() + "%"  ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.eq( false ) )
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.DOCUMENT_TYPE.eq( V2DocumentType.CITIZEN_REQUESTS.getName() )  )
    },
    new ConditionBuilder[]{
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.UID.like( Fields.Journal.CITIZEN_REQUESTS.getValue() + "%"  ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.eq( false ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_PROCESSED_FOLDER.eq( false ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_FAVORITES_FOLDER.eq( false ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.DOCUMENT_TYPE.eq( V2DocumentType.CITIZEN_REQUESTS.getName() )  )
    },
    false, false),

  APPROVE_ASSIGN ( 3, "Подписание/Согласование %s",
    new MainMenuButton[]{
      MainMenuButton.APPROVAL,
      MainMenuButton.ASSIGN,
      MainMenuButton.PROCESSED
    },
    true,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.in( MainMenuButton.ButtonStatus.getProject() ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.eq( false ) ),
    },
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.eq( false ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_PROCESSED_FOLDER.eq( false ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_FAVORITES_FOLDER.eq( false ) ),
    },
    true, true),

  INCOMING_ORDERS ( 4, "НПА %s", new MainMenuButton[]{
    MainMenuButton.PERFORMANCE,
    MainMenuButton.PRIMARY_CONSIDERATION,
    MainMenuButton.VIEWED
  },true,
    new ConditionBuilder[]{
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.UID.like(  Fields.Journal.INCOMING_ORDERS.getValue() + "%"  ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.eq( false ) )
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.DOCUMENT_TYPE.eq( V2DocumentType.INCOMING_ORDERS.getName() )  )
    },
    new ConditionBuilder[]{
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.UID.like(  Fields.Journal.INCOMING_ORDERS.getValue() + "%"  ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.eq( false ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_PROCESSED_FOLDER.eq( false ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_FAVORITES_FOLDER.eq( false ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.DOCUMENT_TYPE.eq( V2DocumentType.INCOMING_ORDERS.getName() )  )
    },
    false, false),

  ORDERS ( 5, "Приказы %s", new MainMenuButton[]{
    MainMenuButton.PERFORMANCE,
    MainMenuButton.PRIMARY_CONSIDERATION,
    MainMenuButton.VIEWED
  },true,
    new ConditionBuilder[]{
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.UID.like( Fields.Journal.ORDERS.getValue() + "%"  ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.eq( false ) )
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.DOCUMENT_TYPE.eq( V2DocumentType.ORDERS.getName() )  )
    },
    new ConditionBuilder[]{
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.UID.like( Fields.Journal.ORDERS.getValue() + "%"  ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.eq( false ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_PROCESSED_FOLDER.eq( false ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_FAVORITES_FOLDER.eq( false ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.DOCUMENT_TYPE.eq( V2DocumentType.ORDERS.getName() )  )
    },
    false, false),

  ORDERS_DDO ( 6, "Приказы ДДО %s", new MainMenuButton[]{
    MainMenuButton.PERFORMANCE,
    MainMenuButton.PRIMARY_CONSIDERATION,
    MainMenuButton.VIEWED
  },true,
    new ConditionBuilder[]{
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.UID.like( Fields.Journal.ORDERS_DDO.getValue()+ "%"  ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.ne( true ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.DOCUMENT_TYPE.eq( V2DocumentType.ORDERS_DDO.getName() )  )
    },
    new ConditionBuilder[]{
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.UID.like( Fields.Journal.ORDERS_DDO.getValue()+ "%"  ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.eq( false ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_PROCESSED_FOLDER.eq( false ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_FAVORITES_FOLDER.eq( false ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.DOCUMENT_TYPE.eq( V2DocumentType.ORDERS_DDO.getName() )  )
    },
    false, false),

  IN_DOCUMENTS ( 7, "Внутренние документы %s", new MainMenuButton[]{
    MainMenuButton.PERFORMANCE,
    MainMenuButton.PRIMARY_CONSIDERATION,
    MainMenuButton.VIEWED
  },
    true,
    new ConditionBuilder[]{
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.UID.like( Fields.Journal.OUTGOING_DOCUMENTS.getValue()+ "%"  ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.eq( false ) )
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.DOCUMENT_TYPE.eq( V2DocumentType.OUTGOING_DOCUMENTS.getName() )  )
    },
    new ConditionBuilder[]{
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.UID.like( Fields.Journal.OUTGOING_DOCUMENTS.getValue()+ "%"  ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.eq( false ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_PROCESSED_FOLDER.eq( false ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_FAVORITES_FOLDER.eq( false ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.DOCUMENT_TYPE.eq( V2DocumentType.OUTGOING_DOCUMENTS.getName() )  )
    },
    false, false),

  ON_CONTROL ( 8, "На контроле %s", new MainMenuButton[]{},
    true,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.CONTROL.eq( true ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.eq( false ) )
    },
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.CONTROL.eq( true ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.eq( false ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_PROCESSED_FOLDER.eq( false ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_FAVORITES_FOLDER.eq( false ) ),
    },
    true, true),
  PROCESSED ( 9, "Обработанное %s", new MainMenuButton[]{},
    true,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_PROCESSED_FOLDER.eq( true ) ),
      new ConditionBuilder( ConditionBuilder.Condition.OR, RDocumentEntity.PROCESSED.eq(true) ),
    },
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_PROCESSED_FOLDER.eq( true ) ),
      new ConditionBuilder( ConditionBuilder.Condition.OR, RDocumentEntity.PROCESSED.eq(true) ),
    },
    true, true),
  FAVORITES ( 10, "Избранное %s", new MainMenuButton[]{},
    true,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FAVORITES.eq( true ) ),
      new ConditionBuilder( ConditionBuilder.Condition.OR, RDocumentEntity.FROM_FAVORITES_FOLDER.eq( true ) ),
    },
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FAVORITES.eq( true ) ),
      new ConditionBuilder( ConditionBuilder.Condition.OR, RDocumentEntity.FROM_FAVORITES_FOLDER.eq( true ) ),

    },
    true, true
  );

  private static final String TAG = "MainMenuItem";
  private final Integer index;
  private final MainMenuButton[] mainMenuButtons;
  private final String name;
  private final Boolean showOrganization;
  private final ConditionBuilder[] countConditions;
  private final ConditionBuilder[] queryConditions;
  private final boolean showAnyWay;
  private final ArrayList<ButtonBuilder> buttonsList = new ArrayList<>();

  private final boolean processed;

  MainMenuItem(final int index, final String name, final MainMenuButton[] mainMenuButtons, Boolean showOrganizations, ConditionBuilder[] countCounditions, ConditionBuilder[] queryConditions, boolean showAnyWay, boolean processed) {
    this.index = index;
    this.name  = name;
    this.mainMenuButtons = mainMenuButtons;
    this.showOrganization = showOrganizations;
    this.countConditions = countCounditions;
    this.queryConditions = queryConditions;
    this.showAnyWay = showAnyWay;
    this.processed = processed;
  }

  public boolean isProcessed() {
    return processed;
  }

  public boolean isShowAnyWay() {
    return showAnyWay;
  }

  public Integer getIndex(){
    return index;
  }

  public ConditionBuilder[] getCountConditions(){
    return countConditions;
  }

  public ConditionBuilder[] getQueryConditions(){
    return queryConditions;
  }

  public String getName(){
    return name;
  }

  public MainMenuButton[] getButtons(){
    return mainMenuButtons;
  }

  public ArrayList<ButtonBuilder> getButtonList(){
    return buttonsList;
  }

  public ArrayList<ButtonBuilder> getMainMenuButtons(){
    Timber.tag(TAG).e("getMainMenuButtons %s", buttonsList);

    if ( buttonsList.size() == 0 ){

      Timber.tag(TAG).e("buttonsList.size() == 0");

      if ( mainMenuButtons.length > 0 ){
        for (int i = 0, length = mainMenuButtons.length-1; i <= length; i++) {

          Timber.tag("CMP").e( "length: %s", Arrays.toString(getQueryConditions()));

          ButtonBuilder button = new ButtonBuilder(
            mainMenuButtons[i].getFormat(),
            mainMenuButtons[i].getConditions(),
            getQueryConditions(),
            isShowAnyWay(),
            mainMenuButtons[i].getIndex()
          );

          if (i == 0){
            button.setLeftCorner();
          } else if ( i == length ){
            button.setRightCorner();
          } else {
            button.setNoneCorner();
          }

          buttonsList.add( button );
        }
      }
    }
    else {
      for (ButtonBuilder button: buttonsList){
        Timber.tag(TAG).e("getMainMenuButtons else recalcuate");
        button.recalculate();
      }
    }

    return buttonsList;
  }

  public Boolean isVisible(){
    return showOrganization;
  }

  public void recalcuate(){

    Timber.tag(TAG).e("recalcuate");
    for (ButtonBuilder button: buttonsList){
      button.recalculate();
    }


  }

  @Override
  public String toString() {
    return name;
  }

}

