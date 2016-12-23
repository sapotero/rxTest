package sapotero.rxtest.views.managers.menu.factories;

import android.content.Context;

import sapotero.rxtest.views.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.views.managers.menu.commands.approval.ChangePerson;
import sapotero.rxtest.views.managers.menu.commands.approval.NextPerson;
import sapotero.rxtest.views.managers.menu.commands.consideration.PrimaryConsideration;
import sapotero.rxtest.views.managers.menu.commands.decision.SaveDecision;
import sapotero.rxtest.views.managers.menu.commands.performance.DelegatePerformance;
import sapotero.rxtest.views.managers.menu.commands.report.FromTheReport;
import sapotero.rxtest.views.managers.menu.commands.report.ReturnToPrimaryConsideration;
import sapotero.rxtest.views.managers.menu.commands.shared.AddToFolder;
import sapotero.rxtest.views.managers.menu.commands.shared.CheckForControl;
import sapotero.rxtest.views.managers.menu.interfaces.Command;
import sapotero.rxtest.views.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.views.managers.menu.utils.CommandParams;
import timber.log.Timber;

public class CommandFactory implements AbstractCommand.Callback{
  private final String TAG = this.getClass().getSimpleName();

  Callback callback;
  private CommandFactory instance;
  private Context context;
  private CommandParams params;
  private DocumentReceiver document;


  public interface Callback {
    void onCommandSuccess(String command);
    void onCommandError();
  }
  public CommandFactory registerCallBack(Callback callback){
    this.callback = callback;
    return this;
  }


  private enum Operation {

    FROM_THE_REPORT {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        FromTheReport doc = new FromTheReport(context, document);
        doc.registerCallBack(instance);
        return doc;
      }
    },
    RETURN_TO_THE_PRIMARY_CONSIDERATION {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        ReturnToPrimaryConsideration doc = new ReturnToPrimaryConsideration(context, document);
        doc.registerCallBack(instance);
        return doc;
      }
    },
    DELEGATE_PERFORMANCE {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        DelegatePerformance command = new DelegatePerformance(context, document);
        command
          .withPerson( params.getPerson() )
          .registerCallBack(instance);
        return command;
      }
    },
    TO_THE_APPROVAL_PERFORMANCE {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        PrimaryConsideration command = new PrimaryConsideration(context, document);
        command
          .withPerson( params.getPerson() )
          .registerCallBack(instance);
        return command;
      }
    },
    TO_THE_PRIMARY_CONSIDERATION {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        return null;
      }
    },
    CHANGE_PERSON {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        ChangePerson command = new ChangePerson(context, document);
        command
          .withPerson( params.getPerson() )
          .registerCallBack(instance);
        return command;
      }
    },
    NEXT_PERSON {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        NextPerson command = new NextPerson(context, document);
        command
          .withPerson( "" )
          .withPerson( "" )
          .registerCallBack(instance);
        return command;
      }
    },
    PREV_PERSON {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        NextPerson command = new NextPerson(context, document);
        command
          .withPerson( "" )
          .withPerson( "" )
          .registerCallBack(instance);
        return command;
      }
    },
    ADD_TO_FOLDER {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        AddToFolder command = new AddToFolder(context, document);
        command
          .withFolder( params.getFolder() )
          .withDocumentId( params.getSign() )
          .registerCallBack(instance);
        return command;
      }
    },
    REMOVE_FROM_FOLDER {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        return null;
      }
    },
    CHECK_FOR_CONTROL {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        CheckForControl command = new CheckForControl(context, document);
        command
          .withDocumentId( params.getSign() )
          .registerCallBack(instance);
        return command;
      }
    },
    SKIP_CONTROL_LABEL {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        return null;
      }
    },
    INCORRECT {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        return null;
      }
    },
    SAVE_DECISION {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        SaveDecision command = new SaveDecision(context, document);
        command
          .withDecision( params.getDecision() )
          .withDecisionId( params.getSign() )
          .registerCallBack(instance);
        return command;
      }
    },
    NEW_DECISION {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        return null;
      }
    },;


    abstract Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params);
  };

  public CommandFactory(Context context) {
    this.context = context;
  }

  public CommandFactory withDocument(DocumentReceiver doc) {
    instance = this;
    document = doc;
    return this;
  }

  public CommandFactory withParams(CommandParams params) {
    this.params = params;
    return this;
  }

  public Command build(String operation_type) {

    Operation operation = null;

    switch ( operation_type ){
      case "save_decision":
        operation = Operation.SAVE_DECISION;
        break;
      case "new_decision":
        operation = Operation.SAVE_DECISION;
        break;

      // sent_to_the_report (отправлен на доклад)
      case "menu_info_from_the_report":
        operation = Operation.FROM_THE_REPORT;
        break;
      case "return_to_the_primary_consideration":
        operation = Operation.RETURN_TO_THE_PRIMARY_CONSIDERATION;
        break;

      // sent_to_the_performance (Отправлен на исполнение)
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
      case "menu_info_shared_to_favorites":
        operation = Operation.ADD_TO_FOLDER;
        break;
      case "menu_info_shared_to_control":
        operation = Operation.CHECK_FOR_CONTROL;
        break;

      default:
        operation = Operation.INCORRECT;
        break;
    }

    Command command = operation.getCommand(this, context, document, params);

    return command;
  }


  @Override
  public void onCommandExecuteSuccess(String command) {
    Timber.tag(TAG).w("onCommandExecuteSuccess" );

    if (callback != null){
      callback.onCommandSuccess(command);
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
