package br.com.ajuda.erp.mb.negocios;

import java.io.Serializable;
import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

import br.com.ajuda.erp.persistencia.NotaFiscalSaida;


public class DataModelNFSaida extends ListDataModel<NotaFiscalSaida> implements SelectableDataModel<NotaFiscalSaida>, Serializable {

	private static final long serialVersionUID = 1L;

	public DataModelNFSaida () {
		
	}
	
	public DataModelNFSaida ( List<NotaFiscalSaida> listaNFSaida) {
		
		super(listaNFSaida);
		
	}	
	
	
	public NotaFiscalSaida getRowData(String arg0) {
		
		try {
			List<NotaFiscalSaida> listaNFSaida = (List<NotaFiscalSaida>) getWrappedData();
			for ( NotaFiscalSaida nfSaida : listaNFSaida ) {
				if ( nfSaida.getIDNotaFiscalSaida() == Integer.parseInt(arg0) ) {
					return nfSaida;
				}
			}			
		} catch (NullPointerException e) {
			// TODO: handle exception
		}

		
		return null;
	}

	
	public Object getRowKey(NotaFiscalSaida nfSaida) {
		
		return nfSaida.getIDNotaFiscalSaida();
	}

}
