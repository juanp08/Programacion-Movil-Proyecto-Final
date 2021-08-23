package com.example.galeriadedeportes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.galeriadedeportes.modelos.Image;
import com.example.galeriadedeportes.modelos.Item;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

public class NewItemActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    Toolbar toolbar;
    EditText txtTituloArticle;
    EditText txtDecripcionArticle;
    ImageView imgProductoArticle;
    Button btnRegistrarArticle;
    Button btnSeleccionarImageArticle;
    Uri selectedImgUri;
    FirebaseFirestore firestore;
    FirebaseStorage storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        txtTituloArticle = findViewById(R.id.txtTituloArticle);
        txtDecripcionArticle = findViewById(R.id.txtDescripcionArticle);
        imgProductoArticle = findViewById(R.id.imgProductoArticle);
        btnRegistrarArticle = findViewById(R.id.btnRegistarArticle);
        btnSeleccionarImageArticle = findViewById(R.id.btnSeleccionarArticle);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        btnRegistrarArticle.setOnClickListener(v -> uploadFile());

        btnSeleccionarImageArticle.setOnClickListener(view -> openFileChoser());
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
            finish();
        }
        return true;
    }


    private void createNewProduct(String image){

        String titulo = txtTituloArticle.getText().toString();
        String descripcion = txtDecripcionArticle.getText().toString();
        DocumentReference idReference = firestore.collection("deportes").document();
        String id = idReference.getId();

        Item item = new Item(descripcion, id, titulo, image);

        idReference.set(item).addOnSuccessListener(
                unused -> Toast.makeText(getApplicationContext(),
                        "El articulo se creo correctamente",
                        Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(),
                        "El articulo no pudo ser registrado",
                        Toast.LENGTH_SHORT).show());
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

            Picasso.with(this).load(selectedImgUri).into(imgProductoArticle);

        }
    }

    private void uploadFile() {
        if(selectedImgUri != null) {
            StorageReference fileReference = storage.getReference().child("images/" + System.currentTimeMillis() +
                    "." + getFileExtension(selectedImgUri));
            fileReference.putFile(selectedImgUri).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Toast.makeText(getApplicationContext(), "La imagen no pudo registrarse, no pudo ser registrado",
                            Toast.LENGTH_SHORT).show();
                }
            })
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        Image image = new Image(uri.toString());
                        createNewProduct(image.getUrl());
                    }));
        }
        else {
            StorageReference fileReference = storage.getReference().child("home/saluddeporte.png");
            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                Image image = new Image(uri.toString());
                createNewProduct(image.getUrl());
            });
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}