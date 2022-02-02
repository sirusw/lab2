import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Tests creating an index and using EXPLAIN on a PostgreSQL database.
 */
public class IndexPostgreSQL 
{
	/**
	 * Connection to database
	 */
	private Connection con;
	
	
	/**
	 * Main method is only used for convenience.  Use JUnit test file to verify your answer.
	 * 
	 * @param args
	 * 		none expected
	 * @throws SQLException
	 * 		if a database error occurs
	 */
	public static void main(String[] args) throws SQLException
	{
		IndexPostgreSQL q = new IndexPostgreSQL();
		q.connect();	
		q.drop();
		q.create();
		q.insert(10000);	
		q.addindex1();	
		q.addindex2();			
		q.close();
	}

	/**
	 * Makes a connection to the database and returns connection to caller.
	 * 
	 * @return
	 * 		connection
	 * @throws SQLException
	 * 		if an error occurs
	 */
	public Connection connect() throws SQLException
	{
		String url = "jdbc:postgresql://localhost/lab2";
		String uid = "testuser";
		String pw = "404postgrespw";
		
		System.out.println("Connecting to database.");
		// Note: Must assign connection to instance variable as well as returning it back to the caller
		// TODO: Make a connection to the database and store connection in con variable before returning it.
		Connection con = DriverManager.getConnection(url,uid,pw);
		this.con = con;
		
		return con;  	                       
	}
	
	/**
	 * Closes connection to database.
	 */
	public void close()
	{
		System.out.println("Closing database connection.");
		// TODO: Close the database connection.  Catch any exception and print out if it occurs.			
		try{
			this.con.close();
		}	
		catch (SQLException ex){
			System.out.println(ex);
		}
	}
	
	/**
	 * Drops the table from the database.  If table does not exist, error is ignored.
	 */
	public void drop()
	{
		System.out.println("Dropping table bench.");
		// TODO: Drop the table bench.  Catch any exception and print out if it occurs.	
		try{ 
			String SQL = "drop table if exists bench";
			Statement stmt = con.createStatement();
			int rowcount = stmt.executeUpdate(SQL);
		}
		catch (SQLException ex){
			System.out.print(ex);
		}		
	}
	
	/**
	 * Creates the table in the database.  Table name: bench
	 * Fields:
	 *  - id - integer, must auto-increment
	 *  - val1 - integer (starts at 1 and each record increases by 1)
	 *  - val2 - integer (val1 % 10)
	 *  - str1 - varchar(20) = "Test"+val1
	 */
	public void create() throws SQLException
	{
		System.out.println("Creating table bench.");
		// TODO: Create the table bench.
		String SQL =
					//Identity(1,1)
					"create table bench " +
						"(id serial primary key," +
						"val1 int not null," +
						"val2 int not null," +
						"str1 varchar(20)" +
						");" ;
						//+ "create index ind on bench (val1)"
						//+ "alter table bench modify column val1 int auto_increment;";		
									
		try{
			Statement stmt = con.createStatement();
		 	int rowcount = stmt.executeUpdate(SQL);
		}
		catch (SQLException ex) {
			System.out.println(ex);
		}			
	}
	
	/**
	 * Inserts the test records in the database.  Must used a PreparedStatement.  	 
	 */
	public void insert(int numRecords) throws SQLException
	{
		System.out.println("Inserting records.");
		// TODO: Insert records	
		String SQL = "insert into bench (val1, val2, str1) Values (?, ?, ?)";

		for(int j = 0; j < numRecords-1; j++){
			SQL += ",(?,?,?)";
		}
		
		try{
			PreparedStatement pstmt = this.con.prepareStatement(SQL);
			for(int i = 1; i <= numRecords; i++){
				pstmt.setInt((i-1)*3+1,i);
				pstmt.setDouble((i-1)*3+2,i % 10);
				pstmt.setString((i-1)*3+3,"Test"+i);
			}
			int rowcount = pstmt.executeUpdate();
			System.out.println(rowcount+" rows inserted.");
		}
		catch (SQLException ex){
			System.out.println(ex);
		}	
	}
	
	/**
	 * Creates a unique index on val1 for bench table.  Returns result of explain.
	 * 
	 * @return
	 * 		ResultSet
	 * @throws SQLException
	 * 		if an error occurs
	 */
	public ResultSet addindex1() throws SQLException
	{
		System.out.println("Building index #1.");
		// TODO: Create index
		
		// TODO: Do explain with query: SELECT * FROM bench WHERE val1 = 500
		String SQL = "create UNIQUE INDEX idxBenchVal1 ON bench (val1);"; 
		Statement stmt = this.con.createStatement();
		int row = stmt.executeUpdate(SQL);
		
		// TODO: Do explain with query: SELECT * FROM bench WHERE val1 = 500
		ResultSet rslt = stmt.executeQuery("explain SELECT * FROM bench WHERE val1 = 500;");
		//System.out.println(rslt);
		return rslt;
	}
	
	/**
	 * Creates an index on val2 and val1 for bench table.  Returns result of explain.
	 * 
	 * @return
	 * 		ResultSet
	 * @throws SQLException
	 * 		if an error occurs
	 */
	public ResultSet addindex2() throws SQLException
	{
		System.out.println("Building index #2.");
		// TODO: Create index
		
		// TODO: Do explain with query: SELECT * FROM bench WHERE val2 = 0 and val1 > 100;
		String SQL = "create INDEX idxBenchVal2Val1 ON bench (val2,val1);";
		Statement stmt = this.con.createStatement();
		int row = stmt.executeUpdate(SQL);
		
		//System.out.println(rslt);
		// TODO: Do explain with query: SELECT * FROM bench WHERE val2 = 0 and val1 > 100;
		ResultSet rslt =stmt.executeQuery(("explain select * from bench where  val2 =0 and val1 >100"));
		return rslt;
	}
	
	/*
	 * Do not change anything below here.
	 */
	/**
     * Converts a ResultSet to a string with a given number of rows displayed.
     * Total rows are determined but only the first few are put into a string.
     * 
     * @param rst
     * 		ResultSet
     * @param maxrows
     * 		maximum number of rows to display
     * @return
     * 		String form of results
     * @throws SQLException
     * 		if a database error occurs
     */    
    public static String resultSetToString(ResultSet rst, int maxrows) throws SQLException
    {                       
        StringBuffer buf = new StringBuffer(5000);
        int rowCount = 0;
        ResultSetMetaData meta = rst.getMetaData();
        buf.append("Total columns: " + meta.getColumnCount());
        buf.append('\n');
        if (meta.getColumnCount() > 0)
            buf.append(meta.getColumnName(1));
        for (int j = 2; j <= meta.getColumnCount(); j++)
            buf.append(", " + meta.getColumnName(j));
        buf.append('\n');
                
        while (rst.next()) 
        {
            if (rowCount < maxrows)
            {
                for (int j = 0; j < meta.getColumnCount(); j++) 
                { 
                	Object obj = rst.getObject(j + 1);                	 	                       	                                	
                	buf.append(obj);                    
                    if (j != meta.getColumnCount() - 1)
                        buf.append(", ");                    
                }
                buf.append('\n');
            }
            rowCount++;
        }            
        buf.append("Total results: " + rowCount);
        return buf.toString();
    }
    
    /**
     * Converts ResultSetMetaData into a string.
     * 
     * @param meta
     * 		 ResultSetMetaData
     * @return
     * 		string form of metadata
     * @throws SQLException
     * 		if a database error occurs
     */
    public static String resultSetMetaDataToString(ResultSetMetaData meta) throws SQLException
    {
	    StringBuffer buf = new StringBuffer(5000);                                   
	    buf.append(meta.getColumnName(1)+" ("+meta.getColumnLabel(1)+", "+meta.getColumnType(1)+"-"+meta.getColumnTypeName(1)+", "+meta.getColumnDisplaySize(1)+", "+meta.getPrecision(1)+", "+meta.getScale(1)+")");
	    for (int j = 2; j <= meta.getColumnCount(); j++)
	        buf.append(", "+meta.getColumnName(j)+" ("+meta.getColumnLabel(j)+", "+meta.getColumnType(j)+"-"+meta.getColumnTypeName(j)+", "+meta.getColumnDisplaySize(j)+", "+meta.getPrecision(j)+", "+meta.getScale(j)+")");
	    return buf.toString();
    }
}
