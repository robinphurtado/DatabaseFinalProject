package com.csumb.cst363;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.Scanner;

public class PrescriptionsReport {
	
	static final String url = "jdbc:mysql://localhost/mydb";
	static final String user = "root";
	static final String password = "LavaMon$ter@8387";

    public static void main(String[] args) {
    	
    	String userInput;
    	Scanner scan = new Scanner(System.in);
    	int pharmID;
    	String startDate = "";
    	String endDate = "";	
    	
    	try (Connection con = DriverManager.getConnection(url, user, password); ) {
            con.setAutoCommit(false);
            
            PreparedStatement ps;
            ResultSet rs;
            
            System.out.println("Enter PharmacyID: ");
            pharmID = scan.nextInt();
            
            System.out.println("Enter start date");
            startDate = scan.next();
            
            System.out.println("Enter end date");
            endDate = scan.next();
            
            String sqlQuery = "SELECT p.quantity, d.trade_name" +
            		" FROM fill AS f JOIN prescription AS p ON f.rxid=p.rxid" +
            		" JOIN drug AS d ON d.drug_id = p.drug_id" +
            		" WHERE f.pharmacy_id =? AND f.date_filled between ? and ?";
            
            ps = con.prepareStatement(sqlQuery);
            ps.setInt(1, pharmID);
            ps.setString(2, startDate);
            ps.setString(3, endDate);
            
            rs = ps.executeQuery();
            
            while(rs.next()) {
            	String drugName = rs.getString("trade_name");
            	int total = rs.getInt("quantity");
            	
            	System.out.println("Name: " + drugName + ", Quantity: " + total);
            }

	    con.commit();
    	} catch (SQLException e) {
    		System.out.println("Error: SQLException " + e.getMessage());
    	}

    }
}
