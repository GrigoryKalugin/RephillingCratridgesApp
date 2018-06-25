package com.example.a1.iturapp;

public class Product {

    int pid;
    String name;
    Double price;
    String image;
    String bigimage;
    String state;
    int isserv;

    Product(int _pid, String _name, Double _price, String _image, String _bigimage, String _state, int _isserv) {
        this.pid = _pid;
        this.name = _name;
        this.price = _price;
        this.image = _image;
        this.bigimage = _bigimage;
        this.state = _state;
        this.isserv = _isserv;
    }
}