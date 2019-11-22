package io.daniel.dto;

import io.daniel.model.Money;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class Transfer implements Serializable {

    private Integer idAccountFrom;
    private Integer idAccountTo;
    private Money amount;

    @Builder
    public Transfer(Integer idAccountFrom, Integer idAccountTo, Money amount) {
        this.idAccountFrom = idAccountFrom;
        this.idAccountTo = idAccountTo;
        this.amount = amount;
    }
}
