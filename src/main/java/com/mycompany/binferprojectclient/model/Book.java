/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.binferprojectclient.model;

import javafx.scene.control.CheckBox;

/**
 * This class is a model Book class
 * @author samzi
 */
public class Book {
    private Integer id;
    private String bookTitle;
    private CheckBox select;

    public Book() {}
    
    public Book(Integer id, String bookTitle) {
        this.id = id;
        this.bookTitle = bookTitle;
        this.select = new CheckBox();
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setBookTitle(String item) {
        this.bookTitle = item;
    }

    public CheckBox getSelect() {
        return select;
    }
    public String getBookTitle() {
        return this.bookTitle;
    }

    public void setSelect(CheckBox select) {
        this.select = select;
    }
}
