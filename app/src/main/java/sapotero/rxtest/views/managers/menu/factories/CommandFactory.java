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
import sapotero.rxtest.views.managers.menu.commands.signing.PrevPerson;
import sapotero.rxtest.views.managers.menu.interfaces.Command;
import sapotero.rxtest.views.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.views.managers.menu.utils.CommandParams;
import timber.log.Timber;

public class CommandFactory implements AbstractCommand.Callback{
  private final String TAG = this.getClass().getSimpleName();

  private CommandFactory instance;
  private Context context;
  private CommandParams params;
  private DocumentReceiver document;

  Callback callback;
  public interface Callback {
    void onCommandSuccess(String command);
    void onCommandError();
  }
  public CommandFactory registerCallBack(Callback callback){
    this.callback = callback;
    return this;
  }


  public enum Operation {

    FROM_THE_REPORT {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        FromTheReport command = new FromTheReport(context, document);
//        doc.withHistory(histrory);
        command.registerCallBack(instance);
        command.withParams(params);
        return command;
      }
    },
    RETURN_TO_THE_PRIMARY_CONSIDERATION {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        ReturnToPrimaryConsideration command = new ReturnToPrimaryConsideration(context, document);
//        doc.withHistory(histrory);
        command.registerCallBack(instance);
        command.withParams(params);
        return command;
      }
    },
    DELEGATE_PERFORMANCE {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        DelegatePerformance command = new DelegatePerformance(context, document);
        command.withParams(params);
        command
          .withPerson( params.getPerson() )
          .registerCallBack(instance);
        command.withParams(params);
        return command;
      }
    },
    TO_THE_APPROVAL_PERFORMANCE {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        PrimaryConsideration command = new PrimaryConsideration(context, document);
        command.withParams(params);
        command
          .withPerson( params.getPerson() )
          .registerCallBack(instance);
        command.withParams(params);
        return command;
      }
    },
    TO_THE_PRIMARY_CONSIDERATION {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        return null;
      }
    },
    APPROVAL_CHANGE_PERSON {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        ChangePerson command = new ChangePerson(context, document);
        command.withParams(params);
        command
          .withPerson( params.getPerson() )
          .registerCallBack(instance);
        command.withParams(params);
        return command;
      }
    },
    APPROVAL_NEXT_PERSON {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        NextPerson command = new NextPerson(context, document);
        command.withParams(params);
        command
          .withPerson( "" )
          .registerCallBack(instance);
        command.withParams(params);
        return command;
      }
    },
    APPROVAL_PREV_PERSON {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        PrevPerson command = new PrevPerson(context, document);
        command.withParams(params);
        command
          .withPerson( "" )
          .registerCallBack(instance);
        command.withParams(params);
        return command;
      }
    },
    SIGNING_CHANGE_PERSON {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        ChangePerson command = new ChangePerson(context, document);
        command.withParams(params);
        command
          .withPerson( params.getPerson() )
          .registerCallBack(instance);
        command.withParams(params);
        return command;
      }
    },
    SIGNING_NEXT_PERSON {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        NextPerson command = new NextPerson(context, document);
        command.withParams(params);
        command
          .withPerson( "" )
          .registerCallBack(instance);
        command.withParams(params);
        return command;
      }
    },
    SIGNING_PREV_PERSON {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        PrevPerson command = new PrevPerson(context, document);
        command.withParams(params);
        command
          .withPerson( "" )
          .registerCallBack(instance);
        command.withParams(params);
        return command;
      }
    },
    ADD_TO_FOLDER {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        AddToFolder command = new AddToFolder(context, document);
        command.withParams(params);
        command
          .withFolder( params.getFolder() )
          .withDocumentId( params.getDocument() )
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
        command.withParams(params);
        command
          .withDocumentId( params.getDocument() )
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
        command.withParams(params);
        command
          .withDecision( params.getDecision() )
          .withDecisionId( params.getSign() )
          .registerCallBack(instance);

        command.withParams(params);
        return command;
      }
    },
    NEW_DECISION {
      @Override
      Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        return null;
      }
    };

    public static Operation getOperation(String operation_type){
      Operation operation = Operation.INCORRECT;

      switch ( operation_type ){
        case "sapotero.rxtest.views.managers.menu.commands.decision.SaveDecision":
          operation = Operation.SAVE_DECISION;
          break;
        case "sapotero.rxtest.views.managers.menu.commands.decision.AddDecision":
          operation = Operation.NEW_DECISION;
          break;

        case "sapotero.rxtest.views.managers.menu.commands.report.ReturnToPrimaryConsideration":
          operation = Operation.RETURN_TO_THE_PRIMARY_CONSIDERATION;
          break;
        // sent_to_the_report (отправлен на доклад)
        case "sapotero.rxtest.views.managers.menu.commands.report.FromTheReport":
          operation = Operation.FROM_THE_REPORT;
          break;

        // sent_to_the_performance (Отправлен на исполнение)
        case "sapotero.rxtest.views.managers.menu.commands.performance.DelegatePerformance":
          operation = Operation.DELEGATE_PERFORMANCE;
          break;
        case "sapotero.rxtest.views.managers.menu.commands.performance.ApprovalPerformance":
          operation = Operation.TO_THE_APPROVAL_PERFORMANCE;
          break;

        // primary_consideration (первичное рассмотрение)
        case "sapotero.rxtest.views.managers.menu.commands.consideration.PrimaryConsideration":
          operation = Operation.TO_THE_PRIMARY_CONSIDERATION;
          break;

        // approval (согласование проектов документов)
        case "sapotero.rxtest.views.managers.menu.commands.approval.ChangePerson":
          operation = Operation.APPROVAL_CHANGE_PERSON;
          break;
        case "sapotero.rxtest.views.managers.menu.commands.approval.NextPerson":
          operation = Operation.APPROVAL_NEXT_PERSON;
          break;
        case "sapotero.rxtest.views.managers.menu.commands.approval.PrevPerson":
          operation = Operation.APPROVAL_PREV_PERSON;
          break;

        // approval (согласование проектов документов)
        case "sapotero.rxtest.views.managers.menu.commands.signing.ChangePerson":
          operation = Operation.SIGNING_CHANGE_PERSON;
          break;
        case "sapotero.rxtest.views.managers.menu.commands.signing.NextPerson":
          operation = Operation.SIGNING_NEXT_PERSON;
          break;
        case "sapotero.rxtest.views.managers.menu.commands.signing.PrevPerson":
          operation = Operation.SIGNING_PREV_PERSON;
          break;


        case "sapotero.rxtest.views.managers.menu.commands.shared.AddToFolder":
          operation = Operation.ADD_TO_FOLDER;
          break;
        case "sapotero.rxtest.views.managers.menu.commands.shared.CheckForControl":
          operation = Operation.CHECK_FOR_CONTROL;
          break;


        default:
          operation = Operation.INCORRECT;
          break;
      }
      return operation;
    }

    abstract Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params);
  };

  public CommandFactory(Context context) {
    this.context = context;
  }

  public CommandFactory withDocument(DocumentReceiver doc) {
    document = doc;
    return this;
  }

  public CommandFactory withParams(CommandParams params) {
    this.params = params;
    return this;
  }

  public Command build(CommandFactory.Operation operation) {
    return operation.getCommand(this, context, document, params);
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
