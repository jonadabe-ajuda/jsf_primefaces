package br.com.ajuda.erp.mb.negocios;

import java.io.Serializable;
import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

import br.com.ajuda.erp.persistencia.Pessoas;

public class DataModelCrmLista extends ListDataModel<Pessoas> implements SelectableDataModel<Pessoas>, Serializable {

	private static final long serialVersionUID = 1L;

	public DataModelCrmLista () {
		
	}
	
	public DataModelCrmLista ( List<Pessoas> listaCrms) {
		
		super(listaCrms);
		
	}	
	
	
	public Pessoas getRowData(String rowKey) {
		
		List<Pessoas> listaTp = (List<Pessoas>) getWrappedData();

		for ( Pessoas crm : listaTp ) {
			if(crm.getIDPessoa() == Integer.parseInt(rowKey))
	                return crm;
		}
		
		return null;
	}

	
	public Object getRowKey(Pessoas crm) {
		
		return crm.getIDPessoa();
	}

}
