package com.razorpay.app.common.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;

@Embeddable
@AllArgsConstructor
public class Money {
    private int amountUnits;
    private String currency;

    private Money of(int amountUnits, String currency){
        return new Money(amountUnits, currency);
    }

    public Money inr(int amountUnits){
        return new Money(amountUnits, "INR");
    }

    public Money add(Money other){
        if(!this.currency.equals(other.currency)){
            throw  new IllegalArgumentException("Can not add money with different currencies");
        }
        return new Money(this.amountUnits+ other.amountUnits, this.currency);
    }

    public Money subtract(Money other){
        if(!this.currency.equals(other.currency)){
            throw  new IllegalArgumentException("Can not add money with different currencies");
        }
        return new Money(this.amountUnits - other.amountUnits, this.currency);
    }

}
