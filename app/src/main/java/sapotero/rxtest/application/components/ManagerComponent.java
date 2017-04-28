package sapotero.rxtest.application.components;

import javax.inject.Singleton;

import dagger.Component;
import sapotero.rxtest.application.modules.EsdModule;
import sapotero.rxtest.application.modules.SettingsModule;
import sapotero.rxtest.db.requery.utils.RequeryDbModule;
import sapotero.rxtest.jobs.bus.BaseJob;
import sapotero.rxtest.jobs.utils.JobModule;
import sapotero.rxtest.managers.DataLoaderManager;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.utils.OperationManagerModule;
import sapotero.rxtest.managers.toolbar.ToolbarManager;
import sapotero.rxtest.retrofit.utils.OkHttpModule;
import sapotero.rxtest.services.MainService;
import sapotero.rxtest.utils.queue.utils.QueueManagerModule;
import sapotero.rxtest.views.activities.DecisionConstructorActivity;
import sapotero.rxtest.views.activities.MainActivity;
import sapotero.rxtest.views.activities.SettingsTemplatesActivity;
import sapotero.rxtest.views.fragments.DecisionPreviewFragment;
import sapotero.rxtest.views.fragments.DecisionRejectionTemplateFragment;
import sapotero.rxtest.views.fragments.DecisionTemplateFragment;
import sapotero.rxtest.views.fragments.InfoActivityDecisionPreviewFragment;

@Singleton
@Component(modules = {
  EsdModule.class,
  SettingsModule.class,
  RequeryDbModule.class,
  OkHttpModule.class,
  JobModule.class,
  QueueManagerModule.class,
  OperationManagerModule.class,
})

public interface ManagerComponent {

  void inject(MainActivity activity);
  void inject(DecisionConstructorActivity activity);
  void inject(SettingsTemplatesActivity activity);

  void inject(MainService service);

  void inject(DecisionTemplateFragment fragment);
  void inject(DecisionRejectionTemplateFragment fragment);

  void inject(DecisionPreviewFragment fragment);
  void inject(InfoActivityDecisionPreviewFragment fragment);

  void inject(BaseJob job);

  void inject(DataLoaderManager context);

  void inject(ToolbarManager context);

  void inject(AbstractCommand context);
}