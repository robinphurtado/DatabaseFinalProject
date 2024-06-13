package com.csumb.cst363;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * Controller class for patient interactions.
 *   register as a new patient.
 *   update patient profile.
 */
@Controller
public class ControllerPatient {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/*
	 * Request blank patient registration form.
	 */
	@GetMapping("/patient/new")
	public String newPatient(Model model) {
		// return blank form for new patient registration
		model.addAttribute("patient", new Patient());
		return "patient_register";
	}

	/*
	 * Process new patient registration
	 */
	@PostMapping("/patient/new")
	public String newPatient(Patient p, Model model) {

		// Check SSN
		if (!IsSSN(p.getSsn())) {
			model.addAttribute("message", "Error: SSN invalid. Must be 9 digit format without '-'.");
			model.addAttribute("patient", p);
			return "patient_register";
		}

		// check Last name, first name, City, and state if alphabet
		if (!IsAlphabetStr(p.getFirst_name())) {
			model.addAttribute("message", "Error: First name invalid.");
			model.addAttribute("patient", p);
			return "patient_register";
		}

		if (!IsAlphabetStr(p.getLast_name())) {
			model.addAttribute("message", "Error: Last name invalid.");
			model.addAttribute("patient", p);
			return "patient_register";
		}

		if (!IsAlphabetStr(p.getCity()) || !CheckValidation(p.getCity())) {
			model.addAttribute("message", "Error: City name invalid.");
			model.addAttribute("patient", p);
			return "patient_register";
		}

		if (!IsAlphabetStr(p.getState()) || !CheckValidation(p.getCity())) {
			model.addAttribute("message", "Error: State name invalid.");
			model.addAttribute("patient", p);
			return "patient_register";
		}

		// Check zip code
		if (!IsNumeric(p.getZipcode()) || (p.getZipcode().length() != 5)) {
			model.addAttribute("message", "Error: Zipcode invalid. Must be numeric or 5 digit zip code.");
			model.addAttribute("patient", p);
			return "patient_register";
		}

		// Check Birthday date
		if (!CheckBday(p.getBirthdate())) {
			model.addAttribute("message", "Error: Birthday invalid. Must be i nthe format yyyy-mm-dd, ex 1999-04-27");
			model.addAttribute("patient", p);
			return "patient_register";
		}

		// Check street validation.
		if (!CheckValidation(p.getStreet())) {
			model.addAttribute("message", "Error: invalid street.");
			model.addAttribute("patient", p);
			return "patient_register";
		}

		// check doctor name for validation errors
		if (!CheckValidation(p.getPrimaryName())) {
			model.addAttribute("message", "Error: doctor name invalid");
			model.addAttribute("patient", p);
			return "patient_register";
		}

		try (Connection con = getConnection();) {
			PreparedStatement ps = con.prepareStatement("insert into patient(last_name, first_name, "
					+ "birthdate, ssn, street, city, state, zip, doctor_id) values(?, ?, ?, ?, ?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, p.getLast_name());
			ps.setString(2, p.getFirst_name());
			ps.setString(3, p.getBirthdate());
			ps.setString(4, p.getSsn());
			ps.setString(5, p.getStreet());
			ps.setString(6, p.getCity());
			ps.setString(7, p.getState());
			ps.setString(8, p.getZipcode());


			 //Get doctor ID 
			int tmpDocID = GetDocID(p.getPrimaryName());
			 
			if(tmpDocID < 1) { 
				 model.addAttribute("message",
						 "Error couldn't find a doctor.");
				 model.addAttribute("patient", p); 
			return "patient_register";
			}

			ps.setInt(9, tmpDocID);

			ps.executeUpdate();

			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next())
				p.setPatientId((int) rs.getLong(1));

			model.addAttribute("message", "Registration successful.");
			model.addAttribute("patient", p);
			return "patient_show";

		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error. " + e.getMessage());
			model.addAttribute("patient", p);
			return "patient_register";
		}

		// return "patient_show";

	}

	/*
	 * Request blank form to search for patient by and and id
	 */
	@GetMapping("/patient/edit")
	public String getPatientForm(Model model) {
		
		return "patient_get";
	}

	/*
	 * Perform search for patient by patient id and name.
	 */
	@PostMapping("/patient/show")
	public String getPatientForm(@RequestParam("patientId") int patientId, @RequestParam("last_name") String last_name,
			Model model) {
		
		Patient p = new Patient();
		
		// check ID
		if(patientId < 1) {
			model.addAttribute("message", "Error invalid patient ID.");
			model.addAttribute("patient", p);
			
			return "patient_get";
		}
		
		if(!CheckValidation(last_name) || !IsAlphabetStr(last_name)) {
			model.addAttribute("message", "Error invalid patient name.");
			model.addAttribute("patient", p);
			
			return "patient_get";
		}
		
		try(Connection con = getConnection();){
			
			PreparedStatement ps = con.prepareStatement("select * from patient where patient_id =? and last_name =?");
			ps.setInt(1, patientId);
			ps.setString(2, last_name);
			
			ResultSet rs = ps.executeQuery();

			if(rs.next()) {
				p.setPatientId(rs.getInt(1));
				p.setLast_name(rs.getString(2));
				p.setFirst_name(rs.getString(3));
				p.setBirthdate(rs.getString(4));
				p.setSsn(rs.getString(5));
				p.setStreet(rs.getString(6));
				p.setCity(rs.getString(7));
				p.setState(rs.getString(8));
				p.setZipcode(rs.getString(9));
				p.setPrimaryID(rs.getInt(10));
				
				// Get Doctor name by ID
				String tmpStr = GetDocName(rs.getInt(10));
				
				if(tmpStr.equals("")) {
					model.addAttribute("message", "Error Getting Doctor name");
					
					return "patient_get";
				}
				 
				p.setPrimaryName(tmpStr);
				
				model.addAttribute("message", "Patient updated successful.");
				model.addAttribute("patient", p);
				return "patient_show";
				
			} else {
				model.addAttribute("message", "Patient not found.");
				return "patient_get";
			}
		} catch (SQLException e) {
			
			System.out.println("SQL error in getPatientForm: " + e.getMessage());
			model.addAttribute("message", "SQL Error: " + e.getMessage());
			model.addAttribute("patient", p);
			return "patient_get";
		}
	}

	/*
	 * Display patient profile for patient id.
	 */
	@GetMapping("/patient/edit/{patientId}")
	public String updatePatient(@PathVariable int patientId, Model model) {

		Patient p = new Patient();
		p.setPatientId(patientId);
		
		try(Connection con = getConnection();){
			
			PreparedStatement ps = con.prepareStatement("select * from patient where patient_id =?");
			ps.setInt(1, patientId);
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				p.setPatientId(rs.getInt(1));
				p.setLast_name(rs.getString(2));
				p.setFirst_name(rs.getString(3));
				p.setBirthdate(rs.getString(4));
				p.setSsn(rs.getString(5));
				p.setStreet(rs.getString(6));
				p.setCity(rs.getString(7));
				p.setState(rs.getString(8));
				p.setZipcode(rs.getString(9));
				p.setPrimaryID(rs.getInt(10));
				
				//get doctors name
				String tmpStr = GetDocName(rs.getInt(10));
				
				if(tmpStr.equals("")) {
					model.addAttribute("message", "Error Getting Doctor name");
					model.addAttribute("patient", p);
					
					return "patient_get";
				}
				
				p.setPrimaryName(tmpStr);
				
				model.addAttribute("patient", p);
				return "patient_edit";
			} else {
				model.addAttribute("message", "Patient not found.");
				model.addAttribute("patient", p);
				return "patient_get";
			}
			
		}catch (SQLException e) {
			model.addAttribute("message", "SQL Error. " + e.getMessage());
			model.addAttribute("patient", p);
			return "doctor_get";
		}
	}

	/*
	 * Process changes to patient profile.
	 */
	@PostMapping("/patient/edit")
	public String updatePatient(Patient p, Model model) {
		
		// Check street validation.
		if (!CheckValidation(p.getStreet())) {
			model.addAttribute("message", "Error: invalid street.");
			model.addAttribute("patient", p);
			return "patient_edit";
		}
		
		// City validation
		if (!IsAlphabetStr(p.getCity()) || !CheckValidation(p.getCity())) {
			model.addAttribute("message", "Error: City name invalid.");
			model.addAttribute("patient", p);
			return "patient_edit";
		}
		
		// check doctor name for validation errors
		if (!CheckValidation(p.getPrimaryName())) {
			model.addAttribute("message", "Error: doctor name invalid");
			model.addAttribute("patient", p);
			return "patient_edit";
		}
		
		if (!IsAlphabetStr(p.getState()) || !CheckValidation(p.getCity())) {
			model.addAttribute("message", "Error: State name invalid.");
			model.addAttribute("patient", p);
			return "patient_edit";
		}

		// Check zip code
		if (!IsNumeric(p.getZipcode()) || (p.getZipcode().length() != 5)) {
			model.addAttribute("message", "Error: Zipcode invalid. Must be numeric or 5 digit zip code.");
			model.addAttribute("patient", p);
			return "patient_edit";
		}
		
		try (Connection con = getConnection();){
			
			PreparedStatement ps = con.prepareStatement("update patient set street=?, city=?, "
					+ "state=?, zip=?, doctor_id=? where patient_id =?");
			ps.setString(1, p.getStreet());
			ps.setString(2, p.getCity());
			ps.setString(3, p.getState());
			ps.setString(4, p.getZipcode());
			ps.setInt(6, p.getPatientId());
			
			//get Doctor ID
			int tmpID = GetDocID(p.getPrimaryName());
			
			System.out.println(tmpID);
			
			if(tmpID < 1) {
				 model.addAttribute("message", "Error couldn't find a doctor.");
				 model.addAttribute("patient", p); 
			return "patient_edit";
			}
			
			//ps.setInt(5,  p.getPrimaryID());
			//ps.setInt(5, 2);
			ps.setInt(5, tmpID);
			
			int rc = ps.executeUpdate();
			if(rc==1) {
				model.addAttribute("message", "Update successful");
				model.addAttribute("patient", p);
				return "patient_show";
			}
			
		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error: " + e.getMessage());
			model.addAttribute("patient", p);
			return "patient_edit";
		}

		model.addAttribute("patient", p);
		return "patient_show";
	}

	/*
	 * return JDBC Connection using jdbcTemplate in Spring Server
	 */

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	
	// Validation input.

	private static boolean CheckValidation(String street) {

		String charList = "<>&”’()#&;+-";

		for (int i = 0; i < street.length(); i++) {

			for (int j = 0; j < charList.length(); j++) {
				if (street.charAt(i) == charList.charAt(j)) {
					return false;
				}
			}

		}

		return true;
	}
	
	 private int GetDocID(String docID) {
		 
		 int tmpID = -1;
		 
		 String fName = "";
		 String lName = "";
		 
		 int index = docID.lastIndexOf(' ');
		 
		 if(index == -1) {
			 fName = docID;
		 } else {
			 fName = docID.substring(0, index);
			 lName = docID.substring(index + 1);
		 }
	 
		 try(Connection con = getConnection();){
			 
			 PreparedStatement ps = con.prepareStatement("select id from doctor where first_name =? and last_name =?"); 
			 ps.setString(1, fName);
			 ps.setString(2, lName);

			 ResultSet rs = ps.executeQuery(); 
			 
			 if(rs.next()) { 
				tmpID = rs.getInt("id"); 
			 }
		 } catch (SQLException e) { 
			 System.out.println(e); 
			 return -1; 
		 }
		 return tmpID; 
	 }
	 
	 
	 private String GetDocName(int id) {
		 
		 String tmpFullName = "";
		 
		 try(Connection con = getConnection();){
			 PreparedStatement ps = con.prepareStatement("select last_name, first_name from doctor where id=?");
			 ps.setInt(1, id);
			 
			 ResultSet rs = ps.executeQuery();
			 
			 if(rs.next()) {
				 tmpFullName = rs.getString("first_name") + " " + rs.getString("last_name");
			 }
			 
		 } catch (SQLException e) {
			 System.out.println(e); 
			 tmpFullName = "";
			 return tmpFullName;
		 }
		  
		 return tmpFullName;
	 }

	 private static boolean CheckBday(String bDay) {
		///////////////////////
		// checking year format
		//////////////////////
		
		if(bDay == "" || bDay == null) {
			return false;
		}
		
		String tmpYear = "";
		for (int i = 0; i < 4; i++) {
			tmpYear += bDay.charAt(i);
		}

		// check any non numeric in year
		if (!IsNumeric(tmpYear)) {
			return false;
		}

		// check if year is 0000
		if (tmpYear == "0000" || (Integer.parseInt(tmpYear) < 1900) || (Integer.parseInt(tmpYear) > 2022)) {
			return false;
		}

		//////////////
		// check month
		//////////////
		String tmpMM = "";
		tmpMM += bDay.charAt(5);
		tmpMM += bDay.charAt(6);

		if (!IsNumeric(tmpMM)) {
			return false;
		}

		if (tmpMM == "00" || (Integer.parseInt(tmpMM) > 12)) {
			return false;
		}

		///////////
		// check day
		//////////
		String tmpDD = "";
		tmpDD += bDay.charAt(8);
		tmpDD += bDay.charAt(9);

		if (!IsNumeric(tmpDD)) {
			return false;
		}

		// check range
		if (tmpDD == "00" || (Integer.parseInt(tmpDD) > 31)) {
			return false;
		}

		return true;
	}

	public static boolean IsSSN(String ssn) {

		// check if null or blank 
		if(ssn == "") {
			return false;
		}
		
		// check length
		if (ssn.length() > 9 || ssn.length() < 9) {
			return false;
		}
		
		//check if its numeric
		if(!IsNumeric(ssn)) {
			return false;
		}

		// check starting number
		if (ssn.charAt(0) == '0' || ssn.charAt(0) == '9') {
			return false;
		}

		// check middle numbers
		if (ssn.charAt(3) == '0' && ssn.charAt(4) == '0') {
			return false;
		}

		// check last for digits
		String tmpStr = "";
		for (int i = 5; i < ssn.length(); i++) {
			tmpStr += ssn.charAt(i);
		}

		if (tmpStr == "0000") {
			return false;
		}

		return true;
	}

	public static boolean IsAlphabetStr(String str) {

		if ((str != null) && (!str.equals("")) && (str.matches("^[a-zA-Z\\s]*$"))) {
			return true;
		} else {
			return false;
		}

	}

	public static boolean IsNumeric(String str) {

		if ((str != null) && (!str.equals("")) && (str.matches("^[0-9_]+$"))) {
			return true;
		} else {
			return false;
		}
	}

}
