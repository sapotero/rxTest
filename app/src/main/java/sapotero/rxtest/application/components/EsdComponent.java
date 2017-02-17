package sapotero.rxtest.application.components;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.application.modules.EsdModule;
import sapotero.rxtest.application.modules.SettingsModule;
import sapotero.rxtest.db.requery.query.DBQueryBuilder;
import sapotero.rxtest.events.utils.SubscriptionsModule;
import sapotero.rxtest.jobs.bus.BaseJob;
import sapotero.rxtest.jobs.utils.JobModule;
import sapotero.rxtest.retrofit.utils.OkHttpModule;
import sapotero.rxtest.utils.queue.QueueManager;
import sapotero.rxtest.utils.queue.db.QueueDBManager;
import sapotero.rxtest.utils.queue.utils.QueueManagerModule;
import sapotero.rxtest.views.activities.DecisionConstructorActivity;
import sapotero.rxtest.views.activities.DocumentImageFullScreenActivity;
import sapotero.rxtest.views.activities.DocumentInfocardFullScreenActivity;
import sapotero.rxtest.views.activities.InfoActivity;
import sapotero.rxtest.views.activities.InfoNoMenuActivity;
import sapotero.rxtest.views.activities.LoginActivity;
import sapotero.rxtest.views.activities.MainActivity;
import sapotero.rxtest.views.activities.SettingsActivity;
import sapotero.rxtest.views.activities.SettingsTemplatesActivity;
import sapotero.rxtest.views.adapters.DecisionAdapter;
import sapotero.rxtest.views.adapters.DocumentsAdapter;
import sapotero.rxtest.views.adapters.OshsAutoCompleteAdapter;
import sapotero.rxtest.views.adapters.PrimaryConsiderationAdapter;
import sapotero.rxtest.views.adapters.models.DocumentTypeItem;
import sapotero.rxtest.views.custom.stepper.build.steps.StepperAuthFragment;
import sapotero.rxtest.views.custom.stepper.build.steps.StepperChooseAuthTypeFragment;
import sapotero.rxtest.views.custom.stepper.build.steps.StepperLoadDataFragment;
import sapotero.rxtest.views.dialogs.InfoCardDialogFragment;
import sapotero.rxtest.views.dialogs.SelectOshsDialogFragment;
import sapotero.rxtest.views.dialogs.SelectTemplateDialogFragment;
import sapotero.rxtest.views.fragments.DecisionFragment;
import sapotero.rxtest.views.fragments.DecisionPreviewFragment;
import sapotero.rxtest.views.fragments.InfoActivityDecisionPreviewFragment;
import sapotero.rxtest.views.fragments.InfoCardDocumentsFragment;
import sapotero.rxtest.views.fragments.InfoCardFieldsFragment;
import sapotero.rxtest.views.fragments.InfoCardLinksFragment;
import sapotero.rxtest.views.fragments.InfoCardWebViewFragment;
import sapotero.rxtest.views.fragments.RoutePreviewFragment;
import sapotero.rxtest.views.fragments.SettingsViewFragment;
import sapotero.rxtest.views.managers.CurrentDocumentManager;
import sapotero.rxtest.views.managers.DataLoaderManager;
import sapotero.rxtest.views.managers.db.managers.DBDocumentManager;
import sapotero.rxtest.views.managers.db.utils.DBDocumentManagerModule;
import sapotero.rxtest.views.managers.menu.OperationManager;
import sapotero.rxtest.views.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.views.managers.menu.commands.approval.ChangePerson;
import sapotero.rxtest.views.managers.menu.commands.approval.NextPerson;
import sapotero.rxtest.views.managers.menu.commands.approval.PrevPerson;
import sapotero.rxtest.views.managers.menu.commands.decision.AddDecision;
import sapotero.rxtest.views.managers.menu.commands.decision.SaveDecision;
import sapotero.rxtest.views.managers.menu.commands.performance.ApprovalPerformance;
import sapotero.rxtest.views.managers.menu.commands.performance.DelegatePerformance;
import sapotero.rxtest.views.managers.menu.commands.report.FromTheReport;
import sapotero.rxtest.views.managers.menu.commands.report.ReturnToPrimaryConsideration;
import sapotero.rxtest.views.managers.menu.commands.shared.AddToFolder;
import sapotero.rxtest.views.managers.menu.commands.shared.CheckForControl;
import sapotero.rxtest.views.managers.menu.utils.OperationHistory;
import sapotero.rxtest.views.managers.menu.utils.OperationManagerModule;
import sapotero.rxtest.views.managers.toolbar.ToolbarManager;
import sapotero.rxtest.views.managers.view.DecisionManager;
import sapotero.rxtest.views.menu.builders.ButtonBuilder;
import sapotero.rxtest.views.menu.factories.ItemsBuilder;
import sapotero.rxtest.views.services.MainService;

@Singleton
@Component(modules = {
  EsdModule.class,
  JobModule.class,
  SubscriptionsModule.class,
  OkHttpModule.class,
  SettingsModule.class,
  DBDocumentManagerModule.class,
  QueueManagerModule.class,
  OperationManagerModule.class
})

public interface EsdComponent {



  void inject(SettingsViewFragment activity);

  void inject(EsdApplication activity);

  void inject(LoginActivity activity);

  void inject(MainActivity  activity);
  void inject(InfoActivity  activity);
  void inject(SettingsActivity activity);
  void inject(DecisionConstructorActivity activity);
  void inject(DocumentImageFullScreenActivity activity);
  void inject(DocumentInfocardFullScreenActivity activity);
  void inject(SettingsTemplatesActivity activity);
  void inject(InfoNoMenuActivity activity);
  void inject(MainService service);


  void inject(DecisionAdapter adapter);

  void inject(DocumentsAdapter adapter);
  void inject(PrimaryConsiderationAdapter adapter);

  void inject(OshsAutoCompleteAdapter context);

  void inject(DBDocumentManager context);
  void inject(QueueManager context);

  void inject(StepperLoadDataFragment fragment);
  void inject(DecisionFragment fragment);
  void inject(DecisionPreviewFragment fragment);
  void inject(InfoCardWebViewFragment fragment);
  void inject(InfoCardDocumentsFragment fragment);
  void inject(SelectOshsDialogFragment fragment);
  void inject(InfoCardFieldsFragment fragment);
  void inject(InfoCardLinksFragment fragment);
  void inject(RoutePreviewFragment fragment);
  void inject(InfoActivityDecisionPreviewFragment fragment);
  void inject(SelectTemplateDialogFragment fragment);
  void inject(InfoCardDialogFragment fragment);
  void inject(StepperAuthFragment fragment);
  void inject(StepperChooseAuthTypeFragment fragment);



  void inject(BaseJob job);


  void inject(ItemsBuilder context);

  void inject(QueueDBManager context);




  void inject(DataLoaderManager context);
  void inject(CurrentDocumentManager context);

  void inject(DocumentTypeItem context);
  void inject(ButtonBuilder context);

  void inject(DBQueryBuilder context);


  void inject(DecisionManager context);

  void inject(OperationManager context);
  void inject(OperationHistory context);
  void inject(ToolbarManager context);



  void inject(AbstractCommand context);

  void inject(FromTheReport context);
  void inject(ReturnToPrimaryConsideration context);
  void inject(ApprovalPerformance context);
  void inject(DelegatePerformance context);
  void inject(AddToFolder context);
  void inject(ChangePerson context);
  void inject(CheckForControl context);

  void inject(NextPerson context);
  void inject(PrevPerson context);
  void inject(sapotero.rxtest.views.managers.menu.commands.signing.NextPerson context);
  void inject(sapotero.rxtest.views.managers.menu.commands.signing.PrevPerson context);

  void inject(SaveDecision context);
  void inject(AddDecision context);




  Application application();
}