package sapotero.rxtest.db.mapper.utils;

import sapotero.rxtest.db.mapper.ActionMapper;
import sapotero.rxtest.db.mapper.AssistantMapper;
import sapotero.rxtest.db.mapper.BlockMapper;
import sapotero.rxtest.db.mapper.ControlLabelMapper;
import sapotero.rxtest.db.mapper.DecisionMapper;
import sapotero.rxtest.db.mapper.DocumentMapper;
import sapotero.rxtest.db.mapper.ExemplarMapper;
import sapotero.rxtest.db.mapper.FavoriteUserMapper;
import sapotero.rxtest.db.mapper.ImageMapper;
import sapotero.rxtest.db.mapper.LinkMapper;
import sapotero.rxtest.db.mapper.PerformerMapper;
import sapotero.rxtest.db.mapper.PrimaryConsiderationMapper;
import sapotero.rxtest.db.mapper.RouteMapper;
import sapotero.rxtest.db.mapper.SignerMapper;
import sapotero.rxtest.db.mapper.StepMapper;
import sapotero.rxtest.db.mapper.TemplateMapper;
import sapotero.rxtest.utils.Settings;

// Keeps all mappers in one place
public class Mappers {

  private static Mappers mappers;

  private ActionMapper actionMapper;
  private AssistantMapper assistantMapper;
  private BlockMapper blockMapper;
  private ControlLabelMapper controlLabelMapper;
  private DecisionMapper decisionMapper;
  private DocumentMapper documentMapper;
  private ExemplarMapper exemplarMapper;
  private FavoriteUserMapper favoriteUserMapper;
  private ImageMapper imageMapper;
  private LinkMapper linkMapper;
  private PerformerMapper performerMapper;
  private PrimaryConsiderationMapper primaryConsiderationMapper;
  private RouteMapper routeMapper;
  private SignerMapper signerMapper;
  private StepMapper stepMapper;
  private TemplateMapper templateMapper;

  public Mappers(Settings settings) {
    mappers = this;

    actionMapper = new ActionMapper();
    assistantMapper = new AssistantMapper();
    blockMapper = new BlockMapper();
    controlLabelMapper = new ControlLabelMapper();
    decisionMapper = new DecisionMapper();
    documentMapper = new DocumentMapper();
    exemplarMapper = new ExemplarMapper();
    favoriteUserMapper = new FavoriteUserMapper();
    imageMapper = new ImageMapper();
    linkMapper = new LinkMapper();
    performerMapper = new PerformerMapper();
    primaryConsiderationMapper = new PrimaryConsiderationMapper();
    routeMapper = new RouteMapper();
    signerMapper = new SignerMapper();
    stepMapper = new StepMapper();
    templateMapper = new TemplateMapper();
  }

  public static Mappers getMappers() {
    return mappers;
  }

  public ActionMapper getActionMapper() {
    return actionMapper;
  }

  public AssistantMapper getAssistantMapper() {
    return assistantMapper;
  }

  public BlockMapper getBlockMapper() {
    return blockMapper;
  }

  public ControlLabelMapper getControlLabelMapper() {
    return controlLabelMapper;
  }

  public DecisionMapper getDecisionMapper() {
    return decisionMapper;
  }

  public DocumentMapper getDocumentMapper() {
    return documentMapper;
  }

  public ExemplarMapper getExemplarMapper() {
    return exemplarMapper;
  }

  public FavoriteUserMapper getFavoriteUserMapper() {
    return favoriteUserMapper;
  }

  public ImageMapper getImageMapper() {
    return imageMapper;
  }

  public LinkMapper getLinkMapper() {
    return linkMapper;
  }

  public PerformerMapper getPerformerMapper() {
    return performerMapper;
  }

  public PrimaryConsiderationMapper getPrimaryConsiderationMapper() {
    return primaryConsiderationMapper;
  }

  public RouteMapper getRouteMapper() {
    return routeMapper;
  }

  public SignerMapper getSignerMapper() {
    return signerMapper;
  }

  public StepMapper getStepMapper() {
    return stepMapper;
  }

  public TemplateMapper getTemplateMapper() {
    return templateMapper;
  }
}
