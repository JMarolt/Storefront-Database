/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class Amazon {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));


   private static String username_main = "";
   private static String password_main = "";
   private static String long_main = "";
   private static String lat_main = "";
   private static String id_main = "";
   /**
    * Creates a new instance of Amazon store
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Amazon(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Amazon

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public static double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query)throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Amazon.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Amazon esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Amazon object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Amazon (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Stores within 30 miles");
                System.out.println("2. View Product List");
                System.out.println("3. Place a Order");
                System.out.println("4. View 5 recent orders");

                //the following functionalities basically used by managers
                System.out.println("5. Update Product");
                System.out.println("6. View 5 recent Product Updates Info");
                System.out.println("7. View 5 Popular Items");
                System.out.println("8. View 5 Popular Customers");
                System.out.println("9. Place Product Supply Request to Warehouse");
		System.out.println("10. View 10 most recent orders for store(Manager)");
		System.out.println("25. Admin");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewStores(esql); break;
                   case 2: viewProducts(esql); break;
                   case 3: placeOrder(esql); break;
                   case 4: viewRecentOrders(esql); break;
                   case 5: updateProduct(esql); break;
                   case 6: viewRecentUpdates(esql); break;
                   case 7: viewPopularProducts(esql); break;
                   case 8: viewPopularCustomers(esql); break;
                   case 9: placeProductSupplyRequests(esql); break;
		   case 10: recentStoreOrders(esql); break;
		   case 25: update(esql); break;

                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(Amazon esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();
         System.out.print("\tEnter latitude: ");   
         String latitude = in.readLine();       //enter lat value between [0.0, 100.0]
         System.out.print("\tEnter longitude: ");  //enter long value between [0.0, 100.0]
         String longitude = in.readLine();
         
         String type="Customer";

			String query = String.format("INSERT INTO USERS (name, password, latitude, longitude, type) VALUES ('%s','%s', %s, %s,'%s')", name, password, latitude, longitude, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Amazon esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String pass = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE name = '%s' AND password = '%s'", name, pass);
         int userNum = esql.executeQuery(query);
	 List<List<String>> user_info = esql.executeQueryAndReturnResult(query);
	 if (userNum > 0){
		username_main = name;
		password_main = pass;
		long_main = user_info.get(0).get(4);
		lat_main = user_info.get(0).get(3);
		id_main = user_info.get(0).get(0);
		return name;
	 }
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

//THIS FUNCTION IS ENTIRELY FINISHED
   public static void viewStores(Amazon esql) {
	   try{
		   String store_loc_query = "SELECT DISTINCT s.latitude, s.longitude, s.storeID FROM Store s";
		   List<List<String>> store_pos = esql.executeQueryAndReturnResult(store_loc_query);
		   for(int i = 0; i < store_pos.size(); i++){
			   double lat1 = Double.parseDouble(store_pos.get(i).get(0));
			   double long1 = Double.parseDouble(store_pos.get(i).get(1));
			   double lat2 = Double.parseDouble(lat_main);
			   double long2 = Double.parseDouble(long_main);
			   double dist = calculateDistance(lat1, long1, lat2, long2);
			   if(dist <= 30){
				   System.out.print("Store ID: " + store_pos.get(i).get(2));
				   System.out.println(", Distance: " + dist);
			   }	
		   }
		
	}catch(Exception e){
		System.err.println(e.getMessage());
	}
   }

//THIS FUNCTION IS ENTIRELY FINISHED
   public static void viewProducts(Amazon esql) {
   	try{
		int store_id = 0;
		System.out.print("Enter Amazon Store ID: ");
		try{
			store_id = Integer.parseInt(in.readLine());
		}catch(NumberFormatException e){
			System.out.println("ID must be a 32-bit Integer.");
			return;
		}
		String query = "SELECT p.productName, p.numberOfUnits, p.pricePerUnit FROM Store s, Product p WHERE s.storeID = p.storeID AND s.storeID = " + store_id;
		String store_id_exists = "SELECT s.managerID FROM Store s WHERE s.storeID = " + store_id;
		int does_exist = esql.executeQuery(store_id_exists);
		if(does_exist == 0){
			System.out.println("Amazon store with that ID does not exist.");
			return;
		}
		int rows = esql.executeQueryAndPrintResult(query);
		if(rows == 0){
			System.out.println("This store has no current products");
		}
	}catch(Exception e){
		System.err.println(e.getMessage());
	}
   }
   public static void placeOrder(Amazon esql) {
   	try{
		System.out.print("Enter Amazon Store ID: ");
		String store_id = in.readLine();
		//within 30 miles of user
		String store_loc_query = "SELECT s.latitude, s.longitude FROM Store s WHERE s.storeID = " + store_id;
		List<List<String>> store_loc = esql.executeQueryAndReturnResult(store_loc_query);
		double lat1 = Double.parseDouble(store_loc.get(0).get(0));
                double long1 = Double.parseDouble(store_loc.get(0).get(1));
                double lat2 = Double.parseDouble(lat_main);
                double long2 = Double.parseDouble(long_main);
                double dist = calculateDistance(lat1, long1, lat2, long2);
                if(dist > 30){
                	System.out.println("Store too far.");
		 	return;	
                }
		System.out.print("Enter Product Name: ");
		String productName = in.readLine();
		System.out.print("Enter Amount: ");
		int amount = Integer.parseInt(in.readLine());
		String productQuery = "SELECT p.productName, p.numberOfUnits FROM Store s, Product p WHERE s.storeID = p.storeID AND s.storeID = " + store_id;
		List<List<String>> product = esql.executeQueryAndReturnResult(productQuery);
		List<List<String>> numOrdersQuery = esql.executeQueryAndReturnResult("SELECT COUNT(*) FROM Orders");
		//int numOrders = Integer.parseInt(numOrdersQuery.get(0).get(0)) + 1;
		int numOrders = 1;
		for(int i = 0; i < product.size(); i++){
			if(product.get(i).get(0).trim().equals(productName.trim())){
				int totalProductAmount = Integer.parseInt(product.get(i).get(1));
				if(amount < totalProductAmount){
					int newAmount = totalProductAmount - amount;
					String updateProductsQuery = "UPDATE Product SET numberOfUnits = " + newAmount + " WHERE productName = '" + product.get(i).get(0) + "' AND storeID = " + store_id;
					esql.executeUpdate(updateProductsQuery);
					String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
					String orderQuery = String.format("INSERT INTO Orders (orderNumber, customerID, storeID, productName, unitsOrdered, orderTime) VALUES (%s, %s, %s, '%s', %s, '%s')", numOrders, id_main, store_id, product.get(i).get(0), amount, currentTime);
					esql.executeUpdate(orderQuery);
					return;
				}else{
					System.out.println("We do not have enough of that product in stock");
				}
			}
		}
		System.out.println("Product does not exist at this store");
	 }catch(Exception e){
		System.err.print(e.getMessage());	
	 }
   }
   public static void viewRecentOrders(Amazon esql) {
   	try{
		String recentOrderQueries = "SELECT * FROM Orders WHERE customerID = " + id_main + " ORDER BY orderNumber DESC LIMIT 5";
		int recentOrders = esql.executeQueryAndPrintResult(recentOrderQueries);
		if(recentOrders == 0){
			System.out.println("No order history");
		}
	}catch(Exception e){
		System.err.print(e.getMessage());
	}	
   }
   public static void updateProduct(Amazon esql) {
   	try{
		String userTypeQuery = "SELECT type FROM Users WHERE userID = " + id_main;
		List<List<String>> type = esql.executeQueryAndReturnResult(userTypeQuery);
		if(type.get(0).get(0).trim().equals("manager") || type.get(0).get(0).trim().equals("admin")){
			//skip this first part if admin
			System.out.println("Which store's products would you like to update?");
		        String st_id = in.readLine();
			if(!type.get(0).get(0).trim().equals("admin")){
				String storeManages = "SELECT s.storeID FROM Store s WHERE s.managerID = " + id_main;
				List<List<String>> stores = esql.executeQueryAndReturnResult(storeManages);
				boolean isIn = false;
				for(int j = 0; j < stores.size(); j++){
					if(stores.get(j).get(0).trim().equals(st_id)){
						isIn = true;
						break;
					}
				}
				if(!isIn){
					System.out.println("You cannot update that store!");
					return;
				}
			}
			List<List<String>> manID = esql.executeQueryAndReturnResult("SELECT managerID FROM Store WHERE storeID = " + st_id);
			String id_to_use = type.get(0).get(0).trim().equals("admin") ? manID.get(0).get(0) : id_main;
			//its because we use id_main which would reject and say no to s.managerID for admin
			String productNamesQuery = "SELECT p.productName, p.storeID FROM Product p, Store s WHERE s.storeID = p.storeID AND s.managerID = " + id_to_use + " AND s.storeID = " + st_id;
			List<List<String>> productNames = esql.executeQueryAndReturnResult(productNamesQuery);
			System.out.println("Which product would you like to update?");
			String name = in.readLine();
			for(int i = 0; i < productNames.size(); i++){
				if(name.equals(productNames.get(i).get(0).trim())){
					System.out.println("Update amount or price? (amount/price)");
					String ans = in.readLine();
					if(ans.equals("amount")){
						System.out.print("Enter new amount: ");
						int newAmount = Integer.parseInt(in.readLine());
						String updateAmountQuery = "UPDATE Product SET numberOfUnits = " + newAmount + " WHERE productName = '" + name + "' AND storeID = '" + productNames.get(i).get(1) + "'";
						esql.executeUpdate(updateAmountQuery);
						List<List<String>> updateSize = esql.executeQueryAndReturnResult("SELECT COUNT(*) FROM ProductUpdates");
						int upSize = Integer.parseInt(updateSize.get(0).get(0)) + 1;
						String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
						String productUpdateTableQuery = String.format("INSERT INTO ProductUpdates (updateNumber, managerID, storeID, productName, updatedOn) VALUES (%s, %s, %s, '%s', '%s')", upSize, id_main, productNames.get(i).get(1), name, currentTime);
					        esql.executeUpdate(productUpdateTableQuery);	
						break;
					}else if(ans.equals("price")){
						System.out.print("Enter new price: ");
						int newPrice = Integer.parseInt(in.readLine());
						String updatePriceQuery = "UPDATE Product SET pricePerUnit = " + newPrice + " WHERE productName = '" + name + "' AND storeID = '" + productNames.get(i).get(1) + "'";
						esql.executeUpdate(updatePriceQuery);

						List<List<String>> updateSize = esql.executeQueryAndReturnResult("SELECT COUNT(*) FROM ProductUpdates");
						int upSize = Integer.parseInt(updateSize.get(0).get(0)) + 1;
						String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                                                String productUpdateTableQuery = String.format("INSERT INTO ProductUpdates (updateNumber, managerID, storeID, productName, updatedOn) VALUES (%s, %s, %s, '%s', '%s')", upSize, id_main, productNames.get(i).get(1), name, currentTime);
                                                esql.executeUpdate(productUpdateTableQuery);
						break;
					}else{
						System.out.println("Unknown choice");
						break;
					}
				}
			}
		}else{
			System.out.println("Something didn't work...");
		}
	}catch(Exception e){
		System.err.print(e.getMessage());
	}
   }
   public static void viewRecentUpdates(Amazon esql) {
   	try{
         String userTypeQuery = "SELECT type FROM Users WHERE userID = " + id_main;
         List<List<String>> type = esql.executeQueryAndReturnResult(userTypeQuery);
         if(type.get(0).get(0).trim().equals("manager") || type.get(0).get(0).trim().equals("admin")){
            System.out.println("Which store’s updates would you like to see? (Enter StoreID)");
            String st_id = in.readLine();
	    //List<List<String>> manID = esql.executeQueryAndReturnResult("SELECT managerID FROM Store WHERE storeID = " + st_id);
	    //String id_to_use = type.get(0).get(0).trim().equals("admin") ? manID.get(0).get(0) : id_main;
	    if(!type.get(0).get(0).trim().equals("admin")){
            	String storeManages = "SELECT s.storeID FROM Store s WHERE s.managerID = " + id_main;
            	List<List<String>> stores = esql.executeQueryAndReturnResult(storeManages);
            	boolean isIn = false;
            	for(int j = 0; j < stores.size(); j++){
               	    if(stores.get(j).get(0).trim().equals(st_id)){
                  	isIn = true;
                  	break;
               	    }	
                }
                if(!isIn){
                    System.out.println("You cannot view that store’s updates!");
                    return;
                }
	    }
	    String recentUpdatesQuery = "SELECT * FROM ProductUpdates WHERE storeID = " + st_id + " ORDER BY updateNumber DESC LIMIT 5";
            //String recentUpdatesQuery = "SELECT * FROM ProductUpdates WHERE storeID = " + st_id + " AND managerID = " + id_to_use + " ORDER BY updateNumber DESC LIMIT 5";
            int recentUpdates = esql.executeQueryAndPrintResult(recentUpdatesQuery);
            if(recentUpdates == 0){
               System.out.println("No update history");
            }
         }
      }catch(Exception e){
         System.err.print(e.getMessage());
      }
   }
   public static void viewPopularProducts(Amazon esql) {
  	  try{
         //check User Type
         String userTypeQuery = "SELECT type FROM Users WHERE userID = " + id_main;
         List<List<String>> type = esql.executeQueryAndReturnResult(userTypeQuery);
         if(type.get(0).get(0).trim().equals("manager") || type.get(0).get(0).trim().equals("admin")){
            System.out.println("Which store’s 5 most popular products would you like to see? (Enter StoreID)");
            String st_id = in.readLine();
	    List<List<String>> manID = esql.executeQueryAndReturnResult("SELECT managerID FROM Store WHERE storeID = " + st_id);
	    String id_to_use = type.get(0).get(0).trim().equals("admin") ? manID.get(0).get(0) : id_main;
            if(!type.get(0).get(0).trim().equals("admin")){
	    	String storeManages = "SELECT s.storeID FROM Store s WHERE s.managerID = " + id_main;
            	List<List<String>> stores = esql.executeQueryAndReturnResult(storeManages);
            	boolean isIn = false;
            	for(int j = 0; j < stores.size(); j++){
                    if(stores.get(j).get(0).trim().equals(st_id)){
                        isIn = true;
                  	break;
               	    }
                }
                if(!isIn){
                    System.out.println("You cannot view that store’s updates!");
                    return;
                }
	    }
            // get popular products
            String popularProdQuery = "SELECT productName, SUM(unitsOrdered) AS NumUnitsPurchased FROM Orders WHERE storeID = " + st_id + " GROUP BY productName ORDER BY NumUnitsPurchased LIMIT 5";
            esql.executeQueryAndPrintResult(popularProdQuery);
         }
      }catch(Exception e){
         System.err.print(e.getMessage());
      } 
   }
   public static void viewPopularCustomers(Amazon esql) {
      try{
         //check User Type
         String userTypeQuery = "SELECT type FROM Users WHERE userID = " + id_main;
         List<List<String>> type = esql.executeQueryAndReturnResult(userTypeQuery);
         if(type.get(0).get(0).trim().equals("manager") || type.get(0).get(0).trim().equals("admin")){
            System.out.println("Which store’s 5 most popular customers would you like to see? (Enter StoreID)");
            String st_id = in.readLine();
	    List<List<String>> manID = esql.executeQueryAndReturnResult("SELECT managerID FROM Store WHERE storeID = " + st_id);                                                                                                                            String id_to_use = type.get(0).get(0).trim().equals("admin") ? manID.get(0).get(0) : id_main;
	    if(!type.get(0).get(0).trim().equals("admin")){
            	String storeManages = "SELECT s.storeID FROM Store s WHERE s.managerID = " + id_main;
            	List<List<String>> stores = esql.executeQueryAndReturnResult(storeManages);
            	boolean isIn = false;
            	for(int j = 0; j < stores.size(); j++){
               	    if(stores.get(j).get(0).trim().equals(st_id)){
                    	isIn = true;
                  	break;
               	    }
                }
                if(!isIn){
                   System.out.println("You cannot view that store’s customers!");
                   return;
                }
	       }
               // get popular products
            	String popularCustomersQuery = "SELECT U.name, O.customerID FROM Users U, Orders O, Store S WHERE S.managerID = " + id_to_use + " AND U.userID = O.customerID AND S.storeID = O.storeID AND S.storeID = " + st_id + " ORDER BY O.orderNumber LIMIT 5";   
	    int topCust = esql.executeQueryAndPrintResult(popularCustomersQuery);
               // if(recentOrders == 0){
               //    System.out.println(“No update history”);
               // }
         }
      }catch(Exception e){
         System.err.print(e.getMessage());
      }
   }
   public static void placeProductSupplyRequests(Amazon esql) {
   	try{
         //check User Type
         String userTypeQuery = "SELECT type FROM Users WHERE userID = " + id_main;
         List<List<String>> type = esql.executeQueryAndReturnResult(userTypeQuery);
         if(type.get(0).get(0).trim().equals("manager")){
            System.out.println("Which store would you like to request products for? (Enter StoreID)");
            String st_id = in.readLine();
            String storeManages = "SELECT s.storeID FROM Store s WHERE s.managerID = " + id_main;
            List<List<String>> stores = esql.executeQueryAndReturnResult(storeManages);
            boolean isIn = false;
            for(int j = 0; j < stores.size(); j++){
               if(stores.get(j).get(0).trim().equals(st_id)){
                  isIn = true;
                  break;
               }
            }
            if(!isIn){
               System.out.println("You cannot order products for that store!");
               return;
            }
               System.out.println("Input Product Name: ");
               String ProductName = in.readLine();
               System.out.println("Enter number of units needed: ");
               int productAmount = Integer.parseInt(in.readLine());
               System.out.println("Enter warehouse ID: ");
               int warehouse = Integer.parseInt(in.readLine());
	       //List<List<String>> request_number_list = esql.executeQueryAndReturnResult("SELECT COUNT(*) FROM ProductSupplyRequests");
	       int request_number = 1;
	       //int request_number = Integer.parseInt(request_number_list.get(0).get(0)) + 1;
               String productSupplyRequestQuery = String.format("INSERT INTO ProductSupplyRequests (requestNumber, managerID, warehouseID, storeID, productName, unitsRequested) VALUES (%s, %s, %s, %s, '%s', %s)", request_number, id_main, warehouse, st_id, ProductName, productAmount);
               esql.executeUpdate(productSupplyRequestQuery);
	       String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
	       //List<List<String>> update_number_list = esql.executeQueryAndReturnResult("SELECT COUNT(*) FROM ProductUpdates");
	       //int update_number = Integer.parseInt(update_number_list.get(0).get(0)) + 1;
	       int update_number = 1;
	       //update_number++;
	       String productUpdateQuery = String.format("INSERT INTO ProductUpdates (updateNumber, managerID, storeID, productName, updatedOn) VALUES (%s, %s, %s, '%s', '%s')", update_number, id_main, st_id, ProductName, currentTime);
	       esql.executeUpdate(productUpdateQuery);
	       String productQuery = "UPDATE Product SET numberOfUnits = numberOfUnits + " + productAmount + " WHERE productName = '" + ProductName + "' AND storeID = " + st_id;
	       esql.executeUpdate(productQuery);
         }else{
		System.out.println("You must be manager at this store!");
	 }
      }catch(Exception e){
         System.err.print(e.getMessage());
      } 
   }
   public static void recentStoreOrders(Amazon esql){
   	try{
         //check User Type
         String userTypeQuery = "SELECT type FROM Users WHERE userID = " + id_main;
         List<List<String>> type = esql.executeQueryAndReturnResult(userTypeQuery);
         if(type.get(0).get(0).trim().equals("manager")){
            System.out.println("Which store’s 10 most recent orders would you like to see? (Enter StoreID)");
               String st_id = in.readLine();
            String storeManages = "SELECT s.storeID FROM Store s WHERE s.managerID = " + id_main;
            List<List<String>> stores = esql.executeQueryAndReturnResult(storeManages);
            boolean isIn = false;
            for(int j = 0; j < stores.size(); j++){
               if(stores.get(j).get(0).trim().equals(st_id)){
                  isIn = true;
                  break;
               }
            }
            if(!isIn){
               System.out.println("You cannot check order history for that store!");
               return;
            }
	    String orderHistoryQuery = "SELECT * FROM ORDERS WHERE storeID = " + st_id + " ORDER BY orderNumber DESC LIMIT 10";
	    esql.executeQueryAndPrintResult(orderHistoryQuery);
	 }else{
		System.out.println("You must be manager at this store!");
	 }
	}catch(Exception e){
		System.err.print(e.getMessage());
	}
   }
   public static void update(Amazon esql){
	try{
		 String userTypeQuery = "SELECT type FROM Users WHERE userID = " + id_main;                                                            List<List<String>> type = esql.executeQueryAndReturnResult(userTypeQuery);                                                           if(!type.get(0).get(0).trim().equals("admin")){
			System.out.println("You are not admin");
			 return;
		}
		System.out.println("Which would you like to update(user/product)?");
		String ans = in.readLine();
		if(ans.equals("user")){
			System.out.println("Which user would you like to edit?");
			String user_id_to_edit = in.readLine();
	        	System.out.println("What would you like to edit about them?(id/name/password/latitude/longitude/type)");
			String to_edit = in.readLine();
			String updateQuery = "";
			if(to_edit.equals("id")){
				System.out.println("What would you like their new id to be?");
				String new_id = in.readLine();
				updateQuery = "UPDATE Users SET userID = " + new_id + " WHERE userID = " + user_id_to_edit;
			}else if(to_edit.equals("name")){
				System.out.println("What would you like their new name to be?");
                                String new_name = in.readLine();
                                updateQuery = "UPDATE Users SET name = " + new_name + " WHERE userID = " + user_id_to_edit;
			}else if(to_edit.equals("password")){
				System.out.println("What would you like their new password to be?");
                                String new_password = in.readLine();
                                updateQuery = "UPDATE Users SET password = " + new_password + " WHERE userID = " + user_id_to_edit;
			}else if(to_edit.equals("latitude")){
				System.out.println("What would you like their new latitude to be?");
                                String new_lat = in.readLine();
                                updateQuery = "UPDATE Users SET latitude = " + new_lat + " WHERE userID = " + user_id_to_edit;
			}else if(to_edit.equals("longitude")){
				System.out.println("What would you like their new longitude to be?");
                                String new_long = in.readLine();
                                updateQuery = "UPDATE Users SET longitude = " + new_long + " WHERE userID = " + user_id_to_edit;
			}else{
				System.out.println("What would you like their new type to be?");
                                String new_type = in.readLine();
                                updateQuery = "UPDATE Users SET type = " + new_type + " WHERE userID = " + user_id_to_edit;
			}
			esql.executeUpdate(updateQuery);	
		}else{
			updateProduct(esql);
		}	
   	}catch(Exception e){
		System.err.print(e.getMessage());
	}
    }

}//end Amazon

