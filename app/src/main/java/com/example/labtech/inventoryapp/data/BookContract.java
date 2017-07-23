package com.example.labtech.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by LABTECH on 13/7/2017.
 */

public class BookContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.labtech.books";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.books/books/ is a valid path for
     * looking at book data. content://com.example.android.books/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_BOOKS = "books";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private BookContract() {
    }

    /**
     * Inner class that defines constant values for the books database table.
     * Each entry in the table represents a single book.
     */
    public static final class BookEntry implements BaseColumns {

        /**
         * The content URI to access the book data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of books.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single book.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        /**
         * Name of database table for books
         */
        public final static String TABLE_NAME = "books";

        /**
         * Name of the book.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_BOOK_NAME = "name";

        /**
         * Price of the book.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_BOOK_PRICE = "price";

        /**
         * SUPPLIER of the book.
         * <p>
         * The only possible values are {@link #SUPPLIER_ESHOP}, {@link #SUPPLIER_AMAZON},
         * or {@link #SUPPLIER_PLAISIO}.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_BOOK_SUPPLIER = "supplier";

        /**
         * Quantity of the book.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_BOOK_QUANTITY = "quantity";

        /**
         * Image of the book.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_BOOK_IMAGE = "image";

        /**
         * Possible values for the supplier of the book.
         */
        public static final int SUPPLIER_ESHOP = 0;
        public static final int SUPPLIER_AMAZON = 1;
        public static final int SUPPLIER_PLAISIO = 2;

        /**
         * Returns whether or not the given supplier is {@link #SUPPLIER_ESHOP}, {@link #SUPPLIER_AMAZON},
         * or {@link #SUPPLIER_PLAISIO}.
         */
        public static boolean isValidSupplier(int supplier) {
            if (supplier == SUPPLIER_ESHOP || supplier == SUPPLIER_AMAZON || supplier == SUPPLIER_PLAISIO) {
                return true;
            }
            return false;
        }


    }

}
