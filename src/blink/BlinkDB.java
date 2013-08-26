package blink;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import sun.misc.IOUtils;
import edu.uci.ics.jung.utils.Pair;

/**
 * BlinkDB
 */
public class BlinkDB {

	private Connection _connection;

	public Connection getConnection() throws SQLException {
		if (_connection == null) {
			try {
				Class.forName("org.sqlite.JDBC").newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			_connection = DriverManager.getConnection("jdbc:sqlite:blink.db3");
		}
		return _connection;


		//      if (_connection == null) {
		//    	try {
		//			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		//		} catch (InstantiationException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		} catch (IllegalAccessException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		} catch (ClassNotFoundException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//        _connection = DriverManager.getConnection("jdbc:derby:database","","");





	}

	public BlinkDB() {
	}

	public void insertBlinks(BlinkEntry ... blinks) throws SQLException {
		ArrayList<BlinkEntry> list = new ArrayList<BlinkEntry>();
		for (int i=0;i<blinks.length;i++) {
			list.add(blinks[i]);
		}
		this.insertBlinks(list);
	}
	
	public void insertBlinks(ArrayList<BlinkEntry> blinks) throws SQLException {
		insertBlinks(blinks, 1);
	}

	public void insertBlinks(ArrayList<BlinkEntry> blinks, int connected) throws SQLException {

		if (blinks.size() == 0)
			return;

		Connection con = getConnection();
		con.setAutoCommit(false);
		Statement stmt = con.createStatement();
//		ResultSet rs = stmt.executeQuery("select max(id) from blink");
//		int maxid = rs.getInt(1);
		for (BlinkEntry b: blinks) {
			if (b == null) continue;
			stmt.addBatch("insert into blink (mapcode, colors, numedges, hg, qi, comment, connected) values ("+
					"'"+b.get_mapCode()+"',"+
					b.get_colors()+","+
					b.get_numEdges()+","+
					"'"+b.get_hg()+"',"+
					b.get_qi()+","+
					"'"+b.get_comment()+"',"+
					connected + ")");
		}
		stmt.executeBatch();
		ResultSet rs = stmt.getGeneratedKeys();
		int i=0;
		while (rs.next()) {
			blinks.get(i++).set_id(rs.getInt(1));
		}
		rs.close();
		con.commit();
		con.setAutoCommit(true);
		stmt.close();
	}


	public void insertQIs(ArrayList<QI> qis) throws SQLException, IOException {
		if (qis.size() == 0)
			return;
		Connection con = getConnection();
		con.setAutoCommit(false);
		PreparedStatement stmt = con.prepareStatement("insert into qi (rmax, hashcode, dados) values (?,?,?)");
		for (QI qi: qis) {
			stmt.setInt(1,qi.get_rmax());
			stmt.setLong(2,qi.getHashCode());
			stmt.setBytes(3, qi.getEntries());
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				qi.set_id(rs.getLong(1));
			}
			rs.close();
		}
		con.commit();
		con.setAutoCommit(true);
		stmt.close();
	}

	public void updateBlinksQI(ArrayList<BlinkEntry> blinks) throws SQLException {
		if (blinks.size() == 0)
			return;

		Connection con = getConnection();
		con.setAutoCommit(false);
		Statement stmt = con.createStatement();
		for (BlinkEntry b: blinks) {
			stmt.addBatch("update blink set qi="+b.get_qi()+" where id="+b.get_id());
		}
		stmt.executeBatch();
		con.commit();
		con.setAutoCommit(true);
		stmt.close();
	}

	public void updateBlinksCatalogNumber(ArrayList<BlinkEntry> blinks) throws SQLException {
		if (blinks.size() == 0)
			return;

		Connection con = getConnection();
		con.setAutoCommit(false);
		Statement stmt = con.createStatement();
		for (BlinkEntry b: blinks) {
			stmt.addBatch("update blink set catalogNumber="+b.getCatalogNumber()+" where id="+b.get_id());
		}
		stmt.executeBatch();
		con.commit();
		con.setAutoCommit(true);
		stmt.close();
	}

	public void insertOffsetOnBlinkIds(int offset) throws SQLException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		stmt.executeUpdate("update blink set id=id+"+offset);
		stmt.close();
	}

	public void updateBlinksIDs(ArrayList<BlinkEntry> blinks) throws SQLException {
		if (blinks.size() == 0)
			return;
		Connection con = getConnection();
		con.setAutoCommit(false);
		Statement stmt = con.createStatement();
		for (int i=0;i<blinks.size();i++) {
			BlinkEntry b = blinks.get(i);
			stmt.addBatch("update blink set id="+(i+1)+" where id="+b.get_id());
		}
		stmt.executeBatch();
		con.commit();
		con.setAutoCommit(true);
		stmt.close();
	}

	public void updateBlinksWithMinGemToNewQI(long oldQI, long newQI, long minGem) throws SQLException {
		Connection con = getConnection();
		con.setAutoCommit(false);
		Statement stmt = con.createStatement();
		stmt.executeUpdate("update blink set qi=" + newQI + " where minGem=" + minGem+" and qi="+oldQI);
		con.commit();
		con.setAutoCommit(true);
		stmt.close();
	}

	public void updateBlinksMinGem(ArrayList<BlinkEntry> blinks) throws SQLException {
		if (blinks.size() == 0)
			return;

		Connection con = getConnection();
		con.setAutoCommit(false);
		Statement stmt = con.createStatement();
		for (BlinkEntry b: blinks) {
			stmt.addBatch("update blink set mingem="+b.getMinGem()+" where id="+b.get_id());
		}
		stmt.executeBatch();
		con.commit();
		con.setAutoCommit(true);
		stmt.close();
	}

	public void updateBlinksGems(ArrayList<BlinkEntry> blinks) throws SQLException, IOException, ClassNotFoundException {
		if (blinks.size() == 0)
			return;

		Connection con = getConnection();
		con.setAutoCommit(false);
		for (BlinkEntry b: blinks) {
			PreparedStatement stmt = con.prepareStatement(
					"update blink set gem=?, path=? where id=?"
					);
			stmt.setLong(1,b.get_gem());
			byte[] is = b.get_pathByteArray();
			if (is == null) {
				stmt.setNull(2,java.sql.Types.BLOB);
			}
			else stmt.setBytes(2, is);
			stmt.setLong(3,b.get_id());
			stmt.execute();
			stmt.close();
		}
		con.commit();
		con.setAutoCommit(true);
	}

	public void updateBlinksHG(ArrayList<BlinkEntry> blinks) throws SQLException {
		if (blinks.size() == 0)
			return;

		Connection con = getConnection();
		con.setAutoCommit(false);
		Statement stmt = con.createStatement();
		for (BlinkEntry b: blinks) {
			stmt.addBatch("update blink set hg='"+b.get_hg()+"' where id="+b.get_id());
		}
		stmt.executeBatch();
		con.commit();
		con.setAutoCommit(true);
		stmt.close();
	}

	public ArrayList<BlinkEntry> getBlinks(int minEdges, int maxEdges) throws SQLException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select id,mapcode,colors,numedges,hg,qi,comment,gem,mingem,catalogNumber from blink where numedges>="+minEdges+" and numedges<="+maxEdges);
		ArrayList<BlinkEntry> result = new ArrayList<BlinkEntry>();
		while (rs.next()) {
			long id = rs.getInt(1);
			String mapCode = rs.getString(2);
			long colors = rs.getLong(3);
			int numEdges = rs.getInt(4);
			String hg = rs.getString(5);
			long qi = rs.getLong(6);
			String comment = rs.getString(7);
			long gem = rs.getLong(8);
			long mingem = rs.getLong(9);
			int catalogNumber = rs.getInt(10);
			result.add(new BlinkEntry(id,mapCode,colors,numEdges,hg,qi,gem,mingem,comment,catalogNumber));
		}
		rs.close();
		stmt.close();
		return result;
	}

	public BlinkEntry getAnyBlinkWithMinGem(long minGem) throws SQLException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select id,mapcode,colors,numedges,hg,qi,comment,gem,mingem,catalogNumber from blink where mingem="+minGem);
		BlinkEntry result = null;
		if (rs.next()) {
			long id = rs.getInt(1);
			String mapCode = rs.getString(2);
			long colors = rs.getLong(3);
			int numEdges = rs.getInt(4);
			String hg = rs.getString(5);
			long qi = rs.getLong(6);
			String comment = rs.getString(7);
			long gem = rs.getLong(8);
			long mingem = rs.getLong(9);
			int catalogNumber = rs.getInt(10);
			result = new BlinkEntry(id,mapCode,colors,numEdges,hg,qi,gem,mingem,comment,catalogNumber);
		}
		rs.close();
		stmt.close();
		return result;
	}

	public ArrayList<BlinkEntry> getBlinksByIDInterval(long minId, long maxId) throws SQLException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select id,mapcode,colors,numedges,hg,qi,comment,gem,mingem,catalogNumber from blink where id>="+minId+" and id<="+maxId);
		ArrayList<BlinkEntry> result = new ArrayList<BlinkEntry>();
		while (rs.next()) {
			long id = rs.getInt(1);
			String mapCode = rs.getString(2);
			long colors = rs.getLong(3);
			int numEdges = rs.getInt(4);
			String hg = rs.getString(5);
			long qi = rs.getLong(6);
			String comment = rs.getString(7);
			long gem = rs.getLong(8);
			long mingem = rs.getLong(9);
			int catalogNumber = rs.getInt(10);
			result.add(new BlinkEntry(id,mapCode,colors,numEdges,hg,qi,gem,mingem,comment,catalogNumber));
		}
		rs.close();
		stmt.close();
		return result;
	}


	public ArrayList<BlinkEntry> getBlinksWithoutGemAndWithNumEdges(int numEdges) throws SQLException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select id,mapcode,colors,hg,qi,comment,gem,mingem,catalogNumber from blink where numedges<="+numEdges+" and gem is null");
		ArrayList<BlinkEntry> result = new ArrayList<BlinkEntry>();
		while (rs.next()) {
			long id = rs.getInt(1);
			String mapCode = rs.getString(2);
			long colors = rs.getLong(3);
			String hg = rs.getString(4);
			long qi = rs.getLong(5);
			String comment = rs.getString(6);
			long gem = rs.getLong(7);
			long mingem = rs.getLong(8);
			int catalogNumber = rs.getInt(9);
			result.add(new BlinkEntry(id,mapCode,colors,numEdges,hg,qi,gem,mingem,comment,catalogNumber));
		}
		rs.close();
		stmt.close();
		return result;
	}

	public ArrayList<QI> getQIByIDInterval(long minId, long maxId) throws SQLException, IOException,
	ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select id,dados from qi where id>="+minId+" and id<="+maxId);
		ArrayList<QI> result = new ArrayList<QI>();
		while (rs.next()) {
			result.add(new QI(rs.getLong(1),new ByteArrayInputStream(rs.getBytes(2))));
		}
		rs.close();
		stmt.close();
		return result;
	}

	public ArrayList<QI> getQIs() throws SQLException, IOException, ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select id,dados from qi");
		ArrayList<QI> result = new ArrayList<QI>();
		while (rs.next()) {
			result.add(new QI(rs.getLong(1),new ByteArrayInputStream(rs.getBytes(2))));
		}
		rs.close();
		stmt.close();
		return result;
	}

	public ArrayList<BlinkEntry> getBlinksByIDs(long ... ids) throws SQLException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();


		StringBuffer st = new StringBuffer();
		boolean first = true;
		for (long k: ids) {
			if (!first) {
				st.append(",");
			}
			st.append(k);
			first=false;
		}

		ResultSet rs = stmt.executeQuery("select id,mapcode,colors,numedges,hg,qi,comment,gem,mingem,catalogNumber from blink where id in ("+st.toString()+")");
		ArrayList<BlinkEntry> result = new ArrayList<BlinkEntry>();
		while (rs.next()) {
			long id = rs.getInt(1);
			String mapCode = rs.getString(2);
			long colors = rs.getLong(3);
			int numEdges = rs.getInt(4);
			String hg = rs.getString(5);
			long qi = rs.getLong(6);
			String comment = rs.getString(7);
			long gem = rs.getLong(8);
			long mingem = rs.getLong(9);
			int catalogNumber = rs.getInt(10);
			result.add(new BlinkEntry(id,mapCode,colors,numEdges,hg,qi,gem,mingem,comment,catalogNumber));
		}
		rs.close();
		stmt.close();
		return result;
	}

	public ArrayList<BlinkEntry> getBlinksByIDs(ArrayList<Long> ids) throws SQLException {

		if (ids.size() == 0)
			return new ArrayList<BlinkEntry>();

		Connection con = getConnection();
		Statement stmt = con.createStatement();

		StringBuffer st = new StringBuffer();
		boolean first = true;
		for (long k: ids) {
			if (!first) {
				st.append(",");
			}
			st.append(k);
			first=false;
		}

		ResultSet rs = stmt.executeQuery("select id,mapcode,colors,numedges,hg,qi,comment,gem,mingem,catalogNumber from blink where id in ("+st.toString()+")");
		ArrayList<BlinkEntry> result = new ArrayList<BlinkEntry>();
		while (rs.next()) {
			long id = rs.getInt(1);
			String mapCode = rs.getString(2);
			long colors = rs.getLong(3);
			int numEdges = rs.getInt(4);
			String hg = rs.getString(5);
			long qi = rs.getLong(6);
			String comment = rs.getString(7);
			long gem = rs.getLong(8);
			long mingem = rs.getLong(9);
			int catalogNumber = rs.getInt(10);
			result.add(new BlinkEntry(id,mapCode,colors,numEdges,hg,qi,gem,mingem,comment,catalogNumber));
		}
		rs.close();
		stmt.close();
		return result;
	}


	public long[] getMinMaxBlinkIDs() throws SQLException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select min(id),max(id) from blink");
		long[] result = {-1L, -1L};
		if (rs.next()) {
			result[0]=rs.getLong(1);
			result[1]=rs.getLong(2);
		}
		rs.close();
		stmt.close();
		return result;
	}

	public long[] getMinMaxGemIDs() throws SQLException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select min(id),max(id) from gem");
		long[] result = {-1L, -1L};
		if (rs.next()) {
			result[0]=rs.getLong(1);
			result[1]=rs.getLong(2);
		}
		rs.close();
		stmt.close();
		return result;
	}

	public ArrayList<GemEntry> getGemsByIDInterval(long minId, long maxId) throws SQLException, IOException,
	ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select id,code,handle,tsclasssize,catalogNumber,status,minGem,tsRepresentant from gem where id>="+minId+" and id<="+maxId);
		ArrayList<GemEntry> result = new ArrayList<GemEntry>();
		while (rs.next()) {
			result.add(new GemEntry(rs.getLong(1),new ByteArrayInputStream(rs.getBytes(2)),
					rs.getInt(3),rs.getInt(4),rs.getInt(5),
					rs.getInt(6),rs.getLong(7),rs.getBoolean(8)));
		}
		rs.close();
		stmt.close();
		return result;
	}

	public ArrayList<GemEntry> getSpaceDefiningGemsFromCatalog28() throws SQLException, IOException,
	ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select id,code,handle,tsclasssize,catalogNumber,status,minGem,tsrepresentant from gem where numvert<=28 and id=mingem");
		ArrayList<GemEntry> result = new ArrayList<GemEntry>();
		while (rs.next()) {
			result.add(new GemEntry(rs.getLong(1),new ByteArrayInputStream(rs.getBytes(2)),rs.getInt(3),rs.getInt(4),rs.getInt(5),rs.getInt(6),rs.getLong(7),rs.getBoolean(8)));
		}
		rs.close();
		stmt.close();
		return result;
	}



	public ArrayList<GemEntry> getGemsByHashcodeAndHandleNumber(long hashcode,int handleNumber) throws SQLException, IOException, ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select id,code,handle,tsclasssize,catalogNumber,status,minGem,tsrepresentant from gem where hashcode="+hashcode+" and handle="+handleNumber);
		ArrayList<GemEntry> result = new ArrayList<GemEntry>();
		while (rs.next()) {
			result.add(new GemEntry(rs.getLong(1),new ByteArrayInputStream(rs.getBytes(2)),
					rs.getInt(3),rs.getInt(4),rs.getInt(5),
					rs.getInt(6),rs.getLong(7),rs.getBoolean(8)));
		}
		rs.close();
		stmt.close();
		return result;
	}

	public ArrayList<GemEntry> getGemByIDs(long ... ids) throws SQLException, IOException, ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();

		StringBuffer st = new StringBuffer();
		boolean first = true;
		for (long k: ids) {
			if (!first) {
				st.append(",");
			}
			st.append(k);
			first=false;
		}

		ResultSet rs = stmt.executeQuery("select id,code,handle,tsclasssize,catalogNumber,status,minGem,tsrepresentant from gem where id in ("+st.toString()+")");
		ArrayList<GemEntry> result = new ArrayList<GemEntry>();
		while (rs.next()) {
			result.add(new GemEntry(rs.getLong(1),new ByteArrayInputStream(rs.getBytes(2)),rs.getInt(3),rs.getInt(4),rs.getInt(5),rs.getInt(6),rs.getLong(7),rs.getBoolean(8)));
		}
		rs.close();
		stmt.close();

		return result;

	}


	public long[] getQIMinMaxIDs() throws SQLException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select min(id),max(id) from qi");
		long[] result = {-1L, -1L};
		if (rs.next()) {
			result[0]=rs.getLong(1);
			result[1]=rs.getLong(2);
		}
		rs.close();
		stmt.close();
		return result;
	}

	public QI getQI(long id) throws SQLException, IOException, ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select dados from qi where id="+id);
		QI result = null;
		if (rs.next()) {
			result = new QI(id,new ByteArrayInputStream(rs.getBytes(1)));
		}
		rs.close();
		stmt.close();
		return result;
	}

	public ArrayList<BlinkEntry> getBlinksByClass(String homolGroup, long quantumInvariant) throws SQLException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select id,mapcode,colors,numedges,hg,qi,comment,gem,mingem,catalogNumber from blink where numedges<=9 and hg='"+homolGroup+"' and qi="+quantumInvariant);
		ArrayList<BlinkEntry> result = new ArrayList<BlinkEntry>();
		while (rs.next()) {
			long id = rs.getInt(1);
			String mapCode = rs.getString(2);
			long colors = rs.getLong(3);
			int numEdges = rs.getInt(4);
			String hg = rs.getString(5);
			long qi = rs.getLong(6);
			String comment = rs.getString(7);
			long gem = rs.getLong(8);
			long mingem = rs.getLong(9);
			int catalogNumber = rs.getInt(10);
			result.add(new BlinkEntry(id,mapCode,colors,numEdges,hg,qi,gem,mingem,comment,catalogNumber));
		}
		rs.close();
		stmt.close();
		return result;
	}

	public ArrayList<BlinkEntry> getBlinksByHGQIs(int maxEdges, String homolGroup, ArrayList<Long> quantumInvariants) throws SQLException {
		if (quantumInvariants.size() == 0)
			throw new RuntimeException("OOoooopsss");

		Connection con = getConnection();
		Statement stmt = con.createStatement();
		String st = "select id,mapcode,colors,numedges,hg,qi,comment,gem,mingem,catalogNumber from blink where numedges<="+maxEdges+" and hg='"+homolGroup+"' and (";
		for (int i=0;i<quantumInvariants.size();i++) {
			if (i > 0)
				st = st +" or ";
			st = st + "qi="+quantumInvariants.get(i);
		}
		st = st + ")";

		ResultSet rs = stmt.executeQuery(st);

		ArrayList<BlinkEntry> result = new ArrayList<BlinkEntry>();
		while (rs.next()) {
			long id = rs.getInt(1);
			String mapCode = rs.getString(2);
			long colors = rs.getLong(3);
			int numEdges = rs.getInt(4);
			String hg = rs.getString(5);
			long qi = rs.getLong(6);
			String comment = rs.getString(7);
			long gem = rs.getLong(8);
			long mingem = rs.getLong(9);
			int catalogNumber = rs.getInt(10);
			result.add(new BlinkEntry(id,mapCode,colors,numEdges,hg,qi,gem,mingem,comment,catalogNumber));
		}
		rs.close();
		stmt.close();
		return result;
	}


	public ArrayList<BlinkEntry> getBlinksByQI(int maxEdges, long quantumInvariant) throws SQLException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select id,mapcode,colors,numedges,hg,qi,comment,gem,mingem,catalogNumber from blink where numedges<="+maxEdges+" and qi="+quantumInvariant);
		ArrayList<BlinkEntry> result = new ArrayList<BlinkEntry>();
		while (rs.next()) {
			long id = rs.getInt(1);
			String mapCode = rs.getString(2);
			long colors = rs.getLong(3);
			int numEdges = rs.getInt(4);
			String hg = rs.getString(5);
			long qi = rs.getLong(6);
			String comment = rs.getString(7);
			long gem = rs.getLong(8);
			long mingem = rs.getLong(9);
			int catalogNumber = rs.getInt(10);
			result.add(new BlinkEntry(id,mapCode,colors,numEdges,hg,qi,gem,mingem,comment,catalogNumber));
		}
		rs.close();
		stmt.close();
		return result;
	}

	public BlinkEntry getBlinkByQI(int maxEdges, long qi) throws SQLException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select id,mapcode,colors,numedges,hg,qi,comment,gem,mingem,catalogNumber from blink where numedges<="+maxEdges+" and qi="+qi+" limit 1");
		BlinkEntry result = null;
		if (rs.next()) {
			long id = rs.getInt(1);
			String mapCode = rs.getString(2);
			long colors = rs.getLong(3);
			int numEdges = rs.getInt(4);
			String hg = rs.getString(5);
			String comment = rs.getString(7);
			long gem = rs.getLong(8);
			long mingem = rs.getLong(9);
			int catalogNumber = rs.getInt(10);
			result = new BlinkEntry(id,mapCode,colors,numEdges,hg,qi,gem,mingem,comment,catalogNumber);
		}
		rs.close();
		stmt.close();
		return result;
	}

	public ArrayList<ClassHGQI> getHGQIClasses(int maxEdges) throws SQLException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select hg,qi,count(id) from blink where numedges<="+maxEdges+" group by hg,qi");
		ArrayList<ClassHGQI> result = new ArrayList<ClassHGQI>();
		while (rs.next()) {
			String hg = rs.getString(1);
			long qi = rs.getLong(2);
			int count = rs.getInt(3);
			result.add(new ClassHGQI(qi,hg,count));
		}
		rs.close();
		stmt.close();
		return result;
	}

	/**
	 * Mount "HG x NormQI" classes.
	 */
	public ArrayList<ClassHGNormQI> getHGNormQIClasses(int maxEdges) throws SQLException, ClassNotFoundException, IOException {
		// create a map with all qis
		ArrayList<QI> qis = this.getQIs();
		HashMap<Long, QI> map = new HashMap<Long, QI>();
		HashMap<Long, HashSet<Long>> mapId2Part = new HashMap<Long, HashSet<Long>>();
		for (QI qi : qis) {
			map.put(qi.get_id(), qi);
		}

		// PrintWriter pw = new PrintWriter(new FileWriter("log/qiscollection.txt"));

		// match the QIs that are the same when normalized
		ArrayList<HashSet<Long>> partition = new ArrayList<HashSet<Long>>();
		for (int i=0;i<qis.size();i++) {
			QI qi = qis.get(i);
			// pw.println("Trying qi "+qi.get_id());
			boolean included = false;
			for (HashSet<Long> part: partition) {
				QI qiPart = map.get(part.iterator().next());
				boolean compareResult = qiPart.compareNormalizedEntries(qi);
				// pw.println("   ...comparing with "+qiPart.get_id()+" ... "+compareResult);
				if (compareResult) {
					included = true;
					part.add(qi.get_id());
					mapId2Part.put(qi.get_id(),part);
					break;
				}
			}
			if (!included) {
				HashSet<Long> part = new HashSet<Long>();
				part.add(qi.get_id());
				partition.add(part);
				mapId2Part.put(qi.get_id(),part);
			}
			// pw.flush();
		}
		// pw.close();

		/*
        for(HashSet<Long> part: partition) {
            System.out.println(Library.collectionToString(part,' '));
        }*/

		ArrayList<ClassHGNormQI> result = new ArrayList<ClassHGNormQI>();
		ArrayList<ClassHGQI> classesHGQI = this.getHGQIClasses(maxEdges);
		for (ClassHGQI c: classesHGQI) {
			boolean included = false;
			for (ClassHGNormQI cn: result) {
				HashSet<Long> part = mapId2Part.get(cn.get_qi(0));
				if (c.get_hg().equals(cn.get_hg()) && part.contains(c.get_qi())) {
					cn.addQI(c.get_qi(),c.get_numElements());
					included = true;
					break;
				}
			}
			if (!included) {
				ClassHGNormQI cn = new ClassHGNormQI(c.get_qi(),c.get_hg(),c.get_numElements());
				result.add(cn);
			}
		}
		return result;
	}

	public ArrayList<ClassQI> getQIClasses(int maxEdges) throws SQLException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select qi,count(id) from blink where numedges<="+maxEdges+" group by qi");
		ArrayList<ClassQI> result = new ArrayList<ClassQI>();
		while (rs.next()) {
			long qi = rs.getLong(1);
			int count = rs.getInt(2);
			result.add(new ClassQI(qi,count));
		}
		rs.close();
		stmt.close();
		return result;
	}

	public void insertGems(ArrayList<GemEntry> list) throws SQLException, IOException {
		if (list.size() == 0)
			return;
		Connection con = getConnection();
		con.setAutoCommit(false);
		PreparedStatement stmt = con.prepareStatement("insert into gem (code,hashcode,handle,numvert,tsclasssize,catalogNumber,status,mingem,tsrepresentant) values (?,?,?,?,?,?,?,?,?)");
		for (GemEntry ge: list) {
			stmt.setBytes(1, ge.getCodeAsByteArray());
			stmt.setLong(2,ge.getGemHashCode());
			stmt.setInt(3, ge.getHandleNumber());
			stmt.setInt(4, ge.getNumVertices());
			stmt.setInt(5, ge.getTSClassSize());
			stmt.setInt(6, ge.getCatalogNumber());
			stmt.setInt(7, ge.getStatus());
			stmt.setLong(8, ge.getMinGem());
			stmt.setBoolean(9, ge.isTSRepresentant());
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				ge.set_id(rs.getLong(1));
			}
			rs.close();
		}
		con.commit();
		con.setAutoCommit(true);
		stmt.close();
	}

	public void insertGems(GemEntry ... list) throws SQLException, IOException {
		if (list.length == 0)
			return;
		Connection con = getConnection();
		con.setAutoCommit(false);
		PreparedStatement stmt = con.prepareStatement("insert into gem (code,hashcode,handle,numvert,tsclasssize,catalogNumber,status,mingem,tsrepresentant) values (?,?,?,?,?,?,?,?,?)");
		for (GemEntry ge: list) {
			stmt.setBytes(1, ge.getCodeAsByteArray());
			stmt.setLong(2,ge.getGemHashCode());
			stmt.setInt(3, ge.getHandleNumber());
			stmt.setInt(4, ge.getNumVertices());
			stmt.setInt(5, ge.getTSClassSize());
			stmt.setInt(6, ge.getCatalogNumber());
			stmt.setInt(7, ge.getStatus());
			stmt.setLong(8, ge.getMinGem());
			stmt.setBoolean(9, ge.isTSRepresentant());
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				ge.set_id(rs.getLong(1));
			}
			rs.close();
		}
		con.commit();
		con.setAutoCommit(true);
		stmt.close();
	}

	public ArrayList<GemEntry> getGems() throws SQLException, IOException, ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select id,code,handle,tsclasssize,catalogNumber,status,minGem,tsrepresentant from gem where tsclasssize<>0");
		ArrayList<GemEntry> result = new ArrayList<GemEntry>();
		while (rs.next()) {
			result.add(new GemEntry(rs.getLong(1),new ByteArrayInputStream(rs.getBytes(2)),rs.getInt(3),rs.getInt(4),rs.getInt(5),rs.getInt(6),rs.getLong(7),rs.getBoolean(8)));
		}
		rs.close();
		stmt.close();
		return result;
	}

	public ArrayList<GemEntry> getGemsByNumVertices(int minVertices, int maxVertices) throws SQLException, IOException, ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(
				"select id,code,handle,tsclasssize,catalogNumber,status,minGem,tsrepresentant "+
						"from gem where numvert>="+minVertices+" "+
						"and numvert<="+maxVertices);
		ArrayList<GemEntry> result = new ArrayList<GemEntry>();
		while (rs.next()) {
			result.add(new GemEntry(rs.getLong(1),new ByteArrayInputStream(rs.getBytes(2)),rs.getInt(3),rs.getInt(4),rs.getInt(5),rs.getInt(6),rs.getLong(7),rs.getBoolean(8)));
		}
		rs.close();
		stmt.close();
		return result;
	}

	public HashMap<Long,GemEntry> getGemsMap() throws ClassNotFoundException, IOException, SQLException {
		ArrayList<GemEntry> gems = this.getGems();
		HashMap<Long,GemEntry> mapGemId2gGemEntry = new HashMap<Long,GemEntry>();
		for (GemEntry ge: gems) {
			mapGemId2gGemEntry.put(ge.getId(),ge);
		}
		return mapGemId2gGemEntry;
	}

	public ArrayList<BlinkEntry> getBlinksByGem(long gemId) throws SQLException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select id,mapcode,colors,numedges,hg,qi,comment,gem,mingem,catalogNumber from blink where gem="+gemId);
		ArrayList<BlinkEntry> result = new ArrayList<BlinkEntry>();
		while (rs.next()) {
			long id = rs.getInt(1);
			String mapCode = rs.getString(2);
			long colors = rs.getLong(3);
			int numEdges = rs.getInt(4);
			String hg = rs.getString(5);
			long qi = rs.getLong(6);
			String comment = rs.getString(7);
			long gem = rs.getLong(8);
			long mingem = rs.getLong(9);
			int catalogNumber = rs.getInt(10);
			result.add(new BlinkEntry(id,mapCode,colors,numEdges,hg,qi,gem,mingem,comment,catalogNumber));
		}
		rs.close();
		stmt.close();
		return result;
	}

	public void loadPaths(BlinkEntry ... list) throws SQLException, IOException, ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();

		StringBuffer st = new StringBuffer();
		boolean first = true;
		HashMap<Long,BlinkEntry> map = new HashMap<Long,BlinkEntry>();
		for (BlinkEntry be: list) {
			map.put(be.get_id(),be);
			if (!first) {
				st.append(",");
			}
			st.append(be.get_id());
			first=false;
		}

		ResultSet rs = stmt.executeQuery("select id,path from blink where id in ("+st.toString()+")");
		while (rs.next()) {
			long id = rs.getLong(1);
			map.get(id).set_path(new ByteArrayInputStream(rs.getBytes(2)));
		}
		rs.close();
		stmt.close();
	}

	public void loadPaths(GemPathEntry ... list) throws SQLException, IOException, ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();

		StringBuffer st = new StringBuffer();
		boolean first = true;
		HashMap<Long,GemPathEntry> map = new HashMap<Long,GemPathEntry>();
		for (GemPathEntry be: list) {
			map.put(be.getId(),be);
			if (!first) {
				st.append(",");
			}
			st.append(be.getId());
			first=false;
		}

		ResultSet rs = stmt.executeQuery("select id,path from blink where id in ("+st.toString()+")");
		while (rs.next()) {
			long id = rs.getLong(1);
			map.get(id).set_path(new ByteArrayInputStream(rs.getBytes(2)));
		}
		rs.close();
		stmt.close();
	}

	public ArrayList<Pair> getHGQIClassesOfGems(ArrayList<GemEntry> list) throws SQLException {

		StringBuffer st = new StringBuffer();
		boolean first = true;
		for (GemEntry ge: list) {
			if (!first) {
				st.append(",");
			}
			st.append(ge.getId());
			first=false;
		}

		ArrayList<Pair> result = new ArrayList<Pair>();

		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select hg,qi from blink where gem in ("+st+") group by hg,qi");
		while (rs.next()) {
			String hg = rs.getString(1);
			long qi = rs.getLong(2);
			result.add(new Pair(hg,qi));
		}
		rs.close();
		stmt.close();
		return result;
	}

	public void insertGemPathEntries(ArrayList<GemPathEntry> list) throws SQLException, IOException,
	ClassNotFoundException {
		if (list.size() == 0)
			return;
		Connection con = getConnection();
		con.setAutoCommit(false);
		for (GemPathEntry b: list) {
			PreparedStatement stmt = con.prepareStatement("insert into gempath (source,target,path) values (?,?,?)");
			stmt.setLong(1,b.getSource());
			stmt.setLong(2,b.getTarget());
			byte[] is = b.get_pathByteArray();
			if (is == null) stmt.setNull(3,java.sql.Types.BLOB);
			else stmt.setBytes(3, is);
			stmt.execute();
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next())
				b.setId(rs.getLong(1));
			rs.close();
			stmt.close();
		}
		con.commit();
		con.setAutoCommit(true);
	}


	public ArrayList<GemPathEntry> getGemPaths() throws SQLException, IOException, ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select id,source,target,path from gempath");
		ArrayList<GemPathEntry> result = new ArrayList<GemPathEntry>();
		while (rs.next()) {
			long id = rs.getLong(1);
			long source = rs.getLong(2);
			long target = rs.getLong(3);
			InputStream pathStream = new ByteArrayInputStream(rs.getBytes(4));
			result.add(new GemPathEntry(id,source,target,pathStream));
		}
		rs.close();
		stmt.close();
		return result;
	}


	public void insertGemPathEntries(GemPathEntry ... list) throws SQLException, IOException,
	ClassNotFoundException {
		if (list.length == 0)
			return;
		Connection con = getConnection();
		con.setAutoCommit(false);
		for (GemPathEntry b: list) {
			PreparedStatement stmt = con.prepareStatement("insert into gempath (source,target,path) values (?,?,?)");
			stmt.setLong(1,b.getSource());
			stmt.setLong(2,b.getTarget());
			byte[] is = b.get_pathByteArray();
			if (is == null) stmt.setNull(3,java.sql.Types.BLOB);
			else stmt.setBytes(3, is);
			stmt.execute();
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next())
				b.setId(rs.getLong(1));
			rs.close();
			stmt.close();
		}
		con.commit();
		con.setAutoCommit(true);
	}

	public void updateGemCatalogNumber(ArrayList<GemEntry> gems) throws SQLException {
		if (gems.size() == 0)
			return;
		Connection con = getConnection();
		con.setAutoCommit(false);
		Statement stmt = con.createStatement();
		for (GemEntry g: gems) {
			stmt.addBatch("update gem set catalogNumber="+g.getCatalogNumber()+" where id="+g.getId());
		}
		stmt.executeBatch();
		con.commit();
		con.setAutoCommit(true);
		stmt.close();
	}

	public void updateGemStatus(ArrayList<GemEntry> gems) throws SQLException {
		if (gems.size() == 0)
			return;
		Connection con = getConnection();
		con.setAutoCommit(false);
		Statement stmt = con.createStatement();
		for (GemEntry g: gems) {
			stmt.addBatch("update gem set status="+g.getStatus()+" where id="+g.getId());
		}
		stmt.executeBatch();
		con.commit();
		con.setAutoCommit(true);
		stmt.close();
	}

	public void updateGemMinGem(ArrayList<GemEntry> gems) throws SQLException {
		if (gems.size() == 0)
			return;
		Connection con = getConnection();
		con.setAutoCommit(false);
		Statement stmt = con.createStatement();
		for (GemEntry g: gems) {
			stmt.addBatch("update gem set mingem="+g.getMinGem()+" where id="+g.getId());
		}
		stmt.executeBatch();
		con.commit();
		con.setAutoCommit(true);
		stmt.close();
	}

	public GemEntry getGemEntryByCatalogNumber(int numVertices, int catalogNumber, int handleNumber) throws
	SQLException, IOException, ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select id,code,handle,tsclasssize,catalogNumber,status,minGem,tsrepresentant from gem where numvert="+numVertices+" and catalogNumber="+catalogNumber+" and handle="+handleNumber);
		GemEntry result = null;
		while (rs.next()) {
			result = new GemEntry(rs.getLong(1),new ByteArrayInputStream(rs.getBytes(2)),rs.getInt(3),rs.getInt(4),rs.getInt(5),rs.getInt(6),rs.getLong(7),rs.getBoolean(8));
		}
		rs.close();
		stmt.close();
		return result;
	}

	public GemEntry getGemById(long id) throws SQLException, IOException,
	ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select id,code,handle,tsclasssize,catalogNumber,status,minGem,tsrepresentant from gem where id="+id);
		GemEntry result = null;
		while (rs.next()) {
			ByteArrayInputStream bais = new ByteArrayInputStream(rs.getBytes(2));
			result = new GemEntry(rs.getLong(1),bais,rs.getInt(3),rs.getInt(4),rs.getInt(5),rs.getInt(6),rs.getLong(7),rs.getBoolean(8));
		}
		rs.close();
		stmt.close();
		return result;
	}

	/**
	 * Get a bunch of gems for a big search.
	 * in numvert <= 0 do not constrain the number of vertices
	 */
	public ArrayList<GemEntry> getSomeGems(long minId, int numVert, int maxResultRows) throws SQLException, IOException, ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		String sqlNumVert = (numVert<=0 ? "" : "numvert=" + numVert + " and ");
		ResultSet rs = stmt.executeQuery("select id,code,handle,tsclasssize,catalogNumber,status,minGem,tsrepresentant "+
				"from gem where "+
				sqlNumVert +
				"id>=" + minId + " "+
				"order by id " +
				"limit " + maxResultRows);
		ArrayList<GemEntry> result = new ArrayList<GemEntry>();
		while (rs.next()) {
			result.add(new GemEntry(
					rs.getLong(1),
					new ByteArrayInputStream(rs.getBytes(2)),
					rs.getInt(3),
					rs.getInt(4),
					rs.getInt(5),
					rs.getInt(6),
					rs.getLong(7),rs.getBoolean(8)));
		}
		rs.close();
		stmt.close();
		return result;
	}

	/**
	 * Get classes entries by id
	 */
	public ArrayList<ClassEntry> getClasses(int id) throws SQLException, IOException,
	ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select numedges,numorder,size,hg,qi,gem,status,qistatus,maxqi from class where id="+id);
		ArrayList<ClassEntry> result = new ArrayList<ClassEntry>();
		while (rs.next()) {
			result.add(new ClassEntry(
					id,
					rs.getInt(1),
					rs.getInt(2),
					rs.getInt(3),
					rs.getString(4),
					rs.getString(5),
					rs.getLong(6),
					rs.getString(7),
					rs.getString(8),
					rs.getInt(9)));
		}
		rs.close();
		stmt.close();
		return result;
	}

	/**
	 * Get classes entries by id
	 */
	public ClassEntry getClass(int id, int numEdges, int order) throws SQLException, IOException,
	ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(
				"select size,hg,qi,gem,status,qistatus,maxqi from class where id="+id+
				" and numedges="+numEdges+" and numorder="+order);
		ClassEntry result = null;
		if (rs.next()) {
			result = new ClassEntry(id,
					numEdges,
					order,
					rs.getInt(1),
					rs.getString(2),
					rs.getString(3),
					rs.getLong(4),
					rs.getString(5),
					rs.getString(6),
					rs.getInt(7));
		}
		rs.close();
		stmt.close();
		return result;
	}

	/**
	 * Get blinks
	 */
	public ArrayList<BlinkEntry> getBlinksByClass(ClassEntry classEntry) throws SQLException, IOException,
	ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(
				"select blink from classblink where idclass="+classEntry.getId()+
				" and numedges="+classEntry.getNumEdges()+
				" and numorder="+classEntry.getOrder());
		ArrayList<Long> ids = new ArrayList<Long>();
		while (rs.next()) {
			ids.add(rs.getLong(1));
		}
		rs.close();
		stmt.close();

		return this.getBlinksByIDs(ids);
	}
	
	public ArrayList<BlinkEntry> getBlinksByConn(int connected, int numedges) throws SQLException, IOException,
	ClassNotFoundException {
		String sql = "select id from blink where" +
				" connected = " + connected +
				" and numedges = " + numedges;
		return getBlinksByConn(sql);
	}
	
	public ArrayList<BlinkEntry> getBlinksByConn(int connected) throws SQLException, IOException,
	ClassNotFoundException {
		String sql = "select id from blink where" +
				" connected = " + connected;
		return getBlinksByConn(sql);
	}
	
	public ArrayList<BlinkEntry> getBlinksByConn(int connected, int minedges, int maxedges) throws SQLException, IOException,
	ClassNotFoundException {
		String sql = "select id from blink where" +
				" connected = " + connected +
				" and numedges between " + minedges +
				" and " + maxedges;
		return getBlinksByConn(sql);
	}

	private ArrayList<BlinkEntry> getBlinksByConn(String sql) throws SQLException, IOException,
	ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		ArrayList<Long> ids = new ArrayList<Long>();
		while (rs.next()) {
			ids.add(rs.getLong(1));
		}
		rs.close();
		stmt.close();

		return this.getBlinksByIDs(ids);
	}
	
	public int getMaxEdgebyConn(int connected) throws SQLException, IOException,
	ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select max(numedges) from blink where" +
				" connected = " + connected);
		int res = 0;
		if(rs.next())
			res = rs.getInt(1);
		rs.close();
		stmt.close();
		return res;
	}

	/**
	 * Add blink to class
	 */
	public void addBlinksToClass(ClassEntry classEntry, ArrayList<BlinkEntry> blinks) throws SQLException, IOException,
	ClassNotFoundException {

		Connection con = getConnection();
		con.setAutoCommit(false);
		Statement stmt = con.createStatement();
		for (BlinkEntry b: blinks) {
			if (b == null) continue;
			stmt.addBatch("insert into classblink (idclass, numedges, numorder, blink) values ("+
					classEntry.getId()+","+
					classEntry.getNumEdges()+","+
					classEntry.getOrder()+","+
					b.get_id()+
					")");
		}
		stmt.executeBatch();
		con.commit();
		con.setAutoCommit(true);
		stmt.close();
	}



	/**
	 * Add blink to class
	 */
	public void addClasses(
			ArrayList<ClassEntry> classes) throws SQLException, IOException,
			ClassNotFoundException {

		Connection con = getConnection();
		con.setAutoCommit(false);
		Statement stmt = con.createStatement();
		for (ClassEntry c: classes) {
			if (c == null) continue;
			stmt.addBatch("insert into class (id, numedges, numorder, size, hg, qi, gem, status, qiStatus, maxqi) values ("+
					c.getId()+","+
					c.getNumEdges()+","+
					c.getOrder()+","+
					c.get_size()+","+
					"'"+c.get_hg()+"',"+
					"'"+c.get_qi()+"',"+
					c.get_gem()+","+
					"'"+c.get_status()+"',"+
					"'"+c.get_qiStatus()+"',"+
					c.get_maxqi()+
					")");
		}
		stmt.executeBatch();
		con.commit();
		con.setAutoCommit(true);
		stmt.close();
	}

	/**
	 * Add blink to class
	 */
	public void deleteClass(int id) throws SQLException, IOException,
	ClassNotFoundException {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		stmt.executeUpdate("delete from classblink where idclass="+id);
		stmt.executeUpdate("delete from class where id="+id);
		stmt.close();
	}

}
