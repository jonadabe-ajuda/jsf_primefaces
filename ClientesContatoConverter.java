package br.com.ajuda.erp.converter;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.ajuda.erp.delegate.PessoasDelegate;
import br.com.ajuda.erp.persistencia.PessoasClientesContatos;
import br.com.ajuda.erp.persistencia.PessoasLogin;

@FacesConverter(value="clientesContatoConverter")  
public class ClientesContatoConverter implements Converter {

	public static List<PessoasClientesContatos> listaCliContatos =  new ArrayList<PessoasClientesContatos>();

	public ClientesContatoConverter () {
	
	}
	
	public static void  getClienteContato ( PessoasLogin pLogin ) {
		listaCliContatos.clear();
		listaCliContatos = PessoasDelegate.getInstance().recuperarPessoaClienteContato(null, pLogin.getNomeConexao());
	}	

	
	public Object getAsObject(FacesContext facesContext, UIComponent component, String submittedValue) {
        if (submittedValue.trim().equals("")) {
            return null;
        } else {        	
            try {
                int number = Integer.parseInt(submittedValue);
 
                for (PessoasClientesContatos cliCont : listaCliContatos) {
                    if (cliCont.getIDPessoasContatos() == number) {
                        return cliCont;
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
	            return String.valueOf(((PessoasClientesContatos) value).getIDPessoasContatos());
	        }
	}




}
