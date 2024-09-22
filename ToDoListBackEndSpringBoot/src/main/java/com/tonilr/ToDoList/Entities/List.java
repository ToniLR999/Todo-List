package com.tonilr.ToDoList.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "List")
public class List {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long list_id;

    private String name;

 // Getters y Setters
    public Long getList_Id() {
        return list_id;
    }

    public void setList_Id(Long list_id) {
        this.list_id = list_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
