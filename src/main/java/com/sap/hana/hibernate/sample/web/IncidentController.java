package com.sap.hana.hibernate.sample.web;

import static org.springframework.data.geo.Metrics.MILES;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sap.hana.hibernate.sample.repositories.IncidentRepository;
import com.sap.hana.hibernate.sample.util.Constants;

@Controller
public class IncidentController extends AbstractController {

	private static final List<Distance> DISTANCES = Arrays.asList( new Distance( 0.1, MILES ), new Distance( 0.5, MILES ),
			new Distance( 1, MILES ), new Distance( 2, MILES ), new Distance( 5, MILES ), new Distance( 10, MILES ) );
	private static final Distance DEFAULT_DISTANCE = new Distance( 0.5, Metrics.MILES );

	private final IncidentRepository repository;

	public IncidentController(IncidentRepository repository) {
		this.repository = repository;
	}

	/**
	 * Build the analysis page
	 * 
	 * @param model The current UI model
	 * @param pageable The current page information
	 * @return The name of the view for rendering the page
	 */
	@RequestMapping(value = "/analyze", method = RequestMethod.GET)
	public String analyze(Model model, Pageable pageable) {

		Point point = DEFAULT_LOCATIONS.get( "SAP SF" );

		List<String> categories = this.repository.findCategories();

		model.addAttribute( "categories", categories );
		model.addAttribute( "distances", DISTANCES );
		model.addAttribute( "selectedDistance", DEFAULT_DISTANCE );
		model.addAttribute( "location", point );
		model.addAttribute( "locations", DEFAULT_LOCATIONS );
		model.addAttribute( "dateFrom", SDF.format( this.repository.findMinDate() ) );
		model.addAttribute( "dateTo", SDF.format( new Date() ) );
		model.addAttribute( "api", Constants.INCIDENT_API_PATH_TEMPLATE );
		model.addAttribute( "apiCategory", Constants.INCIDENT_WITH_CATEGORY_API_PATH_TEMPLATE );
		model.addAttribute( "searchApi", Constants.ADDRESS_API_PATH_TEMPLATE );

		translateUI( model );

		return "analyze";
	}

}
