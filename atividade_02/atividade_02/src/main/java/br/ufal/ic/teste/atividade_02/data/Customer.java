package br.ufal.ic.teste.atividade_02.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author willy
 */
@RequiredArgsConstructor
@ToString
@Getter
public class Customer {
    
    private Long ecleticaId;
    
    private String name;
    private String phone;
    
    private String cep;
    
    @Setter
    private Address address;
    
    protected Customer(String name, String phone) {
        this.name  = name;
        this.phone = phone;
    }
}
