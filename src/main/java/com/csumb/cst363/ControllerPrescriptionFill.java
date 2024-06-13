package com.csumb.cst363;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller   
public class ControllerPrescriptionFill {

	@Autowired
	private JdbcTemplate jdbcTemplate;


	/* 
	 * Patient requests form to search for prescription.
	 */
	@GetMapping("/prescription/fill")
	public String getfillForm(Model model) {
		model.addAttribute("prescription", new Prescription());
		return "prescription_fill";
	}


	/*
	 * Process the prescription fill request from a patient.
	 * 1.  Validate that Prescription p contains rxid, pharmacy name and pharmacy address
	 *     and uniquely identify a prescription and a pharmacy.
	 * 2.  update prescription with pharmacyid, name and address.
	 * 3.  update prescription with today's date.
	 * 4.  Display updated prescription 
	 * 5.  or if there is an error show the form with an error message.
	 */
	@PostMapping("/prescription/fill")
	public String processFillForm(Prescription p,  Model model) {
		
		try (Connection con = getConnection();) {	

		// TODO  				
		
		//check all fields for data
		if(p.getRxid() == "") {
			model.addAttribute("message", "Rx cannot be empty");
			model.addAttribute("prescription", p);
			return "prescription_show";
		} else if(p.getPatientLastName() == "") {
			model.addAttribute("message", "Patient Last Name cannot be empty");
			model.addAttribute("prescription", p);
			return "prescription_show";
		} else if(p.getPharmacyName() == "") {
			model.addAttribute("message", "Patient First Name cannot be empty.");
			model.addAttribute("prescription", p);
			return "prescription_show";
		} else if(p.getPharmacyAddress() == "") {
			model.addAttribute("message", "Pharmacy Name cannot be empty.");
			model.addAttribute("prescription", p);
			return "prescription_show";
		} else if(p.getPharmacyAddress() == "") {
			model.addAttribute("message", "Pharmacy Address cannot be empty.");
			model.addAttribute("prescription", p);
			return "prescription_show";
		}else {
			// uniquely identify a prescription and a pharmacy
//			PreparedStatement ps = con.prepareStatement("select pharmacy_id from pharmacy where name = ?");
//			PreparedStatement ps1 = con.prepareStatement("select drug_id from prescription where rxid = ?");
//			
//			ps.setString(1 , p.getPharmacyName());
//			ps1.setString(1, p.getRxid());
//			
//			ResultSet rs = ps.executeQuery();  //store in rs all fields for pharmacy matching entered name
//			ResultSet rs1 = ps1.executeQuery(); //store in rs1 all fiends for prescription matching rxid
			
			//cannot for the life of me figure out how to do the update
			
//			if(rs.next()) {
//				if(rs1.next()) {
//					PreparedStatement ps3= con.prepareStatement("update fill set pharmacy_name = ?, pharmacy address + ? where fill);
				}//end rs1
			}// end rs
			
			
			
			
//			 * 2.  update prescription with pharmacyid, name and address.
//			 * 3.  update prescription with today's date.
//			 * 4.  Display updated prescription 
//			 * 5.  or if there is an error show the form with an error message.
			
			
		}

		// temporary code to set fake data for now.
//		p.setPharmacyID("70012345");
//		p.setCost(String.format("%.2f", 12.5));
//		p.setDateFilled( new java.util.Date().toString() );
//
//		// display the updated prescription
//
//		model.addAttribute("message", "Prescription has been filled.");
//		model.addAttribute("prescription", p);
//		return "prescription_show";
//
//	}

	/*
	 * return JDBC Connection using jdbcTemplate in Spring Server
	 */

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}