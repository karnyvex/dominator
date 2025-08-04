package com.eve.dominator.model;

import jakarta.persistence.*;

@Entity
@Table(name = "item_names")
public class ItemName {

    @Id
    @Column(name = "type_id")
    private Integer typeId;

    @Column(name = "name", nullable = false)
    private String name;

    // Constructors
    public ItemName() {}

    public ItemName(Integer typeId, String name) {
        this.typeId = typeId;
        this.name = name;
    }

    // Getters and setters
    public Integer getTypeId() { return typeId; }
    public void setTypeId(Integer typeId) { this.typeId = typeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "ItemName{" +
                "typeId=" + typeId +
                ", name='" + name + '\'' +
                '}';
    }
}
