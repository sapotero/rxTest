package sapotero.rxtest.views.menu.builders;

import java.io.Serializable;

import io.requery.query.Expression;
import io.requery.query.LogicalCondition;

public class ConditionBuilder implements Serializable {

  public enum Condition{
    WHERE,
    AND,
    OR,
    LIKE;
  }

  private final Condition condition;

  private LogicalCondition<? extends Expression<?>, ?> field;

  public ConditionBuilder(Condition condition, LogicalCondition<? extends Expression<?>, ?> field) {
    this.condition = condition;
    this.field = field;
  }

  public Condition getCondition() {
    return condition;
  }

  public String toString(){
    return String.format( "NAME: %s - %s %s %s ", condition, field.getLeftOperand(), field.getOperator(), field.getRightOperand() );
  }

  public LogicalCondition<? extends Expression<?>, ?> getField() {
    return field;
  }

}
