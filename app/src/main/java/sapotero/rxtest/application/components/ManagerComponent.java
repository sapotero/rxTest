package sapotero.rxtest.application.components;

import javax.inject.Singleton;

import dagger.Component;
import sapotero.rxtest.application.modules.EsdModule;
import sapotero.rxtest.application.modules.SettingsModule;
import sapotero.rxtest.db.requery.utils.RequeryDbModule;
import sapotero.rxtest.jobs.utils.JobModule;
import sapotero.rxtest.managers.menu.utils.OperationManagerModule;
import sapotero.rxtest.utils.queue.utils.QueueManagerModule;
import sapotero.rxtest.views.activities.MainActivity;
import sapotero.rxtest.views.fragments.InfoActivityDecisionPreviewFragment;

@Singleton
@Component(modules = {
  EsdModule.class,
  SettingsModule.class,
  RequeryDbModule.class,
  JobModule.class,
  QueueManagerModule.class,
  OperationManagerModule.class,
})

public interface ManagerComponent {
  void inject(MainActivity activity);
  void inject(InfoActivityDecisionPreviewFragment fragment);
}