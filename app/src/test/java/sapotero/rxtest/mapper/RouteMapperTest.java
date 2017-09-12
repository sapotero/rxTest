package sapotero.rxtest.mapper;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import sapotero.rxtest.db.mapper.RouteMapper;
import sapotero.rxtest.db.mapper.StepMapper;
import sapotero.rxtest.db.requery.models.RRouteEntity;
import sapotero.rxtest.db.requery.models.RStep;
import sapotero.rxtest.db.requery.models.RStepEntity;
import sapotero.rxtest.retrofit.models.document.Route;
import sapotero.rxtest.retrofit.models.document.Step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RouteMapperTest {

  private RouteMapper mapper;
  private StepMapper stepMapper;
  private Route dummyRoute;
  private RRouteEntity entity;
  private Route model;

  @Before
  public void init() {
    stepMapper = new StepMapper();
    dummyRoute = generateRoute();
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
    mapper = new RouteMapper();
    entity = mapper.toEntity(dummyRoute);

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
    mapper = new RouteMapper();
    entity = mapper.toEntity(dummyRoute);
    model = mapper.toModel(entity);

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
    mapper = new RouteMapper();

    RRouteEntity entity1 = mapper.toEntity(dummyRoute);
    RRouteEntity entity2 = mapper.toEntity(dummyRoute);

    boolean hasDiff = mapper.hasDiff(entity1, entity2);

    assertFalse( hasDiff );

    entity2.setText("111");
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertTrue( hasDiff );
  }
}
