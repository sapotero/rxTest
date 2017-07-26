package sapotero.rxtest.mapper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import sapotero.rxtest.db.mapper.RouteMapper;
import sapotero.rxtest.db.mapper.StepMapper;
import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.RRouteEntity;
import sapotero.rxtest.db.requery.models.RStep;
import sapotero.rxtest.db.requery.models.RStepEntity;
import sapotero.rxtest.retrofit.models.document.Route;
import sapotero.rxtest.retrofit.models.document.Step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;

public class RouteMapperTest {

  private RouteMapper mapper;
  private StepMapper stepMapper;
  private Route dummyRoute;
  private RRouteEntity entity;
  private Route model;

  @Mock Mappers mappers;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    stepMapper = new StepMapper();

    dummyRoute = generateRoute();

    Mockito.when(mappers.getStepMapper()).thenReturn(stepMapper);
  }

  public static Route generateRoute() {
    Route dummyRoute = new Route();
    dummyRoute.setTitle( "kjdshf8923yjhef2ewhfkihf sdkjfhds fkdsh" );

    List<Step> steps = new ArrayList<>();
    Step dummyStep = StepMapperTest.generateStep();
    steps.add(dummyStep);
    dummyRoute.setSteps(steps);

    return dummyRoute;
  }

  @Test
  public void toEntity() {
    mapper = new RouteMapper(mappers);
    entity = mapper.toEntity(dummyRoute);

    Mockito.verify(mappers, times(1)).getStepMapper();

    verifyRoute( dummyRoute, entity, stepMapper );
  }

  public static void verifyRoute(Route expected, RRouteEntity actual, StepMapper stepMapper) {
    assertNotNull( actual );
    assertEquals( 0, actual.getId() );
    assertEquals( expected.getTitle(), actual.getText() );

    int index = 0;
    for ( RStep _step : actual.getSteps() ) {
      RStepEntity stepEntity = (RStepEntity) _step;
      StepMapperTest.verifyStep( expected.getSteps().get(index), stepEntity, stepMapper );
      index++;
    }
  }

  @Test
  public void toModel() {
    mapper = new RouteMapper(mappers);
    entity = mapper.toEntity(dummyRoute);
    model = mapper.toModel(entity);

    Mockito.verify(mappers, times(2)).getStepMapper();

    verifyRoute( dummyRoute, model );
  }

  public static void verifyRoute(Route expected, Route actual) {
    assertNotNull( actual );
    assertEquals( expected.getTitle(), actual.getTitle() );

    int index = 0;
    for ( Step step : actual.getSteps() ) {
      StepMapperTest.verifyStep( expected.getSteps().get(index), step );
      index++;
    }
  }

  @Test
  public void hasDiff() {
    mapper = new RouteMapper(mappers);

    RRouteEntity entity1 = mapper.toEntity(dummyRoute);
    RRouteEntity entity2 = mapper.toEntity(dummyRoute);

    boolean hasDiff = mapper.hasDiff(entity1, entity2);

    assertFalse( hasDiff );

    entity2.setText("111");
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertTrue( hasDiff );
  }
}
