package br.com.ajuda.erp.mb.admCadastro;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import br.com.ajuda.erp.converter.PessoaConverter;
import br.com.ajuda.erp.converter.PlanoContasConverter;
import br.com.ajuda.erp.converter.ProdutosConverter;
import br.com.ajuda.erp.mb.DadosSistemaMB;
import br.com.ajuda.erp.mb.comum.FuncoesMB;
import br.com.ajuda.erp.adm.ProdutosConsumoCommand;
import br.com.ajuda.erp.delegate.ProdutosConsumoDelegate;
import br.com.ajuda.erp.persistencia.FinanPlanoConta;
import br.com.ajuda.erp.persistencia.PedidosConsumoItens;
import br.com.ajuda.erp.persistencia.Pessoas;
import br.com.ajuda.erp.persistencia.PessoasPessoaTipo;
import br.com.ajuda.erp.persistencia.ProdutosConsumo;
import br.com.ajuda.erp.persistencia.ProdutosConsumoPC;
import br.com.ajuda.erp.persistencia.ProdutosConsumoTributos;
import br.com.ajuda.erp.persistencia.ProdutosTipo;
import br.com.ajuda.erp.persistencia.SystemSearch;

@ManagedBean
@ViewScoped
public class ProdutosConsumoMB extends ProdutosConsumoCommand implements Serializable {

	private static final long serialVersionUID = 1L;

	private int seExistiPC;
	
	List<ProdutosConsumo> listaProdConsumo;
	private SystemSearch systemSearchSelected;
	private String systemSearchConteudo;
	private int tipoOperacao = 0; // -- 0 - Sem operacao 1 - inclusao e 2 - alteracao	
	
	private DataModelProdutoConsumo dataModelProdutoConsumo;
	
    @ManagedProperty(value="#{dadosSistemaMB}")
	private DadosSistemaMB dadosSistema;

    @ManagedProperty(value="#{funcoesMB}")
	private FuncoesMB funcoesMB;    
    
	@PostConstruct
    public void setOpcao() {
		setCampos( dadosSistema.getUsuario());  
    	this.mensaTela = "";
    	//--Apenas vai trazer os prod consumo se existir plano de contas vinculado
    	seExistiPC = 1; 
    	if ( dadosSistema.getModulo() == 3) {
    		if ( (dadosSistema.getRotina() == 1) || (dadosSistema.getRotina() == 2) ) {
    			seExistiPC = 0; 
    		}
    	}

    	dataModelProdutoConsumo = new DataModelProdutoConsumo();
 	
    }
	
	public void getOpcao() {
		getCampos();
		if ( dadosSistema.getSystemMenuRotina().getGrupoProdutoTipo() == 1 ) { //- Prod Consumo
			uniProdSelected = funcoesMB.getUniProdSelected(produtosConsumo.getUnidadesProduto());	    			
		} else if ( dadosSistema.getSystemMenuRotina().getGrupoProdutoTipo() == 2 ) { //- Serv. Consumo
			uniServSelected = funcoesMB.getUniProdSelected(produtosConsumo.getUnidadesProduto());	    			
		}
		
		tipoOperacao = 2;
		prodConsSelectedBusca = null;
	}
	
	public void getListaProdConsumoBusca () {
		
		dataModelProdutoConsumo = new DataModelProdutoConsumo();
		if ( systemSearchSelected != null ) {
			systemSearchSelected.setConteudoBusca(systemSearchConteudo);
    		systemSearchSelected.setTipoProdConsumo(dadosSistema.getSystemMenuRotina().getGrupoProdutoTipo());
			listaProdConsBusca = ProdutosConsumoDelegate.getInstance().recuperarListaProdConsumoBusca(systemSearchSelected, dadosSistema.getUsuario().getNomeConexao());
			
		}
		dataModelProdutoConsumo = new DataModelProdutoConsumo(listaProdConsBusca);

	}	

    public void salvarProdutoCons () {
    	
    	mensaTela = "";
    	
    	// -- Quando o sistema e o ERPA Flex, o plano de contas e colocado automatico
    	if ( ( dadosSistema.getQualSistemaERPA() == 2 ) && (produtosConsumo.getListaSetProdutosConsumoPC().isEmpty()) ) {
    		for ( FinanPlanoConta planoConta : PlanoContasConverter.getPo( dadosSistema.getUsuario()) ) {
    			if ( planoConta.getFinanPlanoContaTipo().getDescricao().equalsIgnoreCase("Despesas") ) {
    				produtosConsumo.getListaSetProdutosConsumoPC().add(new ProdutosConsumoPC(produtosConsumo,planoConta));
    				produtosConsumo.setFinanPlanoContaAtivo(planoConta);
    				break;
    			}
    		}
    	}
    	
    	if ( produtosConsumo.getDescricao().isEmpty() ) {
    		mensaTela = "O descriçao é obrigatório";
    	}
    	if ( mensaTela.isEmpty() ) {
    		
	    	if ( produtosConsumo.getIDProdutosConsumo() == null ) {
				
	    		// -- Gerar codigo
	    		long cod = ((new Date()).getTime()/100);
	    		produtosConsumo.setCodigo(Long.toString(cod).replace("-", ""));   	
			
	    		
	    		if ( dadosSistema.getSystemMenuRotina().getGrupoProdutoTipo() == 1 ) { //- Prod Consumo
	    			produtosConsumo.setUnidadesProduto(uniProdSelected);	    			
	    		} else if ( dadosSistema.getSystemMenuRotina().getGrupoProdutoTipo() == 2 ) { //- Serv. Consumo
	    			produtosConsumo.setUnidadesProduto(uniServSelected);	    			
	    		}	    		
	    		
	    		// dadosSistemaMB.systemMenuRotina.grupoProdutoTipo
	    		if ( dadosSistema.getSystemMenuRotina().getGrupoProdutoTipo() == 1 ) { //- Prod Consumo
		    		produtosConsumo.setTipoProduto(dadosSistema.getEmpresa().getProdutoConsumoPadrao());	    			
	    		} else if ( dadosSistema.getSystemMenuRotina().getGrupoProdutoTipo() == 2 ) { //- Serv. Consumo
		    		produtosConsumo.setTipoProduto(dadosSistema.getEmpresa().getServicoConsumoPadrao());	    			
	    		}

	    		if ( produtosConsumo.getTipoProduto() == null ) {
	    			mensaTela = "Atenção, precisa setar o tipo de produto e serviço padrão, no cadastro da empresa.";
	    		} else {
					produtosConsumo.setPessoaEmpresa(dadosSistema.getPessoasEmpresa());
					produtosConsumo.setSituacao(1);
					entityPersist = produtosConsumo;
					salvar();
					produtosConsumo = (ProdutosConsumo) entityPersisted;
					mensaTela = "Salvo com sucesso";	    			
	    		}

				
	    	} else {
	 		
				if ( !listaProdConsPC.isEmpty()) {
					produtosConsumo.getListaProdutosConsumoPC().clear();
					produtosConsumo.getListaProdutosConsumoPC().addAll(listaProdConsPC);	
				}   		
				
	    		if ( dadosSistema.getSystemMenuRotina().getGrupoProdutoTipo() == 1 ) { //- Prod Consumo
	    			produtosConsumo.setUnidadesProduto(uniProdSelected);	    			
	    		} else if ( dadosSistema.getSystemMenuRotina().getGrupoProdutoTipo() == 2 ) { //- Serv. Consumo
	    			produtosConsumo.setUnidadesProduto(uniServSelected);	    			
	    		}	    						
				entityPersist = produtosConsumo;
				update();
				mensaTela = "Alterado com sucesso";   		
	    	}
    	}
    	
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public List<ProdutosConsumo> completeProdConsumo (String query) {
    	List<ProdutosConsumo> resultado = new ArrayList<ProdutosConsumo>();
    	produtosConsumo.setDescricao(query);
    	produtosConsumo.setSeExistirPC(seExistiPC);
    	produtosConsumo.setTipoProduto(new ProdutosTipo(1,1));
	
    	ProdutosConverter.getProdConsumo(produtosConsumo, dadosSistema.getUsuario());
    	for (ProdutosConsumo prodCons : ProdutosConverter.listaProdConsumo) {    
    		if ( !query.equalsIgnoreCase("todos")) {
	    		if ( prodCons.getDescricao().toLowerCase().contains(query.toLowerCase())) {
	    			resultado.add(prodCons);   			
	    		}					
    		} else {
    			resultado.add(prodCons);
    		}

    	}
    	
        Collections.sort (resultado, new Comparator() {
            public int compare(Object o1, Object o2) {
            	ProdutosConsumo c1 = (ProdutosConsumo) o1;
            	ProdutosConsumo c2 = (ProdutosConsumo) o2;
                return c1.getDescricao().compareToIgnoreCase(c2.getDescricao());
              }
        });
        
    	seExistiPC = 1;
    	return resultado;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public List<ProdutosConsumo> completeProdConsumoSemPC (String query) {
    	List<ProdutosConsumo> resultado = new ArrayList<ProdutosConsumo>();

    	produtosConsumo.setDescricao(query);
    	produtosConsumo.setTipoProduto(new ProdutosTipo(1,1));
    	ProdutosConverter.getProdConsumo(produtosConsumo, dadosSistema.getUsuario());
    	for (ProdutosConsumo prodCons : ProdutosConverter.listaProdConsumo) {   
    		if ( !query.equalsIgnoreCase("todos")) {
	    		if ( prodCons.getDescricao().toLowerCase().contains(query.toLowerCase())) {
	    			resultado.add(prodCons);   			
	    		}					
    		} else {
    			resultado.add(prodCons);
    		}

    	}
    	
        Collections.sort (resultado, new Comparator() {
            public int compare(Object o1, Object o2) {
            	ProdutosConsumo c1 = (ProdutosConsumo) o1;
            	ProdutosConsumo c2 = (ProdutosConsumo) o2;
                return c1.getDescricao().compareToIgnoreCase(c2.getDescricao());
              }
        });
        
    	seExistiPC = 1;
    	return resultado;
    }    
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public List<ProdutosConsumo> completeServConsumo (String query) {
    	List<ProdutosConsumo> resultado = new ArrayList<ProdutosConsumo>();
    	produtosConsumo.setDescricao(query);
    	produtosConsumo.setSeExistirPC(seExistiPC);
    	produtosConsumo.setTipoProduto(new ProdutosTipo(2,1));
    	
    	ProdutosConverter.getProdConsumo(produtosConsumo, dadosSistema.getUsuario());
    	for (ProdutosConsumo prodCons : ProdutosConverter.listaProdConsumo) {    
    		if ( !query.equalsIgnoreCase("todos")) {
        		if ( prodCons.getDescricao().toLowerCase().contains(query.toLowerCase())) {
        			resultado.add(prodCons);   			
        		}	    			
    		} else {
    			resultado.add(prodCons);
    		}
				

    	}
    	
        Collections.sort (resultado, new Comparator() {
            public int compare(Object o1, Object o2) {
            	ProdutosConsumo c1 = (ProdutosConsumo) o1;
            	ProdutosConsumo c2 = (ProdutosConsumo) o2;
                return c1.getDescricao().compareToIgnoreCase(c2.getDescricao());
              }
        });    	
    	
    	seExistiPC = 1;
    	return resultado;
    }    
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public List<ProdutosConsumo> completeServConsumoSemPC (String query) {
    	List<ProdutosConsumo> resultado = new ArrayList<ProdutosConsumo>();
    	produtosConsumo.setDescricao(query);
    	produtosConsumo.setTipoProduto(new ProdutosTipo(2,1));
    	
    	ProdutosConverter.getProdConsumo(produtosConsumo, dadosSistema.getUsuario());
    	for (ProdutosConsumo prodCons : ProdutosConverter.listaProdConsumo) {    
    		if ( !query.equalsIgnoreCase("todos")) {
        		if ( prodCons.getDescricao().toLowerCase().contains(query.toLowerCase())) {
        			resultado.add(prodCons);   			
        		}	    			
    		} else {
    			resultado.add(prodCons);
    		}
				

    	}
    	
        Collections.sort (resultado, new Comparator() {
            public int compare(Object o1, Object o2) {
            	ProdutosConsumo c1 = (ProdutosConsumo) o1;
            	ProdutosConsumo c2 = (ProdutosConsumo) o2;
                return c1.getDescricao().compareToIgnoreCase(c2.getDescricao());
              }
        });    	
    	
    	seExistiPC = 1;
    	return resultado;
    }     
    
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void verificarValorBase () {
    	
    	mensaTela = "";
    	if ( !listaPrecoBase.isEmpty() ) {
	    	if ( produtosConsumo.getValorBaseTipoEsc() == 2 ) { // --Menor valor
	            Collections.sort (listaPrecoBase, new Comparator() {
	                public int compare(Object o1, Object o2) {
	                	PedidosConsumoItens c1 = (PedidosConsumoItens) o1;
	                	PedidosConsumoItens c2 = (PedidosConsumoItens) o2;
	                    return c1.getValorUnitario().compareTo(c2.getValorUnitario());
	                  }
	            });
	        	PedidosConsumoItens pedConsItem = listaPrecoBase.get(0);
	        	produtosConsumo.setValorBaseTela(pedConsItem.getValorUnitario());
	        	
	    	} else if ( produtosConsumo.getValorBaseTipoEsc() == 3 ) { // -- Ultimo valor
	            Collections.sort (listaPrecoBase, new Comparator() {
	                public int compare(Object o1, Object o2) {
	                	PedidosConsumoItens c1 = (PedidosConsumoItens) o1;
	                	PedidosConsumoItens c2 = (PedidosConsumoItens) o2;
	                    return c2.getPedidosConsumo().getDataMovimento().compareTo(c1.getPedidosConsumo().getDataMovimento());
	                  }
	            });	    		
	        	PedidosConsumoItens pedConsItem = listaPrecoBase.get(0);
	        	produtosConsumo.setValorBaseTela(pedConsItem.getValorUnitario());	    		
	    	} else {
	    		for ( PedidosConsumoItens pedConsItem : listaFornecedores) {
	    			PessoaConverter.listaPessoas.add(pedConsItem.getPedidosConsumo().getPessoaFornecedor());
	    		}
	    		if (produtosConsumo.getPessoaVBaseFornecedor() != null ) setarValorBaseForn();
	    	}
    	} else {
    	  mensaTela = "Não existe registro para preço base, o valor precisa ser fixo.";	
    	}
    	
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void setarValorBaseForn () {
    	List<PedidosConsumoItens> listaFornVBase = new ArrayList<PedidosConsumoItens>();
    	for ( PedidosConsumoItens pedConsItem : listaPrecoBase ) {
    		if ( pedConsItem.getPedidosConsumo().getPessoaFornecedor().getIDPessoa() == produtosConsumo.getPessoaVBaseFornecedor().getIDPessoa() ) {
    			listaFornVBase.add(pedConsItem);
    		}
    	}
    	if ( !listaFornVBase.isEmpty() ) {
	    	if ( produtosConsumo.getValorBaseTipoEsc() == 4 ) { // --Menor valor
	            Collections.sort (listaPrecoBase, new Comparator() {
	                public int compare(Object o1, Object o2) {
	                	PedidosConsumoItens c1 = (PedidosConsumoItens) o1;
	                	PedidosConsumoItens c2 = (PedidosConsumoItens) o2;
	                    return c1.getValorUnitario().compareTo(c2.getValorUnitario());
	                  }
	            });	    		
	            Collections.sort (listaFornVBase, new Comparator() {
	                public int compare(Object o1, Object o2) {
	                	PedidosConsumoItens c1 = (PedidosConsumoItens) o1;
	                	PedidosConsumoItens c2 = (PedidosConsumoItens) o2;
	                    return c1.getValorUnitario().compareTo(c2.getValorUnitario());
	                  }
	            });
	        	PedidosConsumoItens pedConsItem = listaFornVBase.get(0);
	        	produtosConsumo.setValorBaseTela(pedConsItem.getValorUnitario());
	        	
	    	} else if ( produtosConsumo.getValorBaseTipoEsc() == 5 ) { // -- Ultimo valor
	            Collections.sort (listaPrecoBase, new Comparator() {
	                public int compare(Object o1, Object o2) {
	                	PedidosConsumoItens c1 = (PedidosConsumoItens) o1;
	                	PedidosConsumoItens c2 = (PedidosConsumoItens) o2;
	                    return c2.getPedidosConsumo().getDataMovimento().compareTo(c1.getPedidosConsumo().getDataMovimento());
	                  }
	            });	    		
	            Collections.sort (listaFornVBase, new Comparator() {
	                public int compare(Object o1, Object o2) {
	                	PedidosConsumoItens c1 = (PedidosConsumoItens) o1;
	                	PedidosConsumoItens c2 = (PedidosConsumoItens) o2;
	                    return c2.getPedidosConsumo().getDataMovimento().compareTo(c1.getPedidosConsumo().getDataMovimento());
	                  }
	            });	    		
	        	PedidosConsumoItens pedConsItem = listaFornVBase.get(0);
	        	produtosConsumo.setValorBaseTela(pedConsItem.getValorUnitario());	    		
	    	}     	
    	}
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<Pessoas> completeFornecedorPrecoBase (String query) { 
    	List<Pessoas> resultado = new ArrayList<Pessoas>();
    	for (PedidosConsumoItens pedConsItem : listaFornecedores) {   	
    		if ( pedConsItem.getPedidosConsumo().getPessoaFornecedor().getNome().toLowerCase().contains(query.toLowerCase())) {
    			resultado.add(pedConsItem.getPedidosConsumo().getPessoaFornecedor());   			
    		}
    	}
        Collections.sort (resultado, new Comparator() {
            public int compare(Object o1, Object o2) {
            	PessoasPessoaTipo c1 = (PessoasPessoaTipo) o1;
            	PessoasPessoaTipo c2 = (PessoasPessoaTipo) o2;
                return c1.getPessoas().getNome().compareToIgnoreCase(c2.getPessoas().getNome());
              }
        });    	
    	    	
    	
    	return resultado;
    }    
    
	public void adicionarTributo () {
		
		mensaTela = "";
		if ( impTributosSelected == null ) {
			mensaTela = "Precisa escolher um dos tributos.";
		} else if ( valorAliquotaTributo.compareTo(BigDecimal.ZERO) <= 0) {
			mensaTela = "Digitar o valor do tributo.";
		} else {
			ProdutosConsumoTributos prodConsTrib = new ProdutosConsumoTributos();
			prodConsTrib.setDataInclusao(new Date());
			prodConsTrib.setPessoaUsuario(dadosSistema.getUsuario().getPessoaUsuario());
			prodConsTrib.setProdutosConsumo(produtosConsumo);
			prodConsTrib.setImpTributos(impTributosSelected);
			prodConsTrib.setSituacao(1);
			prodConsTrib.setAliquota(valorAliquotaTributo);
			prodConsTrib.setObs(obsImpTributo);
			produtosConsumo.getListaSetProdutosConsumoTributos().add(prodConsTrib);			
		}
		impTributosSelected = null;		
		obsImpTributo = "";
		valorAliquotaTributo = BigDecimal.ZERO;
			
		
	}  
	
	public void replicacaoDadosProdConsEntreEmpresas () {
		
		String retorno = ProdutosConsumoDelegate.getInstance().replicacaoDadosProdConsEntreEmpresas(174, dadosSistema.getUsuario().getNomeConexao());
		
		
	}
	
    public DadosSistemaMB getDadosSistema() {
		return dadosSistema;
	}

	public void setDadosSistema(DadosSistemaMB dadosSistema) {
		this.dadosSistema = dadosSistema;
	}
	
	public FuncoesMB getFuncoesMB() {
		return funcoesMB;
	}

	public void setFuncoesMB(FuncoesMB funcoesMB) {
		this.funcoesMB = funcoesMB;
	}

	public int getSeExistiPC() {
		return seExistiPC;
	}

	public void setSeExistiPC(int seExistiPC) {
		this.seExistiPC = seExistiPC;
	}

	public SystemSearch getSystemSearchSelected() {
		return systemSearchSelected;
	}

	public String getSystemSearchConteudo() {
		return systemSearchConteudo;
	}

	public int getTipoOperacao() {
		return tipoOperacao;
	}

	public void setSystemSearchSelected(SystemSearch systemSearchSelected) {
		this.systemSearchSelected = systemSearchSelected;
	}

	public void setSystemSearchConteudo(String systemSearchConteudo) {
		this.systemSearchConteudo = systemSearchConteudo;
	}

	public void setTipoOperacao(int tipoOperacao) {
		this.tipoOperacao = tipoOperacao;
	}

	public DataModelProdutoConsumo getDataModelProdutoConsumo() {
		return dataModelProdutoConsumo;
	}

	public void setDataModelProdutoConsumo(
			DataModelProdutoConsumo dataModelProdutoConsumo) {
		this.dataModelProdutoConsumo = dataModelProdutoConsumo;
	}


	
	
	
}
