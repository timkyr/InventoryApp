package com.example.labtech.inventoryapp;

/**
 * Created by LABTECH on 17/7/2017.
 */

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.labtech.inventoryapp.data.BookContract.BookEntry;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * {@link BookCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of book data as its data source. This adapter knows
 * how to create list items for each row of book data in the {@link Cursor}.
 */
public class BookCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link BookCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        // return the list item view (instead of null)
        return view;
    }

    /**
     * This method binds the book data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Find the columns of book attributes that we're interested in
        int idColumnIndex = cursor.getInt(cursor.getColumnIndex(BookEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);


        // Read the book attributes from the Cursor for the current book
        String bookName = cursor.getString(nameColumnIndex);
        String bookQuantity = cursor.getString(quantityColumnIndex);
        String bookPrice = cursor.getString(priceColumnIndex);

        // Update the TextViews with the attributes for the current book
        viewHolder.nameTextView.setText(bookName);
        viewHolder.quantityTextView.setText(bookQuantity);
        viewHolder.priceTextView.setText(bookPrice);

        //get currentBookUri
        final Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, idColumnIndex);
        //get current book's quantity value
        final int currentBookquantity = cursor.getInt(quantityColumnIndex);

        //set listener for each button in the list view
        viewHolder.sellButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ContentResolver resolver = v.getContext().getContentResolver();
                // Create a new map of values, where column names are the keys
                ContentValues values = new ContentValues();
                if (currentBookquantity > 0) {
                    int quantity = currentBookquantity;
                    --quantity;
                    values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);
                    resolver.update(
                            currentBookUri,
                            values,
                            null,
                            null
                    );
                    context.getContentResolver().notifyChange(currentBookUri, null);

                } else {
                    // Otherwise, the reduce cannot be performed and we display a toast.
                    Toast.makeText(context, R.string.quantity_reduce_failed,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * ViewHolder
     * binds views and fields to a variable
     * no need to use findViewById anymore
     */
    static class ViewHolder {
        @BindView(R.id.name)
        TextView nameTextView;
        @BindView(R.id.quantity_text_view)
        TextView quantityTextView;
        @BindView(R.id.price_text_view)
        TextView priceTextView;
        @BindView(R.id.sell_book_button)
        Button sellButton;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}