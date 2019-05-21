package br.com.ajuda.erp.converter;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.ajuda.erp.persistencia.FinanBancosContas;

@FacesConverter(value="bancosContasConverter") 
public class BancosContasConverter implements Converter {

	public static List<FinanBancosContas> listaBancosContas = new ArrayList<FinanBancosContas>();
	
	public Object getAsObject(FacesContext fc, UIComponent arg1, String value) {
		FinanBancosContas bancoContas = null;
		try {
			if(value != null && value.trim().length() > 0) {
	            for ( FinanBancosContas bancoContasRec : listaBancosContas ) {
                	if ( bancoContasRec.getIDFinanBancosContas() == Integer.parseInt(value)) {
                		bancoContas = bancoContasRec;
                	}
	            }
	        }
		} catch (Exception e) {
			bancoContas = null;
		}
		return bancoContas;
        
      
	}

	
	public String getAsString(FacesContext fc, UIComponent arg1, Object obj) {
		if(obj != null) {
            return String.valueOf(((FinanBancosContas) obj).getIDFinanBancosContas());
        }
        else {
            return null;
        }

	}

}


