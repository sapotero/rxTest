package sapotero.rxtest.application.components;

import javax.inject.Singleton;

import dagger.Component;
import sapotero.rxtest.application.modules.EsdModule;
import sapotero.rxtest.application.modules.SettingsModule;
import sapotero.rxtest.retrofit.utils.OkHttpModule;
import sapotero.rxtest.views.adapters.OshsAutoCompleteAdapter;

@Singleton
@Component(modules = {
  EsdModule.class,
  SettingsModule.class,
  OkHttpModule.class,
})

public interface NetworkComponent {
  void inject(OshsAutoCompleteAdapter oshsAutoCompleteAdapter);
}
