package sapotero.rxtest.annotations;


import java.lang.annotation.Annotation;

import sapotero.rxtest.annotations.utils.ESDSource;
import timber.log.Timber;

@ESDSource( name = "AnnotationTest", value = 22)
public class AnnotationTest {
  private static AnnotationTest instance;
  private static final String TAG = "AnnotationTest";

  public static AnnotationTest getInstance() {
    if (instance != null) {
      instance = new AnnotationTest();

    }

    assert instance != null;
    Class clazz = AnnotationTest.class;

    for(Annotation annotation : clazz.getAnnotations() ){
      if(annotation instanceof ESDSource){
        ESDSource source = (ESDSource) annotation;
        Timber.tag(TAG).i("name  : %s", source.name());
        Timber.tag(TAG).i("value : %s", source.value());
      }
    }

    return instance;
  }

}


