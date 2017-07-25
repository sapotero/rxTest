package sapotero.rxtest.mapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.dagger.components.DaggerTestDataComponent;
import sapotero.rxtest.dagger.components.TestDataComponent;
import sapotero.rxtest.db.mapper.BlockMapper;
import sapotero.rxtest.db.mapper.ControlLabelMapper;
import sapotero.rxtest.db.mapper.DecisionMapper;
import sapotero.rxtest.db.mapper.DocumentMapper;
import sapotero.rxtest.db.mapper.ExemplarMapper;
import sapotero.rxtest.db.mapper.ImageMapper;
import sapotero.rxtest.db.mapper.LinkMapper;
import sapotero.rxtest.db.mapper.PerformerMapper;
import sapotero.rxtest.db.mapper.RouteMapper;
import sapotero.rxtest.db.mapper.StepMapper;
import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.retrofit.models.document.ControlLabel;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.document.DocumentInfoAction;
import sapotero.rxtest.retrofit.models.document.Exemplar;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.retrofit.models.document.Route;
import sapotero.rxtest.retrofit.models.document.Signer;
import sapotero.rxtest.utils.ISettings;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ EsdApplication.class })
public class DocumentMapperTest {

  private TestDataComponent testDataComponent;
  private DocumentMapper mapper;
  private PerformerMapper performerMapper;
  private BlockMapper blockMapper;
  private DecisionMapper decisionMapper;
  private ControlLabelMapper controlLabelMapper;
  private ExemplarMapper exemplarMapper;
  private ImageMapper imageMapper;
  private LinkMapper linkMapper;
  private RouteMapper routeMapper;
  private StepMapper stepMapper;
  private DocumentInfo dummyDoc;
  private RDocumentEntity entity;
  private DocumentInfo model;

  @Mock Mappers mappers;

  @Inject ISettings settings;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);

    testDataComponent = DaggerTestDataComponent.builder().build();
    testDataComponent.inject(this);

    performerMapper = new PerformerMapper();
    blockMapper = new BlockMapper(mappers);
    decisionMapper = new DecisionMapper(mappers);
    controlLabelMapper = new ControlLabelMapper();
    exemplarMapper = new ExemplarMapper();
    imageMapper = new ImageMapper();
    linkMapper = new LinkMapper();
    routeMapper = new RouteMapper(mappers);
    stepMapper = new StepMapper();

    generateDocument();

    PowerMockito.mockStatic(EsdApplication.class);
    PowerMockito.when(EsdApplication.getDataComponent()).thenReturn(testDataComponent);

    Mockito.when(mappers.getPerformerMapper()).thenReturn(performerMapper);
    Mockito.when(mappers.getBlockMapper()).thenReturn(blockMapper);
    Mockito.when(mappers.getDecisionMapper()).thenReturn(decisionMapper);
    Mockito.when(mappers.getControlLabelMapper()).thenReturn(controlLabelMapper);
    Mockito.when(mappers.getExemplarMapper()).thenReturn(exemplarMapper);
    Mockito.when(mappers.getImageMapper()).thenReturn(imageMapper);
    Mockito.when(mappers.getLinkMapper()).thenReturn(linkMapper);
    Mockito.when(mappers.getRouteMapper()).thenReturn(routeMapper);
    Mockito.when(mappers.getStepMapper()).thenReturn(stepMapper);
  }

  private void generateDocument() {
    dummyDoc = new DocumentInfo();
    dummyDoc.setUid( "02153f437109fe79ad68cdc63f1ed0e57cca7bcbf88d2c1c53a08ac27b67c68871" );
    dummyDoc.setMd5( "692fe5abec8bec4cbc29f5475d126a67" );
    dummyDoc.setSortKey( null );
    dummyDoc.setTitle( "Входящий документ от 30.06.2017" );
    dummyDoc.setRegistrationNumber( "1323" );
    dummyDoc.setRegistrationDate( "30.06.2017" );
    dummyDoc.setUrgency( "Весьма срочно" );
    dummyDoc.setShortDescription( "sdlkfoi rkji lkjewr 2" );
    dummyDoc.setComment( "S8 sdkjhf8 sjdk |Sd k3jhrkjhf sdkjfh" );
    dummyDoc.setExternalDocumentNumber( "1155" );
    dummyDoc.setReceiptDate( "30.06.2017" );
    dummyDoc.setViewed( true );

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

    // TODO: add links

    // TODO: add infocard
  }

  @Test
  public void toEntity() {

  }

}
