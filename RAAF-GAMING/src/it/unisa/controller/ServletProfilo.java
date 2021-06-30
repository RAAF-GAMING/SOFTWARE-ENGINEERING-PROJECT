package it.unisa.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.json.HTTP;
import org.json.JSONObject;

import it.unisa.model.ClienteBean;
import it.unisa.model.ClienteModelDAO;




/**
 * Servlet implementation class ServletProfilo
 */
@WebServlet("/servletprofilo")
public class ServletProfilo extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public ServletProfilo() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession sessione= request.getSession(true);//controllo se esiste gia una sessione se no la creo
		synchronized(sessione){
			Object logB= sessione.getAttribute("log");//controlle se c'è il campo di log che mi indica se l'utente è loggato
			if(logB!=null) {
				//l'utente potrebbe essere loggato
				Boolean log= (Boolean) logB;
				if(log==true) {
					//l'utente è loggato e può vedere la pagina profilo
					response.setContentType("application/json");//dico che il tipo di dato che restituisce la servlet è un json
					
					BufferedReader body= request.getReader();//ottengo un riferimento al body della request(lo vede come un file)
					StringBuffer jb = new StringBuffer();
					String campo="";
					
					while((campo=body.readLine())!=null){//leggo tutto ciò che sta nel body e lo salvo in jb
						jb.append(campo);
						
					}
					
					String app= jb.toString();
					
					String decodifica= URLDecoder.decode(app, "UTF-8");//decodifichiamo la richiesta
				
					JSONObject oggettoJson= new JSONObject(decodifica);//otteniamo l'oggetto json della richiesta
					
					ClienteModelDAO cdao= new ClienteModelDAO((DataSource)super.getServletContext().getAttribute("DataSource"));//otteniamo il dao del cliente per effettuare le operazioni sul db
					
					try {
						boolean flag= false;//indica se l'utente ha modificato la password
						
						ClienteBean utente= cdao.doRetriveByKey((String)sessione.getAttribute("emailSession"));//ottengo i dati dell'utente loggato che sta visitanto la pagina profilo
						
						String passwordAtt= utente.getPassword();//ottengo la password attuale nel db dell'utente
						
						String passwordNuova= oggettoJson.getString("passwordNuova");//ottengo la nuova password che l'utente vuole impostare
						
						String ibanAtt= utente.getIban();
						String ibanNuovo= oggettoJson.getString("ibanNuovo");
						
						//creo l'utente con i dati nuovi da passare al DAO per aggiornare il cliente
						ClienteBean utenteAggiornato= new ClienteBean();
						utenteAggiornato.setEmail(utente.getEmail());
						utenteAggiornato.setNome(utente.getNome());
						utenteAggiornato.setCognome(utente.getCognome());
						utenteAggiornato.setCarta_fedelta(utente.getCarta_fedelta());
						utenteAggiornato.setData_di_nascita(utente.getData_di_nascita());
						
						if(passwordNuova!="" && passwordNuova!=null && (passwordNuova.length()>=8)) {//se ha modificato la password
							
							//codifico la nuova password in MD5 per confrontarla con quella del DB
							MessageDigest md = MessageDigest.getInstance("MD5"); 
							byte[] digest = md.digest(passwordNuova.getBytes()); 
							BigInteger big=new BigInteger(1,digest);
							String str=big.toString(16);
							while(str.length()<32) {
								str="0"+str;
							}
							
							if(!passwordAtt.equals(str)) {//se la nuova password è diversa da quella nel db
								utenteAggiornato.setPassword(passwordNuova);//setto la nuova password
								flag=true;
								
							}
							else {
								//se la password è uguale mando un errore
								JSONObject erroreJson= new JSONObject();
								erroreJson.put("errorMessage", "LA PASSWORD COINCIDE CON QUELLO GIA' IN USO");
								PrintWriter out= response.getWriter();//posso scrivere sulla response
								out.print(erroreJson.toString());
								return;
							}
						}
						else {
							//se non ha modificato la password assegno quella attuale perchè nel dao aggiorno tutti e 2 i campi
							passwordNuova = null;
							utenteAggiornato.setPassword(null);//setto la stessa password
							
						}
						
						if(ibanNuovo!="" && ibanNuovo!=null && (ibanNuovo.length()==27)) {//se ha modificato l'iban
							
							if(!ibanAtt.equals(ibanNuovo)) {//se il nuovo iban è diverso da quello del db
								utenteAggiornato.setIban(ibanNuovo);//setto il nuovo iban
								
							}
							else {
								//se l'iban è uguale mando un errore
								JSONObject erroreJson= new JSONObject();
								erroreJson.put("errorMessage", "L'IBAN COINCIDE CON QUELLO GIA' IN USO");
								PrintWriter out= response.getWriter();//posso scrivere sulla response
								out.print(erroreJson.toString());
								return;
							}
						}
						else {
							//se non ha modificato l'iban setto quello attuale
							ibanNuovo=ibanAtt;
							utenteAggiornato.setIban(ibanAtt);//setto lo stesso IBAN
							
						}
						
						
						if(utenteAggiornato.getPassword()!=null)
						{
							cdao.doUpdate(utenteAggiornato);
							
						}
						else
						{
							cdao.doUpdateOnlyIban(utenteAggiornato);
							
						}
						//aggiorno i dati dell'utente
						
						utente= cdao.doRetriveByKey((String)sessione.getAttribute("emailSession"));//riottengo l'utente aggiornato
						sessione.setAttribute("passwordSession", utente.getPassword());//aggiorno il campo password della sessione
						
						
						JSONObject rispostaJson= new JSONObject();
						rispostaJson.put("iban", utente.getIban());
						rispostaJson.put("password", flag);						
						PrintWriter out= response.getWriter();//posso scrivere sulla response
						out.print(rispostaJson.toString());
						
					
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				else {
					//esiste il campo log ma è false, quindi non è loggato
					String url="servletindex";
					url=response.encodeURL(url);
					response.sendRedirect(url);//costringo l'utente a ridirezionarsi sulla home
				}
			}
			else {
				//se non è loggato lo porto alla home
				String url="servletindex";
				url=response.encodeURL(url);
				response.sendRedirect(url);//costringo l'utente a ridirezionarsi sulla home
			}
			
			
		}
	}

}
