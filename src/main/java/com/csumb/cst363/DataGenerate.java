package com.csumb.cst363;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This is the DataGenerate JDBC Java application.
 * This program generates 100 random patients, 10 random doctors, and 100 random prescriptions into the cst363 program for Project2
 * Run this program as a Java application.
 */

public class DataGenerate {
		//might have to change url, and will have to change password
	static final String DBURL = "jdbc:mysql://localhost:3306/cst363";  // database URL
	static final String USERID = "root";
	static final String PASSWORD = "CST363";
	
	static final String[] specialties= {"Internal Medicine", "Family Medicine", "Pediatrics", "Orthpedics", "Dermatology", 
			"Cardiology", "Gynecology", "Gastroenterology", "Psychiatry", "Oncology"};
			
	static final String[] firstNames = {"Robin","Michael","David","Nicolas","Sally","Karen","Alexis","John","Megan","George","Julia","Kevin","Alex","Sannya","Jolene"};
	static final String[] lastNames = {"Smith","Jones","Perez","Garcia","Taylor","Conroy","Johnson","Thomas","Fang","Davis","Tarantino","Coppola","Ortiz","Cameron","Spielberg"};
	static final String[] streets = {"Ash Street","Maple Road","Teakwood Lane","First Avenue","Second Circle","Third Lane","Fourth Way","A Street","B Street","C Street","D Street","Main Street","Broadway","Lilac Road","Cherry Lane"};
	static final String[] cities = {"Washington","Franklin","Clinton","Arlington","Centerville"};
	static final String[] states =  {"Alabama","Arkansas","California","Connecticut","Kansas"};
	
	public static void main(String[] args) {
		
		Random gen = new Random();
		
		SimpleDateFormat simpleDate = new SimpleDateFormat("YYYY-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 1950);  // start date for random birthdate
		cal.set(Calendar.DAY_OF_YEAR, 1);

		
			
		// connect to mysql server
		
		try (Connection conn = DriverManager.getConnection(DBURL, USERID, PASSWORD);) {
			
			PreparedStatement ps;
			ResultSet rs;
			int id;
			int patientId;
			int rxid;
			int row_count;
			String primaryID;
			
			
			// doctor section
			
			// delete all doctor rows 
			ps = conn.prepareStatement("delete from doctor");
			row_count = ps.executeUpdate();
			System.out.println("rows deleted "+row_count);
			
			// generate doctor data and insert into table.  We want to generated column "id" value to be returned 
			// as a generated key
			
			String sqlINSERT = "insert into doctor(last_name, first_name, specialty, practice_since, ssn) values( ?, ?, ?, ?, ?)";
			String[] keycols = {"id"};
			ps = conn.prepareStatement(sqlINSERT, keycols);
			
			// insert 10 rows with data
			for (int k=1; k<=10; k++) {
				String practice_since = Integer.toString(2000+gen.nextInt(20));
				// TODO ssn generated is not guaranteed to be unique.  This should be fixed.
				String ssn = Integer.toString(123450000+gen.nextInt(10000));
				ps.setString(1, firstNames[k%firstNames.length]);
				ps.setString(2, "Doctor");
				ps.setString(3, specialties[k%specialties.length]);
				ps.setString(4, practice_since);
				ps.setString(5, ssn);
				row_count = ps.executeUpdate();
				System.out.println("row inserted "+row_count);
				
				// retrieve and print the generated primary key
				
				rs = ps.getGeneratedKeys();
				rs.next();
				id = rs.getInt(1);
				System.out.println("row inserted for doctor id "+id);
			}
			
			//patient section
			
			//delete all patient rows
			ps = conn.prepareStatement("delete from patient");
						row_count = ps.executeUpdate();
			System.out.println("rows deleted "+row_count);
			
			// generate patient data and insert into table.  We want to generated column "patient_id" value to be returned 
			// as a generated key
			
			String sqlINSERTPatient = "insert into patient (last_name, first_name, birthdate, ssn, street, city, state, zip, doctor_id) values( ?, ?, ?, ?, ?, ?, ?, ?,?)";
			String[] patientKeycols = {"patient_id"};
			ps = conn.prepareStatement(sqlINSERTPatient, patientKeycols);
			
			// insert 100 rows with data
			for (int l=1; l<=100; l++) {
				// TODO ssn generated is not guaranteed to be unique.  This should be fixed.
				String patientSsn = Integer.toString(123450000+gen.nextInt(10000));
				cal.set(Calendar.DAY_OF_YEAR, gen.nextInt(365));  
				Date dt = new Date(cal.getTimeInMillis());
				String birthdate = simpleDate.format(dt);
				String houseNumber = Integer.toString(1000+gen.nextInt(999));
				String zip = Integer.toString(00501+gen.nextInt(99450));
				primaryID = Integer.toString(1+gen.nextInt(10));
				ps.setString(1, lastNames[l%lastNames.length]);
				ps.setString(2, firstNames[l%firstNames.length]);				
				ps.setString(3, birthdate);
				ps.setString(4, patientSsn);
				ps.setString(5, houseNumber + " " + streets[l%streets.length]);
				ps.setString(6, cities[l%cities.length]);
				ps.setString(7, states[l%states.length]);
				ps.setString(8, zip);
				ps.setString(9, primaryID);
				row_count = ps.executeUpdate();
				System.out.println("patient row inserted "+row_count);			
			
				// retrieve and print the generated primary key
				
				rs = ps.getGeneratedKeys();
				rs.next();
				id = rs.getInt(1);
				System.out.println("row inserted for patient id "+id);
			}
			
			
			//prescription section 
			
			//delete all prescription rows
			ps = conn.prepareStatement("delete from prescription");
						row_count = ps.executeUpdate();
			System.out.println("rows deleted "+row_count);
			
			// generate prescription data and insert into table.  We want to generated column "rxid" value to be returned 
			// as a generated key
			//Note to self - these must match the fields in the sql schema
			String sqlINSERTRx = "insert into prescription (date_prescribed, quantity, refills_allowed, patient_id, doctor_id, drug_id) values( ?, ?, ?, ?, ?, ?)";
			String[] prescriptionKeycols = {"rxid"};
			ps = conn.prepareStatement(sqlINSERTRx, prescriptionKeycols);
			
			// insert 100 rows with data
			for (int m=1; m<=100; m++) {										
				cal.set(Calendar.DAY_OF_YEAR, gen.nextInt(365));  
				Date dt = new Date(cal.getTimeInMillis());
				String datePrescribed = simpleDate.format(dt);
				String quantity = Integer.toString(1+gen.nextInt(899));
				String refillsAllowed = Integer.toString(1+gen.nextInt(29));
				String prescribedPatientId = Integer.toString(1+gen.nextInt(100));
				String writingDrId = Integer.toString(1+gen.nextInt(10));
				String drugId = Integer.toString(1+gen.nextInt(99));
				ps.setString(1, datePrescribed);	
				ps.setString(2, quantity);				
				ps.setString(3, refillsAllowed);
				ps.setString(4, prescribedPatientId);
				ps.setString(5, writingDrId);
				ps.setString(6, drugId);
				row_count = ps.executeUpdate();
				System.out.println("prescription row inserted "+row_count);			
			
				// retrieve and print the generated primary key
				
				rs = ps.getGeneratedKeys();
				rs.next();
				rxid = rs.getInt(1);
				System.out.println("row inserted for prescription id "+rxid);
			}
			
			
			
			
	
			// display all rows 
			
			//all patients
			
			System.out.println("All patients");
			
			String sqlSELECTPatient = "select patient_id, last_name, first_name, birthdate, ssn, street, city, state, zip, doctor_id from patient";
			ps = conn.prepareStatement(sqlSELECTPatient);
			// there are no parameter markers to set
			rs = ps.executeQuery();
			while (rs.next()) {
				patientId = rs.getInt("patient_id");
				String patient_last_name = rs.getString("last_name");
				String patient_first_name = rs.getString("first_name");
				String birthdate = rs.getString("birthdate");
				String patientSsn = rs.getString("ssn");
				String street = rs.getString("street");
				String city = rs.getString("city");
				String state = rs.getString("state");
				String zip = rs.getString("zip");
				String primaryId = rs.getString("doctor_id");
				System.out.printf("%10d   %-20s  %-12s %-10s %-25s %-15s %-15s %-11s %-3s  \n", patientId, patient_last_name+", "+patient_first_name, birthdate, patientSsn, street, city, state, zip, primaryId);
			}
			
			
			//all doctors
			
			System.out.println("All doctors");
			
			String sqlSELECT = "select id, last_name, first_name, specialty, practice_since, ssn from doctor";
			ps = conn.prepareStatement(sqlSELECT);
			// there are no parameter markers to set
			rs = ps.executeQuery();
			while (rs.next()) {
				id = rs.getInt("id");
				String last_name = rs.getString("last_name");
				String first_name = rs.getString("first_name");
				String specialty = rs.getString("specialty");
				String practice_since = rs.getString("practice_since");
				String ssn = rs.getString("ssn");
				System.out.printf("%10d   %-30s  %-20s %4s %11s \n", id, last_name+", "+first_name, specialty, practice_since, ssn);
			}
			
			//all prescriptions
			
			System.out.println("All prescriptions");
			
			String sqlSELECTRx = "select rxid, date_prescribed, quantity, refills_allowed, patient_id, doctor_id, drug_id  from prescription";
			ps = conn.prepareStatement(sqlSELECTRx);
			// there are no parameter markers to set
			rs = ps.executeQuery();
			while (rs.next()) {
				rxid = rs.getInt("rxid");
				String date_prescribed = rs.getString("date_prescribed");
				String quantity = rs.getString("quantity");
				String refills_allowed = rs.getString("refills_allowed");
				String patient_id = rs.getString("patient_id");
				String doctor_id = rs.getString("doctor_id");
				String drug_id = rs.getString("drug_id");
				System.out.printf("%10d   %-10s  %-5s %-5s  %-10s %-10s  %-10s \n", rxid, date_prescribed, quantity, refills_allowed, patient_id, doctor_id, drug_id);
			}
			
			
		} catch (SQLException e) {
			System.out.println("Error: SQLException "+e.getMessage());
		}
	}	

}
