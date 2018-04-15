package br.ufal.ic.teste.atividade_02.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class Address {
	
	private final String cep;
	private String state;
	private String city;
	private String district;
	private String type;
	private String street;
}
