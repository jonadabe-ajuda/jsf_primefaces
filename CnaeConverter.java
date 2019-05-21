package br.com.ajuda.erp.converter;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.ajuda.erp.delegate.PessoasDelegate;
import br.com.ajuda.erp.persistencia.Cnae;
import br.com.ajuda.erp.persistencia.TipoDespesas;
import br.com.ajuda.erp.util.TratamentoConexao;

@FacesConverter(value="cnaeConverter")  
public class CnaeConverter implements Converter {

	public static List<Cnae> listaCnae = PessoasDelegate.getInstance().recuperarListaCnae(new Cnae(), TratamentoConexao.NomeConexao());

	public CnaeConverter () {
	
	}
	
	
	public Object getAsObject(FacesContext facesContext, UIComponent component, String submittedValue) {
        if (submittedValue.trim().equals("")) {
            return null;
        } else {        	
            try {
                int number = Integer.parseInt(submittedValue);
 
                for (Cnae cnae : listaCnae) {
                    if (cnae.getIDCnae() == number) {
                        return cnae;
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
	            return String.valueOf(((Cnae) value).getIDCnae());
	        }
	}

}
