package com.shoveltusk;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;

public class QManager {

	private String userName = "";
	private String password  = "";
	private String dbServerName = "";
	private String dbPort = "";
	
	private final static String QUEUE_NAME = "dialer_jobs";
	
	
	public void loadQ (String qname) throws java.io.IOException, java.sql.SQLException {
		
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		com.rabbitmq.client.Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		
		channel.exchangeDeclare("voters", "direct");
		channel.queueDeclare(QManager.QUEUE_NAME, true, false, false, null);
		
		Connection con = getConn2DB();
		
		 Statement stmt = null;
		 String query = "SELECT vid, phone, firstname, lastname FROM diggermachine.voterinfo LIMIT 25";
		 
		    try {
		        stmt = con.createStatement();
		        ResultSet rs = stmt.executeQuery(query);

		        while (rs.next()) {
		            String message = "{\"voter\":{";
		        	message += "\"vid\":\"" + rs.getString("vid") + "\",\"phone\":\"" + 
		        			rs.getString("phone") + "\",\"firstname\":\"" + rs.getString("firstname") + 
		        			"\",\"lastname\":\"" + rs.getString("lastname");
		        	message += "\"}}";
		        	
		            System.out.println("rs string: " + message);

		            //put to queue
		            channel.basicPublish("", QManager.QUEUE_NAME, null, message.getBytes());
		    		System.out.println(" [x] Sent '" + message + "'");
		        }
		    } catch (SQLException e ) {
		        System.out.println(e);
		    } finally {
		        System.out.println("Closing database connection...");
		    	if (stmt != null) { stmt.close(); }
		    }
		
		
		channel.close();
		connection.close();
	}
	
	public java.sql.Connection getConn2DB() throws SQLException {
		
		this.initApp();
		
		java.sql.Connection cn = null;
		
		Properties connectionProps = new Properties();
	    connectionProps.put("user", userName);
	    connectionProps.put("password", password);
		
		cn = DriverManager.getConnection("jdbc:mysql://" + dbServerName +
                ":" + dbPort + "/",
                connectionProps);
		
		return cn;
		
	}
	
	private void initApp() {
		
		Properties props = new Properties();
	    InputStream istream = null;
	 
	    // First try loading from the current directory
	    try {
	        File file = new File("app.properties");
	        istream = new FileInputStream( file );
	    }
	    catch ( Exception e ) { istream = null; }
	 
	    try {
	        if ( istream == null ) {
	            // Try loading from classpath
	            istream = getClass().getResourceAsStream("app.properties");
	        }
	 
	        // Try loading properties from the file (if found)
	        props.load( istream );
	    }
	    catch ( Exception e ) { }
	 
	    dbServerName = props.getProperty("dbserver", "127.0.0.1");
	    dbPort = props.getProperty("dbport", "3306");
	    userName = props.getProperty("user", "");
		password = props.getProperty("password", "");
	}

}





