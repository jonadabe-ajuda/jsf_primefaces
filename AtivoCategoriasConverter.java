package br.com.ajuda.erp.converter;

import java.util.HashSet;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.ajuda.erp.persistencia.ControleAtivoCat;

@FacesConverter(value="ativoCategoriasConverter")  
public class AtivoCategoriasConverter implements Converter{

	public static Set<ControleAtivoCat> listaAtivosCategorias = new HashSet<ControleAtivoCat>();

	public AtivoCategoriasConverter () {
	
	}
	
	
	public Object getAsObject(FacesContext facesContext, UIComponent component, String submittedValue) {
        if (submittedValue.trim().equals("")) {
            return null;
        } else {        	
            try {
                int number = Integer.parseInt(submittedValue);
 
                for (ControleAtivoCat cAtivo : listaAtivosCategorias) {
                    if (cAtivo.getIDControleAtivoCat() == number) {
                        return cAtivo;
                    }
                }
 
            } catch(NumberFormatException ex) {
            	ex.printStackTrace();
                //throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid player"));
            }
        }
		return null;
	}

	
	public String getAsString(FacesContext facesContext, UIComponent component, Object value) {
		 if (value == null || value.equals("")) {
	            return "";
	        } else {        	
	            return String.valueOf(((ControleAtivoCat) value).getIDControleAtivoCat());
	        }
	}


}
