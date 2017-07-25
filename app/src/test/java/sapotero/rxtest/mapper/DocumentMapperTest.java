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
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
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

  }

  @Test
  public void toEntity() {

  }

}
