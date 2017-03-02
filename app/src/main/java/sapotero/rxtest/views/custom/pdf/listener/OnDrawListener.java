package sapotero.rxtest.views.custom.pdf.listener;

import android.graphics.Canvas;

public interface OnDrawListener {

    void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage);
}
