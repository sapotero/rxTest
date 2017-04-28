package sapotero.rxtest.application.components;

import javax.inject.Singleton;

import dagger.Component;
import sapotero.rxtest.application.modules.EsdModule;
import sapotero.rxtest.application.modules.SettingsModule;
import sapotero.rxtest.db.requery.utils.RequeryDbModule;
import sapotero.rxtest.managers.menu.utils.OperationManagerModule;
import sapotero.rxtest.managers.toolbar.ToolbarManager;
import sapotero.rxtest.views.activities.DecisionConstructorActivity;
import sapotero.rxtest.views.activities.SettingsTemplatesActivity;
import sapotero.rxtest.views.fragments.DecisionPreviewFragment;
import sapotero.rxtest.views.fragments.DecisionRejectionTemplateFragment;
import sapotero.rxtest.views.fragments.DecisionTemplateFragment;

@Singleton
@Component(modules = {
  EsdModule.class,
  SettingsModule.class,
  RequeryDbModule.class,
  OperationManagerModule.class,
})

public interface OperationManagerComponent {
  void inject(DecisionConstructorActivity activity);
  void inject(SettingsTemplatesActivity activity);

  void inject(DecisionTemplateFragment fragment);
  void inject(DecisionRejectionTemplateFragment fragment);
  void inject(DecisionPreviewFragment fragment);

  void inject(ToolbarManager toolbarManager);
}
