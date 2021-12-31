package prodotto;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

public class SoftwarehouseDAO{
	DataSource ds;
	public SoftwarehouseDAO(DataSource d) {
		ds=d;
	}

	public SoftwarehouseBean doRetriveByKey(String code) throws SQLException {
		
		return null;
	}

	
	public ArrayList<SoftwarehouseBean> allElements(String ordinamento) throws SQLException {
		if(ordinamento==null || ordinamento=="")throw new NullPointerException("ordinamento vuoto o null");
		Connection connessione=ds.getConnection();
		String query="SELECT * FROM softwarehouse ORDER BY ?;";
		
		PreparedStatement ps= connessione.prepareStatement(query);
		
		
			ps.setString(1, ordinamento);
		ArrayList<SoftwarehouseBean> a=new ArrayList<SoftwarehouseBean>();
		ResultSet risultato=ps.executeQuery();
		while(risultato.next()) {
			SoftwarehouseBean app=new SoftwarehouseBean();
			app.setNomesfh(risultato.getString("softwarehouse.nomesfh"));
			app.setLogo(risultato.getBytes("softwarehouse.logo"));
			a.add(app);
		}
		risultato.close();
		ps.close();
		connessione.close();
		
		return a;
		
		
	}
}
