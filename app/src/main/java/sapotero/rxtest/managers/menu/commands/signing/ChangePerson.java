package sapotero.rxtest.managers.menu.commands.signing;

import java.util.List;

import sapotero.rxtest.managers.menu.commands.ApprovalSigningCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;

public class ChangePerson extends ApprovalSigningCommand {

  public ChangePerson(CommandParams params) {
    super(params);
  }

  @Override
  public String getType() {
    return "change_person";
  }

  @Override
  public void executeLocal() {
    local( false );
  }

  @Override
  public void executeRemote() {
    approvalSigningRemote();
  }

  @Override
  public void finishOnOperationSuccess() {
    finishProcessedOperationOnSuccess();
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    finishRejectedProcessedOperationOnError( errors );
  }
}
