package com.csumb.cst363;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@Controller    
public class ControllerPrescriptionCreate {

	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/*
	 * Doctor requests blank form for new prescription.
	 */
	@GetMapping("/prescription/new")
	public String newPrescripton(Model model) {
		model.addAttribute("prescription", new Prescription());
		return "prescription_create";
	}
	
	/* 
	 * Process the new prescription form.
	 * 1.  Validate that Doctor SSN exists and matches Doctor Name.
	 * 2.  Validate that Patient SSN exists and matches Patient Name.
	 * 3.  Validate that Drug name exists.
	 * 4.  Insert new prescription.
	 * 5.  If error, return error message and the prescription form
	 * 6.  Otherwise, return the prescription with the rxid number that was generated by the database.
	 */
	@PostMapping("/prescription")
	public String newPrescription( Prescription p, Model model) {		
		
		// TODO 
		try (Connection con = getConnection();) {
			// for DEBUG 
			System.out.println("start newPrescription "+ p);
			
			//retrieve doctor id if one exists with this ssn and name
			PreparedStatement ps = con.prepareStatement("select id from doctor where last_name = ? AND first_name = ? AND ssn = ?");
			ps.setString(1, p.getDoctorLastName());
			ps.setString(2, p.getDoctorFirstName());
			ps.setString(3, p.getDoctor_ssn());
			
			//retrieve patient id if one exists with this ssn and name
			PreparedStatement ps1 = con.prepareStatement("select patient_id from patient where last_name = ? AND first_name = ? AND ssn = ?");
			ps.setString(1, p.getPatientLastName());
			ps.setString(2, p.getPatientFirstName());
			ps.setString(3, p.getPatient_ssn());
			
			//retrieve drug id if one exists with this name
			PreparedStatement ps2 = con.prepareStatement("select drug_id from drug where trade_name = ?");
			ps.setString(1, p.getDrugName());
			
			//execute queries to find if an id exists for dr, patient and drug matching info entered		
			ResultSet rs = ps.executeQuery();
			ResultSet rs1 = ps1.executeQuery();
			ResultSet rs2 = ps2.executeQuery();
			
			// for DEBUG 
			System.out.println("end setPrescription "+ p);
			
			if(rs.next()) {			//if doctor exists
				if(rs1.next()) {		//if patient exists
					if(rs2.next()) {		//if drug exists
						
						// for DEBUG 
						System.out.println("start setPrescription "+ p);
						//process insert new prescription
						PreparedStatement ps3 = con.prepareStatement("insert into prescription(quantity, patient_id,  doctor_id, drug_id ) values(?, ?, ?, )", Statement.RETURN_GENERATED_KEYS);		
						ps3.setInt(1, p.getQuantity());
						ps3.setInt(2, rs1.getInt(1));
						ps3.setInt(3, rs.getInt(1));
						ps3.setInt(4, rs2.getInt(1));				
											
						ps3.executeUpdate();
						//to save my life, I cannot get this part to work to retrieve the key so it will commit the change. 
						ResultSet rs3 = ps3.getGeneratedKeys();
						if (rs3.next()) {
							long rs3L =rs3.getLong(1);
							String rs3Str =rs3L+"";
							p.setRxid(rs3Str);
						}
						
						// for DEBUG 
						System.out.println("end setPrescription "+p);
						return "prescription_show";
						
						
						
						
					}else {
						model.addAttribute("message", "Drug not found.");
						return "prescription_create";
					}
				}else {
					model.addAttribute("message", "Patient not found.");
					return "prescription_create";
				}
				
			}else {
				model.addAttribute("message", "Doctor not found.");
				return "prescription_create";
			}

			
		} catch(SQLException e) {
			System.out.println("SQL error in newPrescription "+e.getMessage());
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("prescription", p);
			return "prescription_create";
		}
		
		// set fake data for auto-generated prescription id.
//		p.setRxid("RX1980031234");
		
//		model.addAttribute("message", "Prescription created.");
//		model.addAttribute("prescription", p);
//		return "prescription_show";
	}
	
	/*
	 * return JDBC Connection using jdbcTemplate in Spring Server
	 */

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}
	
}