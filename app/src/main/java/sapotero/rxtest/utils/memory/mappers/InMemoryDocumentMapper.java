package sapotero.rxtest.utils.memory.mappers;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RLinksEntity;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.retrofit.models.documents.Signer;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import timber.log.Timber;

public class InMemoryDocumentMapper {

  public static InMemoryDocument fromJson(Document document) {

    InMemoryDocument imd = new InMemoryDocument();
    imd.setUid( document.getUid() );
    imd.setMd5( document.getMd5() );
    imd.setAsNew();
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
    document.setRed( doc.isRed() );
    document.setChanged( doc.isChanged() );
    document.setControl( doc.isControl() );
    document.setFavorites( doc.isFavorites() );
    document.setProcessed( doc.isProcessed() );
    document.setFromFavoritesFolder( doc.isFromFavoritesFolder() );
    document.setFromProcessedFolder( doc.isFromProcessedFolder() );


    if ( doc.getLinks().size() > 0 ){
      Observable
        .from( doc.getLinks() )
        .map( rLinks -> (RLinksEntity) rLinks )
        .first()
        .map( RLinksEntity::getUid )
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          document::setFirstLink,
          Timber::e
        );
    }

    RSignerEntity rSigner = (RSignerEntity) doc.getSigner();
    Signer signer = new Signer();
    signer.setId( rSigner.getUid() );
    signer.setType( rSigner.getType() );
    signer.setName( rSigner.getName() );
    signer.setOrganisation( rSigner.getOrganisation() );

    document.setSigner( signer );
    document.setViewed( doc.isViewed() );

    return document;
  }

  public static InMemoryDocument fromDB(RDocumentEntity document) {

    InMemoryDocument imd = new InMemoryDocument();
    imd.setUid( document.getUid() );
    imd.setMd5( document.getMd5() );
    imd.setFilter(document.getFilter());
    imd.setIndex(document.getDocumentType());
    imd.setDocument( convert(document) );
    imd.setAsNew();

    return imd;
  }
}
