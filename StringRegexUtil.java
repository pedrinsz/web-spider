package crawler.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringRegexUtil {
  private static Logger log = LoggerFactory.getLogger(StringRegexUtil.class);
  
  public static LocalDate extractAsLocalDate(String source, String regex, String pattern, int group) {
    try {
      String date = extractAsString(source, regex, group, true);
      if (StringUtils.isNoneBlank(date)) return LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern));
    } catch (DateTimeParseException ex) {
      log.error(ex.getMessage());
    }
    return null;
  }
  
  public static LocalDate extractAsLocalDate(String source, String regex, int group) {
    return extractAsLocalDate(source, regex, "dd/MM/yyyy", group);
  }
  
  public static LocalDate extractAsLocalDate(String source, String regex) {
    return extractAsLocalDate(source, regex, 1);
  }
  
  public static LocalDateTime extractAsLocalDateTime(String source, String regex) {
    return extractAsLocalDateTime(source, regex, "dd/MM/yyyy HH:mm:ss");
  }
  
  public static LocalDateTime extractAsLocalDateTime(String source, String regex, String pattern) {
    String result = extractAsString(source, regex);
    try {
      if (StringUtils.isNoneBlank(result)) 
        return LocalDateTime.parse(result, DateTimeFormatter.ofPattern(pattern));
      return null;
    } catch(RuntimeException ex) {
      return null;
    }
  }
  
  public static LocalTime extractAsLocalTime(String source, String regex) {
    String result = extractAsString(source, regex);
    try {
      if (StringUtils.isNoneBlank(result)) return LocalTime.parse(result);
      return null;
    } catch (RuntimeException ex) {
      return null;
    }
  }
  
  public static String extractAsString(String source, String regex, int group) {
    return extractAsString(source, regex, group, true);
  }

  public static String extractAsString(String source, String regex, int group, boolean trim) {
    Matcher m = trim 
        ? Pattern.compile(regex, Pattern.DOTALL).matcher(source.replaceAll("\u00A0", " ").trim().replaceAll("\\s{2,}", " ")) 
        : Pattern.compile(regex, Pattern.DOTALL).matcher(source);
        
    while (m.find())
      return trim 
          ? m.group(group).replaceAll("\u00A0", " ").trim().replaceAll("\\s{2,}", " ").replaceAll("\\u0000", "")
          : m.group(group).replaceAll("\u00A0", " ").replaceAll("\\u0000", "");
    
    return "";
  }
  
  public static List<String> extractAsStringList(String source, String regex, int groupIndex) {
    Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
    Matcher matcher = pattern.matcher(source);
    List<String> resultList = new ArrayList<String>();
    while (matcher.find()) {
      resultList.add(matcher.group(groupIndex).trim());
    }
    return resultList;
  }
  
  public static List<String> extractAsStringList(String source, String regex, int groupIndex, int flags) {
    Pattern pattern = Pattern.compile(regex, flags);
    Matcher matcher = pattern.matcher(source);
    List<String> resultList = new ArrayList<String>();
    while (matcher.find()) {
      resultList.add(matcher.group(groupIndex).trim());
    }
    return resultList;
  }
  public static String extractAsString(String source, String regex) {
    return extractAsString(source, regex, 1, true).trim();
  }
  
  public static String extractAsString(String source, String regex, boolean trim) {
    return extractAsString(source, regex, 1, trim);
  }
}
