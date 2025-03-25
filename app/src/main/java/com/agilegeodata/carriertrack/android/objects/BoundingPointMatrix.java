package com.agilegeodata.carriertrack.android.objects;

public class BoundingPointMatrix{
	private static final double EARTH_RADIUS_M = 6371009;

	BoundingPoint bottomLeft;
	BoundingPoint topLeft;
	BoundingPoint bottomRight;
	BoundingPoint topRight;
	BoundingPoint center;

	/*
	 * BoundingPointMatrix is the set of all geocodes that fall within a box
	 * This box is larger than the delivery area to gather look ahead points
	 */
	public BoundingPointMatrix(String geoCode, int distanceFront, int distanceSide, float bearing){

		center = new BoundingPoint(geoCode);
		BoundingPoint[] arr = getExtremeBoundingPointsFrom(center, distanceFront, distanceSide, bearing);

		//=== DATABASE QUERIES EXPECT LOWEST VALUE FIRST IN A 'LAT BETWEEN X AND Y' CLAUSE
		bottomLeft = arr[0];
		bottomRight = arr[1];
		topRight = arr[2];
		topLeft = arr[3];

	}

	/**
	 * Returns an array of two extreme BoundingPoints corresponding to center BoundingPoint and
	 * the distanceSide from the center BoundingPoint. These extreme BoundingPoints are the BoundingPoints
	 * with max/min latitude and longitude.
	 * @param centerPoint
	 * @param distanceSide
	 * @return
	 */
	public static BoundingPoint[] getExtremeBoundingPointsFrom(BoundingPoint centerPoint, int distanceFront, int distanceSide, float azimuth){
//		logger.debug("START");
		int directionOfTravelOrientedQuadrant = 0;

		if(azimuth <= 315 && azimuth > 225){
			directionOfTravelOrientedQuadrant = 2;
		}
		else if(azimuth <= 225 && azimuth > 135){
			directionOfTravelOrientedQuadrant = 4;
		}
		else if(azimuth <= 135 && azimuth > 45){
			directionOfTravelOrientedQuadrant = 1;
		}
		else{
			directionOfTravelOrientedQuadrant = 3;
		}
//		logger.debug("directionOfTravelOrientedQuadrant = " + directionOfTravelOrientedQuadrant);

		double longDiff = 0d;
		double latDiff = 0d;

		if(directionOfTravelOrientedQuadrant == 1 || directionOfTravelOrientedQuadrant == 2){   //we are moving east or west
			longDiff = getExtremeLongitudesDiffForBoundingPoint(centerPoint, distanceFront);
			latDiff = getExtremeLatitudesDiffForBoundingPoint(centerPoint, distanceSide);
		}
		else if(directionOfTravelOrientedQuadrant == 3 || directionOfTravelOrientedQuadrant == 4){  // we are moving north or south
			longDiff = getExtremeLongitudesDiffForBoundingPoint(centerPoint, distanceSide);
			latDiff = getExtremeLatitudesDiffForBoundingPoint(centerPoint, distanceFront);
		}
//		logger.debug("longDiff = " + longDiff);
//		logger.debug("latDiff = " + latDiff);

//		logger.debug("centerPoint = " + centerPoint);
		double rightSideLongitude = centerPoint.getLongitude() + longDiff;
		double leftSideLongitude = centerPoint.getLongitude() - longDiff;
		double topSideLatitude = centerPoint.getLatitude() + latDiff;
		double bottomSideLatitude = centerPoint.getLatitude() - latDiff;

		BoundingPoint pBottomRight = new BoundingPoint(bottomSideLatitude, rightSideLongitude);
//		logger.debug("pBottomRight = " + pBottomRight);
		pBottomRight = validatePoint(pBottomRight);

		BoundingPoint pBottomLeft = new BoundingPoint(bottomSideLatitude, leftSideLongitude);
//		logger.debug("pBottomLeft = " + pBottomLeft);
		pBottomLeft = validatePoint(pBottomLeft);

		BoundingPoint pTopRight = new BoundingPoint(topSideLatitude, rightSideLongitude);
//		logger.debug("pTopRight = " + pTopRight);
		pTopRight = validatePoint(pTopRight);

		BoundingPoint pTopLeft = new BoundingPoint(topSideLatitude, leftSideLongitude);
//		logger.debug("pTopLeft = " + pTopLeft);
		pTopLeft = validatePoint(pTopLeft);

		return new BoundingPoint[]{pBottomLeft, pBottomRight, pTopRight, pTopLeft};
	}

	/**
	 * Validates if the point passed has valid values in degrees i.e. latitude lies between -90 and +90 and the longitude
	 * @param bPoint
	 * @return
	 */
	public static BoundingPoint validatePoint(BoundingPoint bPoint){
		if(bPoint.getLatitude() > 90){
			bPoint.setLatitude(90 - (bPoint.getLatitude() - 90));
		}
		if(bPoint.getLatitude() < -90){
			bPoint.setLatitude(-90 - (bPoint.getLatitude() + 90));
		}
		if(bPoint.getLongitude() > 180){
			bPoint.setLongitude(-180 + (bPoint.getLongitude() - 180));
		}
		if(bPoint.getLongitude() < -180){
			bPoint.setLongitude(180 + (bPoint.getLongitude() + 180));
		}

		return bPoint;
	}

	/**
	 * Returns the difference in degrees of longitude corresponding to the
	 * distance from the center point. This distance can be used to find the
	 * extreme points.
	 * @param p1
	 * @param distance
	 * @return
	 */
	public static double getExtremeLongitudesDiffForBoundingPoint(BoundingPoint p1, double distance){
		//=== THIS FUNCTION ADJUSTS FOR LONGITUDAL CONVERGENCE AT A GIVEN LATITUDE
		double lat1 = p1.getLatitude();
		lat1 = deg2rad(lat1);
		double longitudeRadius = Math.cos(lat1) * EARTH_RADIUS_M;
		double diffLong = (distance / longitudeRadius);
		diffLong = rad2deg(diffLong);
		return diffLong;
	}

	/**
	 * Returns the difference in degrees of latitude corresponding to the
	 * distance from the center BoundingPoint. This distance can be used to find the
	 * extreme BoundingPoints.
	 * @param p1
	 * @param distance
	 * @return
	 */
	public static double getExtremeLatitudesDiffForBoundingPoint(BoundingPoint p1, double distance){
		double latitudeRadians = distance / EARTH_RADIUS_M;
		double diffLat = rad2deg(latitudeRadians);

		return diffLat;
	}

	/**
	 * Method used to convert the value form radians to degrees
	 * @param rad
	 * @return value in degrees
	 */
	public static double rad2deg(double rad){
		return (rad * 180.0 / Math.PI);
	}

	/**
	 * Converts the value from Degrees to radians
	 * @param deg
	 * @return value in radians
	 */
	public static double deg2rad(double deg){
		return (deg * Math.PI / 180.0);
	}

	public String toString(){
		String me =
				"Center = " + center.latitude + " " + center.longitude + "\n" +
				"Bottom Left = " + bottomLeft.latitude + " " + bottomLeft.longitude + "\n" +
				"Bottom Right = " + bottomRight.latitude + " " + bottomRight.longitude + "\n" +
				"Top Right = " + topRight.latitude + " " + topRight.longitude + "\n" +
				"Top Left = " + topLeft.latitude + " " + topLeft.longitude + "\n";
		return me;
	}

	public BoundingPoint getBottomLeft(){
		return bottomLeft;
	}

	public void setBottomLeft(BoundingPoint bottomLeft){
		this.bottomLeft = bottomLeft;
	}

	public BoundingPoint getTopLeft(){
		return topLeft;
	}

	public void setTopLeft(BoundingPoint topLeft){
		this.topLeft = topLeft;
	}

	public BoundingPoint getBottomRight(){
		return bottomRight;
	}

	public void setBottomRight(BoundingPoint bottomRight){
		this.bottomRight = bottomRight;
	}

	public BoundingPoint getTopRight(){
		return topRight;
	}

	public void setTopRight(BoundingPoint topRight){
		this.topRight = topRight;
	}

	public BoundingPoint getCenter(){
		return center;
	}

	public void setCenter(BoundingPoint center){
		this.center = center;
	}
}
