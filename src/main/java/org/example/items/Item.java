package org.example.items;

import jakarta.persistence.*;

@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private long itemId;

    @Column(nullable = false, length = 512)
    private String itemName;

    @Column(length = 1024)
    private String imgSrc;

    protected Item() {}

    public Item(long itemId, String itemName) {
        this.itemId = itemId;
        this.itemName = itemName;
    }

    public Long getId() { return id; }
    public long getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getImgSrc() { return imgSrc; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setImgSrc(String imgSrc) { this.imgSrc = imgSrc; }
}
