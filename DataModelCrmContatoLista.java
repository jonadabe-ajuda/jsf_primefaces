package br.com.ajuda.erp.mb.negocios;

import java.io.Serializable;
import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

import br.com.ajuda.erp.persistencia.PessoasClientesContatos;

public class DataModelCrmContatoLista extends ListDataModel<PessoasClientesContatos> implements SelectableDataModel<PessoasClientesContatos>, Serializable {

	private static final long serialVersionUID = 1L;

	public DataModelCrmContatoLista () {
		
	}
	
	public DataModelCrmContatoLista ( List<PessoasClientesContatos> listaCrms) {
		
		super(listaCrms);
		
	}	
	
	
	public PessoasClientesContatos getRowData(String rowKey) {
		
		List<PessoasClientesContatos> listaTp = (List<PessoasClientesContatos>) getWrappedData();

		for ( PessoasClientesContatos crm : listaTp ) {
			if(crm.getIDPessoasContatos() == Integer.parseInt(rowKey))
	                return crm;
		}
		
		return null;
	}

	
	public Object getRowKey(PessoasClientesContatos crm) {
		
		return crm.getIDPessoasContatos();
	}

}
