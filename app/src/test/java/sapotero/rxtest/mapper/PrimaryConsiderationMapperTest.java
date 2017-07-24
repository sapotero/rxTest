package sapotero.rxtest.mapper;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.inject.Inject;

import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.dagger.components.DaggerTestDataComponent;
import sapotero.rxtest.dagger.components.TestDataComponent;
import sapotero.rxtest.db.mapper.PrimaryConsiderationMapper;
import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.RPrimaryConsiderationEntity;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.utils.Settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ EsdApplication.class })
public class PrimaryConsiderationMapperTest {

  private TestDataComponent testDataComponent;
  private PrimaryConsiderationMapper mapper;
  private Oshs dummyOshs;
  private String dummyLogin;
  private RPrimaryConsiderationEntity entity;
  private Oshs model;

  @Mock Mappers mappers;
  @Mock Context context;

  @Inject Settings settings;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);

    testDataComponent = DaggerTestDataComponent.builder().build();
    testDataComponent.inject(this);

    dummyOshs = generateOshs();
    generateDummyLogin();

    PowerMockito.mockStatic(EsdApplication.class);
    PowerMockito.when(EsdApplication.getDataComponent()).thenReturn(testDataComponent);
  }

  private void generateDummyLogin() {
    dummyLogin = "dummyLogin";
  }

  public static Oshs generateOshs() {
    Oshs dummyOshs = new Oshs();
    dummyOshs.setId( "58f88dfc776b000026000001" );
    dummyOshs.setIsOrganization( false );
    dummyOshs.setIsGroup( false );
    dummyOshs.setName( "Сотрудник_а2 A.T." );
    dummyOshs.setOrganization( "ОДиР ГУ МВД России по Самарской области" );
    dummyOshs.setPosition( "Сотрудник ОДИР" );
    dummyOshs.setLastName( "Сотрудник_а2" );
    dummyOshs.setFirstName( "Android" );
    dummyOshs.setMiddleName( "Test" );
    dummyOshs.setGender( "Мужской" );
    dummyOshs.setImage( null );
    return dummyOshs;
  }

  @Test
  public void toEntity() {
    Mockito.when(settings.getLogin()).thenReturn(dummyLogin);

    mapper = new PrimaryConsiderationMapper(mappers);
    entity = mapper.toEntity(dummyOshs);

    // These two lines verify, that EsdApplication.getDataComponent() was called only once
    PowerMockito.verifyStatic(times(1));
    EsdApplication.getDataComponent();

    Mockito.verify(settings, atLeastOnce()).getLogin();

    assertNotNull( entity );
    assertEquals( 0, entity.getId() );

    assertEquals(dummyLogin, entity.getUser());
  }

}
