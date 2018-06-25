package com.example.a1.iturapp;

public class Zakaz {

    int pid;
    String name;
    Double price;
    String image;
    String bigimage;
    int kol;
    String state;

    Zakaz(int _pid, String _name, Double _price, String _image, String _bigimage, int _kol, String _state) {
        this.pid = _pid;
        this.name = _name;
        this.price = _price;
        this.image = _image;
        this.bigimage = _bigimage;
        this.kol = _kol;
        this.state = _state;
    }
}
