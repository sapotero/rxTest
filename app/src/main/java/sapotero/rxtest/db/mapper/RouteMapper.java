package sapotero.rxtest.db.mapper;

import sapotero.rxtest.db.requery.models.RRouteEntity;
import sapotero.rxtest.db.requery.models.RStep;
import sapotero.rxtest.db.requery.models.RStepEntity;
import sapotero.rxtest.retrofit.models.document.Route;
import sapotero.rxtest.retrofit.models.document.Step;

// Maps between Route and RRouteEntity
public class RouteMapper extends AbstractMapper<Route, RRouteEntity> {

  @Override
  public RRouteEntity toEntity(Route model) {
    RRouteEntity entity = new RRouteEntity();

    entity.setText( model.getTitle() );
    StepMapper stepMapper = new StepMapper();

    for (Step stepModel : model.getSteps() ) {
      RStepEntity stepEntity = stepMapper.toEntity( stepModel );
      stepEntity.setRoute( entity );
      entity.getSteps().add( stepEntity );
    }

    return entity;
  }

  @Override
  public Route toModel(RRouteEntity entity) {
    Route model = new Route();

    model.setTitle( entity.getText() );
    StepMapper stepMapper = new StepMapper();

    for (RStep step : entity.getSteps() ) {
      RStepEntity stepEntity = (RStepEntity) step;
      Step stepModel = stepMapper.toModel( stepEntity );
      model.getSteps().add( stepModel );
    }

    return model;
  }
}
