package it.unisa.controller;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.annotation.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.sql.DataSource;

import it.unisa.model.AbbonamentoBean;
import it.unisa.model.AbbonamentoModelDAO;
import it.unisa.model.ConsoleBean;
import it.unisa.model.ConsoleModelDAO;
import it.unisa.model.DlcBean;
import it.unisa.model.DlcModelDAO;
import it.unisa.model.ProdottoBean;
import it.unisa.model.ProdottoModelDAO;
import it.unisa.model.VideogiocoBean;
import it.unisa.model.VideogiocoModelDAO;

/**
 * Servlet implementation class ServletFormProdNuovoAdmin
 */
@WebServlet("/servletformprodnuovoadmin")
@MultipartConfig(fileSizeThreshold=1024*1024*2,maxFileSize=1024*1024*5,maxRequestSize=1024*1024*5)

public class ServletFormProdNuovoAdmin extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public ServletFormProdNuovoAdmin() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession sessione = request.getSession(true);
		synchronized(sessione) {
			Object logAdminB= sessione.getAttribute("logAdmin");

			if(logAdminB!=null) {
				//l'admin potrebbe essere loggato
				
				boolean logAdmin= (Boolean) logAdminB;
				if(logAdmin==true) {
					//l'admin e' loggato e può eseguire il form
					String nomeProd =request.getParameter("nomeP");//prendo il nome del prodotto
					double prezzoProd= Double.valueOf(request.getParameter("prezzoP"));
					int scontoProd=Integer.parseInt(request.getParameter("scontoP"));
					String data = request.getParameter("dataP");
					Date dataProd= Date.valueOf(data);
					String fornitoreProd = request.getParameter("fornitoreP");
					int quantitaProd=Integer.parseInt(request.getParameter("quantitaP"));

					
					DataSource ds = (DataSource)super.getServletContext().getAttribute("DataSource");
					System.out.println("dopo datasource");
					ProdottoModelDAO ProdDAO = new ProdottoModelDAO(ds);
					
					try {
						ArrayList<ProdottoBean> prodEsistenti = ProdDAO.doRetriveByNameProd(nomeProd);
						System.out.println("dopo Arraylist");
						if(prodEsistenti.size()!=0)//se il nuovo prodotto e gia  nel database 
						{
							System.out.println("dopo prodEsistenti.size()!=0");
							String message = "prodotto gia' esistente";
							request.setAttribute("message", message);
							RequestDispatcher dispatcher = request.getRequestDispatcher(response.encodeURL("/servletgestioneadmin"));
							dispatcher.forward(request, response);
						}
						else //il nuovo prodotto non e presente nel database
						{
							System.out.println("dopo else");
							ProdottoBean prodBean = new ProdottoBean();
							ProdottoBean max =  ProdDAO.getMax();
							prodBean.setCodice_prodotto(max.getCodice_prodotto()+1);
							prodBean.setData_uscita(dataProd);
							prodBean.setFornitore(fornitoreProd);
							//prendere il file dalla request
							System.out.println("prima di request.getpart");
							Part parte = request.getPart("copertina");
							System.out.println("dopo request.getpart");
							InputStream flusso = null;
							
							System.out.println("prima di if");
							if(parte!=null)
							{
								flusso=parte.getInputStream();
								prodBean.setCopertina(flusso.readAllBytes());
							}
							System.out.println("dopo if");
							
							prodBean.setNome(nomeProd);
							prodBean.setPrezzo(prezzoProd);
							prodBean.setQuantita_fornitura(quantitaProd);
							prodBean.setSconto(scontoProd);
							Date dateatt = new Date(Calendar.getInstance().getTime().getTime());
							prodBean.setUltima_fornitura(dateatt);
							
							String scelta = request.getParameter("sceltaP");
							
							if(scelta.equals("videogioco")==true)
							{
								double dimensione  =Double.parseDouble(request.getParameter("dimensioni"));
								int pegi = Integer.parseInt(request.getParameter("pegi"));
								int ncd = Integer.parseInt(request.getParameter("ncd"));
								String chiave = request.getParameter("chiave");
								String nomesfh = request.getParameter("nomesfh");
								boolean edlim = Boolean.parseBoolean(request.getParameter("limitata"));
								
								VideogiocoBean videogicocoBean = new VideogiocoBean();
								videogicocoBean.setDimensione(dimensione);
								videogicocoBean.setEdizione_limitata(edlim);
								videogicocoBean.setPegi(pegi);
								videogicocoBean.setSoftware_house(nomesfh);
								videogicocoBean.setProdotto(prodBean.getCodice_prodotto());
								
						
								
								boolean flag=false;
								
								if(ncd>0)
								{
									flag=true;
								}
								if(flag==true)
								{
									//inserisci un gioco fisico
									videogicocoBean.setNcd(ncd);
									videogicocoBean.setVkey(null);
								}
								else
								{
									//inserisci gioco digitale
									videogicocoBean.setVkey(chiave);
									videogicocoBean.setNcd(0);
								}
								
								ProdDAO.doSave(prodBean);
								VideogiocoModelDAO vDAO = new VideogiocoModelDAO(ds);
								vDAO.doSave(videogicocoBean);
								
							}
							else if(scelta.equals("console")==true)
							{
								String specifica = request.getParameter("specifiche");
								String colore = request.getParameter("colore");
								
								ConsoleBean console = new ConsoleBean();
								console.setColore(colore);
								console.setProdotto(prodBean.getCodice_prodotto());
								console.setSpecifica(specifica);
								
								ConsoleModelDAO cDAO = new ConsoleModelDAO(ds);
								ProdDAO.doSave(prodBean);
								cDAO.doSave(console);
							}
							else if(scelta.equals("dlc")==true)
							{
								String descrizione = request.getParameter("descrizione");
								int dimensioneDlc = Integer.parseInt(request.getParameter("dimensioneDlc"));
								
								DlcBean dlcbean = new DlcBean();
								DlcModelDAO dDAO = new DlcModelDAO(ds);
								dlcbean.setProdotto(prodBean.getCodice_prodotto());
								dlcbean.setDescrizione(descrizione);
								dlcbean.setDimensione(dimensioneDlc);
							
								ProdDAO.doSave(prodBean);
								dDAO.doSave(dlcbean);
							}
							else
							{
								String codice = request.getParameter("codice");
								int durata = Integer.parseInt(request.getParameter("durata"));
								
								AbbonamentoBean abb = new AbbonamentoBean();
								AbbonamentoModelDAO abbDAO = new AbbonamentoModelDAO(ds);
								
								abb.setCodice(codice);
								abb.setDurata_abbonamento(durata);
								abb.setProdotto(prodBean.getCodice_prodotto());
								
								ProdDAO.doSave(prodBean);
								abbDAO.doSave(abb);
							}
							String messageok = "Prodotto inserito con successo!";
							request.setAttribute("messageok", messageok);
							RequestDispatcher dispatcher = request.getRequestDispatcher(response.encodeURL("/servletgestioneadmin"));
							dispatcher.forward(request, response);
						}//fine else prodotto non e presente nel database
						
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					
				}
				else{
					//l'admin non e' loggato
					String url="servletaccessoadmin";
					url= response.encodeURL(url);
					response.sendRedirect(url);
					return;
				}
			}
			else {
				//l'admin non e' loggato
				String url="servletaccessoadmin";
				url= response.encodeURL(url);
				response.sendRedirect(url);
				return;
			}
		}
	}

}
