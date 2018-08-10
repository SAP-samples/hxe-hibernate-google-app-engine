package com.sap.hana.hibernate.sample.web;

import static org.springframework.data.geo.Metrics.MILES;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sap.hana.hibernate.sample.repositories.IncidentClusterRepository;
import com.sap.hana.hibernate.sample.util.Constants;

@Controller
public class ClusterController extends AbstractController {

	private static final List<Distance> DISTANCES = Arrays.asList( new Distance( 0.5, MILES ), new Distance( 1, MILES ),
			new Distance( 2, MILES ), new Distance( 5, MILES ), new Distance( 10, MILES ), new Distance( 20, MILES ) );
	private static final Distance DEFAULT_DISTANCE = new Distance( 20, Metrics.MILES );

	private final IncidentClusterRepository repository;

	public ClusterController(IncidentClusterRepository repository) {
		this.repository = repository;
	}

	/**
	 * Build the visualization page
	 * 
	 * @param model The current UI model
	 * @param pageable The current page information
	 * @return The name of the view for rendering the page
	 */
	@RequestMapping(value = "/cluster", method = RequestMethod.GET)
	public String cluster(Model model, Pageable pageable) {

		Point<G2D> point = DEFAULT_LOCATIONS.get( "Lombard St" );

		List<String> categories = this.repository.findCategories();

		model.addAttribute( "categories", categories );
		model.addAttribute( "distances", DISTANCES );
		model.addAttribute( "selectedDistance", DEFAULT_DISTANCE );
		model.addAttribute( "location", point );
		model.addAttribute( "locations", DEFAULT_LOCATIONS );
		model.addAttribute( "dateFrom", SDF.format( this.repository.findMinDate() ) );
		model.addAttribute( "dateTo", SDF.format( new Date() ) );
		model.addAttribute( "api", Constants.CLUSTER_API_PATH_TEMPLATE );
		model.addAttribute( "apiWithCategory", Constants.CLUSTER_WITH_CATEGORY_API_PATH_TEMPLATE );
		model.addAttribute( "searchApi", Constants.ADDRESS_API_PATH_TEMPLATE );

		translateUI( model );

		return "cluster";
	}
}
