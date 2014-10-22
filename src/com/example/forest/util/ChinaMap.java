package com.example.forest.util;
import com.example.forest.bean.Location;

public class ChinaMap {
	public  double pi = 3.14159265358979324;
	public  double a = 6378245.0;
	public  double ee = 0.00669342162296594323;
	
	public Location LocationMake(double lng, double lat) 
	{
	    Location loc = new Location(0, 0) ;
	    loc.lon = lng;
	    loc.lat = lat; 
	    return loc;
	}
	
	boolean outOfChina(double lat, double lon)
	{
	    if (lon < 72.004 || lon > 137.8347)
	        return true;
	    if (lat < 0.8293 || lat > 55.8271)
	        return true;
	    return false;
	}	

	double transformLat(double x, double y)
	{
	    double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(x > 0 ? x:-x);
	    ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 *Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
	    ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
	    ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
	    return ret;
	}	
	
	double transformLon(double x, double y)
	{
	    double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(x > 0 ? x:-x);
	    ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
	    ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
	    ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
	    return ret;
	}	
	
	Location transformFromWGSToGCJ(Location wgLoc)
	{
		double dLat ;
		double dLon ;
		double radLat ;
		double magic;
		double sqrtMagic ;
	    Location mgLoc = new Location(0, 0) ;
	    if ( outOfChina(wgLoc.lat, wgLoc.lon) )
	    {
	        mgLoc = wgLoc;
	        return mgLoc;
	    }
		dLat = transformLat(wgLoc.lon - 105.0, wgLoc.lat - 35.0);
		dLon = transformLon(wgLoc.lon - 105.0, wgLoc.lat - 35.0);
		radLat = wgLoc.lat / 180.0 * pi;
		magic = Math.sin(radLat);
		magic = 1 - ee * magic * magic;
		sqrtMagic =  Math.sqrt(magic);
	    dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
	    dLon = (dLon * 180.0) / (a / sqrtMagic *  Math.cos(radLat) * pi);
	    mgLoc.lat = wgLoc.lat + dLat;
	    mgLoc.lon = wgLoc.lon + dLon;

	    return mgLoc;
	}	
	
	///
	///  Transform GCJ-02 to WGS-84
	///  Reverse of transformFromWGSToGC() by iteration.
	///

	Location transformFromGCJToWGS(Location gcLoc)
	{
	    Location wgLoc =  new Location(0,0);
	    wgLoc.lon = gcLoc.lon ;
	    wgLoc.lat = gcLoc.lat ;
	    Location currGcLoc = null ;
	    Location dLoc = new Location(0,0) ;
	    while ( true ) {
	        currGcLoc = transformFromWGSToGCJ(wgLoc);
	        dLoc.lat = gcLoc.lat - currGcLoc.lat;
	        dLoc.lon = gcLoc.lon - currGcLoc.lon;
	        if ( Math.abs(dLoc.lat) < 1e-7 &&  Math.abs(dLoc.lon) < 1e-7) {  // 1e-7 ~ centimeter level accuracy
	          // Result of experiment:
	          //   Most of the time 2 iterations would be enough for an 1e-8 accuracy (milimeter level).
	          //
	            return wgLoc;
	        }
	        wgLoc.lat += dLoc.lat;
	        wgLoc.lon += dLoc.lon;
	    }

//	    return wgLoc;
	}	
}
