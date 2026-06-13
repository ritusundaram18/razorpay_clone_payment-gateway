package com.codingshuttle.razorpay.common.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.EmbeddedTable;

@Embeddable
public class Money {
    private int amountUnits;
    private String currency;

    private Money(int amountUnits, String currency){
        this.amountUnits = amountUnits;
        this.currency = currency;
    }
    public Money inr(int amountUnits){
        return new Money(amountUnits, "INR");
    }

    public Money add(Money other){
        if(!this.currency.equals(other.currency)){
            throw new IllegalArgumentException("Can not add money with different currency");
        }
        return new Money(this.amountUnits + other.amountUnits, this.currency);
    }
    public Money subtract(Money other){
        if(!this.currency.equals(other.currency)){
            throw new IllegalArgumentException("Can not subtract money with different currency");
        }
        return new Money(this.amountUnits - other.amountUnits, this.currency);
    }

}
