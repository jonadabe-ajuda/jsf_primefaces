package br.com.ajuda.erp.converter;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.ajuda.erp.persistencia.CepRegioes;

@FacesConverter(value="cepRegioesConverter")  
public class CepRegioesConverter implements Converter {
	
	public static List<CepRegioes> listaCepRegioes = new ArrayList<CepRegioes>();

	public CepRegioesConverter () {
	
	}
	

	public Object getAsObject(FacesContext facesContext, UIComponent component, String submittedValue) {
        if (submittedValue.trim().equals("")) {
            return null;
        } else {        	
            try {
                int number = Integer.parseInt(submittedValue);
                 for (CepRegioes roteRegioes : listaCepRegioes) {
                    if (roteRegioes.getIDCepRegioes() == number) {
                        return roteRegioes;
                    }
                }
 
            } catch(NumberFormatException exception) {
                //throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid player"));
            }
        }
		return null;
	}

	
	public String getAsString(FacesContext facesContext, UIComponent component, Object value) {
		 if (value == null || value.equals("")) {
	            return "";
	        } else {        	
	            return String.valueOf(((CepRegioes) value).getIDCepRegioes());
	        }
	}
	

}

