package com.sap.hana.hibernate.sample.web;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.codec.Wkt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;

public abstract class AbstractController {

	protected static final Map<String, Point<G2D>> DEFAULT_LOCATIONS;
	protected static final SimpleDateFormat SDF = new SimpleDateFormat( "yyyy-MM-dd" );

	/*
	 * Create the map of default locations
	 */
	static {

		Map<String, Point<G2D>> locations = new HashMap<>();

		locations.put( "SAP SF", (Point<G2D>) Wkt.fromWkt( "SRID=4326;POINT (-122.3978736 37.6664341 )" ) );
		locations.put( "Lombard St", (Point<G2D>) Wkt.fromWkt( "SRID=4326;POINT (-122.41876602172852 37.802154870427394)" ) );
		locations.put( "Golden Gate Park", (Point<G2D>) Wkt.fromWkt( "SRID=4326;POINT (-122.4862289428711 37.76942564512426)" ) );

		DEFAULT_LOCATIONS = Collections.unmodifiableMap( locations );
	}

	@Value("${translation.enabled:false}")
	private boolean translateEnabled;

	@Value("${translation.api.key}")
	private String translationApiKey;

	/**
	 * Translate the UI to the current user locale using the Google Cloud Translation API.
	 * 
	 * @param model The current UI model
	 */
	@SuppressWarnings("deprecation")
	protected void translateUI(Model model) {
		if ( !this.translateEnabled ) {
			return;
		}

		String language = LocaleContextHolder.getLocale().getLanguage();

		Translate translate;
		if ( this.translationApiKey == null ) {
			translate = TranslateOptions.getDefaultInstance().getService();
		}
		else {
			translate = TranslateOptions.newBuilder().setApiKey( this.translationApiKey ).build().getService();
		}

		model.addAttribute( "locationText", translateTerm( translate, "Location:", "en", language ) );
		model.addAttribute( "distanceText", translateTerm( translate, "Distance:", "en", language ) );
		model.addAttribute( "categoryText", translateTerm( translate, "Category:", "en", language ) );
		model.addAttribute( "dateText", translateTerm( translate, "Date:", "en", language ) );
		model.addAttribute( "submitText", translateTerm( translate, "Submit:", "en", language ) );
		model.addAttribute( "visualizeText", translateTerm( translate, "Visualize:", "en", language ) );
		model.addAttribute( "analyzeText", translateTerm( translate, "Analyze:", "en", language ) );
		model.addAttribute( "findAddressText", translateTerm( translate, "Find address", "en", language ) );
		model.addAttribute( "resultsText", translateTerm( translate, "Results", "en", language ) );
	}

	private String translateTerm(Translate translate, String term, String sourceLanguage, String targetLanguage) {
		// TODO
		return term;
	}
}
