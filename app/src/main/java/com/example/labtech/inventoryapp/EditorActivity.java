/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.labtech.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.labtech.inventoryapp.data.BookContract.BookEntry;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // photo request responce code
    public static final int PICK_IMAGE_REQUEST = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    // Permission request response code.
    private static final int MY_PERMISSIONS_REQUEST = 2;

    private static final int EXISTING_BOOK_LOADER = 0;
    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();
    /**
     * button  to increase the book's quantity
     */
    Button mAddQuantityButton;
    /**
     * button  to decrease the book's quantity
     */
    Button mReduceQuantityButton;
    String mCurrentPhotoPath;
    /**
     * Content URI for the existing books (null if it's a new book)
     */
    private Uri mCurrentBookUri;
    // uri string image for books
    private String mCurrentPhoto;
    private Uri mPhotoUri;
    /**
     * EditText field to enter the book's name
     */
    private EditText mNameEditText;
    /**
     * EditText field to enter the book's price
     */
    private EditText mPriceEditText;
    /**
     * EditText field to enter the book's quantity
     */
    private EditText mQuantityEditText;
    /**
     * EditText field to enter the book's supplier
     */
    private Spinner mSupplierSpinner;
    /**
     * supplier of the book. The possible values are:
     * 0 for eshop , 1 for amazon, 2 for plaisio.
     */
    private int mSupplier = BookEntry.SUPPLIER_ESHOP;
    /**
     * image  for books cover
     */
    private ImageView mImageBook;
    private Bitmap mBitmap;
    private Button mButtonTakePicture;
    /**
     * Boolean flag that keeps track of whether the book has been edited (true) or not (false)
     */
    private boolean mBookHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mBookHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one.
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        Log.i("EditorActivity.java", "uri is " + mCurrentBookUri);


        //if the intent does not contain a book content URI, then we know that we are
        //creating a new book
        if (mCurrentBookUri == null) {
            //this is a new book so change the app bar to say "add a Book"
            setTitle(getString(R.string.editor_activity_title_new_book));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_book));

            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);

        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_book_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_book_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_book_price);
        mSupplierSpinner = (Spinner) findViewById(R.id.spinner_supplier);
        mImageBook = (ImageView) findViewById(R.id.add_book_image);

        ViewTreeObserver viewTreeObserver = mImageBook.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mImageBook.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mImageBook.setImageBitmap(getBitmapFromUri(mPhotoUri));
            }
        });

        mButtonTakePicture = (Button) findViewById(R.id.button_take_photo);
        mButtonTakePicture.setEnabled(false);

        updateBookPhoto();
        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierSpinner.setOnTouchListener(mTouchListener);


        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_supplier_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSupplierSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mSupplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.supplier_amazon))) {
                        mSupplier = BookEntry.SUPPLIER_AMAZON; // Amazon
                    } else if (selection.equals(getString(R.string.supplier_plasio))) {
                        mSupplier = BookEntry.SUPPLIER_PLAISIO; // Plaisio
                    } else {
                        mSupplier = BookEntry.SUPPLIER_ESHOP; // Eshop
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSupplier = 0; //Eshop
            }
        });
    }

    /**
     * +     * Get user input from editor and save pet into database.
     */
    private void saveBook() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        if (mPhotoUri != null) {
            mCurrentPhoto = mPhotoUri.toString();
        } else {
            mCurrentPhoto = "";
        }
        // Check if this is supposed to be a new book
        // and check if all the fields in the editor are blank
        if (mCurrentBookUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString) && mSupplier == BookEntry.SUPPLIER_ESHOP) {
            // Since no fields were modified, we can return early without creating a new book.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            finish();
        } else {

            //check if the fields are completed - we don't check for supplier because it always has a value
            if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(quantityString) ||
                    TextUtils.isEmpty(priceString) || TextUtils.isEmpty(mCurrentPhoto)) {
                Toast.makeText(this, R.string.fill_fields_msg, Toast.LENGTH_LONG).show();

            } else {

                // Create a new map of values, where column names are the keys
                ContentValues values = new ContentValues();
                values.put(BookEntry.COLUMN_BOOK_NAME, nameString);
                values.put(BookEntry.COLUMN_BOOK_SUPPLIER, mSupplier);
                values.put(BookEntry.COLUMN_BOOK_IMAGE, mCurrentPhoto);

                // If the price is not provided by the user, don't try to parse the string into an
                // integer value. Use 0 by default.
                int price = 0;
                if (!TextUtils.isEmpty(priceString)) {
                    price = Integer.parseInt(priceString);
                }
                values.put(BookEntry.COLUMN_BOOK_PRICE, price);

                // If the quantity is not provided by the user, don't try to parse the string into an
                // integer value. Use 0 by default.
                int quantity = 0;
                if (!TextUtils.isEmpty(quantityString)) {
                    quantity = Integer.parseInt(quantityString);
                }
                values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);

                // Determine if this is a new or existing book by checking if mCurrentBookUri is null or not
                if (mCurrentBookUri == null) {
                    // This is a NEW book, so insert a new book into the provider,
                    // returning the content URI for the new book.
                    Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

                    // Show a toast message depending on whether or not the insertion was successful.
                    if (newUri == null) {
                        // If the new content URI is null, then there was an error with insertion.
                        Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, the insertion was successful and we can display a toast.
                        Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                                Toast.LENGTH_SHORT).show();
                    }

                } else {

                    // Otherwise this is an EXISTING book, so update the book with content URI: mCurrentBookUri
                    // and pass in the new ContentValues. Pass in null for the selection and selection args
                    // because mCurrentBookUri will already identify the correct row in the database that
                    // we want to modify.
                    int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

                    Log.i("EditorActivity.java", "uri is " + mCurrentBookUri);

                    // Show a toast message depending on whether or not the update was successful.
                    if (rowsAffected == 0) {
                        // If no rows were affected, then there was an error with the update.
                        Toast.makeText(this, getString(R.string.editor_update_book_failed),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, the update was successful and we can display a toast.
                        Toast.makeText(this, getString(R.string.editor_update_book_successful),
                                Toast.LENGTH_SHORT).show();
                    }

                }

                finish();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //save book to database
                saveBook();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the book hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_IMAGE,
                BookEntry.COLUMN_BOOK_SUPPLIER};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentBookUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        //When the data from the book is loaded into a cursor, onLoadFinished() is called.
        // Here, I’ll first most the cursor to it’s first item position.
        // Even though it only has one item, it starts at position -1.
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int supplierColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER);
            int imageColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int supplier = cursor.getInt(supplierColumnIndex);
            String photo = cursor.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Integer.toString(price));


            if (!photo.isEmpty()) {
                //convert string to uri
                mPhotoUri = Uri.parse(photo);
                mBitmap = getBitmapFromUri(mPhotoUri);
                mImageBook.setImageBitmap(mBitmap);
            }


            // supplier is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is eshop, 1 is Amazon, 2 is Plaisio).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (supplier) {
                case BookEntry.SUPPLIER_AMAZON:
                    mSupplierSpinner.setSelection(1);
                    break;
                case BookEntry.SUPPLIER_PLAISIO:
                    mSupplierSpinner.setSelection(2);
                    break;
                default:
                    mSupplierSpinner.setSelection(0);
                    break;
            }

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mSupplierSpinner.setSelection(0); // Select default = "eshop" supplier
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton("Cancel", null);

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton("Cancel", null);


        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteBook() {

        // Only perform the delete if this is an existing pet.
        if (mCurrentBookUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentBookUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }

            // Close the activity
            finish();
        }
    }

    //Add quantity method
    public void addQuantity(View view) {
        int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
        quantity++;
        mQuantityEditText.setText(String.valueOf(quantity));

    }

    //reduce quantity method
    public void reduceQuantity(View view) {
        int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
        if (quantity > 0) {
            quantity--;
            mQuantityEditText.setText(String.valueOf(quantity));
        } else {
            // Otherwise, the reduce cannot be performed and we display a toast.
            Toast.makeText(this, getString(R.string.quantity_reduce_failed),
                    Toast.LENGTH_SHORT).show();
        }

    }


    // Check permissions to read external storage and update the photo of the book
    public void updateBookPhoto() {
        // Check if we're running on Android Marshmallow or higher -->     Build.VERSION_CODES.M==23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //if we are in 23 or above we have to request runtime permissions.
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST);
                }
            } else {
                //we are in older device we don't need runtime permission
                mButtonTakePicture.setEnabled(true);
            }
        }
    }

    //Handle the permissions request response
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    mButtonTakePicture.setEnabled(true);
                } else {
                    // permission denied
                    Toast.makeText(this, R.string.device_image_permission, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void selectPhoto(View view) {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        // Show only images, no videos or anything else
        intent.setType("image/*");
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    public void takePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            File file = createImageFile();

            mPhotoUri = FileProvider.getUriForFile(getApplication().getApplicationContext(),
                    "com.example.android.inventory.fileprovider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    mPhotoUri);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // We get the data and define the currentPhotoUri of the image.
            mPhotoUri = data.getData();
            Log.i("EditorActivity.java", "bookPhotoUri is " + mPhotoUri);

            // show photo to editor activity
            mBitmap = getBitmapFromUri(mPhotoUri);
            mImageBook.setImageBitmap(mBitmap);
            mImageBook.setScaleType(ImageView.ScaleType.CENTER_CROP);

        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            mBitmap = getBitmapFromUri(mPhotoUri);
            mImageBook.setImageBitmap(mBitmap);
            mImageBook.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error closing ParcelFile Descriptor");
            }
        }
    }

    public void orderBook(View view) {
        String[] recipient = {"orders@eshop.gr"};
        if (mSupplier == 1) {
            recipient[0] = "orders@amazon.com";
        }
        if (mSupplier == 2) {
            recipient[0] = "orders@plasio.gr";
        }

        String nameProduct = mNameEditText.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.putExtra(Intent.EXTRA_EMAIL, recipient);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Order " + nameProduct);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);

        }

    }


}