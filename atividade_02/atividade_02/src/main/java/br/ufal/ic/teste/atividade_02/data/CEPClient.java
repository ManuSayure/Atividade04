package br.ufal.ic.teste.atividade_02.data;

import java.net.URL;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import lombok.Getter;
import lombok.Setter;

public class CEPClient {

	@Getter @Setter
	private int result = 0;
	
	@Getter @Setter
	private Address address;
	
	public static void main(String[] args) {
		
		CEPClient client = new CEPClient();
		
		client.cepWebBuscar(new Address("57072090"));
		
		System.out.println(client.getAddress());
	}
	
	@SuppressWarnings("rawtypes")
	public void cepWebBuscar(Address toSearch) {

		try {
			URL url = new URL(
					"http://cep.republicavirtual.com.br/web_cep.php?cep=" + toSearch.getCep()	+ "&formato=xml");

			Document document = getDocumento(url);
			
			Element root = document.getRootElement();

			for (Iterator i = root.elementIterator(); i.hasNext();) {
				Element element = (Element) i.next();

				if (element.getQualifiedName().equals("uf"))
					toSearch.setState( element.getText());

				if (element.getQualifiedName().equals("cidade"))
					toSearch.setCity(element.getText());

				if (element.getQualifiedName().equals("bairro"))
					toSearch.setDistrict(element.getText());

				if (element.getQualifiedName().equals("tipo_logradouro"))
					toSearch.setType(element.getText());

				if (element.getQualifiedName().equals("logradouro"))
					toSearch.setStreet(element.getText());

				if (element.getQualifiedName().equals("resultado"))
					setResult(Integer.parseInt(element.getText()));
			}
			
			setAddress(toSearch);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Document getDocumento(URL url) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(url);

		return document;
	}
}
