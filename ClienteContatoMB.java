package br.com.ajuda.erp.mb.negocios;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import br.com.ajuda.erp.converter.ClientesContatoConverter;
import br.com.ajuda.erp.delegate.PessoasDelegate;
import br.com.ajuda.erp.mb.DadosSistemaMB;
import br.com.ajuda.erp.mb.admCadastro.DataModelPessoasTipo;
import br.com.ajuda.erp.negocios.ClienteContatoCommand;
import br.com.ajuda.erp.persistencia.Pessoas;
import br.com.ajuda.erp.persistencia.PessoasClientesContatos;
import br.com.ajuda.erp.persistencia.PessoasPessoaTipo;
import br.com.ajuda.erp.persistencia.SystemSearch;

@ManagedBean
@ViewScoped
public class ClienteContatoMB extends ClienteContatoCommand implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private SystemSearch systemSearchSelected;
	private String systemSearchConteudo;
	private int tipoOperacao = 0; // -- 0 - Sem operacao 1 - inclusao e 2 - alteracao	
	
	private DataModelCrmContatoLista dataModelCrmContatoLista; 
	
	@ManagedProperty(value="#{dadosSistemaMB}")
	private DadosSistemaMB dadosSistema;	

	
	@PostConstruct
    public void setOpcao() {
	
		setCampos(dadosSistema.getUsuario());
	}
	
	public void getListaPessoasBusca () {
		
		listaPessoasCliContatos = new ArrayList<PessoasClientesContatos>();
		dataModelCrmContatoLista = new DataModelCrmContatoLista();
		if ( systemSearchSelected != null ) {
			systemSearchSelected.setConteudoBusca(systemSearchConteudo);
			for ( PessoasClientesContatos pCliContatos : PessoasDelegate.getInstance().recuperarPessoaClienteContatoBusca(systemSearchSelected, dadosSistema.getUsuario().getNomeConexao() ))  {
				if ( dadosSistema.getUsuario().getNivel() != 2 ) {
					listaPessoasCliContatos.add(pCliContatos);
				} else {
					if ( pCliContatos.getPessoaUsuario().equals(dadosSistema.getUsuario().getPessoaUsuario()) ) 
						 listaPessoasCliContatos.add(pCliContatos);
				}
					
			}
		}
		
		dataModelCrmContatoLista = new DataModelCrmContatoLista(listaPessoasCliContatos);

	}	
	
	public void save () {
		
		mensaTela = "";
		try {
			if ( clienteContato.getEmpresa().isEmpty() ) {
				mensaTela = "Nome empresa obrigatório";
			} else if ( clienteContato.getNomeContato().isEmpty() ) {
				mensaTela = "Nome contato obrigatório";
			} else if ( clienteContato.getTelefone().isEmpty() ) {
				mensaTela = "Telefone obrigatório";
			} else {
				if ( clienteContato.getIDPessoasContatos() == null ) {
					clienteContato.setPessoaEmpresa(dadosSistema.getPessoasEmpresa());
					clienteContato.setSituacao(1);
					clienteContato.setDataInclusao(new Date());
					clienteContato.setPessoaUsuario(dadosSistema.getUsuario().getPessoaUsuario());
					clienteContato.setTipoInclusao(0);
					entityPersist =clienteContato;
					salvar();
					clienteContato = (PessoasClientesContatos) entityPersisted;
					ClientesContatoConverter.listaCliContatos.add(clienteContato);
					mensaTela = "Incluido com sucesso.";
				} else {
					entityPersist =clienteContato;
					update();				
					ClientesContatoConverter.listaCliContatos.set(ClientesContatoConverter.listaCliContatos.indexOf(clienteContato),clienteContato);
					mensaTela = "Alterado com sucesso.";
				}
				
				
			}
						
		} catch (Exception e) {
			mensaTela = "Problemas com dados.";
			e.printStackTrace();
		}

		if ( !mensaTela.isEmpty() ) {
	        FacesMessage msg = new FacesMessage("", mensaTela );
	        FacesContext.getCurrentInstance().addMessage(null, msg);			
		}
	}
	
	public void setClienteContato () {
		clienteContato = clienteContatoSelected;
		tipoOperacao = 1;
	}

    @SuppressWarnings({ "unchecked", "rawtypes"})
	public List<PessoasClientesContatos> completeClienteContato (String query) {
    	
    	List<PessoasClientesContatos> resultado = new ArrayList<PessoasClientesContatos>();
    	clienteContato.setEmpresa(query);
    	
    	for (PessoasClientesContatos cliCont : ClientesContatoConverter.listaCliContatos) {   	
    		if ( cliCont.getEmpresa().toLowerCase().contains(query.toLowerCase())) {
    			resultado.add(cliCont);   			
    		}
    	}
    	
        Collections.sort (resultado, new Comparator() {
            public int compare(Object o1, Object o2) {
            	PessoasClientesContatos c1 = (PessoasClientesContatos) o1;
            	PessoasClientesContatos c2 = (PessoasClientesContatos) o2;
                return c1.getEmpresa().compareToIgnoreCase(c2.getEmpresa());
              }
        });    	
    	
    	return resultado;
    	
    }		
	
	public DadosSistemaMB getDadosSistema() {
		return dadosSistema;
	}

	public void setDadosSistema(DadosSistemaMB dadosSistema) {
		this.dadosSistema = dadosSistema;
	}

	public SystemSearch getSystemSearchSelected() {
		return systemSearchSelected;
	}

	public int getTipoOperacao() {
		return tipoOperacao;
	}

	public void setSystemSearchSelected(SystemSearch systemSearchSelected) {
		this.systemSearchSelected = systemSearchSelected;
	}

	public void setTipoOperacao(int tipoOperacao) {
		this.tipoOperacao = tipoOperacao;
	}

	public String getSystemSearchConteudo() {
		return systemSearchConteudo;
	}

	public void setSystemSearchConteudo(String systemSearchConteudo) {
		this.systemSearchConteudo = systemSearchConteudo;
	}

	public DataModelCrmContatoLista getDataModelCrmContatoLista() {
		return dataModelCrmContatoLista;
	}

	public void setDataModelCrmContatoLista(
			DataModelCrmContatoLista dataModelCrmContatoLista) {
		this.dataModelCrmContatoLista = dataModelCrmContatoLista;
	}
	
	

}
