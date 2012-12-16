package com.shoveltusk;
import java.io.IOException;
import java.sql.SQLException;




public class JobRunner {

	public static void main(String[] args) throws IOException, java.sql.SQLException {
		QManager qm = new QManager();
		qm.loadQ("dialer_jobs");
		
	}
	
}
