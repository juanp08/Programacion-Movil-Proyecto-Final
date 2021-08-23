package com.example.galeriadedeportes.modelos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Item implements Serializable {

    @SerializedName("descripcion")
    @Expose
    private String descripcion;

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("titulo")
    @Expose
    private String titulo;

    @SerializedName("url")
    @Expose
    private String url;



    public Item() {
    }

    public Item(String descripcion, String id, String titulo, String url) {
        this.descripcion = descripcion;
        this.id = id;
        this.titulo = titulo;
        this.url = url;
    }



    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
