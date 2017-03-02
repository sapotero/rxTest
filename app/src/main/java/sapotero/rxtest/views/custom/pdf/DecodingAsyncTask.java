package sapotero.rxtest.views.custom.pdf;

import android.content.Context;
import android.os.AsyncTask;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import sapotero.rxtest.views.custom.pdf.source.DocumentSource;

class DecodingAsyncTask extends AsyncTask<Void, Void, Throwable> {

    private boolean cancelled;

    private PDFView pdfView;

    private Context context;
    private PdfiumCore pdfiumCore;
    private PdfDocument pdfDocument;
    private String password;
    private DocumentSource docSource;

    public DecodingAsyncTask(DocumentSource docSource, String password, PDFView pdfView, PdfiumCore pdfiumCore) {
        this.docSource = docSource;
        this.cancelled = false;
        this.pdfView = pdfView;
        this.password = password;
        this.pdfiumCore = pdfiumCore;
        context = pdfView.getContext();
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        try {
            pdfDocument = docSource.createDocument(context, pdfiumCore, password);
            return null;
        } catch (Throwable t) {
            return t;
        }
    }

    @Override
    protected void onPostExecute(Throwable t) {
        if (t != null) {
            pdfView.loadError(t);
            return;
        }
        if (!cancelled) {
            pdfView.loadComplete(pdfDocument);
        }
    }

    @Override
    protected void onCancelled() {
        cancelled = true;
    }
}
