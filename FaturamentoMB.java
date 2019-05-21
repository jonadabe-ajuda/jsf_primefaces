package br.com.ajuda.erp.mb.negocios;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.jrimum.domkee.comum.pessoa.endereco.CEP;
import org.jrimum.domkee.comum.pessoa.endereco.Endereco;
import org.jrimum.domkee.comum.pessoa.endereco.UnidadeFederativa;
import org.jrimum.domkee.financeiro.banco.febraban.Agencia;
import org.jrimum.domkee.financeiro.banco.febraban.Carteira;
import org.jrimum.domkee.financeiro.banco.febraban.Modalidade;
import org.jrimum.domkee.financeiro.banco.febraban.NumeroDaConta;
import org.jrimum.domkee.financeiro.banco.febraban.Sacado;
import org.jrimum.domkee.financeiro.banco.febraban.TipoDeCobranca;
import org.jrimum.domkee.financeiro.banco.febraban.TipoDeTitulo;
import org.jrimum.domkee.financeiro.banco.febraban.Titulo;
import org.primefaces.context.RequestContext;

import com.fincatto.documentofiscal.DFAmbiente;
import com.fincatto.documentofiscal.DFModelo;
import com.fincatto.documentofiscal.DFUnidadeFederativa;
import com.fincatto.documentofiscal.assinatura.AssinaturaDigital;
import com.fincatto.documentofiscal.nfe400.classes.NFRetornoStatus;
import com.fincatto.documentofiscal.nfe400.classes.evento.NFEnviaEventoRetorno;
import com.fincatto.documentofiscal.nfe400.classes.evento.NFEventoRetorno;
import com.fincatto.documentofiscal.nfe400.classes.evento.NFInfoEventoRetorno;
import com.fincatto.documentofiscal.nfe400.classes.evento.inutilizacao.NFRetornoEventoInutilizacao;
import com.fincatto.documentofiscal.nfe400.classes.evento.inutilizacao.NFRetornoEventoInutilizacaoDados;
import com.fincatto.documentofiscal.nfe400.classes.lote.consulta.NFLoteConsultaRetorno;
import com.fincatto.documentofiscal.nfe400.classes.lote.envio.NFLoteEnvio;
import com.fincatto.documentofiscal.nfe400.classes.lote.envio.NFLoteEnvioRetorno;
import com.fincatto.documentofiscal.nfe400.classes.lote.envio.NFLoteEnvioRetornoDados;
import com.fincatto.documentofiscal.nfe400.classes.nota.NFNota;
import com.fincatto.documentofiscal.nfe400.classes.nota.consulta.NFNotaConsultaRetorno;
import com.fincatto.documentofiscal.nfe400.classes.statusservico.consulta.NFStatusServicoConsultaRetorno;
import com.fincatto.documentofiscal.nfe400.webservices.WSFacade;
import com.lowagie.text.DocumentException;

import br.com.ajuda.erp.mb.DadosSistemaMB;
import br.com.ajuda.erp.mb.comum.FuncoesMB;
import br.com.ajuda.erp.util.Compactar;
import br.com.ajuda.erp.util.EnvioEmail;
import br.com.ajuda.erp.util.TratamentoCampos;
import br.com.ajuda.erp.boletos.bancos.BoletosBancos;
import br.com.ajuda.erp.boletos.classes.BoletoTitulo;
import br.com.ajuda.erp.boletos.command.GerarBoletoBancoBrasil;
import br.com.ajuda.erp.delegate.AquisicaoDelegate;
import br.com.ajuda.erp.delegate.FinanceiroDelegate;
import br.com.ajuda.erp.delegate.PedidosDelegate;
import br.com.ajuda.erp.delegate.PessoasDelegate;
import br.com.ajuda.erp.impressao.ImpressaoPedidoPNobre;
import br.com.ajuda.erp.negocios.VendasCommand;
import br.com.ajuda.erp.nfe_310.command.NotaFiscalComando;
import br.com.ajuda.erp.nfe_310.impressao.ImpressaoDanfe_;
import br.com.ajuda.erp.nfe_400.NFeConfigNFe;
import br.com.ajuda.erp.nfe_400.NFeExecutar;
import br.com.ajuda.erp.persistencia.FinanBancosContas;
import br.com.ajuda.erp.persistencia.FinanBancosContasCarteira;
import br.com.ajuda.erp.persistencia.FinanMovBancarioBoleto;
import br.com.ajuda.erp.persistencia.FinanMovPagRec;
import br.com.ajuda.erp.persistencia.ImpCfop;
import br.com.ajuda.erp.persistencia.NotaFiscalSaida;
import br.com.ajuda.erp.persistencia.NotaFiscalSaidaCCe;
import br.com.ajuda.erp.persistencia.NotaFiscalSaidaColetas;
import br.com.ajuda.erp.persistencia.NotaFiscalSaidaDuplicatas;
import br.com.ajuda.erp.persistencia.NotaFiscalSaidaItens;
import br.com.ajuda.erp.persistencia.NotaFiscalSaidaNumInutilizados;
import br.com.ajuda.erp.persistencia.Pedidos;
import br.com.ajuda.erp.persistencia.PedidosConsumo;
import br.com.ajuda.erp.persistencia.PedidosConsumoItens;
import br.com.ajuda.erp.persistencia.PedidosItem;
import br.com.ajuda.erp.persistencia.PedidosLotes;
import br.com.ajuda.erp.persistencia.PedidosStatus;
import br.com.ajuda.erp.persistencia.PedidosVolumes;
import br.com.ajuda.erp.persistencia.PessoasContato;
import br.com.ajuda.erp.persistencia.PessoasContatoTipo;
import br.com.ajuda.erp.persistencia.PessoasEndereco;
import br.com.ajuda.erp.persistencia.SystemSearch;
import br.com.ajuda.erp.persistencia.TipoPessoaContato;
import br.com.ajuda.erp.services.acras.AcrasEnviarNFe;

@ManagedBean
@ViewScoped
public class FaturamentoMB extends VendasCommand implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String mensaTela;
	private String mensaNFe;
	
    private String mesXml;
    private String anoXml;
    private String emailXml;
	
    private Pedidos pedido;
    private PedidosItem pedidoItem;
    private PedidosLotes pedidoLotes;	
    private PedidosConsumo pedidosConsumo;
    
    private List<NotaFiscalSaida> listaNFSaidaDispColeta;
    private List<NotaFiscalSaida> listaNFSaidaSelect;
    private List<PedidosConsumoItens> listaPedidosConsumoItens;

    private List<NotaFiscalSaida> filterNFSaida;
    
	private SystemSearch systemSearchSelected;
	private String systemSearchConteudo;
	private String devolucaoRefXML;
	private Date systemSearchData;
	private int tipoOperacao = 0; // -- 0 - Sem operacao 1 - inclusao e 2 - alteracao	    
	
	private DataModelNFColeta dataModelNFColeta;
	private DataModelNFSaida dataModelNFSaida;
	
	@ManagedProperty(value="#{dadosSistemaMB}")
	private DadosSistemaMB dadosSistema;	
	
	@ManagedProperty(value="#{funcoesMB}")
	private FuncoesMB funcoesMB;

	@PostConstruct
    public void setOpcao() {
		
		mensaTela = "";
		devolucaoRefXML = "";
		nFSaidaNumInutilizados = new NotaFiscalSaidaNumInutilizados();
		nfSaidaColetas = new NotaFiscalSaidaColetas();
		
	    pedido = new Pedidos();
	    pedidoItem = new PedidosItem();
	    pedidoLotes = new PedidosLotes();	
	    pedidosConsumo = new PedidosConsumo();
		
		setCampos(dadosSistema.getUsuario());
		
	}
	
	
	public void getNFColeta () {
		
		 dataModelNFColeta = new DataModelNFColeta();
		 List<NotaFiscalSaidaColetas> listaNFColetas = new ArrayList<NotaFiscalSaidaColetas>();
		 if ( systemSearchSelected != null ) {
			systemSearchSelected.setConteudoBusca(systemSearchConteudo);
			systemSearchSelected.setDataBuscaInicio(systemSearchData);
			listaNFColetas = PedidosDelegate.getInstance().recuperarNFSaidaColetaBusca(systemSearchSelected, dadosSistema.getUsuario().getNomeConexao());
		 }		
		 dataModelNFColeta = new DataModelNFColeta(listaNFColetas);
	 
	}
	
	public void setarNFColeta () {
		
		nfSaidaColetas =  nfSaidaColetasSelected;
		nfSaidaColetasSelected = null;
		
	}
	
	public void salvarNFColeta () {
		
		mensaTela = "";
		
		if ( nfSaidaColetas.getPessoaTransportadora() == null ) {
			mensaTela = "Escolher a transportadora";
		} else if ( nfSaidaColetas.getContato().isEmpty() ) {
			mensaTela = "Preencher o contato";
		} else if ( nfSaidaColetas.getDataAgendado() == null ) {
			mensaTela = "Escolher uma data de agendamento.";
		} else if ( nfSaidaColetas.getListaNotaFiscalColetasItens().isEmpty() ) {
			mensaTela = "Não existe nenhuma nota fiscal vinculada.";			
		} else {

			if ( nfSaidaColetas.getIDNotaFiscalSaidaColetas() == null ) {
				nfSaidaColetas.setPessoaEmpresa(dadosSistema.getPessoasEmpresa());
				nfSaidaColetas.setPessoasUsuario(dadosSistema.getUsuario().getPessoaUsuario());
				nfSaidaColetas.setDataMovimento(new Date());
				nfSaidaColetas.setSituacao(1);
				try {
					nfSaidaColetas = (NotaFiscalSaidaColetas) PedidosDelegate.getInstance().save(nfSaidaColetas, dadosSistema.getUsuario().getNomeConexao());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mensaTela = "Coleta salva com sucesso.";
			} else {
				nfSaidaColetas = (NotaFiscalSaidaColetas) PedidosDelegate.getInstance().update(nfSaidaColetas,dadosSistema.getUsuario().getNomeConexao());
				mensaTela = "Coleta alterada com sucesso.";				
			}
		}
		
 		if ( !mensaTela.isEmpty() ) {
	        FacesMessage msg = new FacesMessage("", mensaTela);
	        FacesContext.getCurrentInstance().addMessage(null, msg);
		}
 		
 		
	}
	
    
    public void getNFeStatus  () {
    	
        NFeConfigNFe config = new NFeConfigNFe(dadosSistema.getEmpresa());     
        
        try {
			NFStatusServicoConsultaRetorno retornoStatus = new WSFacade(config).consultaStatus(DFUnidadeFederativa.SP, DFModelo.NFE);
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }    

    public void getNFeUltima  () {
    	
    	 NFeConfigNFe config = new NFeConfigNFe(dadosSistema.getEmpresa());     

        // Em desenvolvimento.....   
    	
    }     
    
    public void emit () {
    	System.out.println("rere");
    }
	
	public void emitirNFeTelaFaturamento () {
		
		pedidoSelected = null;
		mensaNFe = "";
		mensaTela = "";

		NotaFiscalComando nfComando = new NotaFiscalComando();
		// -- IDTipoPessoaContato 3 - Receber XML e Danfe
		nfComando.setTipoPessoaContato(new TipoPessoaContato(3));
        nfComando.setPedidosLotes(pedidoLotes);
        pedido = pedidoLotes.getListaPedidosVolumes().get(0).getPedidosItem().getPedido();
        pedido.setListaPedItem(new ArrayList<PedidosItem>());
        pedido.getListaPedItem().addAll(PedidosDelegate.getInstance().recuperarListaPedidoItens(pedido, dadosSistema.getUsuario().getNomeConexao()));        
        
        // -- Transporte
        pedido.setTiposFrete(tpFreteSelected);
        pedido.setPessoaTransportadora(transpSelected==null?null:transpSelected.getPessoas());
        
        nfComando.setPedido(pedido);
        
        nfComando.setnInicioNFe(dadosSistema.getPessoasEmpresa().getPessoasEmpresaAtivas().getNfeNumeroHomologacao());
        nfComando.setPessoaLogin(dadosSistema.getUsuario());
        nfComando.setPessoaEmpresa(dadosSistema.getEmpresa());
        //nfComando.setnInicioNFe(dadosSistema.getPessoasEmpresa().getPessoa));
    
		if ( PessoasDelegate.getInstance().recuperarPessoasEndereco(new PessoasEndereco(pedidoLotes.getPessoa(), 1), dadosSistema.getUsuario().getNomeConexao()).isEmpty()) {
			mensaNFe = "Para emitir nota para esse cliente, você precisa adicionar o endereço.";
		} else if ( PessoasDelegate.getInstance().recuperarPessoaContato(new PessoasContato(pedidoLotes.getPessoa()), dadosSistema.getUsuario().getNomeConexao()).isEmpty() ) {
        	mensaNFe = "Para emitir nota para esse cliente, você precisa adicionar o email e telefone.";
        }        	
		
               
        if ( mensaNFe.isEmpty() ) {
        	
        	
        	if ( impCfopSelected == null ) {
        		PedidosItem pItem = pedido.getListaPedItem().get(0);
        		impCfopSelected = pItem.getImpCfop();
        	}
        	
        	NotaFiscalSaida nfSaida = null;
        	
        	try {
				nfSaida = nfComando.gerarNf(impCfopSelected);
				nfSaida.setProtocolo("");	
	        	if ( ( nfSaida.getMensaRetorno() != null)  && (nfSaida.getMensaRetorno().contains("ERRO")) ) mensaNFe = nfSaida.getMensaRetorno();					
			} catch (Exception e) {
				e.printStackTrace();
				mensaNFe = "Erro gerar nfe : " + e.getMessage();
			}

        	List<NotaFiscalSaida> listaNF = new ArrayList<NotaFiscalSaida>();
        	listaNF.add(nfSaida);
        	String retornoNFe = "";
			
            if ( mensaNFe.isEmpty() ) {

            	try {
            		
            		 NFeConfigNFe config = new NFeConfigNFe(dadosSistema.getEmpresa());     

		            // -- Envio lote
		            NFLoteEnvio loteEnvio = NFeExecutar.getNFLoteEnvio(listaNF);

		            //NFLoteEnvioRetorno retornoLote ;
					//etornoLote = new WSFacade(config).enviaLote(loteEnvio);;
					
					NFLoteEnvioRetornoDados retornoLote = new WSFacade(config).enviaLote(loteEnvio);
					

		            int iniNLote = retornoLote.toString().indexOf("<nRec>")+6;
		            int fimNLote = retornoLote.toString().indexOf("</nRec>");
		            int iniNLoteStatus = retornoLote.toString().lastIndexOf("<cStat>")+7;
		            int fimNLoteStatus = retornoLote.toString().lastIndexOf("</cStat>");            
		            String loteStatus = retornoLote.toString().substring(iniNLoteStatus, fimNLoteStatus);
		            String nLoteRec = "";
		            String nStatusNFLote = "";
		                        
		            if (( "103".equals(loteStatus)) || ("105".equals(loteStatus))) {
		                nLoteRec = retornoLote.toString().substring(iniNLote, fimNLote);
		                int nTentativaLote = 1;
		                // -- Verificando a nf na consulta do lote
		                // -- Verificar se o lote esta processando
		                
		                do {
		                    NFLoteConsultaRetorno consultaRetorno = new WSFacade(config).consultaLote(nLoteRec,DFModelo.NFE);
		                    int iniNCSatLote = consultaRetorno.toString().indexOf("<cStat>")+7;
		                    int fimNCSatLote = consultaRetorno.toString().indexOf("</cStat>");
		                    nStatusNFLote = consultaRetorno.toString().substring(iniNCSatLote, fimNCSatLote);                
		                    int iniNCSat = consultaRetorno.toString().lastIndexOf("<cStat>")+7;
		                    int fimNCSat = consultaRetorno.toString().lastIndexOf("</cStat>");
		                    String nStatusNF = consultaRetorno.toString().substring(iniNCSat, fimNCSat);
		                    int iniNCMot = consultaRetorno.toString().lastIndexOf("<xMotivo>")+9;
		                    int fimNCMot = consultaRetorno.toString().lastIndexOf("</xMotivo>");
		                    String nStatusMot = consultaRetorno.toString().substring(iniNCMot, fimNCMot);                    
		                    mensaNFe = nStatusNF + " - " + nStatusMot;
		                    if ( ("104".equals(nStatusNFLote)) && ("100".equals(nStatusNF))) {
		                        String xmlNotaAssinada = "";
		                        for (NFNota nota : loteEnvio.getNotas()) {
		                            xmlNotaAssinada = new AssinaturaDigital(config).assinarDocumento(nota.toString());
		                        }
		                        File arquivo = new File("/home/nfe_xml_saida/"+nfSaida.getIdXML()+".xml"); 
		                        FileOutputStream fos = new FileOutputStream(arquivo);  
		                        fos.write(xmlNotaAssinada.toString().getBytes());  
		                        fos.close();                    
		                        NFNotaConsultaRetorno consultaNf = new WSFacade(config).consultaNota(nfSaida.getIdXML().replace("NFe", ""));                    
		                        int iniProt = consultaNf.toString().lastIndexOf("<nProt>")+7;
		                        int fimProt = consultaNf.toString().lastIndexOf("</nProt>");
		                        nfSaida.setProtocolo(consultaNf.toString().substring(iniProt,fimProt));
		                        nfSaida.setStatus(nStatusNF);
		                        nfSaida.setRetornoEnvio(nStatusMot);
		                        mensaNFe = "";
		                    } else {
		                        if (!"105".equals(nStatusNFLote)) {
		                            for (NFRetornoStatus nfRetorno : NFRetornoStatus.values()) {
		                                if ( nfRetorno.getCodigo() == Integer.parseInt(nStatusNF)) {
		                                    System.out.println("codigo == " + nfRetorno.getCodigo());
		                                    System.out.println("motivo == " + nfRetorno.getMotivo());
		                                    mensaNFe = nfRetorno.getCodigo() + " - " + nfRetorno.getMotivo();
		                                    retornoNFe = Integer.toString(nfRetorno.getCodigo());
		                                }
		                            }
		                            
		                            
		                            
		                        } else {
		                           if ( nTentativaLote > 100 ) {
		                               nStatusNFLote = "106";
		                           } 
		                        }
		                    }
		                    nTentativaLote ++;
		                    
		                } while ("105".equals(nStatusNFLote));
		                	
		            } else {
		                System.out.println("Problemas envio de lote");
		                retornoNFe = "";
		                mensaNFe = loteStatus;
		            }
	            
				} catch (Exception e) {
					retornoNFe = "";
					e.printStackTrace();
				}			            
            
	            
            }
            
            if ( !nfSaida.getProtocolo().isEmpty() ) {
                
                String diretorio = "nfe_xml_saida";
                String arquivo = nfSaida.getIdXML()+".xml";  
                nfSaida.setRetornoLinkXml("/home/"+diretorio+"/"+arquivo);                

    
                diretorio = "nfe_danfe_saida";
                arquivo = nfSaida.getIdXML()+".pdf";
                nfSaida.setRetornoLinkDANFE("/home/"+diretorio+"/"+arquivo);                
        
                nfSaida.setPessoaEmpresa(dadosSistema.getPessoasEmpresa());
                nfSaida.setPedido(pedido);
                try {
					nfSaida = (NotaFiscalSaida) PedidosDelegate.getInstance().save(nfSaida, dadosSistema.getUsuario().getNomeConexao());
				} catch (Exception e) {
					mensaNFe = "Erro salvar nota no banco de dados";
					e.printStackTrace();
				}            
               
				if ( pedidoLotes.getIDPedidosLotes() != null ) {
        	        pedidoLotes.setNotaFiscalSaida(nfSaida);
        	        PedidosDelegate.getInstance().queryPedidoAtualizar(pedidoLotes, "LOTE", dadosSistema.getUsuario().getNomeConexao());
        	        pedidoSelected = pedido;
                } else {
                	pedido.setNotaFiscalSaida(nfSaida);
                	PedidosDelegate.getInstance().queryPedidoAtualizar(pedido, "PED_NF", dadosSistema.getUsuario().getNomeConexao());
                }    
            
                ImpressaoDanfe_ impDanfe =  new ImpressaoDanfe_();
                try {
					impDanfe.criarDanfe(nfSaida, dadosSistema.getEmpresa());
				} catch (IOException e) {
					mensaNFe = "Erro criar o banco";
					e.printStackTrace();
				}    
                
                nfSaidaSelected = nfSaida;
                enviarEmailNfe();
                
            } else {
       	
            	nfSaida.setRetornoEnvio(mensaNFe);
            	pedidoLotes.setNotaFiscalSaida(nfSaida);
            	
            }
	        
        } else {
        	NotaFiscalSaida nfSaida =  new NotaFiscalSaida();
        	nfSaida.setRetornoEnvio(mensaNFe);  
        	pedidoLotes.setNotaFiscalSaida(nfSaida);
        }
           
           
		
	}    
    
	public void emitirNFe () {

		mensaNFe = "";
		mensaTela = "";
        NotaFiscalComando nfComando = new NotaFiscalComando();
        
		// -- IDTipoPessoaContato 3 - Receber XML e Danfe
		nfComando.setTipoPessoaContato(new TipoPessoaContato(3));
        //nfComando.setPedidosLotes(pedidoLotes);
        pedido.setListaPedItem(new ArrayList<PedidosItem>(listaPedItens));
        nfComando.setPedido(pedido);
        nfComando.setnInicioNFe(dadosSistema.getPessoasEmpresa().getPessoasEmpresaAtivas().getNfeNumeroHomologacao());
        nfComando.setPessoaLogin(dadosSistema.getUsuario());
        nfComando.setPessoaEmpresa(dadosSistema.getEmpresa());
        //nfComando.setnInicioNFe(dadosSistema.getPessoasEmpresa().getPessoa));
      
		if ( PessoasDelegate.getInstance().recuperarPessoasEndereco(new PessoasEndereco(pedido.getPessoa(), 1), pessoasLoginConexao.getNomeConexao()).isEmpty()) {
			mensaNFe = "Para emitir nota para esse cliente, você precisa adicionar o endereço.";
		} else if ( PessoasDelegate.getInstance().recuperarPessoaContato(new PessoasContato(pedido.getPessoa()), dadosSistema.getUsuario().getNomeConexao()).isEmpty() ) {
        	mensaNFe = "Para emitir nota para esse cliente, você precisa adicionar o email e telefone.";
        }        	
		
               
        if ( mensaNFe.isEmpty() ) {
	  
	        	if ( impCfopSelected == null ) {
	        		PedidosItem pItem = pedido.getListaPedItem().get(0);
	        		impCfopSelected = pItem.getImpCfop();
	        	}
	        	
	        	NotaFiscalSaida nfSaida = null;
	        	
	        	try {
					nfSaida = nfComando.gerarNf(impCfopSelected);
					nfSaida.setProtocolo("");	
		        	if ( ( nfSaida.getMensaRetorno() != null)  && (nfSaida.getMensaRetorno().contains("ERRO")) ) mensaNFe = nfSaida.getMensaRetorno();					
				} catch (Exception e) {
					e.printStackTrace();
					mensaNFe = "Erro gerar nfe : " + e.getMessage();
				}

	        	List<NotaFiscalSaida> listaNF = new ArrayList<NotaFiscalSaida>();
	        	listaNF.add(nfSaida);
	        	String retornoNFe = "";
	            	        	
	            if ( mensaNFe.isEmpty() ) {

		            	
	            	try {
	            		
			            NFeConfigNFe config = new NFeConfigNFe(dadosSistema.getEmpresa());     
	
			            // -- Envio lote
			            NFLoteEnvio loteEnvio = NFeExecutar.getNFLoteEnvio(listaNF);
				
						NFLoteEnvioRetornoDados retornoLote = new WSFacade(config).enviaLote(loteEnvio);

			            int iniNLote = retornoLote.toString().indexOf("<nRec>")+6;
			            int fimNLote = retornoLote.toString().indexOf("</nRec>");
			            int iniNLoteStatus = retornoLote.toString().lastIndexOf("<cStat>")+7;
			            int fimNLoteStatus = retornoLote.toString().lastIndexOf("</cStat>");            
			            String loteStatus = retornoLote.toString().substring(iniNLoteStatus, fimNLoteStatus);
			            String nLoteRec = "";
			            String nStatusNFLote = "";
			                        
			            if (( "103".equals(loteStatus)) || ("105".equals(loteStatus))) {
			                nLoteRec = retornoLote.toString().substring(iniNLote, fimNLote);
			                int nTentativaLote = 1;
			                // -- Verificando a nf na consulta do lote
			                // -- Verificar se o lote esta processando
			                
			                do {
			                    NFLoteConsultaRetorno consultaRetorno = new WSFacade(config).consultaLote(nLoteRec,DFModelo.NFE);
			                    int iniNCSatLote = consultaRetorno.toString().indexOf("<cStat>")+7;
			                    int fimNCSatLote = consultaRetorno.toString().indexOf("</cStat>");
			                    nStatusNFLote = consultaRetorno.toString().substring(iniNCSatLote, fimNCSatLote);                
			                    int iniNCSat = consultaRetorno.toString().lastIndexOf("<cStat>")+7;
			                    int fimNCSat = consultaRetorno.toString().lastIndexOf("</cStat>");
			                    String nStatusNF = consultaRetorno.toString().substring(iniNCSat, fimNCSat);
			                    int iniNCMot = consultaRetorno.toString().lastIndexOf("<xMotivo>")+9;
			                    int fimNCMot = consultaRetorno.toString().lastIndexOf("</xMotivo>");
			                    String nStatusMot = consultaRetorno.toString().substring(iniNCMot, fimNCMot);                    
			                    mensaNFe = nStatusNF + " - " + nStatusMot;
			                    if ( ("104".equals(nStatusNFLote)) && ("100".equals(nStatusNF))) {
			                        String xmlNotaAssinada = "";
			                        for (NFNota nota : loteEnvio.getNotas()) {
			                            xmlNotaAssinada = new AssinaturaDigital(config).assinarDocumento(nota.toString());
			                        }
			                        File arquivo = new File("/home/nfe_xml_saida/"+nfSaida.getIdXML()+".xml"); 
			                        FileOutputStream fos = new FileOutputStream(arquivo);  
			                        fos.write(xmlNotaAssinada.toString().getBytes());  
			                        fos.close();                    
			                        NFNotaConsultaRetorno consultaNf = new WSFacade(config).consultaNota(nfSaida.getIdXML().replace("NFe", ""));                    
			                        int iniProt = consultaNf.toString().lastIndexOf("<nProt>")+7;
			                        int fimProt = consultaNf.toString().lastIndexOf("</nProt>");
			                        nfSaida.setProtocolo(consultaNf.toString().substring(iniProt,fimProt));
			                        nfSaida.setStatus(nStatusNF);
			                        nfSaida.setRetornoEnvio(nStatusMot);
			                        mensaNFe = "";
			                    } else {
			                        if (!"105".equals(nStatusNFLote)) {
			                            for (NFRetornoStatus nfRetorno : NFRetornoStatus.values()) {
			                                if ( nfRetorno.getCodigo() == Integer.parseInt(nStatusNF)) {
			                                    System.out.println("codigo == " + nfRetorno.getCodigo());
			                                    System.out.println("motivo == " + nfRetorno.getMotivo());
			                                    mensaNFe = nfRetorno.getCodigo() + " - " + nfRetorno.getMotivo();
			                                    retornoNFe = Integer.toString(nfRetorno.getCodigo());
			                                }
			                            }
			                            
			                            
			                            
			                        } else {
			                           if ( nTentativaLote > 100 ) {
			                               nStatusNFLote = "106";
			                           } 
			                        }
			                    }
			                    nTentativaLote ++;
			                    
			                } while ("105".equals(nStatusNFLote));
			                	
			            } else {
			                System.out.println("Problemas envio de lote");
			                retornoNFe = "";
			                mensaNFe = loteStatus;
			            }
		            
					} catch (Exception e) {
						retornoNFe = "";
						e.printStackTrace();
					}			            
	            

		            
	            }
	            
	            	            
	            
	            if ( !nfSaida.getProtocolo().isEmpty() ) {
	                
	                String diretorio = "nfe_xml_saida";
	                String arquivo = nfSaida.getIdXML()+".xml";  
	                nfSaida.setRetornoLinkXml("/home/"+diretorio+"/"+arquivo);                
	
        
	                diretorio = "nfe_danfe_saida";
	                arquivo = nfSaida.getIdXML()+".pdf";
	                nfSaida.setRetornoLinkDANFE("/home/"+diretorio+"/"+arquivo);                
	        
	                nfSaida.setPessoaEmpresa(dadosSistema.getPessoasEmpresa());
	                nfSaida.setPedido(pedido);
	                try {
						nfSaida = (NotaFiscalSaida) PedidosDelegate.getInstance().save(nfSaida, dadosSistema.getUsuario().getNomeConexao());
					} catch (Exception e) {
						mensaNFe = "Erro salvar nota no banco de dados";
						e.printStackTrace();
					}            
	               
					if ( pedidoLotes.getIDPedidosLotes() != null ) {
	        	        pedidoLotes.setNotaFiscalSaida(nfSaida);
	        	        PedidosDelegate.getInstance().queryPedidoAtualizar(pedidoLotes, "LOTE", dadosSistema.getUsuario().getNomeConexao());
	                } else {
	                	pedido.setNotaFiscalSaida(nfSaida);
	                	PedidosDelegate.getInstance().queryPedidoAtualizar(pedido, "PED_NF", dadosSistema.getUsuario().getNomeConexao());
	                }    
	                
	                ImpressaoDanfe_ impDanfe =  new ImpressaoDanfe_();
	                try {
						impDanfe.criarDanfe(nfSaida, dadosSistema.getEmpresa());
					} catch (IOException e) {
						mensaNFe = "Erro criar o banco";
						e.printStackTrace();
					}    
	                
	                nfSaidaSelected = nfSaida;
	                enviarEmailNfe();
	                
	            } else {
	            	nfSaida.setRetornoEnvio(mensaNFe);
	            	pedido.setNotaFiscalSaida(nfSaida);
	            }
	

	        
        } else {
        	NotaFiscalSaida nfSaida =  new NotaFiscalSaida();
        	nfSaida.setRetornoEnvio(mensaNFe);  
        	pedido.setNotaFiscalSaida(nfSaida);
        }
           
		
	}

	public void emitirNFeDev () {
		
		mensaNFe = "";
		mensaTela = "";
        NotaFiscalComando nfComando = new NotaFiscalComando();
        
		// -- IDTipoPessoaContato 3 - Receber XML e Danfe
		nfComando.setTipoPessoaContato(new TipoPessoaContato(3));
		if (  listaPedidosConsumoItens == null ) listaPedidosConsumoItens = new ArrayList<PedidosConsumoItens>();
        pedidosConsumo.setPedidosConsumoItens(new ArrayList<PedidosConsumoItens>(listaPedidosConsumoItens));
		
		for ( PedidosConsumoItens item : pedidosConsumo.getPedidosConsumoItens() ) {
			System.out.println("lista pedidos itens == " + item.getProdutoConsumo().getCodigo() + " - " + item.getQtdeSaldo() );
		}
		
		
        nfComando.setPedidosConsumo(pedidosConsumo);
        nfComando.setnInicioNFe(dadosSistema.getPessoasEmpresa().getPessoasEmpresaAtivas().getNfeNumeroHomologacao());
        nfComando.setPessoaLogin(dadosSistema.getUsuario());
        nfComando.setPessoaEmpresa(dadosSistema.getEmpresa());
        //nfComando.setnInicioNFe(dadosSistema.getPessoasEmpresa().getPessoa));
        
       // -- Provisorio
        impCfopSelected = new ImpCfop(325);
        impCfopSelected.setCfop("5.202");
        impCfopSelected.setDescricao("Devolução de compra para comercialização");
        devolucaoRefXML = devolucaoRefXML.replace(" ","");
        
		if ( PessoasDelegate.getInstance().recuperarPessoasEndereco(new PessoasEndereco(pedidosConsumo.getPessoaFornecedor(), 1), pessoasLoginConexao.getNomeConexao()).isEmpty()) {
			mensaNFe = "Para emitir nota para esse cliente, você precisa adicionar o endereço.";
		} else if ( PessoasDelegate.getInstance().recuperarPessoaContato(new PessoasContato(pedidosConsumo.getPessoaFornecedor()), dadosSistema.getUsuario().getNomeConexao()).isEmpty() ) {
        	mensaNFe = "Para emitir nota para esse cliente, você precisa adicionar o email e telefone.";
        } else if ( impCfopSelected == null ) {
        	mensaNFe = "Escolher o CFOP.";
        } else if ( devolucaoRefXML.length()  != 44 ) {
        	mensaNFe = "O numero do XML referente a nota do fornecedor está inválido, precisa conter 44 caracteres.";
        } else if ( listaPedidosConsumoItens.isEmpty() ) {
        	mensaNFe = "Escolher alguns dos itens para devoluçao..";
        }
		
System.out.println("mensa == " + mensaNFe);		
                      
        if ( mensaNFe.isEmpty() ) {
	        try {
	        	NotaFiscalSaida nfSaida =  nfComando.gerarNfDev(impCfopSelected);
	        	nfSaida.setProtocolo("");
                nfSaida.setDevolucaoRefIdXMl(devolucaoRefXML);
	        	List<NotaFiscalSaida> listaNF = new ArrayList<NotaFiscalSaida>();
	            listaNF.add(nfSaida);

	            NFeConfigNFe config = new NFeConfigNFe(dadosSistema.getEmpresa());     
	           
	            // -- Envio lote
	            NFLoteEnvio loteEnvio = NFeExecutar.getNFLoteEnvio(listaNF);

	            //NFLoteEnvioRetorno retornoLote ;
				//etornoLote = new WSFacade(config).enviaLote(loteEnvio);;
				
	            
				NFLoteEnvioRetornoDados retornoLote = new WSFacade(config).enviaLote(loteEnvio);
	            
	            int iniNLote = retornoLote.toString().indexOf("<nRec>")+6;
	            int fimNLote = retornoLote.toString().indexOf("</nRec>");
	            int iniNLoteStatus = retornoLote.toString().lastIndexOf("<cStat>")+7;
	            int fimNLoteStatus = retornoLote.toString().lastIndexOf("</cStat>");            
	            String loteStatus = retornoLote.toString().substring(iniNLoteStatus, fimNLoteStatus);
	            String nLoteRec = "";
	            String nStatusNFLote = "";
	                        
	            if (( "103".equals(loteStatus)) || ("105".equals(loteStatus))) {
	                nLoteRec = retornoLote.toString().substring(iniNLote, fimNLote);
	                int nTentativaLote = 1;
	                // -- Verificando a nf na consulta do lote
	                // -- Verificar se o lote esta processando
	                do {
	                    NFLoteConsultaRetorno consultaRetorno = new WSFacade(config).consultaLote(nLoteRec,DFModelo.NFE);
	                    int iniNCSatLote = consultaRetorno.toString().indexOf("<cStat>")+7;
	                    int fimNCSatLote = consultaRetorno.toString().indexOf("</cStat>");
	                    nStatusNFLote = consultaRetorno.toString().substring(iniNCSatLote, fimNCSatLote);                
	                    int iniNCSat = consultaRetorno.toString().lastIndexOf("<cStat>")+7;
	                    int fimNCSat = consultaRetorno.toString().lastIndexOf("</cStat>");
	                    String nStatusNF = consultaRetorno.toString().substring(iniNCSat, fimNCSat);
	                    int iniNCMot = consultaRetorno.toString().lastIndexOf("<xMotivo>")+9;
	                    int fimNCMot = consultaRetorno.toString().lastIndexOf("</xMotivo>");
	                    String nStatusMot = consultaRetorno.toString().substring(iniNCMot, fimNCMot);                    
	                    mensaNFe = nStatusNF + " - " + nStatusMot;
	                    if ( ("104".equals(nStatusNFLote)) && ("100".equals(nStatusNF))) {
	                        String xmlNotaAssinada = "";
	                        for (NFNota nota : loteEnvio.getNotas()) {
	                            xmlNotaAssinada = new AssinaturaDigital(config).assinarDocumento(nota.toString());
	                        }
	                        File arquivo = new File("/home/nfe_xml_saida/"+nfSaida.getIdXML()+".xml"); 
	                        FileOutputStream fos = new FileOutputStream(arquivo);  
	                        fos.write(xmlNotaAssinada.toString().getBytes());  
	                        fos.close();                    
	                        NFNotaConsultaRetorno consultaNf = new WSFacade(config).consultaNota(nfSaida.getIdXML().replace("NFe", ""));                    
	                        int iniProt = consultaNf.toString().lastIndexOf("<nProt>")+7;
	                        int fimProt = consultaNf.toString().lastIndexOf("</nProt>");
	                        nfSaida.setProtocolo(consultaNf.toString().substring(iniProt,fimProt));
	                        nfSaida.setStatus(nStatusNF);
	                        nfSaida.setRetornoEnvio(nStatusMot);
	                        mensaNFe = "";
	                    } else {
	                        if (!"105".equals(nStatusNFLote)) {
	                            for (NFRetornoStatus nfRetorno : NFRetornoStatus.values()) {
	                                if ( nfRetorno.getCodigo() == Integer.parseInt(nStatusNF)) {
	                                    System.out.println("codigo == " + nfRetorno.getCodigo());
	                                    System.out.println("motivo == " + nfRetorno.getMotivo());
	                                    mensaNFe = nfRetorno.getCodigo() + " - " + nfRetorno.getMotivo();
	                                }
	                            }                     
	                        } else {
	                           if ( nTentativaLote > 100 ) {
	                               nStatusNFLote = "106";
	                           } 
	                        }
	                    }
	                    nTentativaLote ++;
	                } while ("105".equals(nStatusNFLote));
	                
	                
	            } else {
	                System.out.println("Problemas envio de lote");
	            }
	
	            if ( !nfSaida.getProtocolo().isEmpty() ) {
	                
	                String diretorio = "nfe_xml_saida";
	                String arquivo = nfSaida.getIdXML()+".xml";  
	                nfSaida.setRetornoLinkXml("/home/"+diretorio+"/"+arquivo);                
	
	//              String bucket = "zeppini";                
	//                AWSCredentials awsCredential = new BasicAWSCredentials(accessKey, secretKey);
	//                AmazonS3 s3 = new AmazonS3Client(awsCredential);        
	//
	//                s3.putObject(new PutObjectRequest(bucket+"/"+diretorio+"/",arquivo,
	//                             new File("/home/"+diretorio+"/"+arquivo))); 
	//                
	
	//                 
	//                ArquivoS3.bucket = "peixenobre";
	//                ArquivoS3.diretorio = "nfe_xml_saida";
	//                ArquivoS3.arquivo = nfSaida.getIdXML()+".xml";            
	//                ArquivoS3.enviarArquivo();
	//                nfSaida.setRetornoLinkXml(ArquivoS3.getLink());
	//                
	                
	                diretorio = "nfe_danfe_saida";
	                arquivo = nfSaida.getIdXML()+".pdf";
	                nfSaida.setRetornoLinkDANFE("/home/"+diretorio+"/"+arquivo);                
	                nfSaida.setPessoaEmpresa(dadosSistema.getPessoasEmpresa());
	                nfSaida.setPedidosConsumo(pedidosConsumo);
	                nfSaida = (NotaFiscalSaida) PedidosDelegate.getInstance().save(nfSaida, dadosSistema.getUsuario().getNomeConexao());            
	                    
                	pedidosConsumo.setNotaFiscalSaida(nfSaida);
                	AquisicaoDelegate.getInstance().queryPedidoConsumoAtualizar(pedidosConsumo, "PED_NF", dadosSistema.getUsuario().getNomeConexao());
	                
	                ImpressaoDanfe_ impDanfe =  new ImpressaoDanfe_();
	                impDanfe.criarDanfe(nfSaida, dadosSistema.getEmpresa());    
	                
	                nfSaidaSelected = nfSaida;
	                devolucaoRefXML = "";
	                enviarEmailNfe();
	                
	            } else {
	            	nfSaida.setRetornoEnvio(mensaNFe);
	            	pedidosConsumo.setNotaFiscalSaida(nfSaida);
	            }
	
	        } catch (ParserConfigurationException ex) {
	            Logger.getLogger(FaturamentoMB.class.getName()).log(Level.SEVERE, null, ex);
	            System.out.println("problemas");
	           
	        } catch (TransformerException ex) {
	            Logger.getLogger(FaturamentoMB.class.getName()).log(Level.SEVERE, null, ex);
	            mensaTela = "Problemas em emitir NF TransformerException .";
	            System.out.println("problemas");
	            
	        } catch (NullPointerException nx) {
	            Logger.getLogger(FaturamentoMB.class.getName()).log(Level.SEVERE, null, nx);
	            mensaTela = "Problemas em emitir NF (nx) ." + nx.getMessage();
	            System.out.println("problemas");
	        } catch (Exception ex) {
	            Logger.getLogger(FaturamentoMB.class.getName()).log(Level.SEVERE, null, ex);
	            System.out.println("problemas");
	        }
	        
        } else {
        	NotaFiscalSaida nfSaida =  new NotaFiscalSaida();
        	nfSaida.setRetornoEnvio(mensaNFe);  
        	pedidosConsumo.setNotaFiscalSaida(nfSaida);
        }
        
		
	}
	
	public void emitirNFeAcras () {

		mensaNFe = "";
		mensaTela = "";
        NotaFiscalComando nfComando = new NotaFiscalComando();
        
		// -- IDTipoPessoaContato 3 - Receber XML e Danfe
		nfComando.setTipoPessoaContato(new TipoPessoaContato(3));
        //nfComando.setPedidosLotes(pedidoLotes);
        pedido.setListaPedItem(new ArrayList<PedidosItem>(listaPedItens));
        nfComando.setPedido(pedido);
        nfComando.setnInicioNFe(dadosSistema.getPessoasEmpresa().getPessoasEmpresaAtivas().getNfeNumeroHomologacao());
        nfComando.setPessoaLogin(dadosSistema.getUsuario());
        nfComando.setPessoaEmpresa(dadosSistema.getEmpresa());
        //nfComando.setnInicioNFe(dadosSistema.getPessoasEmpresa().getPessoa));
      
		if ( PessoasDelegate.getInstance().recuperarPessoasEndereco(new PessoasEndereco(pedido.getPessoa(), 1), pessoasLoginConexao.getNomeConexao()).isEmpty()) {
			mensaNFe = "Para emitir nota para esse cliente, você precisa adicionar o endereço.";
		} else if ( PessoasDelegate.getInstance().recuperarPessoaContato(new PessoasContato(pedido.getPessoa()), dadosSistema.getUsuario().getNomeConexao()).isEmpty() ) {
        	mensaNFe = "Para emitir nota para esse cliente, você precisa adicionar o email e telefone.";
        }        	
		
               
        if ( mensaNFe.isEmpty() ) {
	  
	        	if ( impCfopSelected == null ) {
	        		PedidosItem pItem = pedido.getListaPedItem().get(0);
	        		impCfopSelected = pItem.getImpCfop();
	        	}
	        	
	        	NotaFiscalSaida nfSaida = null;
	        	
	        	try {
					nfSaida = nfComando.gerarNf(impCfopSelected);
					nfSaida.setProtocolo("");	
		        	if ( ( nfSaida.getMensaRetorno() != null)  && (nfSaida.getMensaRetorno().contains("ERRO")) ) mensaNFe = nfSaida.getMensaRetorno();					
				} catch (Exception e) {
					e.printStackTrace();
					mensaNFe = "Erro gerar nfe : " + e.getMessage();
				}

	        	List<NotaFiscalSaida> listaNF = new ArrayList<NotaFiscalSaida>();
	        	listaNF.add(nfSaida);
	        	String retornoNFe = "";
	            	        	
	            if ( mensaNFe.isEmpty() ) {

		            	
	            	try {
	            		
	            		AcrasEnviarNFe enviarNFe = new AcrasEnviarNFe();
	            		enviarNFe.enviarNFe(listaNF);
	            		
		
		            
					} catch (Exception e) {
						retornoNFe = "";
						e.printStackTrace();
					}			            
	            

		            
	            }
	            
	            	            
	            
//	            if ( !nfSaida.getProtocolo().isEmpty() ) {
//	                
//	                String diretorio = "nfe_xml_saida";
//	                String arquivo = nfSaida.getIdXML()+".xml";  
//	                nfSaida.setRetornoLinkXml("/home/"+diretorio+"/"+arquivo);                
//	
//        
//	                diretorio = "nfe_danfe_saida";
//	                arquivo = nfSaida.getIdXML()+".pdf";
//	                nfSaida.setRetornoLinkDANFE("/home/"+diretorio+"/"+arquivo);                
//	        
//	                nfSaida.setPessoaEmpresa(dadosSistema.getPessoasEmpresa());
//	                nfSaida.setPedido(pedido);
//	                try {
//						nfSaida = (NotaFiscalSaida) PedidosDelegate.getInstance().save(nfSaida, dadosSistema.getUsuario().getNomeConexao());
//					} catch (Exception e) {
//						mensaNFe = "Erro salvar nota no banco de dados";
//						e.printStackTrace();
//					}            
//	               
//					if ( pedidoLotes.getIDPedidosLotes() != null ) {
//	        	        pedidoLotes.setNotaFiscalSaida(nfSaida);
//	        	        PedidosDelegate.getInstance().queryPedidoAtualizar(pedidoLotes, "LOTE", dadosSistema.getUsuario().getNomeConexao());
//	                } else {
//	                	pedido.setNotaFiscalSaida(nfSaida);
//	                	PedidosDelegate.getInstance().queryPedidoAtualizar(pedido, "PED_NF", dadosSistema.getUsuario().getNomeConexao());
//	                }    
//	                
//	                ImpressaoDanfe_ impDanfe =  new ImpressaoDanfe_();
//	                try {
//						impDanfe.criarDanfe(nfSaida, dadosSistema.getEmpresa());
//					} catch (IOException e) {
//						mensaNFe = "Erro criar o banco";
//						e.printStackTrace();
//					}    
//	                
//	                nfSaidaSelected = nfSaida;
//	                enviarEmailNfe();
//	                
//	            } else {
//	            	nfSaida.setRetornoEnvio(mensaNFe);
//	            	pedido.setNotaFiscalSaida(nfSaida);
//	            }
	

	        
        } else {
        	NotaFiscalSaida nfSaida =  new NotaFiscalSaida();
        	nfSaida.setRetornoEnvio(mensaNFe);  
        	pedido.setNotaFiscalSaida(nfSaida);
        }
           
		
	}	
	
	public void criarDanfeNFe () {
      
        try {

            ImpressaoDanfe_ impDanfe =  new ImpressaoDanfe_();
            impDanfe.criarDanfe(nfSaidaSelected, dadosSistema.getEmpresa());
            PedidosDelegate.getInstance().update(nfSaidaSelected, dadosSistema.getUsuario().getNomeConexao());

        } catch (NullPointerException nx) {
            Logger.getLogger(FaturamentoMB.class.getName()).log(Level.SEVERE, null, nx);
            mensaTela = "Problemas em emitir NF (nx) ." + nx.getMessage();
            System.out.println("problemas");
        } catch (Exception ex) {
            Logger.getLogger(FaturamentoMB.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("problemas");
        }
        
		
	}	
	
	public void exibirDanfeNFe () {
	      		
        try {

            ImpressaoDanfe_ impDanfe =  new ImpressaoDanfe_();
            impDanfe.exibirDanfe(nfSaidaSelected, dadosSistema.getEmpresa());
            PedidosDelegate.getInstance().update(nfSaidaSelected, dadosSistema.getUsuario().getNomeConexao());

        } catch (NullPointerException nx) {
            Logger.getLogger(FaturamentoMB.class.getName()).log(Level.SEVERE, null, nx);
            mensaTela = "Problemas em emitir NF (nx) ." + nx.getMessage();
            System.out.println("problemas");
        } catch (Exception ex) {
            Logger.getLogger(FaturamentoMB.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("problemas");
        }
        
		
	}	
	
    public void emitirNFeCancelar  () {
        
        mensaTela = "";
        mensaNFe = "";

         if ( motivoCancelaNFe.length() < 16 ) {
        	 mensaTela = "Justificativa do cancelamento da NFe deve ter entre 15 e 1000 caracteres."; 
         } else {
        	 
             String retornoMotStatus = "";
             String protocolo = "";      
             int retornoStatus = 0;             
             try {

            	 NFeConfigNFe config = new NFeConfigNFe(dadosSistema.getEmpresa());     
                 NFEnviaEventoRetorno retorno = new WSFacade(config).cancelaNota(nfSaidaSelected.getIdXML().replace("NFe", ""),
                		 									nfSaidaSelected.getProtocolo(),
                		 									motivoCancelaNFe);
                 

                 for ( NFEventoRetorno eventoRetorno : retorno.getEventoRetorno() ) {
                      NFInfoEventoRetorno infoEventoRet = eventoRetorno.getInfoEventoRetorno();
                      retornoMotStatus = infoEventoRet.getDescricaoEvento();
                      retornoStatus = infoEventoRet.getCodigoStatus();
                      protocolo = infoEventoRet.getNumeroProtocolo();
                      
                 }                 
             } catch (Exception ex) {
                 Logger.getLogger(FaturamentoMB.class.getName()).log(Level.SEVERE, null, ex);
                 mensaTela = "Problemas cancelamento NFe : " + ex.getMessage();
             }
             
             // 135 Evento registrado e vinculado a NF-e cancelada / 573 = Duplicidade de evento a nf ja foi cancelada
             if ( retornoStatus == 135 ) {

                 try {
                	 
                	 nfSaidaSelected.setCancelaProtocolo(protocolo);
                	 nfSaidaSelected.setCancelaData(new Date());
                	 nfSaidaSelected.setCancelaIDPessoa(dadosSistema.getUsuario().getPessoaUsuario());                	 
                	 PedidosDelegate.getInstance().update(nfSaidaSelected, dadosSistema.getUsuario().getNomeConexao());
                     mensaTela = "A NFe.: "+ nfSaidaSelected.getnNF() + " foi cancelada com sucesso.";                        
                 } catch (Exception ex) {
                     Logger.getLogger(FaturamentoMB.class.getName()).log(Level.SEVERE, null, ex);
                     mensaTela = "Problemas para salvar cancelamento NFe : " + ex.getMessage();
                 }

             } else if ( retornoStatus == 573 ) {
            	 
            	 nfSaidaSelected.setCancelaMotivo("Essa nota já foi cancelada, por outro sistema.");
            	 mensaNFe = "Verificar no link abaixo, com a chave No = "+nfSaidaSelected.getIdXML().replace("NFe", "");
             }
             
         }
                 
         
     }	

    public void getNFeInutilizar  () {
    	
    	listaFiscalSaidaNumInutilizados = PedidosDelegate.getInstance().recuperarNFSaidaNumInutilizados(new NotaFiscalSaidaNumInutilizados(), dadosSistema.getUsuario().getNomeConexao());
    	
    }

    public void emitirNFeInutilizar  () {
        
        mensaTela = "";
 
         if ( nFSaidaNumInutilizados.getJustificativa().length() < 16 ) {
        	 mensaTela = "Justificativa deve ter entre 15 e 1000 caracteres."; 
         } else if ( nFSaidaNumInutilizados.getNumeroInicial() < 1 ) {
            	 mensaTela = "Numero inicial obrigatório.";
         } else if ( nFSaidaNumInutilizados.getNumeroFinal() < 1 ) {
        	 mensaTela = "Numero final obrigatório, caso for o mesmo do inicial, digitar novamente.";            	 
         } else {
          
             try {
            	 NFeConfigNFe config = new NFeConfigNFe(dadosSistema.getEmpresa());     
                 
                 //final int anoInutilizacaoNumeracao, final String cnpjEmitente, final String serie, final String numeroInicial, final String numeroFinal, final String justificativa
                 NFRetornoEventoInutilizacao retorno = new WSFacade(config).inutilizaNota(Calendar.getInstance().get(Calendar.YEAR), 
                		                                                           dadosSistema.getPessoasEmpresa().getInscFederal(), 
                		                                                           nFSaidaNumInutilizados.getSerie(), 
                		                                                           Integer.toString(nFSaidaNumInutilizados.getNumeroInicial()), 
                		                                                           Integer.toString(nFSaidaNumInutilizados.getNumeroFinal()), 
                		                                                           nFSaidaNumInutilizados.getJustificativa(),DFModelo.NFE);
                 
                 NFRetornoEventoInutilizacaoDados eventoRetorno = retorno.getDados();
                 ZoneId defaultZoneId = ZoneId.systemDefault();
                 Date dtInutilizacao = Date.from(eventoRetorno.getDatahoraRecebimento().atZone(defaultZoneId).toInstant());
                 nFSaidaNumInutilizados.setDataInutilizacao(dtInutilizacao);
                 nFSaidaNumInutilizados.setProtocolo(eventoRetorno.getNumeroProtocolo());
                 nFSaidaNumInutilizados.setStatus(eventoRetorno.getStatus());
                 nFSaidaNumInutilizados.setStatusDescricao(eventoRetorno.getMotivo());
                 DFAmbiente nfAmbiente = eventoRetorno.getAmbiente();
                 nFSaidaNumInutilizados.setAmbiente(Integer.parseInt(nfAmbiente.getCodigo()));
                 
            	 nFSaidaNumInutilizados = (NotaFiscalSaidaNumInutilizados) PedidosDelegate.getInstance().save(nFSaidaNumInutilizados, dadosSistema.getUsuario().getNomeConexao());
                 listaFiscalSaidaNumInutilizados.add(nFSaidaNumInutilizados);
                 mensaTela = "Inutilização executada com sucesso.";
                 nFSaidaNumInutilizados = new NotaFiscalSaidaNumInutilizados();
            	 
             } catch (Exception ex) {
                 Logger.getLogger(FaturamentoMB.class.getName()).log(Level.SEVERE, null, ex);
                 mensaTela = "Problemas para inutilização NFe : " + ex.getMessage();
             }
             
            
         }
 
 		if ( !mensaTela.isEmpty() ) {
	        FacesMessage msg = new FacesMessage("", mensaTela);
	        FacesContext.getCurrentInstance().addMessage(null, msg);
		}         
         
     }	
	    
    
	// -- Carta de correcao nfe
	public void emitirNFeCCe () {
		
		mensaTela = "";
		
		if ( (descricaoCorrecaoNfe.length() < 15) || (descricaoCorrecaoNfe.length() > 1000) ) {
			mensaTela = "A quantidade de caracteres estão incorreto.";
		} else {
			
	        NFeConfigNFe config = new NFeConfigNFe(dadosSistema.getEmpresa());   
	        try {
	        	
	        	// <retEnvEvento versao="1.00"><idLote>1</idLote><tpAmb>2</tpAmb>
	        	// <verAplic>SP_EVENTOS_PL_100</verAplic><cOrgao>35</cOrgao><cStat>128</cStat>
	        	//<xMotivo>Lote de Evento Processado</xMotivo><retEvento versao="1.00">
	        	//<infEvento><tpAmb>2</tpAmb><verAplic>SP_EVENTOS_PL_100</verAplic>
	        	//<cOrgao>35</cOrgao><cStat>135</cStat>
	        	//<xMotivo>Evento registrado e vinculado a NF-e</xMotivo>
	        	//<chNFe>35150903661909000189550010000000221000000063</chNFe>
	        	//<tpEvento>110110</tpEvento><xEvento>Carta de Correção registrada</xEvento>
	        	//<nSeqEvento>1</nSeqEvento><CNPJDest>53915849000151</CNPJDest>
	        	//<emailDest>jonadabe.alves@gmail.com</emailDest>
	        	//<dhRegEvento>2015-10-29T22:32:52-02:00</dhRegEvento>
	        	//<nProt>135150005030366</nProt></infEvento></retEvento></retEnvEvento>
	        	
				NFEnviaEventoRetorno retornoCce = new WSFacade(config).corrigeNota(nfSaidaSelected.getIdXML().replace("NFe", ""),
						                                                            descricaoCorrecaoNfe,1);

				NotaFiscalSaidaCCe nfCCe = new NotaFiscalSaidaCCe();
				nfCCe.setNotaFiscalSaida(nfSaidaSelected);
				nfCCe.setDataEnvio(new Date());
				nfCCe.setPessoasUsuario(dadosSistema.getUsuario().getPessoaUsuario());
				nfCCe.setCorrecao(descricaoCorrecaoNfe);
				nfCCe.setStatus(Integer.toString(retornoCce.getCodigoStatusReposta()));
				nfCCe.setStatusDescricao(retornoCce.getMotivo());
				
	            int iniNSequencia = retornoCce.toString().indexOf("<nSeqEvento>")+12;
	            int fimNSequencia = retornoCce.toString().indexOf("</cStat>");
	            nfCCe.setSequencia(Integer.parseInt(retornoCce.toString().substring(iniNSequencia,fimNSequencia)));
				
	            int iniProt = retornoCce.toString().lastIndexOf("<nProt>")+7;
	            int fimProt = retornoCce.toString().lastIndexOf("</nProt>");	
	            nfCCe.setProtocolo(retornoCce.toString().substring(iniProt,fimProt));
	            
				nfSaidaSelected.getSetNotaFiscalSaidaCCe().add(nfCCe);
				PedidosDelegate.getInstance().update(nfSaidaSelected, dadosSistema.getUsuario().getNomeConexao()); 
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mensaTela = "Problemas envio Cc-e" + e.getMessage();
			}	
	        descricaoCorrecaoNfe = "";
		}
		
		if ( !mensaTela.isEmpty() ) {
	        FacesMessage msg = new FacesMessage("", mensaTela);
	        FacesContext.getCurrentInstance().addMessage(null, msg);
		}
        
	}
	
	public void getNfSaida () {
		mensaNFe = "";
		mensaTela = "";
		listaNfFiscalSaidas = PedidosDelegate.getInstance().recuperarNFSaida(new NotaFiscalSaida(1), dadosSistema.getUsuario().getNomeConexao());

	}

	public void setarNFeConsulta () {
		
		mensaNFe = "";
		mensaTela = "";		
		// - Itens
		nfSaidaSelected = (NotaFiscalSaida) PedidosDelegate.getInstance().recuperarID(nfSaidaSelected, dadosSistema.getUsuario().getNomeConexao());
		BigDecimal vTotUni = BigDecimal.ZERO, vtotItem = BigDecimal.ZERO, qtdeTotal = BigDecimal.ZERO;
		for ( NotaFiscalSaidaItens nfSaidaItem : nfSaidaSelected.getSetNotaFiscalSaidaItens() ) {
			vTotUni = vTotUni.add(nfSaidaItem.getvUnCom());
			vtotItem = vtotItem.add(nfSaidaItem.getvProd());
			qtdeTotal = qtdeTotal.add(nfSaidaItem.getqCom());
		}
		nfSaidaSelected.setItemVUnitTotal(vTotUni);
		nfSaidaSelected.setItemVTotal(vtotItem);
		nfSaidaSelected.setItemQtdeTotal(qtdeTotal);
		nfSaidaSelected.setItemTotal(nfSaidaSelected.getSetNotaFiscalSaidaItens().size());
		
		// -- Parcelas duplicatas
		vTotUni = BigDecimal.ZERO; 
		for ( NotaFiscalSaidaDuplicatas nfSaidaDupl : nfSaidaSelected.getSetNotaFiscalSaidaDuplicatas() ) {
			vTotUni = vTotUni.add(nfSaidaDupl.getvDup());
		}	
		nfSaidaSelected.setDuplVTotalParcelas(vTotUni);
		nfSaidaSelected.setDuplqQtdeParcelas(nfSaidaSelected.getSetNotaFiscalSaidaDuplicatas().size());
		nfSaidaSelected.setChaveAcesso(TratamentoCampos.format("####.####.####.####.####.####.####.####.####.####.####", nfSaidaSelected.getIdXML().replace("NFe", "")));
		nfSaidaSelected.setDestEndCep(TratamentoCampos.format("###-#####",nfSaidaSelected.getDestEndCep()));
		nfSaidaSelected.setDestCnpj(TratamentoCampos.format("###.###.###/####-##",nfSaidaSelected.getDestCnpj()));
	}
	
	public void enviarEmailNfe () {
		
        // fnEnviaEmailNFe( String mailMessagem,  String mailAssunto, String html, String deMail, String deNome, 
        //String paraMail, String paraNome, String arquivoDanfe, String arquivoXML){
		
		criarDanfeNFe();
		String paraEmail = "";
		if ( dadosSistema.getEmpresa().getNfeAmbiente() == 1 ) { // producao
			if ( nfSaidaSelected.getPedido().getPessoasContatoPadrao() == null ) {
		        PessoasContato pContatDest = new PessoasContato(nfSaidaSelected.getPessoaCliente());
		        for ( PessoasContato pCont : PessoasDelegate.getInstance().recuperarPessoaContato(pContatDest, dadosSistema.getUsuario().getNomeConexao())) {
		        	for ( PessoasContatoTipo pContTipo : pCont.getListaSetTipos()) {
		        		if ( pContTipo.getTipoPessoaContato().getIDTipoPessoaContato() == 3) { // -- Contato xml e danfe
		        			paraEmail = pCont.getEmailPrincipal();
		        			if ( !pCont.getEmailSecundario().isEmpty() ) 
		        				 paraEmail += ","+pCont.getEmailSecundario();
		        			break;
		        		} 
		        	}
		        } 
			} else {
    			paraEmail = nfSaidaSelected.getPedido().getPessoasContatoPadrao().getEmailPrincipal();
    			if ( !nfSaidaSelected.getPedido().getPessoasContatoPadrao().getEmailSecundario().isEmpty() ) 
    				 paraEmail += ","+nfSaidaSelected.getPedido().getPessoasContatoPadrao().getEmailSecundario();				
			}
		} else if (dadosSistema.getEmpresa().getNfeAmbiente() == 2 ) { // homologacao
			paraEmail = "jonadabe.alves@gmail.com";
		}
        if ( !paraEmail.isEmpty() ) {
	        String retorno = EnvioEmail.fnEnviaEmailNFe( 
	        						   "Referente a Nota Fiscal No.: " + nfSaidaSelected.getnNF(), 
	        		                   dadosSistema.getPessoasEmpresa().getApelido()+" - NFe Envio de XML e Danfe", "",
	        		                   "nfe@ajudainformatica.com.br", 
	        		                   dadosSistema.getPessoasEmpresa().getApelido(), 
	        		                   paraEmail, "Depto Fiscal", 
	                                   nfSaidaSelected.getRetornoLinkDANFE(), 
	                                   nfSaidaSelected.getRetornoLinkXml(),
	        		                   dadosSistema.getPessoasEmpresa().getApelido());
	        
	        if ( retorno.isEmpty() ) retorno = "Danfe e XML, da Nota Fiscal No " + nfSaidaSelected.getnNF() + " foi enviado com sucesso.";
	        FacesMessage msg = new FacesMessage("", retorno);
	        FacesContext.getCurrentInstance().addMessage(null, msg);
	        RequestContext.getCurrentInstance().execute("PF('wdMensaTelaSimples').show()");

	        nfSaidaSelected = null;
	        
        } else {
        	FacesMessage msg = new FacesMessage("", "Problemas enviar email.");
	        FacesContext.getCurrentInstance().addMessage(null, msg);
	        RequestContext.getCurrentInstance().execute("PF('wdMensaTelaSimples').show()");   	
        }
		
	}

	public void emitirBoletoNfe () {
		
		final FinanBancosContas fBancoConta = FinanceiroDelegate.getInstance().recuperarBancoContas(new FinanBancosContas(1,1), dadosSistema.getUsuario().getNomeConexao());

		if ( pedidos.getIDPedido() != null ) {

			for ( final FinanMovPagRec finanMovPagRec : FinanceiroDelegate.getInstance().recuperarListaMovPagPedCons(new FinanMovPagRec(1,pedidos), dadosSistema.getUsuario().getNomeConexao()) ) {
	
				if ( finanMovPagRec.getFormasPagamentoPagar().getDescricao().contains("Boleto") ) {
				
					BoletosBancos bancoBB = new BoletosBancos() {	
					
					
						public String getTituloNumeroParcelas() {
							return "1";
						}
						
						
						public BoletoTitulo getTitulo() {
							BoletoTitulo titulo = new BoletoTitulo();
							titulo.setNumeroDoDocumento(Integer.toString(finanMovPagRec.getPedidosVendas().getNumeroPedido()));
							titulo.setNossoNumero(Integer.toString(finanMovPagRec.getIDFinanMovPagRec()));
							titulo.setValor(finanMovPagRec.getValorMovimento());
							titulo.setDesconto(BigDecimal.ZERO);
			                titulo.setDeducao(BigDecimal.ZERO);
			                titulo.setMora(BigDecimal.ZERO);
			                titulo.setAcrecimo(BigDecimal.ZERO);
			                titulo.setValorCobrado(BigDecimal.ZERO); 
			                
			                titulo.setDataDoDocumento(finanMovPagRec.getDataMovimento());
			                titulo.setDataDoVencimento(finanMovPagRec.getDataVencimento());  
			                
			                titulo.setTipoDeDocumento(TipoDeTitulo.DM_DUPLICATA_MERCANTIL);
			                titulo.setAceite(Titulo.Aceite.N);                
			                
							return titulo;
						}
						
						
						public Sacado getSacado() {
			                Sacado sacado = new Sacado(finanMovPagRec.getPessoas().getNome(), 
			                		finanMovPagRec.getPessoas().getInscFederal());
				            PessoasEndereco pSacadoEndereco = new PessoasEndereco("1",finanMovPagRec.getPessoas());
				            pSacadoEndereco.setSituacao(1);
				            pSacadoEndereco = PessoasDelegate.getInstance().recuperarPessoasEnderecoUnico(pSacadoEndereco, dadosSistema.getUsuario().getNomeConexao());
				            Endereco enderecoSac = new Endereco();
			                enderecoSac.setUF(UnidadeFederativa.SP);
			                enderecoSac.setLocalidade(pSacadoEndereco.getIdMunicipios().getNome());
			                enderecoSac.setCep(new CEP(pSacadoEndereco.getCep()));
			                enderecoSac.setBairro(pSacadoEndereco.getBairro());
			                enderecoSac.setLogradouro(pSacadoEndereco.getEndereco());
			                enderecoSac.setNumero(pSacadoEndereco.getNumero());
			                sacado.addEndereco(enderecoSac);
							return sacado;
						}
						
						
						public String getPathTemplateBoleto() {
			
							return "/home/boletos";
						}
						
						
						public String getPathSaidaBoleto() {
							return "/home/boletos";			}
						
						
						public String getPathLogoBanco() {
							return "/home/boletos";			}
						
						
						public String getPathLogoEmpresa () {
							return "/home/pessoas_logos";			}			
						
						
						public String getPathImagem() {
							return "/home/boletos";			}
						
						
						public String getNossoNumero() {
							// TODO Auto-generated method stub
							return Integer.toString(finanMovPagRec.getIDFinanMovPagRec());
						}
						
						
						public String getNome() {
							return finanMovPagRec.getPessoaEmpresa().getApelido();
						}
						
						
						public String getLocalPagamento() {
				
							return "Pagável em qualquer banco até o vencimento. Após, atualize o boleto no site bb.com.br";
						}
						
						
						public String getInstrucaoTerceira() {
							return fBancoConta.getInstrucaoTerceira();
						}
						
						
						public String getInstrucaoSexta() {
							return fBancoConta.getInstrucaoSexta();				}
						
						
						public String getInstrucaoSegunda() {
							return fBancoConta.getInstrucaoSegunda();
						}
						
						
						public String getInstrucaoSacado() {
							return fBancoConta.getInstrucaoSacado();
						}
						
						
						public String getInstrucaoQuinta() {
							return fBancoConta.getInstrucaoQuinta();
						}
						
						
						public String getInstrucaoQuarta() {
							return fBancoConta.getInstrucaoQuarta();
						}
						
						
						public String getInstrucaoPrimeira() {
							return fBancoConta.getInstrucaoPrimeira();
						}
						
						
						public String getInscFederal() {
							return finanMovPagRec.getPessoaEmpresa().getInscFederal();
						}
						
						
						public Endereco getCedenteEndereco() {
				            PessoasEndereco pSacadoEndereco = new PessoasEndereco("1",finanMovPagRec.getPessoaEmpresa());
				            pSacadoEndereco.setSituacao(1);
				            pSacadoEndereco = PessoasDelegate.getInstance().recuperarPessoasEnderecoUnico(pSacadoEndereco, dadosSistema.getUsuario().getNomeConexao());					
			                Endereco enderecoCed = new Endereco();
			                enderecoCed.setUF(UnidadeFederativa.SP);
			                enderecoCed.setLocalidade(pSacadoEndereco.getIdMunicipios().getNome());
			                enderecoCed.setCep(new CEP(pSacadoEndereco.getCep()));
			                enderecoCed.setBairro(pSacadoEndereco.getBairro());
			                enderecoCed.setLogradouro(pSacadoEndereco.getEndereco());
			                enderecoCed.setNumero(pSacadoEndereco.getNumero());
							return enderecoCed;
						}				
						
						
						public NumeroDaConta getContaNumero() {
							
							
			                /*
			                 * Informando dados sobre a conta bancária do título.O.
			                 * Nº Convenio = 2231484
			                 * Carteira = 17
			                 * Variação = 19
			                 * B.Brasil
			                 * AG:02258-6
			                 * C/C: 17987-6 
			                 */                
		//	                ContaBancaria contaBancaria = new ContaBancaria(BancosSuportados.BANCO_DO_BRASIL.create());
		//	                //contaBancaria.setNumeroDaConta(new NumeroDaConta(17987,"6"));
		//	                contaBancaria.setNumeroDaConta(new NumeroDaConta(2231484));
		//	                contaBancaria.setCarteira(new Carteira(17, TipoDeCobranca.COM_REGISTRO));
		//	                contaBancaria.setAgencia(new Agencia(2258, "6"));
		//	                contaBancaria.setModalidade(new Modalidade(11));
		//	                
		//	                
		//	                String nossoNumero = Integer.toString(movPagRecBoleto.getIDMovPagarReceber());
		//	                int qtdeNossoNumero = nossoNumero.length();
		//	                for (int i = 0; i < qtdeNossoNumero-2; i++) {
		//	                    nossoNumero = "0"+nossoNumero;                  
		//	                }
		//
		//	                Titulo titulo = new Titulo(contaBancaria, sacado, cedente);
		//	                titulo.setNumeroDoDocumento(Integer.toString(movPagRecBoleto.getIdMovCompVenPagRec().getIdPedido().getNumeroPedido()));
		//	                titulo.setNossoNumero("2231484"+nossoNumero); // -- Primeiros numero convenio
			             					
							
							NumeroDaConta conta = new NumeroDaConta();
							for ( final FinanBancosContasCarteira fBancContaCarteira : fBancoConta.getSetFinanBancosContasCarteiras()) {
								if ( fBancContaCarteira.getPadrao() == 1 ) conta.setCodigoDaConta(fBancContaCarteira.getNumeroConvenio());
							}
							conta.setDigitoDaConta(fBancoConta.getContaCorrenteDigito());
							return conta;
						}
						
						
						public Modalidade getContaModalidade() {
							return new Modalidade(11);
						}
						
						
						public Carteira getContaCarteira() {
							int carteira = 0;
							for ( final FinanBancosContasCarteira fBancContaCarteira : fBancoConta.getSetFinanBancosContasCarteiras()) {
								if ( fBancContaCarteira.getPadrao() == 1 ) carteira = fBancContaCarteira.getTipoCarteiraRemessa();
							}					
							return new Carteira(carteira, TipoDeCobranca.COM_REGISTRO);
						}
						
						
						public Agencia getContaAgencia() {
							return new Agencia(Integer.parseInt(fBancoConta.getAgencia()), fBancoConta.getAgenciaDigito());
						}
						
						
						public String getNomeLogoEmpresa () {
							return "logoPeixeNobre.png";
						}
					
					};
					
					String nossoNumero  = GerarBoletoBancoBrasil.geraBoletoBancoBrasil(bancoBB);
					
					if ( finanMovPagRec.getMovBancarioBoleto() == null ) {
						// -- Registrando na tabela FinanMovBancarioBolero
						FinanMovBancarioBoleto finanMovBancarioBoleto = new FinanMovBancarioBoleto();
						finanMovBancarioBoleto.setPessoaEmpresa(dadosSistema.getPessoasEmpresa());
						finanMovBancarioBoleto.setFinanMovPagRec(finanMovPagRec);
						finanMovBancarioBoleto.setDataGeracaoBoleto(new Date());
						finanMovBancarioBoleto.setValorBoleto(finanMovPagRec.getValorMovimento().toString().replace(".",""));
						finanMovBancarioBoleto.setGeradoBoleto(1);
						finanMovBancarioBoleto.setNossoNumero(nossoNumero);
						finanMovBancarioBoleto = (FinanMovBancarioBoleto) FinanceiroDelegate.getInstance().save(finanMovBancarioBoleto, dadosSistema.getUsuario().getNomeConexao());
						
						finanMovPagRec.setMovBancarioBoleto(finanMovBancarioBoleto);
						FinanceiroDelegate.getInstance().queryMovBancarioAtualizar(finanMovPagRec, "BOLETO", dadosSistema.getUsuario().getNomeConexao());
					}	
				
				}
			
			}
			
			GerarBoletoBancoBrasil.geraArquivoBoletoBancoBrasil("/home/boletos","/home/boletos","BoletoBancoBrasil"+dadosSistema.getUsuario().getIDPessoaLogin());
			
		}
		
	}
	
	
	
	public void enviarEmailBoleto () {
		
		emitirBoletoNfe();
		String nomeArquivoBoletoComExteDir = "/home/boletos/BoletoBancoBrasil"+dadosSistema.getUsuario().getIDPessoaLogin()+".pdf";
		
		String paraEmail = "";
		Pedidos pedContato = pedidoLotes.getListaPedidosVolumes().get(0).getPedidosItem().getPedido();
        PessoasContato pContatDest = new PessoasContato(pedContato.getPessoa());
        for ( PessoasContato pCont : PessoasDelegate.getInstance().recuperarPessoaContato(pContatDest, dadosSistema.getUsuario().getNomeConexao())) {
        	for ( PessoasContatoTipo pContTipo : pCont.getListaSetTipos()) {
        		if ( pContTipo.getTipoPessoaContato().getIDTipoPessoaContato() == 4) { // -- Contato boleto
        			paraEmail = pCont.getEmailPrincipal();
        			if ( !pCont.getEmailSecundario().isEmpty() ) 
        				 paraEmail += ","+pCont.getEmailSecundario();
        			break;
        		} 
        	}
        } 
        
        if ( !paraEmail.isEmpty() ) {
	        boolean retorno = EnvioEmail.fnEnviaEmailBoletos( 
	        						   "Referente ao(s) boleto(s): ", 
	        		                   dadosSistema.getPessoasEmpresa().getApelido()+" - Referente ao(s) boleto(s)", "",
	        		                   "nfe@ajudainformatica.com.br", 
	        		                   dadosSistema.getPessoasEmpresa().getApelido(), 
	        		                   paraEmail, "Depto Fiscal", 
	        		                   nomeArquivoBoletoComExteDir,
	        		                   dadosSistema.getPessoasEmpresa().getApelido());
	        
	        nfSaidaSelected = null;
	        System.out.println("retorno");
        } else {
	        FacesMessage msg = new FacesMessage("", "Não existe email cadastrado, para enviar os boletos");
	        FacesContext.getCurrentInstance().addMessage(null, msg);        	
        }		
		
	}
	
	
    public  void enviarXMLEmail () {

        mensaTela = "";
        //mensaTela = Validador.validaEmail(emailXml);
        if ( mensaTela.isEmpty() ) {
            if ( mesXml.length() == 1) mesXml = "0"+mesXml;
            Compactar comp = new Compactar();
            mensaTela = comp.compactarXmls("Pacote_XML_CNPJ="+dadosSistema.getEmpresa().getPessoas().getInscFederal(), mesXml, anoXml.substring(2,4));
            if ( mensaTela.isEmpty() ) {
                String mensa = "Xml's da empresa : " + dadosSistema.getEmpresa().getPessoas().getNome() + " mes de " + mesXml + " e ano de " + anoXml ;
                String arquivo = "Pacote_XML_CNPJ="+dadosSistema.getEmpresa().getPessoas().getInscFederal()+"_"+anoXml.substring(2,4)+mesXml;
                String emailEnviar = emailXml;
                boolean retorno = EnvioEmail.fnEnviaEmailXmlZip(mensa, "Peixe Nobre - Envio de Xml's", "","nfe@ajudainformatica.com.br",
                                                             "Peixe Nobre", emailEnviar, "Depto Fiscal", arquivo+".zip");
                if ( !retorno ) {
                    mensaTela = "Problema do envio do pacote dos XML's";
                } else {
                    mensaTela = "Enviado com sucesso";
                    File arq = new File("/home/nfe_xml_saida/"+arquivo+".zip");
                    arq.delete();
                }
            }
        }
        
    }	
	
	public DadosSistemaMB getDadosSistema() {
		return dadosSistema;
	}

	public FuncoesMB getFuncoesMB() {
		return funcoesMB;
	}

	public void setDadosSistema(DadosSistemaMB dadosSistema) {
		this.dadosSistema = dadosSistema;
	}

	public void setFuncoesMB(FuncoesMB funcoesMB) {
		this.funcoesMB = funcoesMB;
	}


	public Pedidos getPedido() {
		return pedido;
	}


	public PedidosItem getPedidoItem() {
		return pedidoItem;
	}


	public PedidosLotes getPedidoLotes() {
		return pedidoLotes;
	}


	public void setPedido(Pedidos pedido) {
		this.pedido = pedido;
	}


	public void setPedidoItem(PedidosItem pedidoItem) {
		this.pedidoItem = pedidoItem;
	}


	public void setPedidoLotes(PedidosLotes pedidoLotes) {
		this.pedidoLotes = pedidoLotes;
	}


	public String getMensaNFe() {
		return mensaNFe;
	}


	public void setMensaNFe(String mensaNFe) {
		this.mensaNFe = mensaNFe;
	}


	public String getMensaTela() {
		return mensaTela;
	}


	public void setMensaTela(String mensaTela) {
		this.mensaTela = mensaTela;
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

	public DataModelNFColeta getDataModelNFColeta() {
		return dataModelNFColeta;
	}

	public void setDataModelNFColeta(DataModelNFColeta dataModelNFColeta) {
		this.dataModelNFColeta = dataModelNFColeta;
	}


	public Date getSystemSearchData() {
		return systemSearchData;
	}


	public void setSystemSearchData(Date systemSearchData) {
		this.systemSearchData = systemSearchData;
	}


	public List<NotaFiscalSaida> getListaNFSaidaDispColeta() {
		return listaNFSaidaDispColeta;
	}


	public void setListaNFSaidaDispColeta(
			List<NotaFiscalSaida> listaNFSaidaDispColeta) {
		this.listaNFSaidaDispColeta = listaNFSaidaDispColeta;
	}


	public List<NotaFiscalSaida> getListaNFSaidaSelect() {
		return listaNFSaidaSelect;
	}


	public void setListaNFSaidaSelect(List<NotaFiscalSaida> listaNFSaidaSelect) {
		this.listaNFSaidaSelect = listaNFSaidaSelect;
	}


	public DataModelNFSaida getDataModelNFSaida() {
		return dataModelNFSaida;
	}


	public void setDataModelNFSaida(DataModelNFSaida dataModelNFSaida) {
		this.dataModelNFSaida = dataModelNFSaida;
	}


	public PedidosConsumo getPedidosConsumo() {
		return pedidosConsumo;
	}


	public void setPedidosConsumo(PedidosConsumo pedidosConsumo) {
		this.pedidosConsumo = pedidosConsumo;
	}


	public List<PedidosConsumoItens> getListaPedidosConsumoItens() {
		return listaPedidosConsumoItens;
	}


	public void setListaPedidosConsumoItens(
			List<PedidosConsumoItens> listaPedidosConsumoItens) {
		this.listaPedidosConsumoItens = listaPedidosConsumoItens;
	}


	public String getDevolucaoRefXML() {
		return devolucaoRefXML;
	}


	public void setDevolucaoRefXML(String devolucaoRefXML) {
		this.devolucaoRefXML = devolucaoRefXML;
	}


	public List<NotaFiscalSaida> getFilterNFSaida() {
		return filterNFSaida;
	}


	public void setFilterNFSaida(List<NotaFiscalSaida> filterNFSaida) {
		this.filterNFSaida = filterNFSaida;
	}


	public String getMesXml() {
		return mesXml;
	}


	public void setMesXml(String mesXml) {
		this.mesXml = mesXml;
	}


	public String getAnoXml() {
		return anoXml;
	}


	public void setAnoXml(String anoXml) {
		this.anoXml = anoXml;
	}


	public String getEmailXml() {
		return emailXml;
	}


	public void setEmailXml(String emailXml) {
		this.emailXml = emailXml;
	}



	
	
	
	

}
