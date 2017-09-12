package sapotero.rxtest.mapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.dagger.components.DaggerTestDataComponent;
import sapotero.rxtest.dagger.components.TestDataComponent;
import sapotero.rxtest.db.mapper.DocumentMapper;
import sapotero.rxtest.db.mapper.StepMapper;
import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RLinks;
import sapotero.rxtest.db.requery.models.RLinksEntity;
import sapotero.rxtest.db.requery.models.RRouteEntity;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.db.requery.models.actions.RAction;
import sapotero.rxtest.db.requery.models.actions.RActionEntity;
import sapotero.rxtest.db.requery.models.control_labels.RControlLabels;
import sapotero.rxtest.db.requery.models.control_labels.RControlLabelsEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecision;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.exemplars.RExemplar;
import sapotero.rxtest.db.requery.models.exemplars.RExemplarEntity;
import sapotero.rxtest.db.requery.models.images.RImage;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.retrofit.models.document.ControlLabel;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.document.DocumentInfoAction;
import sapotero.rxtest.retrofit.models.document.Exemplar;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.retrofit.models.document.Route;
import sapotero.rxtest.retrofit.models.document.Signer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ EsdApplication.class })
public class DocumentMapperTest {

  private TestDataComponent testDataComponent;
  private DocumentMapper mapper;
  private StepMapper stepMapper;
  private DocumentInfo dummyDoc;
  private RDocumentEntity entity;
  private DocumentInfo model;
  private int dummyYear;
  private String dummyLogin;
  private String dummyCurrentUserId;

  @Mock Mappers mappers;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);

    testDataComponent = DaggerTestDataComponent.builder().build();
    testDataComponent.inject(this);

    stepMapper = new StepMapper();

    generateDocument();

    PowerMockito.mockStatic(EsdApplication.class);
    PowerMockito.when(EsdApplication.getDataComponent()).thenReturn(testDataComponent);
  }

  private void generateDocument() {
    dummyDoc = new DocumentInfo();
    dummyDoc.setUid( "02153f437109fe79ad68cdc63f1ed0e57cca7bcbf88d2c1c53a08ac27b67c68871" );
    dummyDoc.setMd5( "692fe5abec8bec4cbc29f5475d126a67" );
    dummyDoc.setSortKey( 1496673841 );
    dummyDoc.setTitle( "Входящий документ от 30.06.2017" );
    dummyDoc.setRegistrationNumber( "1323" );
    dummyDoc.setRegistrationDate( "30.06.2017" );
    dummyDoc.setUrgency( "Весьма срочно" );
    dummyDoc.setShortDescription( "sdlkfoi rkji lkjewr 2" );
    dummyDoc.setComment( "S8 sdkjhf8 sjdk |Sd k3jhrkjhf sdkjfh" );
    dummyDoc.setExternalDocumentNumber( "1155" );
    dummyYear = 2017;
    dummyDoc.setReceiptDate( "30.06." + dummyYear );
    dummyDoc.setViewed( true );

    String dummyInfoCard = generateInfoCard();
    dummyDoc.setInfoCard(dummyInfoCard);

    Signer dummySigner = SignerMapperTest.generateSigner();
    dummyDoc.setSigner(dummySigner);

    Decision dummyDecision = DecisionMapperTest.generateDecision();
    dummyDoc.getDecisions().add(dummyDecision);

    Route dummyRoute = RouteMapperTest.generateRoute();
    dummyDoc.setRoute(dummyRoute);

    Exemplar dummyExemplar = ExemplarMapperTest.generateExemplar();
    dummyDoc.getExemplars().add(dummyExemplar);

    Image dummyImage = ImageMapperTest.generateImage();
    dummyDoc.getImages().add(dummyImage);

    ControlLabel dummyControlLabel = ControlLabelMapperTest.generateControlLabel();
    dummyDoc.getControlLabels().add(dummyControlLabel);

    DocumentInfoAction dummyAction = ActionMapperTest.generateAction();
    dummyDoc.getActions().add(dummyAction);

    String dummyLink = LinkMapperTest.generateLink();
    dummyDoc.getLinks().add(dummyLink);

    dummyLogin = "dummyLogin";
    dummyCurrentUserId = "dummyCurrentUserId";
  }

  private String generateInfoCard() {
    return "dklsfhjDSF23fsdSDFDSf23jFG54fDFg3434gwreg2r23fdsSDFgfCVNm";
  }

  @Test
  public void toEntity() {
    mapper = new DocumentMapper(mappers);
    entity = mapper.withLogin(dummyLogin).toEntity(dummyDoc);

    assertNotNull( entity );
    assertEquals( 0, entity.getId() );
    assertEquals( dummyDoc.getUid(), entity.getUid() );
    assertEquals( dummyDoc.getMd5(), entity.getMd5() );
    assertEquals( dummyDoc.getSortKey(), entity.getSortKey() );
    assertEquals( dummyDoc.getTitle(), entity.getTitle() );
    assertEquals( dummyDoc.getRegistrationNumber(), entity.getRegistrationNumber() );
    assertEquals( dummyDoc.getRegistrationDate(), entity.getRegistrationDate() );
    assertEquals( dummyDoc.getUrgency(), entity.getUrgency() );
    assertEquals( dummyDoc.getShortDescription(), entity.getShortDescription() );
    assertEquals( dummyDoc.getComment(), entity.getComment() );
    assertEquals( dummyDoc.getExternalDocumentNumber(), entity.getExternalDocumentNumber() );
    assertEquals( dummyDoc.getReceiptDate(), entity.getReceiptDate() );
    assertEquals( dummyDoc.getViewed(), entity.isViewed() );

    assertEquals( dummyYear, entity.getYear() );
    assertEquals( dummyDoc.getSigner().getOrganisation(), entity.getOrganization() );
    assertEquals( false, entity.isFavorites() );
    assertEquals( false, entity.isProcessed() );
    assertEquals( false, entity.isControl() );
    assertEquals( false, entity.isFromLinks() );
    assertEquals( false, entity.isFromProcessedFolder() );
    assertEquals( false, entity.isFromFavoritesFolder() );
    assertEquals( false, entity.isChanged() );
    assertEquals( true, entity.isWithDecision() );
    assertEquals( false, entity.isRed() );
    assertEquals( false, entity.isReturned() );
    assertEquals( false, entity.isRejected() );
    assertEquals( false, entity.isAgain() );
    assertEquals( dummyLogin, entity.getUser() );

    assertEquals( dummyDoc.getInfoCard(), entity.getInfoCard() );

    SignerMapperTest.verifySigner( dummyDoc.getSigner(), (RSignerEntity) entity.getSigner() );

    int index = 0;
    for (RDecision _decision : entity.getDecisions() ) {
      RDecisionEntity decisionEntity = (RDecisionEntity) _decision;
      DecisionMapperTest.verifyDecision( dummyDoc.getDecisions().get(index), decisionEntity );
      index++;
    }

    RouteMapperTest.verifyRoute( dummyDoc.getRoute(), (RRouteEntity) entity.getRoute(), stepMapper );

    index = 0;
    for (RExemplar _exemplar : entity.getExemplars() ) {
      RExemplarEntity exemplarEntity = (RExemplarEntity) _exemplar;
      ExemplarMapperTest.verifyExemplar( dummyDoc.getExemplars().get(index), exemplarEntity );
      index++;
    }

    index = 0;
    for (RImage _image : entity.getImages() ) {
      RImageEntity imageEntity = (RImageEntity) _image;
      ImageMapperTest.verifyImage( dummyDoc.getImages().get(index), imageEntity );
      index++;
    }

    index = 0;
    for (RControlLabels _controlLabel : entity.getControlLabels() ) {
      RControlLabelsEntity controlLabelsEntity = (RControlLabelsEntity) _controlLabel;
      ControlLabelMapperTest.verifyControlLabel( dummyDoc.getControlLabels().get(index), controlLabelsEntity );
      index++;
    }

    index = 0;
    for (RAction _action : entity.getActions() ) {
      RActionEntity actionEntity = (RActionEntity) _action;
      ActionMapperTest.verifyAction( dummyDoc.getActions().get(index), actionEntity );
      index++;
    }

    index = 0;
    for (RLinks _link : entity.getLinks() ) {
      RLinksEntity linksEntity = (RLinksEntity) _link;
      LinkMapperTest.verifyLink( dummyDoc.getLinks().get(index), linksEntity );
      index++;
    }
  }

  @Test
  public void toModel() {
    mapper = new DocumentMapper(mappers);
    entity = mapper.toEntity(dummyDoc);
    model = mapper.toModel(entity);

    assertNotNull( model );
    assertEquals( dummyDoc.getUid(), model.getUid() );
    assertEquals( dummyDoc.getMd5(), model.getMd5() );
    assertEquals( dummyDoc.getSortKey(), model.getSortKey() );
    assertEquals( dummyDoc.getTitle(), model.getTitle() );
    assertEquals( dummyDoc.getRegistrationNumber(), model.getRegistrationNumber() );
    assertEquals( dummyDoc.getRegistrationDate(), model.getRegistrationDate() );
    assertEquals( dummyDoc.getUrgency(), model.getUrgency() );
    assertEquals( dummyDoc.getShortDescription(), model.getShortDescription() );
    assertEquals( dummyDoc.getComment(), model.getComment() );
    assertEquals( dummyDoc.getExternalDocumentNumber(), model.getExternalDocumentNumber() );
    assertEquals( dummyDoc.getReceiptDate(), model.getReceiptDate() );
    assertEquals( dummyDoc.getViewed(), model.getViewed() );

    assertEquals( dummyDoc.getInfoCard(), model.getInfoCard() );

    SignerMapperTest.verifySigner( dummyDoc.getSigner(), model.getSigner() );

    int index = 0;
    for ( Decision decision : model.getDecisions() ) {
      DecisionMapperTest.verifyDecision( dummyDoc.getDecisions().get(index), decision );
      index++;
    }

    RouteMapperTest.verifyRoute( dummyDoc.getRoute(), model.getRoute() );

    index = 0;
    for ( Exemplar exemplar : model.getExemplars() ) {
      ExemplarMapperTest.verifyExemplar( dummyDoc.getExemplars().get(index), exemplar );
      index++;
    }

    index = 0;
    for ( Image image : model.getImages() ) {
      ImageMapperTest.verifyImage( dummyDoc.getImages().get(index), image );
      index++;
    }

    index = 0;
    for ( ControlLabel controlLabel : model.getControlLabels() ) {
      ControlLabelMapperTest.verifyControlLabel( dummyDoc.getControlLabels().get(index), controlLabel );
      index++;
    }

    index = 0;
    for ( DocumentInfoAction action : model.getActions() ) {
      ActionMapperTest.verifyAction( dummyDoc.getActions().get(index), action );
      index++;
    }

    index = 0;
    for ( String link : model.getLinks() ) {
      LinkMapperTest.verifyLink( dummyDoc.getLinks().get(index), link );
      index++;
    }
  }

  @Test
  public void hasDiff() {
    mapper = new DocumentMapper(mappers);

    RDocumentEntity entity1 = mapper.toEntity(dummyDoc);
    RDocumentEntity entity2 = mapper.toEntity(dummyDoc);

    boolean hasDiff = mapper.hasDiff(entity1, entity2);

    assertFalse( hasDiff );

    entity2.setUid( "" );
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertTrue( hasDiff );
  }

  @Test
  public void control() {
    dummyDoc.getControlLabels().get(0).setOfficialId( dummyCurrentUserId );

    mapper = new DocumentMapper(mappers);
    entity = mapper.withCurrentUserId(dummyCurrentUserId).toEntity(dummyDoc);

    assertEquals( true, entity.isControl() );
  }

  @Test
  public void withoutOrganization() {
    dummyDoc.getSigner().setOrganisation( "" );

    mapper = new DocumentMapper(mappers);
    entity = mapper.toEntity(dummyDoc);

    assertEquals( "Без организации", entity.getOrganization() );
  }

  @Test
  public void shared() {
    mapper = new DocumentMapper(mappers);
    entity = mapper.toEntity(dummyDoc);

    mapper.setShared( entity, false );
    assertEquals( "", entity.getAddressedToType() );

    mapper.setShared( entity, true );
    assertEquals( "group", entity.getAddressedToType() );
  }

  @Test
  public void withoutDecision() {
    dummyDoc.getDecisions().clear();

    mapper = new DocumentMapper(mappers);
    entity = mapper.toEntity(dummyDoc);

    assertEquals( false, entity.isWithDecision() );
  }

  @Test
  public void red() {
    dummyDoc.getDecisions().get(0).setRed(true);

    mapper = new DocumentMapper(mappers);
    entity = mapper.toEntity(dummyDoc);

    assertEquals( true, entity.isRed() );
  }

  @Test
  public void emptyDoc() {
    DocumentInfo emptyDoc = new DocumentInfo();

    mapper = new DocumentMapper(mappers);
    entity = mapper.toEntity(emptyDoc);

    assertNotNull( entity );
    assertEquals( 0, entity.getId() );

    model = mapper.toModel(entity);

    assertNotNull( model );
  }

  @Test
  public void journalFilter() {
    String dummyJournal = "incoming_documents_production_db_core_cards_incoming_documents_cards";
    String dummyFilter = "sent_to_the_report";

    mapper = new DocumentMapper(mappers);
    entity = mapper.toEntity(dummyDoc);

    mapper.setJournal( entity, dummyJournal );
    mapper.setFilter( entity, dummyFilter );

    assertEquals( dummyJournal, entity.getDocumentType() );
    assertEquals( dummyFilter, entity.getFilter() );
  }
}
