package sapotero.rxtest.db.requery.utils;

import java.util.Objects;

public class Fields {

  public enum Journal {
    ALL_JOURNALS              ( "90", " ",                     ""),
    CITIZEN_REQUESTS          ( "01", "Обращение граждан",     "Вам поступило "),
    INCOMING_DOCUMENTS        ( "02", "Входящий документ",     "Вам поступил "),
    INCOMING_ORDERS           ( "03", "НПА",                   "Вам поступил "),
    ORDERS                    ( "04", "Приказ старый",         "Вам поступил "),
    OUTGOING_DOCUMENTS        ( "05", "Исходящий документ",    "Вам поступил "),
    ORDERS_DDO                ( "10", "Приказ",                "Вам поступил "),
    SIGN                      ( "98", "Подписание",            "Вам поступил документ на "),
    APPROVE                   ( "99", "Согласование",          "Вам поступил документ на ");

    private final String value;
    private final String single;
    private final String formattedName;

    Journal(final String value, final String single, final  String formattedName) {
      this.value = value;
      this.single  = single;
      this.formattedName = formattedName;
    }

    public String getFormattedName() {
      return formattedName;
    }

    public String getValue(){
      return value;
    }
    public String getSingle() {
      return single;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  public enum Menu {
    ALL                ( 0  , "Документы / Проекты" ),
    INCOMING_DOCUMENTS ( 1  , "Входящие документы" ),
    CITIZEN_REQUESTS   ( 2  , "Обращения граждан" ),
    APPROVE_ASSIGN     ( 3  , "Подписание/Согласование" ),
    INCOMING_ORDERS    ( 4  , "НПА" ),
    ORDERS             ( 5  , "Приказы" ),
    ORDERS_DDO         ( 6  , "Приказы ДДО" ),
    IN_DOCUMENTS       ( 7  , "Внутренние документы" ),
    ON_CONTROL         ( 8  , "На контроле" ),
    PROCESSED          ( 9  , "Обработанное" ),
    FAVORITES          ( 10 , "Избранное" );

    private final Integer index;
    private final String title;

    Menu(final int index, final String title) {
      this.index  = index;
      this.title  = title;
    }

    public Integer getIndex() {
      return index;
    }

    public String getTitle() {
      return title;
    }

    public static Menu getMenu( String string){
      Menu menu = null;

      for ( Menu item: Menu.values()  ){
        if ( Objects.equals( item.getIndex() , Integer.valueOf(string)) ){
          menu = item;
          break;
        }
      }

      return menu;
    }
  }
}
