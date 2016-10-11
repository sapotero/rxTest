package sapotero.rxtest;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import sapotero.rxtest.Jobs.BaseJob;
import sapotero.rxtest.views.activities.InfoActivity;

@Singleton
@Component(modules = EsdModule.class)

public interface EsdComponent {
  void inject(InfoActivity activity);
  void inject(BaseJob job);

  Application application();
}