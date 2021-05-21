package com.morningstar.automation.base.core.report;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class DefaultReportStyle {
    /*Default Style*/
    static String basicTableStyle = "table { width:85%;font-size:x-small}, th, td {font-family: Trebuchet MS,"
            + "Arial, Helvetica, sans-serif;border: 1px solid black; border-collapse: collapse;} "
            + "th, td { padding: 5px; text-align: left;}";

    static String buildInfoTableStyle = "table#build td {background-color: lightgrey;font-size:x-small;padding:0px;margin:0px;margin-auto:0px;mso-line-height-rule: exactly;line-height:100%;} table#build p{font-family: Trebuchet MS, Arial, Helvetica, sans-serif;border}";
    static String summaryTableStyle = "table#summary th{background-color: darkblue;color: white;}";
    static String failedTableStyle = "table#failedList th{background-color: #ff2222;color: white;}";
    static String passedTableStyle = "table#passedList th{background-color: #4AA02C;color: white;}";
    static String subHeader = "h4{font-family: Trebuchet MS,Arial, Helvetica, sans-serif;text-align: left;}";

    static String longestTimeTDStyle = "td.longestTime{color: red;}";
    static String longerTimeTDStyle = "td.longerTime{color: yellow;}";

    public static String createBody(String body) {
        return "<body>" + body + "</body>";
    }

    public static String createTable(String tableValue, String id) {
        return "<table id=" + id + ">" + tableValue + "</table>";
    }

    public static String createTable(String[] headers, String tableBody, String id) {
        String finalHeader = "";
        for (String header : headers) {
            finalHeader = finalHeader + createHeaderColumn(header);
        }
        return createTable(finalHeader + tableBody, id);
    }

    public static String createRow(String rowValue) {
        return "<tr>" + rowValue + "</tr>";
    }

    public static String createHeaderColumn(String columnName) {
        return "<th>" + columnName + "</th>";
    }

    public static String createColumn(String columnValue) {
        return "<td>" + columnValue + "</td>";
    }

    public static String createColumn(String columnValue, int timeLevel) {
        String style = "";
        if(timeLevel > 0) style = "class='longestTime'";
        else if(timeLevel == 0) style = "class='longerTime'";
        return "<td "+style+">" + columnValue + "</td>";
    }

    public static String createScreenshotLink(String link) {
        String linkStr = "";
        try {
            linkStr = URLEncoder.encode(link,"utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "<a href='" + link + "'>Screenshot Link</a>";
    }

    public static String createHeader(String header) {
        return "<h4>" + header + "</h4>";
    }

    public static String createStyle(String style){
        return "<style>" + style + "</style>";
    }

    public static String getDefaultStyle(){
        String res = "";
        res += basicTableStyle;
        res += buildInfoTableStyle;
        res += summaryTableStyle;
        res += failedTableStyle;
        res += passedTableStyle;
        res += subHeader;
        res += longestTimeTDStyle;
        res += longerTimeTDStyle;
        return "<style>" + res + "</style>";
    }
}

