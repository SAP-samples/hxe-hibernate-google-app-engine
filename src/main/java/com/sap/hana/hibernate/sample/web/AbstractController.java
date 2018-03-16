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

		locations.put( "SAP SF", (Point<G2D>) Wkt.fromWkt( "SRID=4326;POINT (37.6664341 -122.3978736)" ) );
		locations.put( "Lombard St", (Point<G2D>) Wkt.fromWkt( "SRID=4326;POINT (37.802154870427394 -122.41876602172852)" ) );
		locations.put( "Golden Gate Park", (Point<G2D>) Wkt.fromWkt( "SRID=4326;POINT (37.76942564512426 -122.4862289428711)" ) );

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

		String location = translate
				.translate( "Location:", TranslateOption.sourceLanguage( "en" ), TranslateOption.targetLanguage( language ) )
				.getTranslatedText();
		model.addAttribute( "locationText", location );

		String distance = translate
				.translate( "Distance:", TranslateOption.sourceLanguage( "en" ), TranslateOption.targetLanguage( language ) )
				.getTranslatedText();
		model.addAttribute( "distanceText", distance );

		String category = translate
				.translate( "Category:", TranslateOption.sourceLanguage( "en" ), TranslateOption.targetLanguage( language ) )
				.getTranslatedText();
		model.addAttribute( "categoryText", category );

		String date = translate
				.translate( "Date:", TranslateOption.sourceLanguage( "en" ), TranslateOption.targetLanguage( language ) )
				.getTranslatedText();
		model.addAttribute( "dateText", date );

		String submit = translate
				.translate( "Submit", TranslateOption.sourceLanguage( "en" ), TranslateOption.targetLanguage( language ) )
				.getTranslatedText();
		model.addAttribute( "submitText", submit );

		String visualize = translate
				.translate( "Visualize", TranslateOption.sourceLanguage( "en" ), TranslateOption.targetLanguage( language ) )
				.getTranslatedText();
		model.addAttribute( "visualizeText", visualize );

		String analyze = translate
				.translate( "Analyze", TranslateOption.sourceLanguage( "en" ), TranslateOption.targetLanguage( language ) )
				.getTranslatedText();
		model.addAttribute( "analyzeText", analyze );

		String findAddress = translate.translate( "Find address", TranslateOption.sourceLanguage( "en" ),
				TranslateOption.targetLanguage( language ) ).getTranslatedText();
		model.addAttribute( "findAddressText", findAddress );

		String results = translate
				.translate( "Results", TranslateOption.sourceLanguage( "en" ), TranslateOption.targetLanguage( language ) )
				.getTranslatedText();
		model.addAttribute( "resultsText", results );
	}
}
