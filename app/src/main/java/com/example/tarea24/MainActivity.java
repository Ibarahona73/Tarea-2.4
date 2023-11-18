package com.example.tarea24;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kyanogen.signatureview.SignatureView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText descriptionEditText;
    private SignatureView signaturePad;
    private Button firmas;
    private SignatureAdapter signatureAdapter;
    private ArrayList<Signatures> signatureList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        descriptionEditText = findViewById(R.id.TextoDescription);
        signaturePad = findViewById(R.id.signaturePad);

        Button saveButton = findViewById(R.id.buttonSave);

        //RecyclerView y adapter
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Firmas De La BD
        signatureList = getSignaturesFromDatabase();
        signatureAdapter = new SignatureAdapter(signatureList);

        //adaptador en el Recycler
        recyclerView.setAdapter(signatureAdapter);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signatureList.clear();
                signatureList.addAll(getSignaturesFromDatabase());
                signatureAdapter.notifyDataSetChanged();

                recyclerView.smoothScrollToPosition(signatureAdapter.getItemCount() - 1);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSignature();
            }
        });
    }

    private void saveSignature() {
        Bitmap signatureBitmap = signaturePad.getSignatureBitmap();

        if (addJpgSignatureToGallery(signatureBitmap, descriptionEditText.getText().toString())) {
            // Info De La Firma
            saveToDatabase(descriptionEditText.getText().toString(), signatureBitmap);

            // Add the new signature to the list and notify the adapter
            signatureList.add(new Signatures(descriptionEditText.getText().toString(), signatureBitmap));
            signatureAdapter.notifyItemInserted(signatureList.size() - 1);

            Toast.makeText(this, "Firma Salvada", Toast.LENGTH_SHORT).show();
            clearSignature();
        } else {
            Toast.makeText(this, "No Se Pudo Guardar Esa Firma", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean addJpgSignatureToGallery(Bitmap signature, String description) {
        boolean result = false;
        try {
            File photo = new File(getAlbumStorageDir("SignaturePad"), description + ".jpg");
            saveBitmapToJPG(signature, photo);
            scanMediaFile(photo.getAbsolutePath());
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private File getAlbumStorageDir(String albumName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("SignaturePad", "Directorio No Creado");
        }
        return file;
    }

    private void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        try (FileOutputStream out = new FileOutputStream(photo)) {
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        }
    }

    private void scanMediaFile(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(path);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void clearSignature() {
        signaturePad.clearCanvas();
        descriptionEditText.setText("");
    }

    private void saveToDatabase(String description, Bitmap signature) {

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_DESCRIPTION, description);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        signature.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        values.put(DBHelper.COLUMN_DIGITAL_SIGNATURE, byteArray);

        long newRowId = database.insert(DBHelper.TABLE_NAME, null, values);
        Log.d("MainActivity", "New row id: " + newRowId);

        dbHelper.close();
    }


    private ArrayList<Signatures> getSignaturesFromDatabase() {
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        String[] projection = {
                DBHelper.COLUMN_DESCRIPTION,
                DBHelper.COLUMN_DIGITAL_SIGNATURE
        };

        Cursor cursor = database.query(
                DBHelper.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        ArrayList<Signatures> signatureList = new ArrayList<>();


        while (cursor.moveToNext()) {
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DESCRIPTION));
            byte[] signatureByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DIGITAL_SIGNATURE));
            Bitmap signatureBitmap = BitmapFactory.decodeByteArray(signatureByteArray, 0, signatureByteArray.length);

            Signatures signature = new Signatures(description, signatureBitmap);
            signatureList.add(signature);
        }

        cursor.close();
        dbHelper.close();


      return signatureList;
    }
}