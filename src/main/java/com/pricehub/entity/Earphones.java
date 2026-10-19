package com.pricehub.entity;

import com.pricehub.entity.enums.ProductCategory;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * OOP: Inheritance - Specialization of Product for audio equipment.
 */
@Entity
@DiscriminatorValue("EARPHONES")
public class Earphones extends Product {

    @Column(name = "has_mic")
    private Boolean hasMic;

    @Column(name = "is_wireless")
    private Boolean wireless;

    public Earphones() {
        super();
        setCategory(ProductCategory.EARPHONES);
    }

    // OOP: Method Overloading - Constructor 1
    public Earphones(String name) {
        super(name, ProductCategory.EARPHONES);
        this.wireless = name != null && name.toLowerCase().contains("wireless");
        this.hasMic = true;
    }

    // OOP: Method Overloading - Constructor 2
    public Earphones(String name, Boolean wireless, Boolean hasMic) {
        super(name, ProductCategory.EARPHONES);
        this.wireless = wireless;
        this.hasMic = hasMic;
    }

    public Boolean getHasMic() {
        return hasMic;
    }

    public void setHasMic(Boolean hasMic) {
        this.hasMic = hasMic;
    }

    public Boolean getWireless() {
        return wireless;
    }

    public void setWireless(Boolean wireless) {
        this.wireless = wireless;
    }
}