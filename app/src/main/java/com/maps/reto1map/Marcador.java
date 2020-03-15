package com.maps.reto1map;

import java.io.Serializable;

public class Marcador  implements Serializable {

    private String titulo;
    private String direccion;

    public Marcador(String direccion) {
        this.direccion = direccion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }


    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
}
