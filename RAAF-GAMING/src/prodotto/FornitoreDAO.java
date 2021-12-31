package prodotto;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

public class FornitoreDAO{

	DataSource ds =null;
	
	public  FornitoreDAO(DataSource d)
	{
		ds=d;
	}

	public ArrayList<FornitoreBean> allElements(String ordinamento) throws SQLException {
		if(ordinamento==null || ordinamento=="")throw new NullPointerException("ordinamento vuoto o null");
		Connection connessione = ds.getConnection();
		String Query="SELECT * FROM fornitore ORDER BY ?;";	
		PreparedStatement ps= connessione.prepareStatement(Query);	
		ps.setString(1, ordinamento);
		ResultSet rs= ps.executeQuery();
		ArrayList<FornitoreBean> a= new ArrayList<FornitoreBean>();
		
		while(rs.next())
		{
		FornitoreBean app= new FornitoreBean();
		app.setNome(rs.getString("nome"));
		app.setIndirizzo(rs.getString("indirizzo"));
		app.setTelefono(rs.getString("telefono"));
		a.add(app);
		}
		rs.close();
		ps.close();
		connessione.close();
		return a;
	}
}
