package org.openhds.mobile.fragment;



import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
import org.openhds.mobile.model.Form;
import org.openhds.mobile.model.LocationVisit;


import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * ValueFormFragment is responsible for showing a list of forms, and then
 * notifying the activity using this fragment which entity has been selected.
 */
public class ValueFormFragment extends ListFragment implements LoaderCallbacks<Cursor> {


   private static final int FORM_LOADER = 6;

   // create the column mappings so they don't need to be recreated on every
   // load
    private static final String[] HIERARCHY_COLUMNS = new String[] { OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME,
           OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID};

   private static final String[] FORM_COLUMNS = new String[] { OpenHDS.Forms.COLUMN_FORM_NAME};

   private static final int[] VIEW_BINDINGS = new int[] { android.R.id.text1};

   private SimpleCursorAdapter adapter;

   // since this fragment displays different types of entities, it needs to
   // keep track of which one is currently showing
   private Displayed listCurrentlyDisplayed;
   private ValueListener listener;

   private enum Displayed {
       FORM;
   }

   public interface ValueListener {
         void onFormSelected(Form form);
   }

   @Override
   public void onAttach(Activity activity) {
       super.onAttach(activity);

       try {
           listener = (ValueListener) activity;
       } catch (ClassCastException e) {
           throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
       }

       adapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item, null, HIERARCHY_COLUMNS,
               VIEW_BINDINGS, 0);
       setListAdapter(adapter);
   }

   @Override
   public void onListItemClick(ListView l, View v, int position, long id) {
       Cursor cursor = (Cursor) adapter.getItem(position);

       switch (listCurrentlyDisplayed) {
       case FORM:
           Form form = Converter.toForms(cursor);
           listener.onFormSelected(form);
           break;
       }

       adapter.swapCursor(null);
   }


   public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
       switch (arg0) {

       case FORM_LOADER:
           adapter.changeCursorAndColumns(null, FORM_COLUMNS, VIEW_BINDINGS);
           return buildFormCursorLoader(arg1);        
       }

       return null;
   }

   private Loader<Cursor> buildFormCursorLoader(Bundle arg1) {
       if (TextUtils.isEmpty(arg1.getString("name"))) {
           return buildCursorLoader(OpenHDS.Forms.CONTENT_ID_URI_BASE, OpenHDS.Forms.COLUMN_FORM_GENDER +"='All' OR "+OpenHDS.Forms.COLUMN_FORM_GENDER + " = ?",  new String[] {  arg1.getString("gender")});
       } else {
           return buildCursorLoader(OpenHDS.Forms.CONTENT_ID_URI_BASE,"("+OpenHDS.Forms.COLUMN_FORM_GENDER +"='All' OR "+OpenHDS.Forms.COLUMN_FORM_GENDER +"='"+ arg1.getString("gender") + "') and " + OpenHDS.Forms.COLUMN_FORM_NAME
                   + " like ?", new String[] {  arg1.getString("name")  /*+ "%"*/});
      }
   }


   private Loader<Cursor> buildCursorLoader(Uri uri, String where, String[] args) {
       return new CursorLoader(getActivity(), uri, null, where, args, null);
   }

   public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
       adapter.swapCursor(arg1);
   }

   public void onLoaderReset(Loader<Cursor> arg0) {
       adapter.swapCursor(null);
   }

 
   /**
    * Loads a list of Forms that are filtered by the arguments
    * 
    * @param location
    *            the location id to filter, or null to ignore filtering on
    *            location
    */
   public void loadFilteredForms(String formName, LocationVisit location) {
       listCurrentlyDisplayed = Displayed.FORM;
       Bundle bundle = new Bundle();
       bundle.putString("name", formName);
       bundle.putString("gender", location.getSelectedIndividual().getGender());
       getLoaderManager().restartLoader(FORM_LOADER, bundle, this);
   }

}
