package sapotero.rxtest.db.mapper;

import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.retrofit.models.document.IPerformer;
import sapotero.rxtest.retrofit.models.document.Performer;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;

// Maps between Performer, RPerformerEntity, PrimaryConsiderationPeople and Oshs
public class PerformerMapper extends AbstractMapper<Performer, RPerformerEntity> {

  public static enum DestinationType {
    PERFORMER,
    PRIMARYCONSIDERATIONPEOPLE,
    OSHS
  }

  @Override
  public RPerformerEntity toEntity(Performer model) {
    RPerformerEntity entity = new RPerformerEntity();

    entity.setUid(model.getId());
    entity.setNumber(model.getNumber());
    entity.setPerformerId(model.getPerformerId());
    entity.setPerformerType(model.getPerformerType());
    entity.setPerformerText(model.getPerformerText());
    entity.setPerformerGender(model.getPerformerGender());
    entity.setOrganizationText(model.getOrganizationText());
    entity.setIsOriginal(model.getIsOriginal());
    entity.setIsResponsible(model.getIsResponsible());
    entity.setIsOrganization(model.getOrganization());
    entity.setImage(model.getIImage());
    entity.setForInformation(model.getIForInformation());

    return entity;
  }

  @Override
  public Performer toModel(RPerformerEntity entity) {
    Performer model = new Performer();

    setBaseFields(model, entity);
    model.setNumber(entity.getNumber());
    model.setPerformerType(entity.getPerformerType());
    model.setPerformerText(entity.getPerformerText());
    model.setPerformerGender(entity.getPerformerGender());
    model.setOrganizationText(entity.getOrganizationText());
    model.setIImage(entity.getImage());
    model.setIForInformation(entity.isForInformation());

    return model;
  }

  Performer toFormattedModel(RPerformerEntity entity) {
    Performer formattedModel = new Performer();

    setBaseFields( formattedModel, entity );
    formattedModel.setGroup( false );
    formattedModel.setIForInformation(entity.isForInformation());

    return formattedModel;
  }

  private void setBaseFields(Performer model, RPerformerEntity entity) {
    model.setId( entity.getUid() );
    model.setPerformerId( entity.getPerformerId() );
    model.setIsOriginal( entity.isIsOriginal() );
    model.setIsResponsible( entity.isIsResponsible() );
    model.setOrganization( entity.isIsOrganization() );
    model.setIImage(entity.getImage());
    model.setIForInformation(entity.isForInformation());
  }

  // Returns IPerformer of destinationType, converted from IPerformer source
  // Used for conversions between Performer, PrimaryConsiderationPeople and Oshs
  public IPerformer convert(IPerformer source, DestinationType destinationType) {
    IPerformer destination;

    switch ( destinationType ) {
      case PERFORMER:
        destination = new Performer();
        break;
      case PRIMARYCONSIDERATIONPEOPLE:
        destination = new PrimaryConsiderationPeople();
        break;
      case OSHS:
        destination = new Oshs();
        break;
      default:
        destination = new Performer();
        break;
    }

    destination.setIPerformerUid( source.getIPerformerUid() );
    destination.setIPerformerNumber( source.getIPerformerNumber() );
    destination.setIPerformerId( source.getIPerformerId() );
    destination.setIPerformerType( source.getIPerformerType() );
    destination.setIPerformerName( source.getIPerformerName() );
    destination.setIPerformerGender( source.getIPerformerGender() );
    destination.setIPerformerOrganizationName( source.getIPerformerOrganizationName() );
    destination.setIPerformerAssistantId( source.getIPerformerAssistantId() );
    destination.setIPerformerPosition( source.getIPerformerPosition() );
    destination.setIPerformerLastName( source.getIPerformerLastName() );
    destination.setIPerformerFirstName( source.getIPerformerFirstName() );
    destination.setIPerformerMiddleName( source.getIPerformerMiddleName() );
    destination.setIPerformerImage( source.getIPerformerImage() );
    destination.setIsIPerformerOriginal( source.isIPerformerOriginal() );
    destination.setIsIPerformerResponsible( source.isIPerformerResponsible() );
    destination.setIsIPerformerGroup( source.isIPerformerGroup() );
    destination.setIsIPerformerOrganization( source.isIPerformerOrganization() );
    destination.setIImage( source.getIImage() );
    destination.setIForInformation( source.getIForInformation() );

    return destination;
  }
}
