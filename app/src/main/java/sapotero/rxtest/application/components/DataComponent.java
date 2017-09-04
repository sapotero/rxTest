package sapotero.rxtest.application.components;

import dagger.Component;
import sapotero.rxtest.application.modules.EsdModule;
import sapotero.rxtest.application.modules.SettingsModule;
import sapotero.rxtest.application.scopes.DataScope;
import sapotero.rxtest.db.mapper.PrimaryConsiderationMapper;
import sapotero.rxtest.db.mapper.TemplateMapper;
import sapotero.rxtest.db.mapper.utils.MappersModule;
import sapotero.rxtest.db.requery.utils.Deleter;
import sapotero.rxtest.db.requery.utils.DocumentStateSaver;
import sapotero.rxtest.db.requery.utils.RequeryDbModule;
import sapotero.rxtest.db.requery.utils.validation.ValidationModule;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.managers.view.DecisionManager;
import sapotero.rxtest.retrofit.utils.OkHttpModule;
import sapotero.rxtest.utils.memory.utils.MemoryStoreModule;
import sapotero.rxtest.utils.queue.db.QueueDBManager;
import sapotero.rxtest.views.activities.DocumentImageFullScreenActivity;
import sapotero.rxtest.views.activities.DocumentInfocardFullScreenActivity;
import sapotero.rxtest.views.activities.FileSignActivity;
import sapotero.rxtest.views.activities.LogActivity;
import sapotero.rxtest.views.adapters.DecisionSpinnerAdapter;
import sapotero.rxtest.views.adapters.DocumentLinkAdapter;
import sapotero.rxtest.views.adapters.DocumentTypeAdapter;
import sapotero.rxtest.views.adapters.PrimaryConsiderationAdapter;
import sapotero.rxtest.views.adapters.PrimaryUsersAdapter;
import sapotero.rxtest.views.custom.OrganizationSpinner;
import sapotero.rxtest.views.custom.stepper.build.steps.StepperChooseAuthTypeFragment;
import sapotero.rxtest.views.custom.stepper.build.steps.StepperLoadDataFragment;
import sapotero.rxtest.views.dialogs.InfoCardDialogFragment;
import sapotero.rxtest.views.dialogs.SelectOshsDialogFragment;
import sapotero.rxtest.views.dialogs.SelectTemplateDialogFragment;
import sapotero.rxtest.views.fragments.DecisionFragment;
import sapotero.rxtest.views.fragments.InfoCardFieldsFragment;
import sapotero.rxtest.views.fragments.InfoCardLinksFragment;
import sapotero.rxtest.views.fragments.InfoCardWebViewFragment;
import sapotero.rxtest.views.fragments.RoutePreviewFragment;
import sapotero.rxtest.views.fragments.SettingsViewFragment;
import sapotero.rxtest.views.menu.factories.ItemsBuilder;

@DataScope
@Component(modules = {
  EsdModule.class,
  SettingsModule.class,
  RequeryDbModule.class,
  MemoryStoreModule.class,
  MappersModule.class
})

public interface DataComponent {
  NetworkComponent plusNetworkComponent(OkHttpModule okHttpModule);
  ValidationComponent plusValidationComponent(ValidationModule validationModule);

  void inject(LogActivity activity);
  void inject(SettingsViewFragment activity);
  void inject(FileSignActivity activity);
  void inject(DocumentImageFullScreenActivity activity);
  void inject(DocumentInfocardFullScreenActivity activity);

  void inject(DocumentTypeAdapter adapter);
  void inject(PrimaryConsiderationAdapter adapter);
  void inject(DocumentLinkAdapter documentLinkAdapter);
  void inject(PrimaryUsersAdapter primaryUsersAdapter);
  void inject(DecisionSpinnerAdapter decisionSpinnerAdapter);

  void inject(StepperLoadDataFragment fragment);
  void inject(DecisionFragment fragment);
  void inject(InfoCardWebViewFragment fragment);

  void inject(SelectOshsDialogFragment fragment);
  void inject(InfoCardFieldsFragment fragment);
  void inject(InfoCardLinksFragment fragment);
  void inject(RoutePreviewFragment fragment);
  void inject(SelectTemplateDialogFragment fragment);
  void inject(InfoCardDialogFragment fragment);
  void inject(StepperChooseAuthTypeFragment fragment);

  void inject(ItemsBuilder itemsBuilder);
  void inject(QueueDBManager queueDBManager);

  void inject(DecisionManager decisionManager);

  void inject(Deleter deleter);
  void inject(DocumentStateSaver documentStateSaver);

  void inject(OrganizationSpinner organizationSpinner);

  void inject(CommandParams commandParams);
}
