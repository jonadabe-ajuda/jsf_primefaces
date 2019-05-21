package br.com.ajuda.erp.mb.admCadastro;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.SelectEvent;

import br.com.ajuda.erp.converter.DepartamentoConverter;
import br.com.ajuda.erp.converter.OrcaCustoConverter;
import br.com.ajuda.erp.converter.PessoaTipoConverter;
import br.com.ajuda.erp.mb.DadosSistemaMB;
import br.com.ajuda.erp.adm.AreaDeptoCommand;
import br.com.ajuda.erp.delegate.PessoasDelegate;
import br.com.ajuda.erp.delegate.ProdutosDelegate;
import br.com.ajuda.erp.persistencia.AlcadasDescontos;
import br.com.ajuda.erp.persistencia.AlcadasDescontosPessoas;
import br.com.ajuda.erp.persistencia.Departamentos;
import br.com.ajuda.erp.persistencia.DepartamentosAlcadas;
import br.com.ajuda.erp.persistencia.DepartamentosCC;
import br.com.ajuda.erp.persistencia.DepartamentosMO;
import br.com.ajuda.erp.persistencia.FinanCentroCusto;
import br.com.ajuda.erp.persistencia.Organograma;


@ManagedBean
@ViewScoped
public class CadastroAreaDeptoMB extends AreaDeptoCommand implements Serializable{

	private static final long serialVersionUID = 1L;
	private String tituloTelaAlcada;
	
	List<Departamentos> listaDeptoComplete;		
	
    @ManagedProperty(value="#{dadosSistemaMB}")
	private DadosSistemaMB dadosSistema;	
	
    @PostConstruct
    public void setOpcao() {	
    	this.mensaTela = "";
    	tituloTelaAlcada = "";
		setCampos( dadosSistema.getUsuario()); 
    	PessoaTipoConverter.getPessoaTipo(3,dadosSistema.getUsuario());
		
    }
    
    public void salvarDepto () {
 	
    	this.mensaTela = ""; 
    	Departamentos depto = new Departamentos();
    	departamentos.setAlcadaValor(departamentos.getAlcadaValorResp());
    	departamentos.setSetAlcadas(new HashSet<DepartamentosAlcadas>());
    	if ( departamentos.getIDDepartamento() == null ) {
    		   		
    		departamentos.setDataInclusao(new Date());
        	departamentos.setResponsavel1(responsavelSelected1);
        	departamentos.setPessoaEmpresa(dadosSistema.getPessoasEmpresa());
        	if ( responsavelSelected2 != null ){
        		departamentos.setResponsavel2(responsavelSelected2);
        	}
	    	depto = getDepartamentos();
	    	depto.setSituacao(1);
	    	
	    	entityPersist = depto;
	    	salvar();
	    	departamentos = (Departamentos) entityPersisted;
	    	departamentosSelected = departamentos;
	    	this.mensaTela = "Salvo com sucesso.";
	    	DepartamentoConverter.listaDepartamentos.add(departamentosSelected);
	    	
    	} else {
    		
    		departamentos.setPessoaEmpresa(dadosSistema.getPessoasEmpresa());
         	if ( responsavelSelected1 != null ){
        		departamentos.setResponsavel1(responsavelSelected1);
        	} 
        	if ( responsavelSelected2 != null ){
        		departamentos.setResponsavel2(responsavelSelected2);
        	}         	
  		
        	if ( departamentos.getAlcadaAprovarPessoa() != null ) {
        		if ( (departamentos.getAlcadaDataInicio() == null) || (departamentos.getAlcadaDataFim() == null) ) {
        			mensaTela = "Quando é colocado um colaborador para aprovar, é obrigado colocar as data de inicio e fim.";
        		}
        	}
        	
        	if ( mensaTela.isEmpty() ) {
	 	    	entityPersist = departamentos;
		    	update();
		    	this.mensaTela = "Alterado com sucesso.";
        	}
	    	
    	}
    }

	public void addCentroCustoDepto () {
		this.mensaTela = "";
		try {
			if ( getCentroCustoSelected().getIDFinanCentroCusto() == null ) throw new NullPointerException();
			departamentos.getListaCentroCustos().add(new DepartamentosCC(departamentos,getCentroCustoSelected(),1));
			departamentos.getSetCentroCustos().add(new DepartamentosCC(departamentos,getCentroCustoSelected(),1));
			setCentroCustoSelected(null);			
		} catch (Exception e) {
			this.mensaTela = "Escolher centro de custo";
			FacesMessage msg = new FacesMessage("", mensaTela);
	        FacesContext.getCurrentInstance().addMessage(null, msg);			
		}
		
	}  

	public void remCentroCustoDepto () {
		this.mensaTela = "";
		try {
			departamentos.getListaCentroCustos().remove(deptoCCSelected);
			departamentos.getSetCentroCustos().remove(deptoCCSelected);
			deptoCCSelected = null;
		} catch (Exception e) {
			this.mensaTela = "Escolher centro de custo";
			FacesMessage msg = new FacesMessage("", mensaTela);
	        FacesContext.getCurrentInstance().addMessage(null, msg);			
		}
		
	}  
	

	
	public void adicionarMO () {
		
		this.mensaTela = "";
		if ( descricaoMO.isEmpty() ) {
			mensaTela = "Descrição MO incorreta.";
		} else if ( valorUnitMO.longValue() < 1) {
			mensaTela = "Cst. liq tem que ser maior que zero.";
		} else {
			DepartamentosMO deptoMO = new DepartamentosMO();
			deptoMO.setDescricao(descricaoMO);
			deptoMO.setFrequencia(frequenciaMO);
			deptoMO.setTempoMinimo(tempMinMO);
			deptoMO.setValorCusto(valorUnitMO);
			deptoMO.setDepartamento(departamentos);
			deptoMO.setSituacao(1);
			deptoMO.setEmpresa(dadosSistema.getPessoasEmpresa());
			departamentos.getSetMo().add(deptoMO);
			departamentos.getListaMo().add(deptoMO);
		}
		
		if ( !mensaTela.isEmpty() ) {
			FacesMessage msg = new FacesMessage("", mensaTela);
	        FacesContext.getCurrentInstance().addMessage(null, msg);			
		}
		
		descricaoMO = "";
		frequenciaMO = 0;
		tempMinMO = new BigDecimal(0);
		valorUnitMO = new BigDecimal(0);
		
	}	
	
	public void remMo () {
		departamentos.getSetMo().remove(deptoMOSelected);
		departamentos.getListaMo().remove(deptoMOSelected);
		deptoMOSelected = null;
	}
	
	public void getDepartamentoSelected (SelectEvent event) {
		if ( departamentosSelected != null ) {
			departamentos = departamentosSelected;
		}
		getCampos();
	}	
	
	public void getVerificarAlcadaAprovar (String alcada) {
		
		if ( "SOLICITACAO".equals(alcada)) {
			tituloTelaAlcada = "Permitir que outro colaborador, aprove solicitações em seu lugar por um periodo.";
		} else if ( "PEDVENDAS".equals(alcada)) {
			tituloTelaAlcada = "Permitir que outro colaborador, aprove pedidos vendas em seu lugar por um periodo.";
		}
		for ( Departamentos depto : DepartamentoConverter.getDeptoCompelte(1,dadosSistema.getUsuario()) ) {
			if ( depto.getSeAreaDepto() == 1 ) {
				if ( depto.getResponsavel1().getPessoas().getIDPessoa() == dadosSistema.getUsuario().getPessoaUsuario().getIDPessoa() ) {
					if ( (depto.getAlcadaNivel() == 2) || (depto.getAlcadaNivel() == 3) ) {
						departamentosSelected = depto;
						List<DepartamentosAlcadas> listaDeptoAlc = depto.getListaAlcadas();
						depto.getSetAlcadas().clear();
						for ( DepartamentosAlcadas deptoAlcadas :  listaDeptoAlc ) {
							if ( ("SOLICITACAO".equals(alcada)) && (deptoAlcadas.getAlcadaTipo() == 0)) {
								departamentosSelected.setAlcadaTipoEsc(0);
								depto.getSetAlcadas().add(deptoAlcadas);
							} else if ( "PEDVENDAS".equals(alcada) && (deptoAlcadas.getAlcadaTipo() == 1)) {
								depto.getSetAlcadas().add(deptoAlcadas);
								departamentosSelected.setAlcadaTipoEsc(1);
							}
						}
						if ("SOLICITACAO".equals(alcada)) {
							departamentosSelected.setAlcadaTipoEsc(0);
							departamentosSelected.setAlcadaAprovarPessoaPedidos(null);
						} else if ( "PEDVENDAS".equals(alcada)) {
							departamentosSelected.setAlcadaTipoEsc(1);
							departamentosSelected.setAlcadaAprovarPessoa(null);
						}						
						break;
					} else {
						departamentosSelected = null;
					}
				}
			}
		}
		
	}
	
	public void salvarAlcadaApr () {
		
		mensaTela = "";
		Calendar dtAtual = Calendar.getInstance();
		dtAtual.set(dtAtual.get(Calendar.YEAR), dtAtual.get(Calendar.MONTH), dtAtual.get(Calendar.DAY_OF_MONTH),0,0,0);
		Calendar dtIni = Calendar.getInstance();
		dtIni.setTime(alcadaDataInicio);
		dtIni.add(Calendar.MINUTE,24);
		Calendar dtFim = Calendar.getInstance();
		dtFim.setTime(alcadaDataFim);
		dtFim.add(Calendar.MINUTE,24);
    	if ( alcadaAprovarPessoa != null ) {
    		if ( (alcadaDataInicio == null) || (alcadaDataFim == null) ) {
    			mensaTela = "Quando é colocado um colaborador para aprovar, é obrigado colocar as data de inicio e fim.";
    		}
    	}
    	if ( alcadaAprovarPessoa == null  ) {
    		mensaTela = "Escolher o colaborador.";
    	} else if ( alcadaValor.compareTo(departamentosSelected.getAlcadaValorResp()) > 0 ) {
    		mensaTela = "O valor digitado, precisa ser menor ou igual ao valor registrado.";
    	} else if ( dtIni.before(dtAtual) ) {
    		mensaTela = "Data inicio não pode ser inferior a data atual.";
		} else if ( dtFim.before(dtAtual) ) {
			mensaTela = "Data final não pode ser inferior a data atual.";
		} else if ( dtFim.before(dtIni) ) {
			mensaTela = "Data final não pode ser inferior a data inicial.";			
		} else if ( alcadaValor.compareTo(BigDecimal.ZERO) <=0 ) {
			mensaTela = "Valor máximo tem que ser maior que zero.";    		
    	} else {
    		if ( departamentosSelected.getAlcadaTipoEsc() == 0 ) { // -- Solicitacao
	    		departamentosSelected.setAlcadaAprovarPessoa(alcadaAprovarPessoa);
	    		departamentosSelected.setAlcadaValor(alcadaValor);
	    		departamentosSelected.setAlcadaDataFim(alcadaDataFim);
	    		departamentosSelected.setAlcadaDataInicio(alcadaDataInicio);
    		} else if ( departamentosSelected.getAlcadaTipoEsc() == 1 ) { // -- Ped.Vendas 
	    		departamentosSelected.setAlcadaAprovarPessoaPedidos(alcadaAprovarPessoa);
	    		departamentosSelected.setAlcadaValorPedidos(alcadaValor);
	    		departamentosSelected.setAlcadaDataFimPedidos(alcadaDataFim);
	    		departamentosSelected.setAlcadaDataInicioPedidos(alcadaDataInicio);
    			
    		}
    		List<DepartamentosAlcadas> listaAlcadas = new ArrayList<DepartamentosAlcadas>(departamentosSelected.getSetAlcadas());
    		departamentosSelected.getSetAlcadas().clear();
    		DepartamentosAlcadas deptoAlcadas = new DepartamentosAlcadas();
    		deptoAlcadas.setDepartamento(departamentosSelected);
    		deptoAlcadas.setAlcadaDataFim(departamentosSelected.getAlcadaDataFim());
    		deptoAlcadas.setAlcadaDataInicio(departamentosSelected.getAlcadaDataInicio());
    		deptoAlcadas.setAlcadaNivel(departamentosSelected.getAlcadaNivel());
    		deptoAlcadas.setAlcadaValor(departamentosSelected.getAlcadaValor());
    		deptoAlcadas.setAlcadaAprovarPessoa(departamentosSelected.getAlcadaAprovarPessoa());
	    	departamentosSelected.getSetAlcadas().add(deptoAlcadas);
	    	departamentosSelected.getSetAlcadas().addAll(listaAlcadas);	    	
 	    	entityPersist = departamentosSelected;
	    	update();

	    	this.mensaTela = "Incluido com sucesso.";
	    	
	    	alcadaAprovarPessoa = null;
			alcadaDataInicio = new Date();
			alcadaDataFim = new Date();	    	
	    	
    	}		

		alcadaValor = BigDecimal.ZERO;
	}
	
	public void desativarAlcadaApr () {
		departamentosSelected.setAlcadaAprovarPessoa(null);
		departamentosSelected.setAlcadaValor(departamentosSelected.getAlcadaValorResp());
		departamentosSelected.setAlcadaDataFim(null);
		departamentosSelected.setAlcadaDataInicio(null);	
	    entityPersist = departamentosSelected;
    	update();
    	departamentosSelected.setAlcadaValor(BigDecimal.ZERO);
    	this.mensaTela = "Desativado com sucesso.";		
	}
	
	
	public void getAlcadasTabelaDescontos () {
		
		mensaTela = "";
		alcadasDescontos = new AlcadasDescontos();
		alcadasDescontosSelected = null;
		listaAlcadasDescontos = PessoasDelegate.getInstance().recuperarListaAlcadaTabelaDesconto(new AlcadasDescontos(1), dadosSistema.getUsuario().getNomeConexao());
		
	}
	
	public void saveAlcadasTabelaDescontos () {
		
		mensaTela = "";
		if ( alcadasDescontos.getDescricaoPolitica().length() < 5 ) {
			mensaTela = "A descrição politica está curta, no minímo 5 caracteres";
		} else if ( alcadasDescontos.getDescricaoFaixa().length() < 5 ) {
			mensaTela = "A descrição faixa está curta, no minímo 5 caracteres";
		} else if ( alcadasDescontos.getValorInicioFaixa().compareTo(BigDecimal.ZERO) <= 0 ) {
			mensaTela = "Valor ICMS inválido";
		} else if ( alcadasDescontos.getValorInicioDesconto().compareTo(BigDecimal.ZERO) < 0 ) {
			mensaTela = "Valor Desconto minímo inválido";
		} else if ( alcadasDescontos.getValorFimDesconto().compareTo(BigDecimal.ZERO) <= 0 ) {
			mensaTela = "Valor Desconto maxímo inválido";			
		} else {
			
			if ( !verificarAlcada() ) {
				if ( alcadasDescontos.getIDAlcadasDescontos() == null ) {
					alcadasDescontos.setSituacao(1);
					alcadasDescontos.setPessoaEmpresa(dadosSistema.getPessoasEmpresa());
					alcadasDescontos.setPessoaUsuario(dadosSistema.getUsuario().getPessoaUsuario());
					alcadasDescontos.setValorFimFaixa(alcadasDescontos.getValorInicioFaixa());
		 	    	entityPersist = alcadasDescontos;
			    	salvar();
			    	alcadasDescontos = (AlcadasDescontos) entityPersisted;
			    	this.mensaTela = "Incluido com sucesso.";		
			    	listaAlcadasDescontos.add(alcadasDescontos);
				} else {
					alcadasDescontos.setValorFimFaixa(alcadasDescontos.getValorInicioFaixa());
		 	    	entityPersist = alcadasDescontos;
			    	update();
			    	this.mensaTela = "Alterado com sucesso.";	
			    	listaAlcadasDescontos.set(listaAlcadasDescontos.indexOf(alcadasDescontos),alcadasDescontos);
				}
				
				alcadasDescontos = new AlcadasDescontos();
			}
		}		
		
		
		
	}
	
	public void alterarAlcadasTabelaDescontos () {
		
		alcadasDescontos = alcadasDescontosSelected;
		alcadasDescontosSelected = null;
				
	}
	
	public void removeAlcadasTabelaDescontos () {
		
		alcadasDescontosSelected.setSituacao(0);
		if (alcadasDescontosSelected.getIDAlcadasDescontos() != null) 
			update();
    	this.mensaTela = "Inativada com sucesso.";
    	listaAlcadasDescontos.remove(alcadasDescontosSelected);
		alcadasDescontosSelected = null;
				
	}	
	
	public void addAlcadasPessoa () {
		
		mensaTela = "";
		if ( responsavelSelected1 == null ) {
			mensaTela = "Escolher o colaborador(a)";
		} else {
			AlcadasDescontosPessoas alcadaPessoa = new AlcadasDescontosPessoas();
			alcadaPessoa.setPessoAprovar(responsavelSelected1.getPessoas());
			alcadaPessoa.setAlcadasDescontos(alcadasDescontos);
			alcadaPessoa.setNivel(0);
			alcadasDescontos.getListaSetAlcadasDescontosPessoas().add(alcadaPessoa);
			responsavelSelected1 = null;
		}
		
	}
	
	public void remAlcadasPessoa () {
		
		alcadasDescontos.getListaSetAlcadasDescontosPessoas().remove(alcadasDesPessoasSelected);
		alcadasDesPessoasSelected = null;
	}
	
    @SuppressWarnings({ "unchecked", "rawtypes"})
	public List<Departamentos> completeDepartamento (String query) {
    	
    	Departamentos deptoNull = new Departamentos();
    	List<Departamentos> resultado = new ArrayList<Departamentos>();
    	departamentos.setDescricao(query);
    	for (Departamentos depto : DepartamentoConverter.getDeptoCompelte(0,dadosSistema.getUsuario())) {
    		if ( "todos".equalsIgnoreCase(query)) {
    			resultado.add(depto);
    		} else {
        		if ( depto.getDescricao().toLowerCase().startsWith(query.toLowerCase())) {
        			resultado.add(depto);
        		} else {
        			if ( resultado.isEmpty() ) {
        				resultado = new ArrayList<Departamentos>();
	        			deptoNull.setIDDepartamento(null);
	        			deptoNull.setDescricao("Não existe área ou depto, que inicia com essa(s) letra(s), para verificar a lista de todos, digitar a palavra 'todos'.");
	        			resultado.add(deptoNull);
        			}
        		}    			
    		}

    	}
    	
    	if ( resultado.size() > 1) {
    		resultado.remove(deptoNull);    		
    	}
    	
        Collections.sort (resultado, new Comparator() {
            public int compare(Object o1, Object o2) {
            	Departamentos c1 = (Departamentos) o1;
            	Departamentos c2 = (Departamentos) o2;
                return c1.getDescricao().compareToIgnoreCase(c2.getDescricao());
              }
        });    	
    	
    	return resultado;
    	
    }	
    
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Departamentos> completeDepto (String query) {
    	
    	List<Departamentos> resultado = new ArrayList<Departamentos>();
    	departamentos.setDescricao(query);
    			
    	if ( !listaOrganograma.isEmpty() ) {
	    	for (Organograma area : listaOrganograma ) {
				if ((area.getAreas().equals(areaSelected))) {
					if ( area.getDepartamentos().getDescricao().toLowerCase().contains(query.toLowerCase())) {
        				resultado.add(area.getDepartamentos());   

	        		}
				}
	    	}	    	
    	} else {
        	for (Departamentos depto : DepartamentoConverter.getDeptoCompelte(0,dadosSistema.getUsuario())) {   	
        		if ( depto.getDescricao().toLowerCase().contains(query.toLowerCase())) {
        			if (depto.getSeAreaDepto() == 2) {
        				resultado.add(depto);   
        			}
        		}
        	}    		
    	}

        Collections.sort (resultado, new Comparator() {
            public int compare(Object o1, Object o2) {
            	Departamentos c1 = (Departamentos) o1;
            	Departamentos c2 = (Departamentos) o2;
                return c1.getDescricao().compareToIgnoreCase(c2.getDescricao());
              }
        });    	
    	
    	return resultado;
    	
    }     
      
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Departamentos> completeArea (String query) {
    	
    	List<Departamentos> resultado = new ArrayList<Departamentos>();
    	departamentos.setDescricao(query);
    	
    	if ( listaOrganograma.isEmpty() )listaOrganograma = PessoasDelegate.getInstance().recuperarListaOrganograma(null, dadosSistema.getUsuario().getNomeConexao());	
    	for (Departamentos depto : DepartamentoConverter.getDeptoCompelte(0,dadosSistema.getUsuario())) {   	
    		if ( depto.getDescricao().toLowerCase().contains(query.toLowerCase())) {
    			if (depto.getSeAreaDepto() == 1) resultado.add(depto);   			
    		}
    	}
    	
        Collections.sort (resultado, new Comparator() {
            public int compare(Object o1, Object o2) {
            	Departamentos c1 = (Departamentos) o1;
            	Departamentos c2 = (Departamentos) o2;
                return c1.getDescricao().compareToIgnoreCase(c2.getDescricao());
              }
        });    	
    	
    	return resultado;
    	
    }    
    
    public void loadDepto () {
    	for ( FinanCentroCusto cc : OrcaCustoConverter.listaCentroCustoConv ) {
    		listaCentroCusto.add(cc);
    	}
    	//listaCentroCusto = OrcaCustoConverter.listaCentroCustoConv;
    	carregarDepto();
    }
    
	public void carregarCC () {

		listaCentroCusto.clear();
		DepartamentosCC deptoCCFind = new DepartamentosCC();
		deptoCCFind.setDepartamento(new Departamentos(idDepto));
		List<DepartamentosCC> listaDeptoCC = PessoasDelegate.getInstance().recuperarListaDeptoCC(deptoCCFind, dadosSistema.getUsuario().getNomeConexao());
		centroCustoSelected = new FinanCentroCusto();
		for (DepartamentosCC deptoCC : listaDeptoCC ) {
			listaCentroCusto.add(deptoCC.getCentroCusto());
			centroCustoSelected = deptoCC.getCentroCusto();
		}	
		//OrcaCustoConverter.listaCentroCustoConv.addAll(listaCentroCusto);
		
	}	    
	
	public void setarArea (SelectEvent event) {
		
		if ( event != null ) {
			areaSelected = (Departamentos) event.getObject();
		}
	}

	public List<Departamentos> getListaDeptoComplete() {
		return listaDeptoComplete;
	}

	public void setListaDeptoComplete(List<Departamentos> listaDeptoComplete) {
		this.listaDeptoComplete = listaDeptoComplete;
	}

	public DadosSistemaMB getDadosSistema() {
		return dadosSistema;
	}

	public void setDadosSistema(DadosSistemaMB dadosSistema) {
		this.dadosSistema = dadosSistema;
	}

	public String getTituloTelaAlcada() {
		return tituloTelaAlcada;
	}

	public void setTituloTelaAlcada(String tituloTelaAlcada) {
		this.tituloTelaAlcada = tituloTelaAlcada;
	}


	
	
}
