package sapotero.rxtest.views.menu.fields;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;

public enum Button {

  PROJECTS ( 1,
    "Проекты %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq("approval")  ),
      new ConditionBuilder( ConditionBuilder.Condition.OR,  RDocumentEntity.FILTER.eq("signing")  ), }
  ),
  PERFORMANCE ( 2,
    "На рассмотрение %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq("primary_consideration")  ),
    }
  ),
  PRIMARY_CONSIDERATION ( 3,
    "Первичное рассмотрение %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq("primary_consideration")  ),
    }
  ),
  VIEWED ( 4,
    "Рассмотренные %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq("viewed")  ),
    }
  ),
  ASSIGN ( 5,
    "На подпись %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq("signing")  ),
    }
  ),
  APPROVAL ( 6, "На согласование %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq("approval")  ),
    }
  ),
  PROCESSED ( 7, "Обработанные %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq("success")  ),
    }
  );

  private final Integer index;
  public final String format;
  public final ConditionBuilder[] conditions;

  public Boolean active;

  Button( final Integer index, final String format, final ConditionBuilder[] conditions ) {
    this.index = index;
    this.format = format;
    this.conditions = conditions;
  }

  public ConditionBuilder[] getConditions() {
    return conditions;
  }

  public String getFormat() {
    return format;
  }
}