package br.com.ajuda.erp.converter;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.ajuda.erp.persistencia.Bancos;

@FacesConverter(value="bancosConverter") 
public class BancosConverter implements Converter {

	public static List<Bancos> listaBancos = new ArrayList<Bancos>();
	
	public Object getAsObject(FacesContext fc, UIComponent arg1, String value) {
        if(value != null && value.trim().length() > 0) {
            for ( Bancos banco : listaBancos ) {
            	try {
                	if ( banco.getCodigo().equals(value)) {
                		return banco;
            	    }            		
				} catch (Exception e) {
					e.printStackTrace();
				}

            }
        } 
        return null;
        
      
	}

	
	public String getAsString(FacesContext fc, UIComponent arg1, Object obj) {
		if(obj != null) {
            return String.valueOf(((Bancos) obj).getCodigo());
        }
        else {
            return null;
        }

	}

}


