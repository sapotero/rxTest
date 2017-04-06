package sapotero.rxtest.managers.menu.factories;

import android.content.Context;

import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.commands.approval.ChangePerson;
import sapotero.rxtest.managers.menu.commands.decision.AddAndApproveDecision;
import sapotero.rxtest.managers.menu.commands.decision.AddDecision;
import sapotero.rxtest.managers.menu.commands.decision.AddTemporaryDecision;
import sapotero.rxtest.managers.menu.commands.decision.ApproveDecision;
import sapotero.rxtest.managers.menu.commands.decision.ApproveDecisionDelayed;
import sapotero.rxtest.managers.menu.commands.decision.RejectDecision;
import sapotero.rxtest.managers.menu.commands.decision.SaveAndApproveDecision;
import sapotero.rxtest.managers.menu.commands.decision.SaveDecision;
import sapotero.rxtest.managers.menu.commands.decision.SaveTemporaryDecision;
import sapotero.rxtest.managers.menu.commands.file.SignFile;
import sapotero.rxtest.managers.menu.commands.performance.ApprovalPerformance;
import sapotero.rxtest.managers.menu.commands.performance.DelegatePerformance;
import sapotero.rxtest.managers.menu.commands.primary_consideration.PrimaryConsideration;
import sapotero.rxtest.managers.menu.commands.report.FromTheReport;
import sapotero.rxtest.managers.menu.commands.report.ReturnToPrimaryConsideration;
import sapotero.rxtest.managers.menu.commands.shared.AddToFolder;
import sapotero.rxtest.managers.menu.commands.shared.CheckForControl;
import sapotero.rxtest.managers.menu.commands.shared.DoNothing;
import sapotero.rxtest.managers.menu.commands.shared.RemoveFromFolder;
import sapotero.rxtest.managers.menu.commands.signing.PrevPerson;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import timber.log.Timber;

public class CommandFactory implements AbstractCommand.Callback{
  private static CommandFactory instance;
  private final String TAG = this.getClass().getSimpleName();

  private Context context;
  private CommandParams params;
  private DocumentReceiver document;

  Callback callback;

  public static CommandFactory getInstance() {
    if (instance == null){
      instance = new CommandFactory(EsdApplication.getContext());
    }
    return instance;
  }

  public interface Callback {
    void onCommandSuccess(String command);
    void onCommandError();
  }
  public CommandFactory registerCallBack(Callback callback){
    this.callback = callback;
    return this;
  }


  public enum Operation {
    FILE_SIGN {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        SignFile command = new SignFile(context, document);
        command.registerCallBack(instance);
        command.withParams(params);
        return command;
      }

      @Override
      public String getRussinaName() {
        return "Подписание электронного образа";
      }
    },
    FROM_THE_REPORT {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        FromTheReport command = new FromTheReport(context, document);
//        doc.withHistory(histrory);
        command.registerCallBack(instance);
        command.withParams(params);
        return command;
      }

      @Override
      public String getRussinaName() {
        return "Возврат с доклада";
      }
    },
    RETURN_TO_THE_PRIMARY_CONSIDERATION {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        ReturnToPrimaryConsideration command = new ReturnToPrimaryConsideration(context, document);
//        doc.withHistory(histrory);
        command.registerCallBack(instance);
        command.withParams(params);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Отклонения документа с возвратом на первичное рассмотрение";
      }
    },
    DELEGATE_PERFORMANCE {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        DelegatePerformance command = new DelegatePerformance(context, document);
        command.withParams(params);
        command
          .withPerson( params.getPerson() )
          .registerCallBack(instance);
        command.withParams(params);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Передача исполнения";
      }
    },
    TO_THE_APPROVAL_PERFORMANCE {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        ApprovalPerformance command = new ApprovalPerformance(context, document);
        command.withParams(params);
        command
          .withPerson( params.getPerson() )
          .registerCallBack(instance);
        command.withParams(params);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Исполнение без ответа";
      }
    },
    TO_THE_PRIMARY_CONSIDERATION {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        PrimaryConsideration command = new PrimaryConsideration(context, document);
        command.withParams(params);
        command
          .withPerson( params.getPerson() )
          .registerCallBack(instance);
        command.withParams(params);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Передачи первичного рассмотрения";
      }
    },
    APPROVAL_CHANGE_PERSON {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        sapotero.rxtest.managers.menu.commands.signing.ChangePerson command = new sapotero.rxtest.managers.menu.commands.signing.ChangePerson(context, document);
        command.withParams(params);
        command
          .withPerson( params.getPerson() )
          .registerCallBack(instance);
        command.withParams(params);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Передача согласования";
      }
    },
    APPROVAL_NEXT_PERSON {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        sapotero.rxtest.managers.menu.commands.approval.NextPerson command = new sapotero.rxtest.managers.menu.commands.approval.NextPerson(context, document);
        command.withParams(params);
        command
          .registerCallBack(instance);
        command.withParams(params);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Передача согласования документа следующему в маршруте ДЛ";
      }
    },
    APPROVAL_PREV_PERSON {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        PrevPerson command = new PrevPerson(context, document);
        command.withParams(params);
        command
          .withPerson( "" )
          .registerCallBack(instance);
        command.withParams(params);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Передача согласования документа предыдущему в маршруте ДЛ";
      }
    },
    SIGNING_CHANGE_PERSON {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        ChangePerson command = new ChangePerson(context, document);
        command.withParams(params);
        command
          .withPerson( params.getPerson() )
          .registerCallBack(instance);
        command.withParams(params);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Передача подписания";
      }
    },
    SIGNING_NEXT_PERSON {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        sapotero.rxtest.managers.menu.commands.signing.NextPerson command = new sapotero.rxtest.managers.menu.commands.signing.NextPerson(context, document);
        command.withParams(params);
        command
          .registerCallBack(instance);
        command.withParams(params);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Передача подписания документа предыдущему в маршруте ДЛ";
      }
    },
    SIGNING_PREV_PERSON {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        PrevPerson command = new PrevPerson(context, document);
        command.withParams(params);
        command
          .withPerson( "" )
          .registerCallBack(instance);
        command.withParams(params);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Передача подписания документа предыдущему в маршруте ДЛ";
      }
    },
    ADD_TO_FOLDER {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        AddToFolder command = new AddToFolder(context, document);
        command.withParams(params);
        command
          .withFolder( params.getFolder() )
          .withDocumentId( params.getDocument() )
          .registerCallBack(instance);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Добавление в избранное";
      }
    },
    REMOVE_FROM_FOLDER {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        RemoveFromFolder command = new RemoveFromFolder(context, document);
        command.withParams(params);
        command
          .withFolder( params.getFolder() )
          .withDocumentId( params.getDocument() )
          .registerCallBack(instance);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Удаление из избранного";
      }
    },
    CHECK_FOR_CONTROL {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        CheckForControl command = new CheckForControl(context, document);
        command.withParams(params);
        command
          .withDocumentId( params.getDocument() )
          .registerCallBack(instance);
        
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Установка/удаление отметки о необходимости постановки на контроль";
      }
    },
    SKIP_CONTROL_LABEL {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        return null;
      }
      @Override
      public String getRussinaName() {
        return "Установка/удаление отметки о необходимости постановки на контроль";
      }
    },
    INCORRECT {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        DoNothing command = new DoNothing(context, document);
        command.withParams(params);

        return command;
      }
      @Override
      public String getRussinaName() {
        return "Операция заглушка для тестов";
      }
    },
    SAVE_DECISION {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        SaveDecision command = new SaveDecision(context, document);
        command.withParams(params);
        command
//          .withDecision( params.getDecision() )
          .withDecisionId( params.getDecisionId() )
          .registerCallBack(instance);

        command.withParams(params);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Сохранение резолюции";
      }
    },
    CREATE_DECISION {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        AddDecision command = new AddDecision(context, document);
        command.withParams(params);
        command
          .withDecisionId( params.getDecisionId() )
          .registerCallBack(instance);

        command.withParams(params);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Создание резолюции";
      }
    },

    CREATE_TEMPORARY_DECISION {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        AddTemporaryDecision command = new AddTemporaryDecision(context, document);
        command.withParams(params);
        command
          .withDecisionId( params.getDecisionId() )
          .registerCallBack(instance);

        command.withParams(params);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "*Создание резолюции";
      }
    },
    SAVE_TEMPORARY_DECISION {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        SaveTemporaryDecision command = new SaveTemporaryDecision(context, document);
        command.withParams(params);
        command.registerCallBack(instance);

        command.withParams(params);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "*Сохранение резолюции";
      }
    },
    SAVE_AND_APPROVE_DECISION {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        SaveAndApproveDecision command = new SaveAndApproveDecision(context, document);
        command.withParams(params);
        command
//          .withDecision( params.getDecision() )
          .withDecisionId( params.getDecisionId() )
          .withSign(true)
          .registerCallBack(instance);

        command.withParams(params);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Сохранение и подписание резолюции";
      }
    },

    CREATE_AND_APPROVE_DECISION {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        AddAndApproveDecision command = new AddAndApproveDecision(context, document);
        command.withParams(params);
        command.registerCallBack(instance);

        command.withParams(params);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Создание и подписание резолюции";
      }
    },
    APPROVE_DECISION {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        ApproveDecision command = new ApproveDecision(context, document);
        command.withParams(params);
        command
//          .withDecision( params.getDecision() )
          .withDecisionId( params.getDecisionId() )
          .registerCallBack(instance);

        command.withParams(params);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Подписание резолюции";
      }
    },
    APPROVE_DECISION_DELAYED {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        ApproveDecisionDelayed command = new ApproveDecisionDelayed(context, document);
        command.withParams(params);
        command.registerCallBack(instance);
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Подписание резолюции";
      }
    },

    REJECT_DECISION {
      @Override
      public Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params) {
        Timber.tag("CommandFactory").w("REJECT_DECISION star" );

        RejectDecision command = new RejectDecision(context, document);
        Timber.tag("CommandFactory").w("REJECT_DECISION create" );
        command.withParams(params);
        Timber.tag("CommandFactory").w("REJECT_DECISION params" );
        command
//          .withDecision( params.getDecision() )
          .withDecisionId( params.getDecisionId() )
          .registerCallBack(instance);

        Timber.tag("CommandFactory").w("REJECT_DECISION after register callback" );

        command.withParams(params);
        Timber.tag("CommandFactory").w("REJECT_DECISION after params" );
        return command;
      }
      @Override
      public String getRussinaName() {
        return "Отклонение резолюции";
      }
    };

    public static Operation getOperation(String operation_type){
      Operation operation = Operation.INCORRECT;

      switch ( operation_type ){
        case "sapotero.rxtest.managers.menu.commands.file.SignFile":
          operation = Operation.FILE_SIGN;
          break;

        case "sapotero.rxtest.managers.menu.commands.decision.AddDecision":
          operation = Operation.CREATE_DECISION;
          break;
        case "sapotero.rxtest.managers.menu.commands.decision.AddTemporaryDecision":
          operation = Operation.CREATE_TEMPORARY_DECISION;
          break;
        case "sapotero.rxtest.managers.menu.commands.decision.AddAndApproveDecision":
          operation = Operation.CREATE_AND_APPROVE_DECISION;
          break;

        case "sapotero.rxtest.managers.menu.commands.decision.SaveDecision":
          operation = Operation.SAVE_DECISION;
          break;
        case "sapotero.rxtest.managers.menu.commands.decision.SaveAndApproveDecision":
          operation = Operation.SAVE_AND_APPROVE_DECISION;
          break;
        case "sapotero.rxtest.managers.menu.commands.decision.SaveTemporaryDecision":
          operation = Operation.SAVE_TEMPORARY_DECISION;
          break;


        case "sapotero.rxtest.managers.menu.commands.decision.ApproveDecision":
          operation = Operation.APPROVE_DECISION;
          break;
        case "sapotero.rxtest.managers.menu.commands.decision.ApproveDecisionDelayed":
          operation = Operation.APPROVE_DECISION_DELAYED;
          break;

        case "sapotero.rxtest.managers.menu.commands.decision.RejectDecision":
          operation = Operation.REJECT_DECISION;
          break;

        case "sapotero.rxtest.managers.menu.commands.report.ReturnToPrimaryConsideration":
          operation = Operation.RETURN_TO_THE_PRIMARY_CONSIDERATION;
          break;
        // sent_to_the_report (отправлен на доклад)
        case "sapotero.rxtest.managers.menu.commands.report.FromTheReport":
          operation = Operation.FROM_THE_REPORT;
          break;

        // sent_to_the_performance (Отправлен на исполнение)
        case "sapotero.rxtest.managers.menu.commands.performance.DelegatePerformance":
          operation = Operation.DELEGATE_PERFORMANCE;
          break;
        case "sapotero.rxtest.managers.menu.commands.performance.ApprovalPerformance":
          operation = Operation.TO_THE_APPROVAL_PERFORMANCE;
          break;

        // primary_consideration (первичное рассмотрение)
        case "sapotero.rxtest.managers.menu.commands.primary_consideration.PrimaryConsideration":
          operation = Operation.TO_THE_PRIMARY_CONSIDERATION;
          break;

        // approval (согласование проектов документов)
        case "sapotero.rxtest.managers.menu.commands.approval.ChangePerson":
          operation = Operation.APPROVAL_CHANGE_PERSON;
          break;
        case "sapotero.rxtest.managers.menu.commands.approval.NextPerson":
          operation = Operation.APPROVAL_NEXT_PERSON;
          break;
        case "sapotero.rxtest.managers.menu.commands.approval.PrevPerson":
          operation = Operation.APPROVAL_PREV_PERSON;
          break;

        // approval (согласование проектов документов)
        case "sapotero.rxtest.managers.menu.commands.signing.ChangePerson":
          operation = Operation.SIGNING_CHANGE_PERSON;
          break;
        case "sapotero.rxtest.managers.menu.commands.signing.NextPerson":
          operation = Operation.SIGNING_NEXT_PERSON;
          break;
        case "sapotero.rxtest.managers.menu.commands.signing.PrevPerson":
          operation = Operation.SIGNING_PREV_PERSON;
          break;


        case "sapotero.rxtest.managers.menu.commands.shared.AddToFolder":
          operation = Operation.ADD_TO_FOLDER;
          break;
        case "sapotero.rxtest.managers.menu.commands.shared.CheckForControl":
          operation = Operation.CHECK_FOR_CONTROL;
          break;

        case "sapotero.rxtest.managers.menu.commands.shared.DoNothing":
          operation = Operation.INCORRECT;
          break;
      }
      return operation;
    }

    public abstract Command getCommand(CommandFactory instance, Context context, DocumentReceiver document, CommandParams params);
    public abstract String getRussinaName();
  };

  public CommandFactory(Context context) {


    this.context = context;
  }

  public CommandFactory withDocument(DocumentReceiver doc) {
    Timber.tag(TAG).w("withDocument" );
    document = doc;
    return this;
  }

  public CommandFactory withParams(CommandParams params) {
    Timber.tag(TAG).w("withParams" );
    this.params = params;
    return this;
  }

  public Command build(CommandFactory.Operation operation) {
    Timber.tag(TAG).w("build" );

    Command command = operation.getCommand(this, context, document, params);

    Timber.tag("CommandFactory").w("after build" );
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
  public void onCommandExecuteError(String type) {
    Timber.tag(TAG).w("onCommandExecuteError");

    if (callback != null){
      callback.onCommandError();
    }
  }

}
