/**
 * SynchronousHTMLEditorKit.java Copyright UCAR (c) 2017. University Corporation for Atmospheric Research (UCAR), National Center for Atmospheric Research
 * (NCAR), Research Applications Laboratory (RAL), P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2017.
 */

package edu.ucar.metviewer.scorecard.html2image;

import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;

/**
 * @author : tatiana $
 * @version : 1.0 : 17/01/17 13:24 $
 */
class SynchronousHTMLEditorKit extends HTMLEditorKit {

  @Override
  public Document createDefaultDocument() {
    HTMLDocument doc = (HTMLDocument) super.createDefaultDocument();
    doc.setAsynchronousLoadPriority(-1);
    return doc;
  }

  @Override
  public ViewFactory getViewFactory() {
    return new HTMLEditorKit.HTMLFactory() {
      @Override
      public View create(Element elem) {
        View view = super.create(elem);
        if (view instanceof ImageView) {
          ((ImageView) view).setLoadsSynchronously(true);
        }

        return view;
      }
    };
  }

}