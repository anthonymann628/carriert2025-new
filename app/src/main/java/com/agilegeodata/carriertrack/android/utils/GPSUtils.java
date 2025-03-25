package com.agilegeodata.carriertrack.android.utils;

import static com.agilegeodata.carriertrack.android.constants.GlobalConstants.DEF_DELIVERY_QUADS_RIGHT_FRONT;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import android.location.Location;

import com.agilegeodata.carriertrack.android.constants.GlobalConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Generic GPS Utilities
 * Some code was modified from http://xebee.xebia.in/2010/10/28/working-with-geolocations/
 *
 */
public class GPSUtils{
	private static final String TAG = GPSUtils.class.getSimpleName();

	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);

	public static double distFromAsLocation(double lat1, double lon1, double lat2, double lon2){
		double dist = 0;
		Location loc = convertToLocationFromGeoCode(lat1 + "," + lon1);
		Location loc2 = convertToLocationFromGeoCode(lat2 + "," + lon2);

		//=== distance is meters
		dist = loc.distanceTo(loc2);

		return dist;
	}

	//=== SEARCH
	public static final Location getLocationByGeoCode(String geoCode){
		Location l = new Location("My Location");
		if(geoCode != null){
			String[] geoArr = geoCode.split(",");
			try{
				double lat = new Double(geoArr[0].trim());
				double lon = new Double(geoArr[1].trim());
				l.setLatitude(lat);
				l.setLongitude(lon);
			}
			catch(Exception e){
				//DO SOMETHING HERE
			}
		}

		return l;
	}

	public static double getDistanceToPointOnEllipseCircumferenceWithBearingUsingMajorMinorAxis(double bearing, double majorAxis, double minorAxis){
		//     radius =                      majorAxis * minorAxis
		//                  ----------------------------------------------------------
		//                                               2                          2
		//                  sqrt((minorAxis * cos(theta)) + (majorAxis * sin(theta)) )

		double distance = 0f;
		double minorPart = cos(bearing) * minorAxis;
		double majorPart = sin(bearing) * majorAxis;

		distance = (majorAxis * minorAxis) / Math.sqrt((minorPart * minorPart) + (majorPart * majorPart));

		return distance;
	}

	public static String getRelativeQuadrantEnglishString(int quadrant){
		//=== QUADRANT IS RELATIVE TO bearing of travel
		String quadrantLabel = "NONE";

		if(quadrant == GlobalConstants.DEF_DELIVERY_QUADS_NONE){
			quadrantLabel = "NONE";
		}
		else{
			quadrantLabel = "";

			if((quadrant & GlobalConstants.DEF_DELIVERY_QUADS_RIGHT_FRONT) > 0){
				quadrantLabel += "RIGHT_FRONT ";
			}
			if((quadrant & GlobalConstants.DEF_DELIVERY_QUADS_RIGHT_REAR) > 0){
				quadrantLabel += "RIGHT_REAR ";
			}
			if((quadrant & GlobalConstants.DEF_DELIVERY_QUADS_LEFT_REAR) > 0){
				quadrantLabel += "LEFT_REAR ";
			}
			if((quadrant & GlobalConstants.DEF_DELIVERY_QUADS_LEFT_FRONT) > 0){
				quadrantLabel += "LEFT_FRONT ";
			}
		}

		return quadrantLabel;
	}

	public static int determineRelativeQuadrant_Common(double azimuthOfTravel, double azimuthOfTarget){
		//=== QUADRANT IS RELATIVE TO azimuth of travel
		int quadrant = 0;

		double quadrantAzimuthToTargetTheta = azimuthOfTarget - azimuthOfTravel;
		if(quadrantAzimuthToTargetTheta < 0){
			quadrantAzimuthToTargetTheta += 360;
		}

		if(quadrantAzimuthToTargetTheta > 0.0000001 && quadrantAzimuthToTargetTheta <= 90){
			quadrant = DEF_DELIVERY_QUADS_RIGHT_FRONT;  //forward and to the right
		}
		else if(quadrantAzimuthToTargetTheta > 90.000001 && quadrantAzimuthToTargetTheta <= 180){
			quadrant = GlobalConstants.DEF_DELIVERY_QUADS_RIGHT_REAR;    //back and to the right
		}
		else if(quadrantAzimuthToTargetTheta > 180.000001 && quadrantAzimuthToTargetTheta <= 270){
			quadrant = GlobalConstants.DEF_DELIVERY_QUADS_LEFT_REAR;    //back and to the left
		}
		else if(quadrantAzimuthToTargetTheta > 270.000001 && quadrantAzimuthToTargetTheta <= 360){
			quadrant = GlobalConstants.DEF_DELIVERY_QUADS_LEFT_FRONT;    //forward and to the left
		}

		return quadrant;
	}

	public static float normalizeBearingToAzimuth(float value){
		if(value >= 0.0f && value <= 180.0f){
			return value;
		}
		else{
			return 180 + (180 + value);
		}
	}

	public static double normalizeAzimuthToEllipseThetaAngle(double azimuth){
		double theta = azimuth;

		while(theta > 90){
			theta -= 90;
		}

		return theta;
	}

	public static final Location convertToLocationFromGeoCode(String geoCode){
		Location l = new Location("");

		if(geoCode != null){
			String[] geoArr = geoCode.split(",");
			try{
				double lat = new Double(geoArr[0].trim());
				double lon = new Double(geoArr[1].trim());
				l.setLatitude(lat);
				l.setLongitude(lon);
			}
			catch(Exception e){
				logger.error("Exception", e);
			}
		}

		return l;
	}

	static public Location movePoint(double latitude, double longitude, double distanceInMetres, double bearing){
		double brngRad = toRadians(bearing);
		double latRad = toRadians(latitude);
		double lonRad = toRadians(longitude);
		int earthRadiusInMetres = 6371000;
		double distFrac = distanceInMetres / earthRadiusInMetres;

		double latitudeResult = Math.asin(sin(latRad) * Math.cos(distFrac) + Math.cos(latRad) * Math.sin(distFrac) * Math.cos(brngRad));
		double a = Math.atan2(sin(brngRad) * Math.sin(distFrac) * Math.cos(latRad), Math.cos(distFrac) - Math.sin(latRad) * Math.sin(latitudeResult));
		double longitudeResult = (lonRad + a + 3 * PI) % (2 * PI) - PI;

		double newLatitude = Math.toDegrees(latitudeResult);
		double newLongitude = Math.toDegrees(longitudeResult);

		Location newLocation = new Location("");
		newLocation.setLatitude(newLatitude);
		newLocation.setLongitude(newLongitude);

		return newLocation;
	}

	/**
	 * Method used to convert the value form radians to degrees
	 * @param rad
	 * @return value in degrees
	 */
	public static double rad2deg(double rad){
		return (rad * 180.0 / PI);
	}

	/**
	 * Converts the value from Degrees to radians
	 * @param deg
	 * @return value in radians
	 */
	public static double deg2rad(double deg){
		return (deg * PI / 180.0);
	}
}
