package sapotero.rxtest.utils.memory.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.utils.V2DocumentType;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import timber.log.Timber;

public class Filter {

  @Inject ISettings settings;

  private final String TAG = this.getClass().getSimpleName();
  private final ArrayList<ConditionBuilder> conditions;

  private final ArrayList<String> statuses = new ArrayList<>();
  private final ArrayList<String> types    = new ArrayList<>();

  private Boolean isProcessed = false;
  private Boolean isFavorites = false;
  private Boolean isControl   = false;

  public Filter(ArrayList<ConditionBuilder> conditions) {
    this.conditions = conditions;
    processConditions();
    EsdApplication.getManagerComponent().inject(this);
  }

  // FIXME: 06.07.17
  // убрать чейнинг .filter().filter()...
  // из всех мест, заменить везде на 1 метод

  // totallylazy
  // https://totallylazy.com/

  private void processConditions() {
    if (conditions != null) {
      for (ConditionBuilder condition : conditions ){
        if (condition.getField().getLeftOperand() == RDocumentEntity.FILTER){

          try {
            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> array = new Gson().fromJson(String.valueOf(condition.getField().getRightOperand()), listType);

            for (String st: array ) {
              statuses.add(st);
            }

            Timber.tag(TAG).e( "$$ %s | %s", array, array.size() );
          } catch (JsonSyntaxException e) {

            statuses.add( String.valueOf(condition.getField().getRightOperand()) );
          }

        }

        if (condition.getField().getLeftOperand() == RDocumentEntity.DOCUMENT_TYPE){

          String indexName = getIndexName( String.valueOf(condition.getField().getRightOperand()));
          if (indexName != null) {
            Timber.tag(TAG).w("new index: %s", String.valueOf(condition.getField().getRightOperand()));
            types.add( indexName );
//            types.add( String.valueOf(condition.getField().getRightOperand()) );
          }
        }

        if (condition.getField().getLeftOperand() == RDocumentEntity.CONTROL){
          if ( condition.getField().getRightOperand().equals(true)  ){
            isControl = true;
          }
        }

        if ( condition.getField().getLeftOperand() == RDocumentEntity.PROCESSED ){
          if ( condition.getField().getRightOperand().equals(true)  ){
            isProcessed = true;
          }
        }

        if (condition.getField().getLeftOperand() == RDocumentEntity.FAVORITES){
          if ( condition.getField().getRightOperand().equals(true)  ){
            isFavorites = true;
          }
        }

      }
    }
  }

  public Boolean hasStatuses() {
    return statuses.size() > 0;
  }

  public Boolean hasTypes() {
    return types.size() > 0;
  }

  public ArrayList<String> getTypes() {
    return types;
  }

  public ArrayList<String> getStatuses() {
    return statuses;
  }

  public Boolean getProcessed() {
    return isProcessed;
  }

  public Boolean isProcessed(InMemoryDocument doc) {
    Boolean result = true;

    // Фильтруем обработанные для всех журналов, кроме Избранное
    // resolved https://tasks.n-core.ru/browse/MVDESD-13985
    // Убрать из списка "На контроле" рассмотренные документы
//    if ( !isFavorites && !isControl ) {
    if ( !isFavorites ) {
      result = isProcessed == doc.isProcessed();
    }

    return result;
  }

  public Boolean isNotProcessed(InMemoryDocument doc) {
    return isProcessed != doc.isProcessed();
  }

  public Boolean byYear(InMemoryDocument doc) {
    return doc.getYear() == 0 || settings.getYears().contains(String.valueOf(doc.getYear()));
  }

  public Boolean isFavorites(InMemoryDocument doc) {
    Boolean result = true;

    if ( isFavorites ){
      result = doc != null && doc.getDocument() != null && doc.getDocument().getFavorites() != null &&  doc.getDocument().getFavorites() || doc != null && doc.getDocument() != null && doc.getDocument().isFromFavoritesFolder();
    }

    return result;
  }

  public Boolean isControl(InMemoryDocument doc) {
    Boolean result = true;

    if ( isControl ){
      // resolved https://tasks.n-core.ru/browse/MVDESD-13985
      // Не показываем документы из папки Избранное с замочком во вкладке На контроль
      result = doc != null && doc.getDocument() != null && doc.getDocument().getControl() != null && doc.getDocument().getControl() && !doc.getDocument().isFromFavoritesFolder();
    }

    return result;
  }

  public Boolean byType(InMemoryDocument document) {
    return types.size() <= 0 || types.contains( document.getIndex() );
  }

  public Boolean byStatus(InMemoryDocument document) {
    return statuses.size() == 0 || statuses.contains( document.getFilter() );
  }

  public static Boolean isChanged(String s1, String s2){
    return !android.text.TextUtils.equals(s1, s2);
  }

  public static Integer bySortKey(InMemoryDocument imd1, InMemoryDocument imd2) {
    int result = -1;

    if (imd1.getDocument().getSortKey() != null && imd2.getDocument().getSortKey() != null) {
      result = imd1.getDocument().getSortKey().compareTo( imd2.getDocument().getSortKey() );
    }

    return result;
  }

  // resolved https://tasks.n-core.ru/browse/MVDESD-14115
  // Сортировка документов в списке
  public int byJournalDateNumber(InMemoryDocument o1, InMemoryDocument o2) {
    int result = 0;

    // Sort by journal
    Integer journalNumber1 = getJournalNumber( o1.getIndex() );
    Integer journalNumber2 = getJournalNumber( o2.getIndex() );
    result = journalNumber1.compareTo( journalNumber2 );

    if ( result == 0 ) {
      if ( o1.getDocument() == null ) {
        result = 1; // documents with null values should go to the end of the list
      } else if ( o2.getDocument() == null ) {
        result = -1;
      } else {
        // Sort by date
        result = compareDates( o1.getDocument().getRegistrationDate(), o2.getDocument().getRegistrationDate() );

        // Sort by registration number
        if ( result == 0 ) {
          result = compareRegNumbers( o1.getDocument().getRegistrationNumber(), o2.getDocument().getRegistrationNumber() );
        }
      }
    }

    return result;
  }

  private Integer getJournalNumber(String journalName) {
    Integer result;

    if ( Objects.equals( journalName, V2DocumentType.INCOMING_DOCUMENTS.getName() ) ) {
      result = 1;
    } else if ( Objects.equals( journalName, V2DocumentType.CITIZEN_REQUESTS.getName() ) ) {
      result = 2;
    } else if ( Objects.equals( journalName, V2DocumentType.INCOMING_ORDERS.getName() ) ) {
      result = 3;
    } else if ( Objects.equals( journalName, V2DocumentType.ORDERS.getName() ) ) {
      result = 4;
    } else if ( Objects.equals( journalName, V2DocumentType.ORDERS_DDO.getName() ) ) {
      result = 5;
    } else if ( Objects.equals( journalName, V2DocumentType.OUTGOING_DOCUMENTS.getName() ) ) {
      result = 6;
    } else {
      result = 7;
    }

    return result;
  }

  private int compareDates(String o1, String o2) {
    int result = 0;

    if ( o1 == null || Objects.equals( o1, "" ) ) {
      result = 1;
    } else if ( o2 == null || Objects.equals( o2, "" ) ) {
      result = -1;
    } else {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

      try {
        Date date1 = format.parse( o1 );
        Date date2 = format.parse( o2 );
        result = date2.compareTo( date1 );  // вначале свежие
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }

    return result;
  }

  private int compareRegNumbers(String regNum1, String regNum2) {
    int result = 0;

    if ( regNum1 == null || Objects.equals( regNum1, "" ) ) {
      result = 1;
    } else if ( regNum2 == null || Objects.equals( regNum2, "" ) ) {
      result = -1;
    } else {
      if ( regNum1.contains("/") && regNum2.contains("/") ) {
        result = compareRegNumbersWithPrefixes( regNum1, regNum2 );
      } else {
        result = compareNumbers( regNum1, regNum2 );
      }
    }

    return result;
  }

  private int compareNumbers(String o1, String o2) {
    int result = 0;

    if ( fastIntCheck(o1) && fastIntCheck(o2) ){
      Long num1 = Long.valueOf( o1 );
      Long num2 = Long.valueOf( o2 );
      result = num2.compareTo( num1 );  // вначале наибольший номер
    }
    return result;
  }

  // Сравнение регистрационных номеров вида 3/17790032428
  private int compareRegNumbersWithPrefixes(String regNum1, String regNum2) {
    int result = 0;

      String[] split1 = regNum1.split("/");
      String[] split2 = regNum2.split("/");

      if ( split1.length >= 2 && split2.length >= 2 ) {
        String regNum1Prefix = split1[0];
        String regNum1Number = split1[1];

        String regNum2Prefix = split2[0];
        String regNum2Number = split2[1];

        result = compareNumbers(regNum1Prefix, regNum2Prefix);

        // Сортировка по номеру (то, что после "/")
        if ( result == 0 ) {
          result = compareNumbers(regNum1Number, regNum2Number);
        }
      }

    return result;
  }

  public Boolean getControl() {
    return isControl;
  }

  public Boolean getFavorites() {
    return isFavorites;
  }

  public static String getIndexName(String raw_index) {
    String indexName = null;

    if ( raw_index != null ) {
      String[] index = raw_index.split("_production_db_");
      indexName = index[0];
    }

    return indexName;
    }


  private boolean fastIntCheck(String str) {
    return TextUtils.isDigitsOnly(str);
  }

}