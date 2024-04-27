package com.dokio.message.response.additional.calendar;

public class ItemResource {

    private Long id;      // id of resource
    private String name;  // name of resource (Hostel room bed, Hyundai i30, Massage table etc.)
    private int usedQuantity; // summary quantity of this resource to do the service of item (reservation, appointment etc.)
    private int totalQuantity; // total quantity of this resource in the part of department (Hostel room, rent a car garage, Massage cabinet etc.)

    public ItemResource() {
    }

    public ItemResource(Long id, String name,  int usedQuantity, int totalQuantity) {
        this.id = id;
        this.name = name;
        this.usedQuantity = usedQuantity;
        this.totalQuantity = totalQuantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUsedQuantity() {
        return usedQuantity;
    }

    public void setUsedQuantity(int usedQuantity) {
        this.usedQuantity = usedQuantity;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}
