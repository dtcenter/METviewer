package edu.ucar.metviewer;

import java.awt.*;
import java.io.PrintStream;
import java.sql.*;
import java.util.*;
import java.util.regex.*;
import java.text.*;

public class MVUtil{
	
	public static final double INVALID_DATA = -9999;
	
	public static final Pattern _patProb		= Pattern.compile("PROB\\(([\\w\\d]+)([<>=]+)([^\\)]+)\\)");

	public static String getSQLDateFormat(String field){ return "DATE_FORMAT(" + field + ", '%Y-%m-%d %H:%i:%s')"; }
	
	/**
	 * Query the database for a list of all ordered distinct fcst_init_begs and return them for use as 
	 * an aggregate value list.  If a date range is desired, specify either or both the begin and
	 * end dates in the SQL format YYYY-MM-dd HH:mm:ss.  
	 * @param con database connection to use for query
	 * @param begin database field name for date of interest, for example fcst_valid_beg or fcst_init_beg
	 * @param begin optional begin date for list
	 * @param end optional end date for list
	 * @param hour optional 24-hour clock hour value for restricting the search
	 * @return list containing the requested dates in SQL format
	 */
	public static String[] buildDateAggList(Connection con, String field, String begin, String end, String hour){
		ArrayList listDates = new ArrayList();
		try{
			String strWhere = "";
			if( (null != begin && !begin.equals("")) && (null != end && !end.equals("")) ){
				strWhere = "WHERE " + field + " BETWEEN '" + begin + "' AND '" + end + "' ";
			} else if( null != begin && !begin.equals("") ) {
				strWhere = "WHERE " + field + " >= '" + begin + "' ";
			} else if( null != end && !end.equals("") ){
				strWhere = "WHERE " + field + " <= '" + end + "' ";
			}
			
			if( null != hour && !hour.equals("") ){
				strWhere += ( strWhere.equals("") ? "WHERE" : "AND" ) + " HOUR(" + field + ") = '" + hour + "' "; 
			}
			
			Statement stmt = con.createStatement();
			String strTable = (field.equalsIgnoreCase("fcst_valid") || field.equalsIgnoreCase("fcst_init")? "mode_header" : "stat_header");
			ResultSet res = stmt.executeQuery("SELECT DISTINCT " + getSQLDateFormat(field) + "FROM " + strTable + " " + strWhere + "ORDER BY " + field);
			for(int i=0; res.next(); i++){ listDates.add( res.getString(1) ); }
			stmt.close();
		} catch (Exception e) {
			System.err.println("  **  ERROR: caught " + e.getClass() + " in buildDateAggList(): " + e.getMessage());
			e.printStackTrace();
		}
		return (String[])listDates.toArray(new String[]{});
	}
	public static String[] buildDateAggList(Connection con, String field, String begin, String end){ return buildDateAggList(con, field, begin, end, "");   }
	public static String[] buildDateAggList(Connection con, String field, String inithour)         { return buildDateAggList(con, field, "", "", inithour); }
	public static String[] buildDateAggList(Connection con, String field)                          { return buildDateAggList(con, field, "", "", "");       }

	/**
	 * Build a list of strings representing consecutive dates between the input dates start and
	 * end, incrementing by incr number of seconds.  It is assumed that the format of start and
	 * end is given by the java date format string format.  The output dates will have the same
	 * format. 
	 * @param start Beginning date, given in the format specified by the format input
	 * @param end End date, given in the format specified by the format input
	 * @param incr Number of seconds to increment between successive dates in the list
	 * @param format Java date format string, describing input and output dates
	 * @return List of date strings
	 */
	public static String[] buildDateList(String start, String end, int incr, String format){
		SimpleDateFormat formatDate = new SimpleDateFormat(format);
		formatDate.setTimeZone(TimeZone.getTimeZone("UTC"));
		ArrayList listDates = new ArrayList();
		
		try{
			java.util.Date dateStart = formatDate.parse(start);
			java.util.Date dateEnd = formatDate.parse(end);
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));			
			cal.setTime(dateStart);
			
			while( (incr > 0 && cal.getTime().getTime() <= dateEnd.getTime()) ||
				   (incr < 0 && cal.getTime().getTime() >= dateEnd.getTime()) ){
				listDates.add( formatDate.format(cal.getTime()) );
				cal.add(Calendar.SECOND, incr);
			}
		
		}catch(Exception e){
			System.out.println("  **  ERROR: caught " + e.getClass() + " in buildDateList(): " + e.getMessage());
		}
		return (String[])listDates.toArray(new String[]{});
	}
	
	/**
	 * Build a String representation of the date specified by the input <date_offset> {@link MVNode}.  The offset
	 * is taken either from the current date (default) or from the date specified by the input date.
	 * @param node MVNode structure specifying the offset
	 * @param format (optional) String representation of the input/output date formats
	 * @param date (optional) String representation of the date from which to offset
	 * @return String representation of the offset date
	 */
	public static String parseDateOffset(MVNode node, String format, String date){
		int intOffset = 0;
		int intHour = 0;
		
		for(int i=0; i < node._children.length; i++){
			MVNode nodeChild = node._children[i];
			if     ( nodeChild._tag.equals("day_offset") ){ intOffset = Integer.parseInt(nodeChild._value); }
			else if( nodeChild._tag.equals("hour")       ){ intHour   = Integer.parseInt(nodeChild._value); }
		}
		
		SimpleDateFormat formatOffset = new SimpleDateFormat(format);
		formatOffset.setTimeZone(TimeZone.getTimeZone("UTC"));
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		try{
			cal.setTime( formatOffset.parse(date) );
		} catch(Exception e){}
		cal.set(Calendar.HOUR_OF_DAY, intHour);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.add(Calendar.DATE, intOffset);
		
		return formatOffset.format(cal.getTime());
	}
	public static String parseDateOffset(MVNode node, String format){ return parseDateOffset(node, format, null); }
	public static String parseDateOffset(MVNode node){ return parseDateOffset(node, "yyyy-MM-dd", null); }
	
	/**
	 * Concatenate the elements of the input list with surrounding ticks and separated by commas for
	 * use in the where clause of a SQL query.  For example, the function call  
	 * <code>buildValueList(new String[]{"a", "bb", "c"})</code> will return the string 
	 * "'a', 'bb', 'c'".
	 * @param values The list of values to be concatenated
	 * @return The string of concatenated values for use in a SQL where clause
	 */
	public static String buildValueList(String[] values){
		String strValueList = "";		
		for(int i=0; null != values && i < values.length; i++){
			strValueList += (0 < i? ", " : "") + "'" + values[i] + "'";
		}
		return strValueList;
	}
	
	/**
	 * Create a {@link MVDataTable} whose fields are the keys of the input table and whose rows 
	 * represent every permutation of the values stored in the input table.  It is assumed that
	 * the table contains a mapping from String to String[].  If the input table is an 
	 * {@link MVOrderedMap}, the fields of the output MVDataTable are ordered in the same order as 
	 * the keys of the input.      
	 * @param table Contains key/value pairs of String/String[] which will be permuted
	 * @return MVDataTable whose rows are the permutations
	 */
	public static MVDataTable permute(MVOrderedMap table){
		
		//  use the ordered list of table entries, if appropriate
		Map.Entry[] listVals = {};
		listVals = ((MVOrderedMap)table).getOrderedEntries();
		
		//  if the input table contains a single value, build and return the simplest table
		if( 1 == listVals.length ){
			MVDataTable dtRet = new MVDataTable();
			String strField = (String)listVals[0].getKey(); 
			Object objVal = listVals[0].getValue();

			//  handle simple field value lists
			if( objVal instanceof String[] ){
				dtRet.addField(strField);
				String[] listVal = (String[])listVals[0].getValue();
				for(int i=0; i < listVal.length; i++){
					MVOrderedMap tableRow = new MVOrderedMap();
					tableRow.put(strField, listVal[i]);
					dtRet.addRow(tableRow);
				}
			
			//  handle field value sets
			} else if( objVal instanceof MVOrderedMap ){
				dtRet.addField(strField + "_set");
				Map.Entry[] listValSet = ((MVOrderedMap)objVal).getOrderedEntries();
				for(int i=0; i < listValSet.length; i++){
					MVOrderedMap tableRow = new MVOrderedMap();
					tableRow.put(strField + "_set", listValSet[i].getKey());
					dtRet.addRow(tableRow);
				}
			}
			return dtRet;
		}
		
		//  if the input table contains more than one value, build the sub-table first
		String strField = (String)listVals[0].getKey();
		MVOrderedMap tableSub = new MVOrderedMap(table);
		tableSub.remove(strField);
		MVDataTable dtSub = permute(tableSub);
		
		//  build a new table with one copy of the sub-table for each value of the current field
		MVOrderedMap[] listRows = dtSub.getRows();
		MVDataTable dtRet = new MVDataTable(dtSub.getFields());
		Object objVal = listVals[0].getValue();
		
		//  handle simple field value lists
		if( objVal instanceof String[] ){
			dtRet.addField(strField, "", 0);
			String[] listVal = (String[])listVals[0].getValue();
			for(int i=0; i < listVal.length; i++){
				for(int j=0; j < listRows.length; j++){
					MVOrderedMap tableRow = new MVOrderedMap(listRows[j]);
					tableRow.put(strField, listVal[i]);
					dtRet.addRow(tableRow);
				}
			}
			
		//  handle field value sets
		} else if( objVal instanceof MVOrderedMap ){
			dtRet.addField(strField + "_set", "", 0);
			Map.Entry[] listValSet = ((MVOrderedMap)objVal).getOrderedEntries();
			for(int i=0; i < listValSet.length; i++){
				for(int j=0; j < listRows.length; j++){
					MVOrderedMap tableRow = new MVOrderedMap(listRows[j]);
					tableRow.put(strField + "_set", listValSet[i].getKey());
					dtRet.addRow(tableRow);
				}
			}
		}

		return dtRet;
	}
	
	public static SimpleDateFormat _formatDB = null;
	public static SimpleDateFormat _formatDBms = null;
	public static SimpleDateFormat _formatPlot = null;
	public static SimpleDateFormat _formatStat = null;
	public static SimpleDateFormat _formatFcstLead = null;
	public static SimpleDateFormat _formatBase = null;
	public static SimpleDateFormat _formatDate = null;
	static{
		try{ _formatDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");		_formatDB.setTimeZone(TimeZone.getTimeZone("UTC"));			}catch(Exception e){}
		try{ _formatDBms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");	_formatDBms.setTimeZone(TimeZone.getTimeZone("UTC"));		}catch(Exception e){}
		try{ _formatPlot = new SimpleDateFormat("yyyyMMddHH");				_formatPlot.setTimeZone(TimeZone.getTimeZone("UTC"));		}catch(Exception e){}
		try{ _formatStat = new SimpleDateFormat("yyyyMMdd_HHmmss");			_formatStat.setTimeZone(TimeZone.getTimeZone("UTC"));		}catch(Exception e){}
		try{ _formatFcstLead = new SimpleDateFormat("HHmmss");				_formatFcstLead.setTimeZone(TimeZone.getTimeZone("UTC"));	}catch(Exception e){}
		try{ _formatBase = new SimpleDateFormat("yyyyMMdd'b'");				_formatBase.setTimeZone(TimeZone.getTimeZone("UTC"));		}catch(Exception e){}
		try{ _formatDate = new SimpleDateFormat("yyyyMMdd");				_formatDate.setTimeZone(TimeZone.getTimeZone("UTC"));		}catch(Exception e){}
	}
	
	/**
	 * Populate a template string, specified by tmpl, with values specified in the input map vals.  If
	 * a template tag is not found in the input vals table, a warning is printed and the tag is passed
	 * through to the output.  
	 * @param tmpl Template String containing tags with format <tag_name>
	 * @param vals Contains a mapping from tag names to values
	 * @param tmplMaps Map of value maps for each template field, used with map template parm (optional) 
	 * @return String built using the template and values
	 */
	public static String buildTemplateString(String tmpl, MVOrderedMap vals, MVOrderedMap tmplMaps){
		
		
		String strRet = new String(tmpl);
		Matcher matTmpl = _patPlotTmpl.matcher(tmpl);
		while( matTmpl.find() ){			
			String strTmplTag = matTmpl.group(1);
			String strTmplTagName = matTmpl.group(2);
			
			MVOrderedMap mapParms = parseTagParams(strTmplTag);
			//String strVal = ((String)vals.get(strTmplTagName)).replace(" ", "_");
			if( strTmplTagName.equals("date") ){
				vals.put("date", _formatDate.format(new java.util.Date()));				
			}
			
			if( !vals.containsKey(strTmplTagName) ){
				System.out.println("  **  WARNING: template tag " + strTmplTagName + " not found in agg perm");
				continue;
			}

			String strVal = ((String)vals.get(strTmplTagName));			
			
			//  if there is a corresponding tag value map, use the map value
			if( mapParms.containsKey("map") && mapParms.getStr("map").equalsIgnoreCase("true") &&
				null != tmplMaps && tmplMaps.containsKey(strTmplTagName) ){
				MVOrderedMap mapTmplVal = (MVOrderedMap)tmplMaps.get(strTmplTagName);
				strVal = (mapTmplVal.containsKey(strVal)? mapTmplVal.getStr(strVal) : strVal);
			}
			
			//  if there is a format parameter, apply it to the value
			if( mapParms.containsKey("format") ){
				String strFormat = mapParms.getStr("format");
				
				if( strTmplTagName.equals("fcst_lead") ){
					if( strFormat.equals("HH") ){		strVal = strVal.substring(0, strVal.length() - 4);		}
					if( strFormat.equals("HHmm") ){		strVal = strVal.substring(0, strVal.length() - 2);		}
					while( strFormat.length() > strVal.length() ){ strVal = "0" + strVal; }

				} else if( strTmplTagName.equals("init_hour") || strTmplTagName.equals("valid_hour") ){
					if( strFormat.equals("HH") ){		while(2 > strVal.length()){ strVal = "0" + strVal; }	}					
				}
				
				if( mapParms.getStr("format").equalsIgnoreCase("R") ){
					strVal = formatR(strVal);
				}
			}
			
			//  if the tag value is a date, format it accordingly
			java.util.Date dateParse = null;
			try{
				if( null != (dateParse = _formatDB.parse(strVal)) || null != (dateParse = _formatDBms.parse(strVal)) ){
					strVal = _formatPlot.format(dateParse);
				}
			}catch(Exception e){}
			
			//  if the tag is a threshold, format it accordingly
			if( strTmplTagName.equals("fcst_thresh") || strTmplTagName.equals("fcst_thr") ||
				strTmplTagName.equals("obs_thresh")  || strTmplTagName.equals("obs_thr") ){
				strVal = formatFcstThresh(strTmplTag, strVal);
			}
			
			strRet = strRet.replace("{" + strTmplTag + "}", strVal);
		}
		return strRet;
	}
	public static String buildTemplateString(String tmpl, MVOrderedMap vals){ return buildTemplateString(tmpl, vals, null); }
	
	public static final Pattern _patPlotTmpl	= Pattern.compile("\\{((\\w+)(?:\\?[^}]*)?)\\}");
	
	/**
	 * Reformat the fcst_thresh value using the directions provided in the body of the template
	 * tag.  It is assumed that the input template tag has the parameterized tag format:
	 *   
	 *   <i>fcst_thresh?param1=val1;param2=val2[;...]  where the params can be the following:</i>
	 *   <ul>
	 *   <li><b>units</b> set to either mm or in (input assumed to be in mm) 
	 *   <li><b>format</b> set to the java formatting string to apply, for example 0.00# 
	 *   <li><b>symbol</b> set to either letters or math, for example ge or >=, respectively
	 *   </ul> 
	 * 
	 * @param fcstTag Template tag name (including params) for fcst_thresh
	 * @param fcstThresh Template map value to be formatted
	 * @return
	 */
	public static String formatFcstThresh(String fcstTag, String fcstThresh){
		String strThreshRet = fcstThresh;
		MVOrderedMap mapParams = parseTagParams(fcstTag);
		DecimalFormat format = new DecimalFormat("0.000");

		//  attempt to parse the input threshold
		String strSymbol = "", strThresh = "";
		double dblThresh = -1.0;
		Matcher matFcstThresh = _patFcstThresh.matcher(fcstThresh);
		if( matFcstThresh.matches() ){
			strSymbol = matFcstThresh.group(1);
			strThresh = matFcstThresh.group(2);
			dblThresh = Double.parseDouble(strThresh);
		} else {
			System.out.println("  **  WARNING: threshhold " + fcstThresh + " not matched");
			return strThreshRet;
		}
		
		//  change the units, if requested
		if( mapParams.containsKey("units") ){
			String strUnits = mapParams.get("units").toString();
			if( strUnits.equals("in") ){
				strThresh = format.format( dblThresh /= 25.4 );
				strThreshRet = strSymbol + strThresh;						
			}
		}
		
		//  change the format, if requested
		if( mapParams.containsKey("format") ){
			String strFormat = mapParams.get("format").toString();
			strThresh = (new DecimalFormat(strFormat)).format(dblThresh);
			strThreshRet = strSymbol + strThresh;			
		}
		
		//  change the logic symbol, if requested
		if( mapParams.containsKey("symbol") ){
			String strSymbolType = mapParams.get("symbol").toString();
			if( strSymbolType.equals("letters") ){
				strSymbol = strSymbol.replace("==", "eq")
									 .replace("!=", "ne")
									 .replace("<=", "le")
									 .replace(">=", "ge")
									 .replace("<",  "lt")
									 .replace(">",  "gt");
				strThreshRet = strSymbol + strThresh;
			}
		}
		
		return strThreshRet;
	}
	public static final Pattern _patFcstThresh = Pattern.compile("([<>=!]{1,2})(\\d+\\.\\d+)");
	
	/**
	 * Parse template tag parameter pairs and return them in an ordered map.  For example,
	 * <i>parseTagParams("tag_name?param1=val1;param2=val2")</i> returns a map with two 
	 * members, param1 and param2 with their values set accordingly.
	 * @param tag Formatted tag with param/value pairs to parse
	 * @return Ordered map containing parsed param/value pairs
	 */
	public static MVOrderedMap parseTagParams(String tag){
		MVOrderedMap mapRet = new MVOrderedMap();
		Matcher mat = _patTag.matcher(tag);
		if( mat.matches() && null != mat.group(2) ){
			String[] listPairs = mat.group(2).split("\\s*;\\s*");
			for(int i=0; i < listPairs.length; i++){
				String[] listPair = listPairs[i].split("\\s*=\\s*");
				mapRet.put(listPair[0], listPair[1]);
			}
		}

		return mapRet;
	}
	public static final Pattern _patTag = Pattern.compile("([\\w\\d]+)(?:\\s*\\?(.*))?");	
	
	/**
	 * Pads input str with spaces appended to the end so that the length of the returned String is 
	 * at least width characters
	 * @param str The string to pad
	 * @param width The minimum number of characters in the returned String 
	 * @return the padded version of the input str
	 */
	public static String padEnd(String str, int width){
		while( width > str.length() ) str += " ";
		return str;
	}
	public static String padEnd(String str){ return padEnd(str, 16); }
	
	/**
	 * Pads input str with spaces appended to the beginning so that the length of the returned String 
	 * is at least width characters
	 * @param str The string to pad
	 * @param width The minimum number of characters in the returned String 
	 * @return the padded version of the input str
	 */
	public static String padBegin(String str, int width){
		while( width > str.length() ) str = " " + str;
		return str;
	}
	public static String padBegin(String str){ return padBegin(str, 16); }
	
	/**
	 * Create a string representation for the input time span, which should represent milliseconds
	 * between events.  For example, a time span message can be generated as follows:
	 * <code>formatTimeStamp(dateEnd.getTime() - dateStart.getTime())</code>
	 * @param span Time span, in milliseconds
	 * @return Time span in format [days]d H:mm:ss.mmmm
	 */
	public static String formatTimeSpan(long span){
		long intDay	= span / (24l * 60l * 60l * 1000l);
		span		-= intDay * 24l * 60l * 60l * 1000l;
		long intHr	= span / (60l * 60l * 1000l);
		span		-= intHr * 60l * 60l * 1000l;
		long intMin	= span / (60l * 1000l);
		span		-= intMin * 60l * 1000l;
		long intSec	= span / 1000l;
		span		-= intSec * 1000l;
		long intMS	= span;
				
		return (0 < intDay? "" + intDay + "d " : "") +	"" + intHr + 
			   (10 > intMin? ":0" : ":") + intMin + (10 > intSec? ":0" : ":") + intSec + "." +
			   (100 > intMS? "0" + (10 > intMS? "0" : "") : "") + intMS;						
	}
	
	/**
	 * Generate a list of ARGB hex color strings which sample the "rainbow" color continuum at the
	 * requested number of point in the direction of red to violet.  Mimics the R function of the
	 * same name. 
	 * @param num The number of samples on the rainbow continuum to generate
	 * @return The list of color hex strings
	 */
	public static String[] rainbow(int num){
		if( 1 > num )	{ return new String[]{};			}
		if( 1 == num )	{ return new String[]{"#FF0000FF"}; }
		
		String[] listRet = new String[num];
		double dblInc = 1.0 / (double)(num-1);
		double dblVal = 0;
		for(int i=0; i < num; i++, dblVal += dblInc){
			listRet[i] = Integer.toHexString( interpolateColor(dblVal).getRGB() ).toUpperCase();
			listRet[i] = "#" + listRet[i].substring(2) +  "FF";
		}
		return listRet;
	}
	
	public static Color interpolateColor(double rel){
		if( rel < 0.0 )		return new Color(1f, 0f, 0f);
		else if( rel > 1.0)	return new Color(1f, 0f, 1f);
	
		float min = 0f;
		float max = 1f;
		
		switch( (int)(rel/0.16667) ){
			/*
			case 0:				return new Color(max, max*(min + (1-min)*(float)(rel/.25)), min);
			case 1:	rel -= .25;	return new Color(min + max*(1-min)*(float)(1 - rel/.25), max, max*(min + (1-min)*(float)(rel/.25)));
			case 2:	rel -= .50;	return new Color(min, max*(min + (1-min)*(float)(1 - rel/.25)), max);
			case 3:	rel -= .75;	return new Color(max*(min + (1-min)*(float)(rel/.25)), min, max);
			*/
			case 0:					return new Color(max, max*(min + (1-min)*(float)(rel/.25)), min);
			case 1:	rel -= .16667;	return new Color(min + max*(1-min)*(float)(1 - rel/.25), max, min);
			case 2:	rel -= .33333;	return new Color(min, max, max*(min + (1-min)*(float)(rel/.25)));
			case 3:	rel -= .50000;	return new Color(min, max*(1-min)*(float)(1 - rel/.25), max);
			case 4:	rel -= .66667;	return new Color(max*(min + (1-min)*(float)(rel/.25)), min, max);
			case 5:	rel -= .83333;	return new Color(max, min, max*(1-min)*(float)(1 - rel/.25));
			default:				return new Color(max, min, max);
		}
	}

	
	/**
	 * Creates a list of length rep of copies of the input val.  Mimics the R function of the same
	 * name 
	 * @param val Value to repeat
	 * @param rep Number of time to repeat
	 * @return List of repeated values, with length specified by input rep
	 */
	public static String[] rep(String val, int rep){
		if(1 > rep){ return new String[]{}; }
		String[] listRet = new String[rep];
		for(int i=0; i < rep; i++){ listRet[i] = val; }
		return listRet;
	}
	
	/**
	 * Creates a list of length rep of copies of the input val.  Mimics the R function of the same
	 * name 
	 * @param val Value to repeat
	 * @param rep Number of time to repeat
	 * @return List of repeated values, with length specified by input rep
	 */
	public static Integer[] rep(int val, int rep){
		if(1 > rep){ return new Integer[]{}; }
		Integer[] listRet = new Integer[rep];
		for(int i=0; i < rep; i++){ listRet[i] = new Integer(val); }
		return listRet;
	}
	
	/**
	 * Creates a list of the same length as the input list with the elements in reverse order
	 * @param list List to reverse
	 * @return Reversed list
	 */
	public static String[] rev(String[] list){
		String[] ret = new String[list.length];
		for(int i=list.length-1; i >= 0; i--){ ret[i] = list[i]; }
		return ret;
	}
	
	/**
	 * Build a list by removing elements of the input list at the specified frequency.
	 * @param list List to decimate
	 * @param freq (optional) Frequency at which to remove members from the input list, defaults to 30
	 * @return Decimated list
	 */
	public static String[] decimate(String[] list, int freq){
		String[] ret = new String[list.length];
		for(int i=0; i < list.length; i++){ ret[i] = (i % freq == 0? list[i] : ""); }
		return ret;
	}
	public static String[] decimate(String[] list){
		return decimate( list, Math.round( (float)list.length / 30f ) );
	}
	
	
	/**
	 * Append the first array with the values of the second
	 * @param l1 Array to be appended
	 * @param l2 Array to append
	 * @return The combined array
	 */
	public static Object[] append(Object[] l1, Object[] l2, Object[] cast){
		ArrayList listRet = new ArrayList();
		for(int i=0; i < l1.length; i++){ listRet.add(l1[i]); }
		for(int i=0; i < l2.length; i++){ listRet.add(l2[i]); }
		return listRet.toArray(cast);
		
		/*
		List listRet = Arrays.asList(l1);
		listRet.addAll( Arrays.asList(l2) );
		return listRet.toArray();
		*/
	}	
	public static String[]		append( String[] s1,	String[] s2)	{ return (String[])append(s1, s2, new String[]{});			}
	public static String[]		append( String s1,		String[] s2)	{ return append(new String[]{s1}, s2);						}
	public static String[]		append( String[] s1,	String s2)		{ return append(s1, new String[]{s2});						}
	public static Map.Entry[]	append( Map.Entry[] s1,	Map.Entry[] s2)	{ return (Map.Entry[])append(s1, s2, new Map.Entry[]{});	}
	public static MVPlotJob[]	append( MVPlotJob[] s1, MVPlotJob[] s2)	{ return (MVPlotJob[])append(s1, s2, new MVPlotJob[]{});	}
	
	public static String[] unique(String[] data){
		Hashtable table = new Hashtable();
		for(int i=0; i < data.length; i++){ if( !table.containsKey(data[i]) ){ table.put(data[i], "true"); } }
		return (String[])table.keySet().toArray(new String[]{});
	}
	
	public static int sum(int[] data){
		if( 1 > data.length ){ return (int)0; }
		int intSum = 0;
		for(int i=0; i < data.length; i++){ intSum += data[i]; }
		return intSum;
	}
	
	/**
	 * Attempt to convert the input ArrayList, which is assumed to contain all Strings, to a String[]. 
	 * @param list ArrayList to convert
	 * @return Converted list
	 */
	public static String[]		toArray(ArrayList list)					{ return (String[])list.toArray(new String[]{});			}
	
	/**
	 * Create a deep copy of the input list
	 * @param list List to copy
	 * @return Copied list
	 */
	public static MVOrderedMap[] copyList(MVOrderedMap[] list){
		MVOrderedMap[] listRet = new MVOrderedMap[list.length];
		for(int i=0; i < list.length; i++){ listRet[i] = new MVOrderedMap(list[i]); }		
		return listRet;
	}

	/**
	 * Create a deep copy of the input list
	 * @param list List to copy
	 * @return Copied list
	 */
	public static String[] copyList(String[] list){
		String[] listRet = new String[list.length];
		for(int i=0; i < list.length; i++){ listRet[i] = list[i]; }
		return listRet;
	}
	
	/**
	 * Creates a string representation of an R collection containing the list of values in the input
	 * list, val.
	 * @param val List of values to print in the R collection
	 * @param ticks (optional) Print tickmarks around values, for when constituents are factors as 
	 *              opposed to numeric, defaults to true
	 * @return String representation of the R collection
	 */
	public static String printRCol(Object[] val, boolean ticks){
		String strRet = "c(";
		for(int i=0; i < val.length; i++){
			strRet += (0 < i? ", " : "") + (val[i] instanceof String && ticks? "\"" + val[i] + "\"" : val[i]);
		}
		strRet += ")";		
		return strRet;
	}
	public static String printRCol(Object[] val){ return printRCol(val, true); }
	
	public static String[] parseRCol(String strRCol){
		Matcher matRCol = Pattern.compile("c\\(\\s*\"(.*)\"\\s*\\)").matcher(strRCol);
		if( !matRCol.matches() ){ return new String[]{}; }		
		String strList = matRCol.group(1);
		return strList.split("\"\\s*,\\s*\"");
	}
	
	/**
	 * Returns a string representation of the MVOrderedMap in R declaration syntax
	 * @param map Data structure to convert to R list representation
	 * @return The R-syntax representation of the input map
	 */
	public static String getRDecl(MVOrderedMap map){
		String strRDecl = "list(\n";
		String[] listKeys = (String[])map._listKeys.toArray(new String[]{});
		for(int i=0; i < listKeys.length; i++){
			strRDecl += (0 < i? ",\n" : "") + MVBatch.padBegin( formatR(listKeys[i]) ) + " = ";
			Object objVal = map.get(listKeys[i]);
			if( objVal instanceof String ){
				strRDecl += "\"" + objVal.toString() + "\"";
			} else if( objVal instanceof String[] ){
				strRDecl += "c(";
				String[] listVal = (String[])objVal;
				for(int j=0; j < listVal.length; j++){
					strRDecl += (0 < j? ", " : "") + "\"" + listVal[j] + "\"";
				}
				strRDecl += ")";
			} else if( objVal instanceof MVOrderedMap ){
				strRDecl += ((MVOrderedMap)objVal).getRDecl();
			} else {
				strRDecl += "\"???\",\n";
			}
		}
		strRDecl += "\n)";
		return strRDecl;
	}
	
	
	/**
	 * Format the input String so that it conforms to R variable name standards  
	 * @param in String to format
	 * @return Formatted String
	 */
	public static String formatR(String in){
		
		String strFormatR = in;
		Matcher matProb = _patProb.matcher(in);
		if( matProb.matches() ){
			if( !in.contains("*") ){ strFormatR = "PROB_" + matProb.group(1) + matProb.group(2) + matProb.group(3); }
			else                   { strFormatR = "PROB_" + matProb.group(1);                                       } 
		}		
		
		return strFormatR.replace("(",	"")
						 .replace(")",	"")
						 .replace(".",	"_d_")
						 .replace("<=",	"le")
						 .replace(">=",	"ge")
						 .replace("=",	"eq")
						 .replace("<",	"lt")
						 .replace(">",	"gt");
	}
	
	/*
	public static class TxtProgBar{
		
		protected double _dblValue = 0;
		protected double _dblMaxValue = 100;
		protected int _intLength = 21;
		protected PrintStream _str = System.out;
		
		public TxtProgBar(PrintStream str, double value, double maxValue, int length){
			_str = str;
			_dblValue = value;
			_dblMaxValue = maxValue;
			_intLength = (5 > length? 5 : (0 == length % 2 ? length + 1 : length));
			
			int intMid = _intLength / 2;
			for(int i=0; i < _intLength; i++){
				if     ( 0 == i )              { _str.print("|"); }
				else if( _intLength - 1 == i ) { _str.print("|"); }
				else if( intMid == i )         { _str.print("v"); }
				else                           { _str.print("_"); }
			}
			_str.println();
		}
		public TxtProgBar(double value, double maxValue, int length){ this(System.out, value, maxValue, length); }
		public TxtProgBar(double maxValue, int length)              { this(System.out, 0, maxValue, length);     }
		public TxtProgBar(double maxValue)                          { this(System.out, 0, maxValue, 21);         }
		public TxtProgBar(int length)                               { this(System.out, 0, 100, length);          }
		public TxtProgBar()                                         { this(System.out, 0, 100, 21);              }
		
		public void updateProgress(double value){
			if( _dblValue > value ){ return; }

			int intProgPrev = (int)Math.round( (_dblValue / _dblMaxValue) * (double)_intLength );
			int intProgCur  = (int)Math.round( (value / _dblMaxValue) * (double)_intLength);
			
			int intMid = _intLength / 2;
			for(int i=intProgPrev; i < intProgCur; i++){
				if     ( 0 == i )              { _str.print(" "); }
				else if( _intLength - 1 == i ) { _str.print(" "); }
				else if( intMid == i )         { _str.print("^"); }
				else                           { _str.print("^"); }
			}
			_dblValue = value;
			//if( _intLength == intProgCur ){ _str.println(); }		
		}
	}
	*/
}
