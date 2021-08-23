package com.example.galeriadedeportes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.galeriadedeportes.modelos.Image;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ModifyActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    Toolbar toolbar;
    EditText txtTituloEdit;
    EditText txtDescripcionEdit;
    ImageView imgProductoEdit;
    Button btnEditar;
    Button btnSeleccionarImageEdit;
    Uri selectedImgUri;

    private String itemId;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtTituloEdit = findViewById(R.id.txtTituloArticleEdit);
        txtDescripcionEdit = findViewById(R.id.txtDescripcionArticleEdit);
        imgProductoEdit = findViewById(R.id.imgProductoEdit);
        btnEditar = findViewById(R.id.btnEditarArticle);
        btnSeleccionarImageEdit = findViewById(R.id.btnSeleccionarEdit);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        itemId = getIntent().getStringExtra("itemId");

        getValues();

        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });

        btnSeleccionarImageEdit.setOnClickListener(view -> openFileChoser());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.optionMenu1){
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
        return true;
    }

    private void getValues(){
        firestore.collection("deportes").document(itemId).get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                String titulo = documentSnapshot.getString("titulo");
                String descripcion = documentSnapshot.getString("descripcion");
                String imgUrl = documentSnapshot.getString("url");

                txtDescripcionEdit.setText(descripcion);
                txtTituloEdit.setText(titulo);
                Picasso.with(ModifyActivity.this).load(imgUrl).into(imgProductoEdit);
            }
        });
    }

    private void updateItem(String image){

        String titulo = txtTituloEdit.getText().toString();
        String descripcion = txtDescripcionEdit.getText().toString();

        Map<String, Object> mapItem = new HashMap<>();
        mapItem.put("descripcion", descripcion);
        mapItem.put("id", itemId);
        mapItem.put("titulo", titulo);
        mapItem.put("url", image);

        firestore.collection("deportes").document(itemId).update(mapItem).addOnSuccessListener(unused ->
                Toast.makeText(ModifyActivity.this, "Los datos se han actualizado!",
                        Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Toast.makeText(ModifyActivity.this, "Los datos se han actualizado!",
                            Toast.LENGTH_SHORT).show();
                });
        finish();
    }


    private void openFileChoser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            selectedImgUri = data.getData();

            Picasso.with(this).load(selectedImgUri).into(imgProductoEdit);

        }
    }

    private void uploadFile() {
        if(selectedImgUri != null) {
            StorageReference fileReference = storage.getReference().child("images/" +
                    System.currentTimeMillis() + "." + getFileExtension(selectedImgUri));
            fileReference.putFile(selectedImgUri).addOnFailureListener(e -> Toast.makeText(getApplicationContext(),
                    "La imagen no pudo registrarse no pudo ser registrado", Toast.LENGTH_SHORT).show())
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        Image image = new Image(uri.toString());
                        updateItem(image.getUrl());
                    }));
        }
        else {
            firestore.collection("deportes").document(itemId).get().addOnSuccessListener(documentSnapshot -> {
                if(documentSnapshot.exists()){
                    String imgUrl = documentSnapshot.getString("url");
                    Image image = new Image(imgUrl);
                    updateItem(image.getUrl());
                }
            });
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}