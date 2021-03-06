package sapotero.rxtest.utils.memory.mappers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sapotero.rxtest.db.mapper.ActionMapper;
import sapotero.rxtest.db.mapper.DecisionMapper;
import sapotero.rxtest.db.mapper.ImageMapper;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RRouteEntity;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.db.requery.models.actions.RAction;
import sapotero.rxtest.db.requery.models.actions.RActionEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecision;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.images.RImage;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.DocumentInfoAction;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.retrofit.models.documents.Signer;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;

public class InMemoryDocumentMapper {

  public static InMemoryDocument fromJson(Document document) {

    InMemoryDocument imd = new InMemoryDocument();
    imd.setUid( document.getUid() );
    imd.setMd5( document.getMd5() );
    imd.setAsLoading();
    imd.setDocument(document);

    return imd;
  }

  private static Document convert(RDocumentEntity doc) {

    Document document = new Document();
    document.setUid( doc.getUid() );
    document.setMd5( doc.getMd5() );
    document.setSortKey( doc.getSortKey() );
    document.setTitle( doc.getTitle() );
    document.setOrganization( doc.getOrganization() );
    document.setRegistrationNumber( doc.getRegistrationNumber() );
    document.setRegistrationDate( doc.getRegistrationDate() );
    document.setUrgency( doc.getUrgency() );
    document.setShortDescription( doc.getShortDescription() );
    document.setComment( doc.getComment() );
    document.setExternalDocumentNumber( doc.getExternalDocumentNumber() );
    document.setReceiptDate( doc.getReceiptDate() );
    document.setRed( doc.isRed() != null ? doc.isRed() : false );
    document.setChanged( doc.isChanged() );
    document.setControl( doc.isControl() );
    document.setFavorites( doc.isFavorites() );
    document.setProcessed( doc.isProcessed() != null ? doc.isProcessed() : false );
    document.setFromFavoritesFolder( doc.isFromFavoritesFolder() != null ? doc.isFromFavoritesFolder() : false );
    document.setFromProcessedFolder( doc.isFromProcessedFolder() != null ? doc.isFromProcessedFolder() : false );
    document.setFirstLink( doc.getFirstLink() );
    document.setAddressedToType( doc.getAddressedToType() );
    document.setFromLinks( doc.isFromLinks() );

    RSignerEntity rSigner = (RSignerEntity) doc.getSigner();
    Signer signer = new Signer();

    if ( rSigner != null ) {
      signer.setId( rSigner.getUid() );
      signer.setType( rSigner.getType() );
      signer.setName( rSigner.getName() );
      signer.setOrganisation( rSigner.getOrganisation() );
    }

    document.setSigner( signer );
    document.setViewed( doc.isViewed() );
    document.setReturned( doc.isReturned() );
    document.setRejected( doc.isRejected() );
    document.setAgain( doc.isAgain() );

    return document;
  }

  private static List<Decision> convertDecisions(RDocumentEntity document) {
    List<Decision> decisions = new ArrayList<>();

    if ( document != null && document.getDecisions() != null ) {
      DecisionMapper decisionMapper = new DecisionMapper();

      for ( RDecision decision : document.getDecisions() ) {
        RDecisionEntity decisionEntity = (RDecisionEntity) decision;
        Decision decisionModel = decisionMapper.toModel( decisionEntity );
        decisionModel.setChanged( decisionEntity.isChanged() != null ? decisionEntity.isChanged() : false );
        decisionModel.setTemporary( decisionEntity.isTemporary() != null ? decisionEntity.isTemporary() : false );
        decisions.add( decisionModel );
      }
    }

    return decisions;
  }

  private static List<DocumentInfoAction> convertActions(RDocumentEntity document) {
    List<DocumentInfoAction> actions = new ArrayList<>();

    if ( document != null && document.getActions() != null ) {
      ActionMapper actionMapper = new ActionMapper();

      for ( RAction action : document.getActions() ) {
        RActionEntity actionEntity = (RActionEntity) action;
        DocumentInfoAction actionModel = actionMapper.toModel( actionEntity );

        if (actionEntity.getUpdatedAt() != null) {
          try {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            Date date = format.parse( actionEntity.getUpdatedAt() );
            actionModel.setUpdatedAtTimestamp( (int) (date.getTime()/1000) );
          } catch (ParseException e) {
            e.printStackTrace();
          }
        }

        actions.add( actionModel );
      }
    }

    return actions;
  }

  private static List<Image> convertImages(RDocumentEntity document) {
    List<Image> images = new ArrayList<>();

    if ( document != null && document.getImages() != null ) {
      ImageMapper imageMapper = new ImageMapper();

      for ( RImage image : document.getImages() ) {
        RImageEntity imageEntity = (RImageEntity) image;
        Image imageModel = imageMapper.toModel( imageEntity );
        images.add( imageModel );
      }
    }

    return images;
  }

  public static InMemoryDocument fromDB(RDocumentEntity document) {

    InMemoryDocument imd = new InMemoryDocument();
    Document doc = convert(document);
    doc.setProject(document.getRoute() != null && ((RRouteEntity) document.getRoute()).getSteps() != null && ((RRouteEntity) document.getRoute()).getSteps().size() > 0);

    if ( doc.getChanged() != null && doc.getChanged() ) {
      imd.setAsLoading();
    } else {
      imd.setAsReady();
    }

    List<Decision> decisions = convertDecisions(document);
    List<DocumentInfoAction> actions = convertActions(document);
    List<Image> images = convertImages(document);

    imd.setUid( document.getUid() );
    imd.setUpdatedAt( document.getUpdatedAt() );
    imd.setMd5( document.getMd5() );
    imd.setFilter(document.getFilter());
    imd.setIndex(document.getDocumentType());
    imd.setDocument( doc );
    imd.setDecisions( decisions );
    imd.setActions( actions );
    imd.setImages( images );
    imd.setYear( document.getYear() );
    imd.setProcessed( imd.getDocument().isProcessed() );
    imd.setHasDecision( document.isWithDecision() != null ? document.isWithDecision() : false );
    imd.setProject(document.getRoute() != null && ((RRouteEntity) document.getRoute()).getSteps() != null && ((RRouteEntity) document.getRoute()).getSteps().size() > 0);
    imd.setUser( document.getUser() );

    if (document.getRegistrationDate() != null) {
      try {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        Date date = format.parse( document.getRegistrationDate() );
        imd.setCreatedAt((int) (date.getTime()/1000) );
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }

    imd.setUpdatedFromDB( true );

    return imd;
  }
}
