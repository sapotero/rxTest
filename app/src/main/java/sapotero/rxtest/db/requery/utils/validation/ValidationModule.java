package sapotero.rxtest.db.requery.utils.validation;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class ValidationModule {

  @Provides
  @Singleton
  Validation provideValidation(Context context) {
    return new Validation(context);
  }

}