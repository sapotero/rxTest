package sapotero.rxtest.views.managers.menu.factories;

import android.content.Context;

import sapotero.rxtest.views.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.views.managers.menu.commands.FromTheReport;
import sapotero.rxtest.views.managers.menu.commands.ReturnToPrimaryConsideration;
import sapotero.rxtest.views.managers.menu.interfaces.Command;
import sapotero.rxtest.views.managers.menu.receivers.DocumentReceiver;
import timber.log.Timber;

public class CommandFactory implements AbstractCommand.Callback{
  private final String TAG = this.getClass().getSimpleName();

  Callback callback;
  private CommandFactory instance;
  private Context context;

  public CommandFactory(Context context) {
    this.context = context;
  }

  public interface Callback {
    void onCommandSuccess();
    void onCommandError();
  }

  public CommandFactory registerCallBack(Callback callback){
    this.callback = callback;
    return this;
  }

  private enum Operation {

    FROM_THE_REPORT {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document) {
        FromTheReport doc = new FromTheReport(context, document);
        doc.registerCallBack(instance);
        return doc;
      }
    },
    RETURN_TO_THE_PRIMARY_CONSIDERATION {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document) {
        ReturnToPrimaryConsideration doc = new ReturnToPrimaryConsideration(context, document);
        doc.registerCallBack(instance);
        return doc;
      }
    },
    TO_THE_APPROVAL_PERFORMANCE {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document) {
        return null;
      }
    },
    TO_THE_PRIMARY_CONSIDERATION {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document) {
        return null;
      }
    },
    DELEGATE_PERFORMANCE {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document) {
        return null;
      }
    },
    CHANGE_PERSON {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document) {
        return null;
      }
    },
    NEXT_PERSON {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document) {
        return null;
      }
    },
    PREV_PERSON {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document) {
        return null;
      }
    },
    ADD_TO_FOLDER {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document) {
        return null;
      }
    },
    REMOVE_FROM_FOLDER {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document) {
        return null;
      }
    },
    CHECK_FOR_CONTROL {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document) {
        return null;
      }
    },
    SKIP_CONTROL_LABEL {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document) {
        return null;
      }
    },
    INCORRECT {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document) {
        return null;
      }
    };


    abstract Command getCommand(CommandFactory instance, Context context, DocumentReceiver document);
  };

  private DocumentReceiver document;

  public CommandFactory withDocument(DocumentReceiver doc) {
    instance = this;
    document = doc;
    return this;
  }

  public Command build(String operation_type) {

    Operation operation = null;

    switch ( operation_type ){
      // sent_to_the_report (отправлен на доклад)
      case "menu_info_from_the_report":
        operation = Operation.FROM_THE_REPORT;
        break;
      case "return_to_the_primary_consideration":
        operation = Operation.RETURN_TO_THE_PRIMARY_CONSIDERATION;
        break;

      // sent_to_the_report (отправлен на доклад)
      case "menu_info_delegate_performance":
        operation = Operation.DELEGATE_PERFORMANCE;
        break;
      case "menu_info_to_the_approval_performance":
        operation = Operation.TO_THE_APPROVAL_PERFORMANCE;
        break;

      // primary_consideration (первичное рассмотрение)
      case "menu_info_to_the_primary_consideration":
        operation = Operation.TO_THE_PRIMARY_CONSIDERATION;
        break;

      // approval (согласование проектов документов)
      case "menu_info_change_person":
        operation = Operation.CHANGE_PERSON;
        break;
      case "menu_info_next_person":
        operation = Operation.NEXT_PERSON;
        break;
      case "menu_info_prev_person":
        operation = Operation.PREV_PERSON;
        break;
      default:
        operation = Operation.INCORRECT;
        break;
    }

    Command command = operation.getCommand(this, context, document);

    return command;
  }


  @Override
  public void onCommandExecuteSuccess() {
    Timber.tag(TAG).w("onCommandExecuteSuccess" );

    if (callback != null){
      callback.onCommandSuccess();
    }
  }

  @Override
  public void onCommandExecuteError() {
    Timber.tag(TAG).w("onCommandExecuteError");

    if (callback != null){
      callback.onCommandError();
    }
  }

}
