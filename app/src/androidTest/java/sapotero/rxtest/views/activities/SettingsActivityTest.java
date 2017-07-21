package sapotero.rxtest.views.activities;


import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest {

  @Rule
  public ActivityTestRule<SettingsActivity> mActivityTestRule = new ActivityTestRule<>(SettingsActivity.class);


  private UiDevice mDevice;

  @Before
  public void before() {

    mDevice = UiDevice.getInstance( InstrumentationRegistry.getInstrumentation() );
    assertThat(mDevice, notNullValue());


  }


  @Test
  public void visibilityCheck() throws UiObjectNotFoundException {


    UiScrollable appViews = new UiScrollable( new UiSelector().scrollable(true) );

    waitUI();

    // проверяем что в найстройках отображаются необходимые поля
    onView( withText("Стартовая страница")).check(matches(isDisplayed()));
    onView( withText("Стартовая вкладка")).check(matches(isDisplayed()));
    onView( withText("Журналы")).check(matches(isDisplayed()));
    onView( withText("Года отображения документов")).check(matches(isDisplayed()));
    onView( withText("Отображать документы без резолюции")).check(matches(isDisplayed()));
    onView( withText("Не отображать статус «Первичное рассмотрение»")).check(matches(isDisplayed()));
    onView( withText("Загрузка")).check(matches(isDisplayed()));

    appViews.scrollForward();

    onView( withText("Хранение")).check(matches(isDisplayed()));
    onView( withText("Не отображать кнопки «Прошу доложить» и «Прошу ознакомить»")).check(matches(isDisplayed()));
    onView( withText("Отображать настройки срочности")).check(matches(isDisplayed()));
    onView( withText("C одним типом «Срочно»")).check(matches(isDisplayed()));
    onView( withText("Обновлять автоматически дату резолюции")).check(matches(isDisplayed()));
    onView( withText("Возможность выбора размера шрифта")).check(matches(isDisplayed()));
    onView( withText("Отображать настройки подлинника")).check(matches(isDisplayed()));
    onView( withText("Возможность смены подписавшего резолюцию для статуса «На рассмотрение»")).check(matches(isDisplayed()));

    appViews.scrollForward();

    onView( withText("Показывать кнопку «Создать поручение»")).check(matches(isDisplayed()));
    onView( withText("Показывать комментарий при отклонении документа/резолюции/без ответа")).check(matches(isDisplayed()));
    onView( withText("Показывать подтверждения о действиях с документом")).check(matches(isDisplayed()));
    onView( withText("Показывать подтверждения о постановке на контроль документов для раздела «Обращения граждан»")).check(matches(isDisplayed()));
    onView( withText("Возможность согласования документов на первичном рассмотрении")).check(matches(isDisplayed()));
    onView( withText("Максимальный размер файла для подписи")).check(matches(isDisplayed()));

    onView( withText("Режим отладки")).check(matches(isDisplayed()));
    onView( withText("Отображение окна авторизации")).check(matches(isDisplayed()));


    waitUI();

  }

  

  private void waitUI() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
