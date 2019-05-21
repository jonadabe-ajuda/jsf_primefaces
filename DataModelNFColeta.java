package br.com.ajuda.erp.mb.negocios;

import java.io.Serializable;
import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

import br.com.ajuda.erp.persistencia.NotaFiscalSaidaColetas;


public class DataModelNFColeta extends ListDataModel<NotaFiscalSaidaColetas> implements SelectableDataModel<NotaFiscalSaidaColetas>, Serializable {

	private static final long serialVersionUID = 1L;

	public DataModelNFColeta () {
		
	}
	
	public DataModelNFColeta ( List<NotaFiscalSaidaColetas> listaNFColetas) {
		
		super(listaNFColetas);
		
	}	
	
	
	public NotaFiscalSaidaColetas getRowData(String arg0) {
		
		try {
			List<NotaFiscalSaidaColetas> listaNFColetas = (List<NotaFiscalSaidaColetas>) getWrappedData();
			for ( NotaFiscalSaidaColetas nfColeta : listaNFColetas ) {
				if ( nfColeta.getIDNotaFiscalSaidaColetas() == Integer.parseInt(arg0) ) {
					return nfColeta;
				}
			}			
		} catch (NullPointerException e) {
			// TODO: handle exception
		}

		
		return null;
	}

	
	public Object getRowKey(NotaFiscalSaidaColetas nfColeta) {
		
		return nfColeta.getIDNotaFiscalSaidaColetas();
	}

}
