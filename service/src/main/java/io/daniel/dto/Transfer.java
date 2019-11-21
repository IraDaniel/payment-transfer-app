package io.daniel.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class Transfer implements Serializable {

    private Integer idAccountFrom;
    private Integer idAccountTo;
    private BigDecimal amount;

    @Builder
    public Transfer(Integer idAccountFrom, Integer idAccountTo, BigDecimal amount) {
        this.idAccountFrom = idAccountFrom;
        this.idAccountTo = idAccountTo;
        this.amount = amount;
    }
}
