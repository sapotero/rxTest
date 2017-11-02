package sapotero.rxtest.managers.menu.commands.signing;

import java.util.List;

import sapotero.rxtest.managers.menu.commands.ApprovalSigningCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;

public class PrevPerson extends ApprovalSigningCommand {

  public PrevPerson(CommandParams params) {
    super(params);
  }

  @Override
  public String getType() {
    return "prev_person";
  }

  @Override
  public void executeLocal() {
    local( true );
  }

  @Override
  public void executeRemote() {
    approvalSigningRemote();
  }

  @Override
  public void finishOnOperationSuccess() {
    finishRejectedOperationOnSuccess();
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    finishRejectedProcessedOperationOnError( errors );
  }
}
