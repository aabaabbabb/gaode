package com.jinyan.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Maps;
import com.google.common.base.Joiner;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.StrKit;

public abstract class StringUtils {

	private static final String FOLDER_SEPARATOR = "/";

	private static final String WINDOWS_FOLDER_SEPARATOR = "\\";

	private static final String TOP_PATH = "..";

	private static final String CURRENT_PATH = ".";

	private static final char EXTENSION_SEPARATOR = '.';

	// ---------------------------------------------------------------------
	// General convenience methods for working with Strings
	// ---------------------------------------------------------------------

	/**
	 * Apply the given relative path to the given path, assuming standard Java
	 * folder separation (i.e. "/" separators);
	 * 
	 * @param path         the path to start from (usually a full file path)
	 * @param relativePath the relative path to apply (relative to the full file
	 *                     path above)
	 * @return the full file path that results from applying the relative path
	 */
	public static String applyRelativePath(String path, String relativePath) {
		int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		if (separatorIndex != -1) {
			String newPath = path.substring(0, separatorIndex);
			if (!relativePath.startsWith(FOLDER_SEPARATOR)) {
				newPath += FOLDER_SEPARATOR;
			}
			return newPath + relativePath;
		} else {
			return relativePath;
		}
	}

	/**
	 * Convenience method to return a String array as a CSV String. E.g. useful for
	 * toString() implementations.
	 * 
	 * @param arr array to display. Elements may be of any type (toString will be
	 *            called on each element).
	 */
	public static String arrayToCommaDelimitedString(Object[] arr) {
		return arrayToDelimitedString(arr, ",");
	}

	/**
	 * Convenience method to return a String array as a delimited (e.g. CSV) String.
	 * E.g. useful for toString() implementations.
	 * 
	 * @param arr   array to display. Elements may be of any type (toString will be
	 *              called on each element).
	 * @param delim delimiter to use (probably a ",")
	 */
	public static String arrayToDelimitedString(Object[] arr, String delim) {
		if (arr == null) {
			return "";
		}

		StringBuffer sb = new StringBuffer();
		int arrLength = arr.length;
		for (int i = 0; i < arrLength; i++) {
			if (i > 0) {
				sb.append(delim);
			}
			sb.append(arr[i]);
		}
		return sb.toString();
	}

	/**
	 * Capitalize a <code>String</code>, changing the first letter to upper case as
	 * per {@link Character#toUpperCase(char)}. No other letters are changed.
	 * 
	 * @param str the String to capitalize, may be <code>null</code>
	 * @return the capitalized String, <code>null</code> if null
	 */
	public static String capitalize(String str) {
		return changeFirstCharacterCase(str, true);
	}

	/**
	 * 
	 * @param card
	 * @return
	 */
	public static String cardCompare(String card) {
		if (StrKit.notBlank(card)) {
			card = card.replaceAll(" ", "");
			if (card != null && !card.equals("")) {
				StringBuffer sb = new StringBuffer();
				sb.append(card.substring(0, 4)).append(" ");
				sb.append(card.substring(4, 8)).append(" ");
				sb.append(card.substring(8, 12)).append(" ");
				if (card.length() == 16) {
					sb.append(card.substring(12, 16));
				} else {
					sb.append(card.substring(12, 15));
				}
				return sb.toString();
			}
		}
		return "";
	}

	private static String changeFirstCharacterCase(String str, boolean capitalize) {
		if (str == null || str.length() == 0) {
			return str;
		}
		StringBuffer buf = new StringBuffer(str.length());
		if (capitalize) {
			buf.append(Character.toUpperCase(str.charAt(0)));
		} else {
			buf.append(Character.toLowerCase(str.charAt(0)));
		}
		buf.append(str.substring(1));
		return buf.toString();
	}

	/**
	 * Normalize the path by suppressing sequences like "path/.." and inner simple
	 * dots.
	 * <p>
	 * The result is convenient for path comparison. For other uses, notice that
	 * Windows separators ("\") are replaced by simple slashes.
	 * 
	 * @param path the original path
	 * @return the normalized path
	 */
	public static String cleanPath(String path) {
		String pathToUse = replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);

		// Strip prefix from path to analyze, to not treat it as part of the
		// first path element. This is necessary to correctly parse paths like
		// "file:core/../core/io/Resource.class", where the ".." should just
		// strip the first "core" directory while keeping the "file:" prefix.
		int prefixIndex = pathToUse.indexOf(":");
		String prefix = "";
		if (prefixIndex != -1) {
			prefix = pathToUse.substring(0, prefixIndex + 1);
			pathToUse = pathToUse.substring(prefixIndex + 1);
		}

		String[] pathArray = delimitedListToStringArray(pathToUse, FOLDER_SEPARATOR);
		List<String> pathElements = new LinkedList<String>();
		int tops = 0;
		int pathArrayLength = pathArray.length;
		for (int i = pathArrayLength - 1; i >= 0; i--) {
			if (CURRENT_PATH.equals(pathArray[i])) {
				// Points to current directory - drop it.
			} else if (TOP_PATH.equals(pathArray[i])) {
				// Registering top path found.
				tops++;
			} else {
				if (tops > 0) {
					// Merging path element with corresponding to top path.
					tops--;
				} else {
					// Normal path element found.
					pathElements.add(0, pathArray[i]);
				}
			}
		}

		// Remaining top paths need to be retained.
		for (int i = 0; i < tops; i++) {
			pathElements.add(0, TOP_PATH);
		}

		return prefix + collectionToDelimitedString(pathElements, FOLDER_SEPARATOR);
	}

	/**
	 * Convenience method to return a Collection as a CSV String. E.g. useful for
	 * toString() implementations.
	 * 
	 * @param coll Collection to display
	 */
	@SuppressWarnings("unchecked")
	public static String collectionToCommaDelimitedString(Collection coll) {
		return collectionToDelimitedString(coll, ",");
	}

	/**
	 * Convenience method to return a Collection as a delimited (e.g. CSV) String.
	 * E.g. useful for toString() implementations.
	 * 
	 * @param coll  Collection to display
	 * @param delim delimiter to use (probably a ",")
	 */
	@SuppressWarnings("unchecked")
	public static String collectionToDelimitedString(Collection coll, String delim) {
		return collectionToDelimitedString(coll, delim, "", "");
	}

	/**
	 * Convenience method to return a Collection as a delimited (e.g. CSV) String.
	 * E.g. useful for toString() implementations.
	 * 
	 * @param coll   Collection to display
	 * @param delim  delimiter to use (probably a ",")
	 * @param prefix string to start each element with
	 * @param suffix string to end each element with
	 */
	@SuppressWarnings("unchecked")
	public static String collectionToDelimitedString(Collection coll, String delim, String prefix, String suffix) {
		if (coll == null) {
			return "";
		}

		StringBuffer sb = new StringBuffer();
		Iterator it = coll.iterator();
		int i = 0;
		while (it.hasNext()) {
			if (i > 0) {
				sb.append(delim);
			}
			sb.append(prefix).append(it.next()).append(suffix);
			i++;
		}
		return sb.toString();
	}

	/**
	 * Convenience method to convert a CSV string list to a set. Note that this will
	 * suppress duplicates.
	 * 
	 * @param str CSV String
	 * @return a Set of String entries in the list
	 */
	@SuppressWarnings("unchecked")
	public static Set commaDelimitedListToSet(String str) {
		Set set = new TreeSet();
		String[] tokens = commaDelimitedListToStringArray(str);
		int tokensLength = tokens.length;
		for (int i = 0; i < tokensLength; i++) {
			set.add(tokens[i]);
		}
		return set;
	}

	// ---------------------------------------------------------------------
	// Convenience methods for working with formatted Strings
	// ---------------------------------------------------------------------

	/**
	 * Convert a CSV list into an array of Strings.
	 * 
	 * @param str CSV list
	 * @return an array of Strings, or the empty array if s is null
	 */
	public static String[] commaDelimitedListToStringArray(String str) {
		return delimitedListToStringArray(str, ",");
	}

	/**
	 * Count the occurrences of the substring in string s.
	 * 
	 * @param str string to search in. Return 0 if this is null.
	 * @param sub string to search for. Return 0 if this is null.
	 */
	public static int countOccurrencesOf(String str, String sub) {
		if (str == null || sub == null || str.length() == 0 || sub.length() == 0) {
			return 0;
		}
		int count = 0, pos = 0, idx = 0;
		while ((idx = str.indexOf(sub, pos)) != -1) {
			++count;
			pos = idx + sub.length();
		}
		return count;
	}

	/**
	 * Delete all occurrences of the given substring.
	 * 
	 * @param pattern the pattern to delete all occurrences of
	 */
	public static String delete(String inString, String pattern) {
		return replace(inString, pattern, "");
	}

	/**
	 * Delete any character in a given string.
	 * 
	 * @param charsToDelete a set of characters to delete. E.g. "az\n" will delete
	 *                      'a's, 'z's and new lines.
	 */
	public static String deleteAny(String inString, String charsToDelete) {
		if (inString == null || charsToDelete == null) {
			return inString;
		}
		StringBuffer out = new StringBuffer();
		int inStringLength = inString.length();
		for (int i = 0; i < inStringLength; i++) {
			char c = inString.charAt(i);
			if (charsToDelete.indexOf(c) == -1) {
				out.append(c);
			}
		}
		return out.toString();
	}

	/**
	 * Take a String which is a delimited list and convert it to a String array.
	 * <p>
	 * A single delimiter can consists of more than one character: It will still be
	 * considered as single delimiter string, rather than as bunch of potential
	 * delimiter characters - in contrast to <code>tokenizeToStringArray</code>.
	 * 
	 * @param str       the input String
	 * @param delimiter the delimiter between elements (this is a single delimiter,
	 *                  rather than a bunch individual delimiter characters)
	 * @return an array of the tokens in the list
	 * @see #tokenizeToStringArray
	 */
	@SuppressWarnings("unchecked")
	public static String[] delimitedListToStringArray(String str, String delimiter) {
		if (str == null) {
			return new String[0];
		}
		if (delimiter == null) {
			return new String[] { str };
		}

		List result = new ArrayList();
		if ("".equals(delimiter)) {
			int strLength = str.length();
			for (int i = 0; i < strLength; i++) {
				result.add(str.substring(i, i + 1));
			}
		} else {
			int pos = 0;
			int delPos = 0;
			while ((delPos = str.indexOf(delimiter, pos)) != -1) {
				result.add(str.substring(pos, delPos));
				pos = delPos + delimiter.length();
			}
			if (str.length() > 0 && pos <= str.length()) {
				// Add rest of String, but not in case of empty input.
				result.add(str.substring(pos));
			}
		}
		return toStringArray(result);
	}

	/**
	 * Test if the given String ends with the specified suffix, ignoring upper/lower
	 * case.
	 * 
	 * @param str    the String to check
	 * @param suffix the suffix to look for
	 * @see java.lang.String#endsWith
	 */
	public static boolean endsWithIgnoreCase(String str, String suffix) {
		if (str == null || suffix == null) {
			return false;
		}
		if (str.endsWith(suffix)) {
			return true;
		}
		if (str.length() < suffix.length()) {
			return false;
		}

		String lcStr = str.substring(str.length() - suffix.length()).toLowerCase();
		String lcSuffix = suffix.toLowerCase();
		return lcStr.equals(lcSuffix);
	}

	/**
	 * Extract the filename from the given path, e.g. "mypath/myfile.txt" ->
	 * "myfile.txt".
	 * 
	 * @param path the file path (may be <code>null</code>)
	 * @return the extracted filename, or <code>null</code> if none
	 */
	public static String getFilename(String path) {
		if (path == null) {
			return null;
		}
		int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		return (separatorIndex != -1 ? path.substring(separatorIndex + 1) : path);
	}

	/**
	 * Extract the filename extension from the given path, e.g. "mypath/myfile.txt"
	 * -> "txt".
	 * 
	 * @param path the file path (may be <code>null</code>)
	 * @return the extracted filename extension, or <code>null</code> if none
	 */
	public static String getFilenameExtension(String path) {
		if (path == null) {
			return null;
		}
		int sepIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
		return (sepIndex != -1 ? path.substring(sepIndex + 1) : null);
	}

	/**
	 * 
	 * @param result
	 * @return
	 */
	public static String getPattern(String result, String pattern) {
		try {
			Pattern patt = Pattern.compile(pattern);
			Matcher matcher = patt.matcher(result);
			while (matcher.find()) {
				return matcher.group();
			}
		} catch (Exception e) {

		}
		return "";
	}

	public static String getRandom(int lenth) {
		StringBuffer buffer = new StringBuffer("");
		// 用于记录取得第几�?
		int count = 0;
		while (count < lenth) {
			// 生成随机数，random取到的是0�?之间的数�?
			int i = (int) (Math.random() * 122);
			// 48�?7是数字，65�?0是小写字母，97�?22是大写字�?
			if ((i >= 48 && i <= 57) || (i >= 65 && i <= 90)) {
				// 记录取到�?��
				count++;
				// 向StringBuffer中加字符
				buffer.append((char) i);
			}
		}
		return buffer.toString();
	}

	/**
	 * 生成字符串和数字的组�?
	 * 
	 * @param lenth
	 * @return
	 */
	public static String getRandomNum(int lenth) {
		StringBuffer buffer = new StringBuffer("");
		// 用于记录取得第几�?
		int count = 0;
		while (count < lenth) {
			// 生成随机数，random取到的是0�?之间的数�?
			int i = (int) (Math.random() * 122);
			// 48�?7是数字，65�?0是小写字母，97�?22是大写字�?
			if ((i >= 48 && i <= 57) || (i >= 65 && i <= 90) || (i >= 97 && i <= 122)) {
				// 记录取到�?��
				count++;
				// 向StringBuffer中加字符
				buffer.append((char) i);
			}
		}
		return buffer.toString();
	}

	/**
	 * 得到字符串中的中文
	 * 
	 * @param str
	 * @return
	 */
	public static String getZhongwen(String str) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			if ((str.charAt(i) + "").getBytes().length > 1) {
				sb.append(str.charAt(i));
			}
		}
		return sb.toString();
	}

	/**
	 * Check if a String has length.
	 * <p>
	 * 
	 * <pre>
	 *      StringUtils.hasLength(null) = false
	 *      StringUtils.hasLength(&quot;&quot;) = false
	 *      StringUtils.hasLength(&quot; &quot;) = true
	 *      StringUtils.hasLength(&quot;Hello&quot;) = true
	 * </pre>
	 * 
	 * @param str the String to check, may be <code>null</code>
	 * @return <code>true</code> if the String is not null and has length
	 */
	public static boolean hasLength(String str) {
		return (str != null && str.length() > 0);
	}

	/**
	 * Check if a String has text. More specifically, returns <code>true</code> if
	 * the string not <code>null<code>, it's <code>length is > 0</code>, and it has
	 * at least one non-whitespace character.
	 * <p>
	 * 
	 * <pre>
	 *      StringUtils.hasText(null) = false
	 *      StringUtils.hasText(&quot;&quot;) = false
	 *      StringUtils.hasText(&quot; &quot;) = false
	 *      StringUtils.hasText(&quot;12345&quot;) = true
	 *      StringUtils.hasText(&quot; 12345 &quot;) = true
	 * </pre>
	 * 
	 * @param str the String to check, may be <code>null</code>
	 * @return <code>true</code> if the String is not null, length > 0, and not
	 *         whitespace only
	 * @see java.lang.Character#isWhitespace
	 */
	public static boolean hasText(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return false;
		}
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	// ---------------------------------------------------------------------
	// Convenience methods for working with String arrays
	// ---------------------------------------------------------------------

	public static boolean isChineseString(String str) {
		if (!hasLength(str))
			return false;
		String check = "[\u4e00-\u9fff]+";
		Pattern regex = Pattern.compile(check);
		Matcher matcher = regex.matcher(str);
		boolean isMatched = matcher.matches();
		return isMatched;
	}

	/**
	 * 验证邮箱有效�?
	 * 
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email) {
		boolean tag = true;
		final String pattern1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		final Pattern pattern = Pattern.compile(pattern1);
		final Matcher mat = pattern.matcher(email);
		if (!mat.find()) {
			tag = false;
		}
		return tag;
	}

	/**
	 * �?��字符串中是否包含该字�?
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isInclude(String str, String c) {
		return str.contains(c);
	}

	public static boolean isMobile(String mobile) {
		// 当前运营商号段分�?
		// 中国移动号段 1340-1348 135 136 137 138 139 150 151 152 157 158 159 187 188
		// 147
		// 中国联�?号段 130 131 132 155 156 185 186 145
		// 中国电信号段 133 1349 153 180 189
		String regular = "1[3,4,5,8]{1}\\d{9}";
		Pattern pattern = Pattern.compile(regular);
		boolean flag = false;
		if (mobile != null) {
			Matcher matcher = pattern.matcher(mobile);
			flag = matcher.matches();
		}
		return flag;
	}

	/**
	 * 判断obj是否是NULL,返回robj
	 * 
	 * @param obj
	 * @param robj
	 * @return
	 */
	public static Object ISNULL(Object obj, Object robj) {
		if (obj == null || obj.equals(null)) {
			return robj;
		} else {
			return obj;
		}
	}

	/**
	 * 生成指定长度的验证码
	 * 
	 * @return
	 */
	public static String makeConsumeCode(int length) {
		Random random = new Random();
		String code = "";
		for (int i = 0; i < length; i++) {
			int num = random.nextInt(10);
			code = code + num;
		}
		return code;
	}

	/**
	 * 生成随机数
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public static int nextInt(final int min, final int max) {
		Random rand = new Random();
		int tmp = Math.abs(rand.nextInt());
		return tmp % (max - min + 1) + min;
	}

	/**
	 * Parse the given locale string into a <code>java.util.Locale</code>. This is
	 * the inverse operation of Locale's <code>toString</code>.
	 * 
	 * @param localeString the locale string, following
	 *                     <code>java.util.Locale</code>'s toString format ("en",
	 *                     "en_UK", etc). Also accepts spaces as separators, as
	 *                     alternative to underscores.
	 * @return a corresponding Locale instance
	 */
	public static Locale parseLocaleString(String localeString) {
		String[] parts = tokenizeToStringArray(localeString, "_ ", false, false);
		String language = (parts.length > 0 ? parts[0] : "");
		String country = (parts.length > 1 ? parts[1] : "");
		String variant = (parts.length > 2 ? parts[2] : "");
		return (language.length() > 0 ? new Locale(language, country, variant) : null);
	}

	/**
	 * Compare two paths after normalization of them.
	 * 
	 * @param path1 First path for comparizon
	 * @param path2 Second path for comparizon
	 * @return whether the two paths are equivalent after normalization
	 */
	public static boolean pathEquals(String path1, String path2) {
		return cleanPath(path1).equals(cleanPath(path2));
	}

	/**
	 * Quote the given String with single quotes.
	 * 
	 * @param str the input String (e.g. "myString")
	 * @return the quoted String (e.g. "'myString'"), or
	 *         <code>null<code> if the input was <code>null</code>
	 */
	public static String quote(String str) {
		return (str != null ? "'" + str + "'" : null);
	}

	/**
	 * Turn the given Object into a String with single quotes if it is a String;
	 * keeping the Object as-is else.
	 * 
	 * @param obj the input Object (e.g. "myString")
	 * @return the quoted String (e.g. "'myString'"), or the input object as-is if
	 *         not a String
	 */
	public static Object quoteIfString(Object obj) {
		return (obj instanceof String ? quote((String) obj) : obj);
	}

	public static String readToString(String fileName) {

		String encoding = "UTF-8";
		File file = new File(fileName);
		Long filelength = file.length();
		byte[] filecontent = new byte[filelength.intValue()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(filecontent);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			return new String(filecontent, encoding);
		} catch (UnsupportedEncodingException e) {
			System.err.println("The OS does not support " + encoding);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Replace all occurences of a substring within a string with another string.
	 * 
	 * @param inString   String to examine
	 * @param oldPattern String to replace
	 * @param newPattern String to insert
	 * @return a String with the replacements
	 */
	public static String replace(String inString, String oldPattern, String newPattern) {
		if (inString == null) {
			return null;
		}
		if (oldPattern == null || newPattern == null) {
			return inString;
		}

		StringBuffer sbuf = new StringBuffer();
		// output StringBuffer we'll build up
		int pos = 0; // our position in the old string
		int index = inString.indexOf(oldPattern);
		// the index of an occurrence we've found, or -1
		int patLen = oldPattern.length();
		while (index >= 0) {
			sbuf.append(inString.substring(pos, index));
			sbuf.append(newPattern);
			pos = index + patLen;
			index = inString.indexOf(oldPattern, pos);
		}
		sbuf.append(inString.substring(pos));

		// redistributors to append any characters to the right of a match
		return sbuf.toString();
	}

	public static String replaceBlank(String result) {
		Pattern p = Pattern.compile("\\s*|\t|\r|\n");
		Matcher m = p.matcher(result);
		return m.replaceAll("");
	}

	/**
	 * Split a String at the first occurrence of the delimiter. Does not include the
	 * delimiter in the result.
	 * 
	 * @param toSplit   the string to split
	 * @param delimiter to split the string up with
	 * @return a two element array with index 0 being before the delimiter, and
	 *         index 1 being after the delimiter (neither element includes the
	 *         delimiter); or <code>null</code> if the delimiter wasn't found in the
	 *         given input String
	 */
	public static String[] split(String toSplit, String delimiter) {
		if (!hasLength(toSplit) || !hasLength(delimiter)) {
			return null;
		}
		int offset = toSplit.indexOf(delimiter);
		if (offset < 0) {
			return null;
		}
		String beforeDelimiter = toSplit.substring(0, offset);
		String afterDelimiter = toSplit.substring(offset + delimiter.length());
		return new String[] { beforeDelimiter, afterDelimiter };
	}

	/**
	 * Take an array Strings and split each element based on the given delimiter. A
	 * <code>Properties</code> instance is then generated, with the left of the
	 * delimiter providing the key, and the right of the delimiter providing the
	 * value.
	 * <p>
	 * Will trim both the key and value before adding them to the
	 * <code>Properties</code> instance.
	 * 
	 * @param array     the array to process
	 * @param delimiter to split each element using (typically the equals symbol)
	 * @return a <code>Properties</code> instance representing the array contents,
	 *         or <code>null</code> if the array to process was null or empty
	 */
	public static Properties splitArrayElementsIntoProperties(String[] array, String delimiter) {
		return splitArrayElementsIntoProperties(array, delimiter, null);
	}

	/**
	 * Take an array Strings and split each element based on the given delimiter. A
	 * <code>Properties</code> instance is then generated, with the left of the
	 * delimiter providing the key, and the right of the delimiter providing the
	 * value.
	 * <p>
	 * Will trim both the key and value before adding them to the
	 * <code>Properties</code> instance.
	 * 
	 * @param array         the array to process
	 * @param delimiter     to split each element using (typically the equals
	 *                      symbol)
	 * @param charsToDelete one or more characters to remove from each element prior
	 *                      to attempting the split operation (typically the
	 *                      quotation mark symbol), or <code>null</code> if no
	 *                      removal should occur
	 * @return a <code>Properties</code> instance representing the array contents,
	 *         or <code>null</code> if the array to process was null or empty
	 */
	public static Properties splitArrayElementsIntoProperties(String[] array, String delimiter, String charsToDelete) {

		if (array == null || array.length == 0) {
			return null;
		}

		Properties result = new Properties();
		int arrayLength = array.length;
		for (int i = 0; i < arrayLength; i++) {
			String element = array[i];
			if (charsToDelete != null) {
				element = deleteAny(array[i], charsToDelete);
			}
			String[] splittedElement = split(element, delimiter);
			if (splittedElement == null) {
				continue;
			}
			result.setProperty(splittedElement[0].trim(), splittedElement[1].trim());
		}
		return result;
	}

	/**
	 * url转map
	 * 
	 * @param urlparam
	 * @return
	 */
	public static Map<String, Object> SplitUrl(String urlparam) {
		Map<String, Object> map = new HashMap<String, Object>();
		String[] param = urlparam.split(",");
		for (String keyvalue : param) {
			String[] pair = keyvalue.split("=");
			if (pair.length == 2) {
				map.put(pair[0], pair[1]);
			}
		}
		return map;
	}

	/**
	 * Test if the given String starts with the specified prefix, ignoring
	 * upper/lower case.
	 * 
	 * @param str    the String to check
	 * @param prefix the prefix to look for
	 * @see java.lang.String#startsWith
	 */
	public static boolean startsWithIgnoreCase(String str, String prefix) {
		if (str == null || prefix == null) {
			return false;
		}
		if (str.startsWith(prefix)) {
			return true;
		}
		if (str.length() < prefix.length()) {
			return false;
		}
		String lcStr = str.substring(0, prefix.length()).toLowerCase();
		String lcPrefix = prefix.toLowerCase();
		return lcStr.equals(lcPrefix);
	}

	/**
	 * Strip the filename extension from the given path, e.g. "mypath/myfile.txt" ->
	 * "mypath/myfile".
	 * 
	 * @param path the file path (may be <code>null</code>)
	 * @return the path with stripped filename extension, or <code>null</code> if
	 *         none
	 */
	public static String stripFilenameExtension(String path) {
		if (path == null) {
			return null;
		}
		int sepIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
		return (sepIndex != -1 ? path.substring(0, sepIndex) : path);
	}

	/**
	 * 排除字符串中的空�?
	 * 
	 * @param str
	 * @return
	 */
	public static String StrTrimAll(String str) {
		return str.replace(" ", "");
	}

	/**
	 * Tokenize the given String into a String array via a StringTokenizer. Trims
	 * tokens and omits empty tokens.
	 * <p>
	 * The given delimiters string is supposed to consist of any number of delimiter
	 * characters. Each of those characters can be used to separate tokens. A
	 * delimiter is always a single character; for multi-character delimiters,
	 * consider using <code>delimitedListToStringArray</code>
	 * 
	 * @param str        the String to tokenize
	 * @param delimiters the delimiter characters, assembled as String (each of
	 *                   those characters is individually considered as delimiter).
	 * @return an array of the tokens
	 * @see java.util.StringTokenizer
	 * @see java.lang.String#trim
	 * @see #delimitedListToStringArray
	 */
	public static String[] tokenizeToStringArray(String str, String delimiters) {
		return tokenizeToStringArray(str, delimiters, true, true);
	}

	/**
	 * Tokenize the given String into a String array via a StringTokenizer.
	 * <p>
	 * The given delimiters string is supposed to consist of any number of delimiter
	 * characters. Each of those characters can be used to separate tokens. A
	 * delimiter is always a single character; for multi-character delimiters,
	 * consider using <code>delimitedListToStringArray</code>
	 * 
	 * @param str               the String to tokenize
	 * @param delimiters        the delimiter characters, assembled as String (each
	 *                          of those characters is individually considered as
	 *                          delimiter)
	 * @param trimTokens        trim the tokens via String's <code>trim</code>
	 * @param ignoreEmptyTokens omit empty tokens from the result array (only
	 *                          applies to tokens that are empty after trimming;
	 *                          StringTokenizer will not consider subsequent
	 *                          delimiters as token in the first place).
	 * @return an array of the tokens
	 * @see java.util.StringTokenizer
	 * @see java.lang.String#trim
	 * @see #delimitedListToStringArray
	 */
	@SuppressWarnings("unchecked")
	public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens,
			boolean ignoreEmptyTokens) {

		StringTokenizer st = new StringTokenizer(str, delimiters);
		List tokens = new ArrayList();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (trimTokens) {
				token = token.trim();
			}
			if (!ignoreEmptyTokens || token.length() > 0) {
				tokens.add(token);
			}
		}
		return toStringArray(tokens);
	}

	/**
	 * Copy the given Collection into a String array. The Collection must contain
	 * String elements only.
	 * 
	 * @param collection the Collection to copy
	 * @return the String array (<code>null</code> if the Collection was
	 *         <code>null</code> as well)
	 */
	@SuppressWarnings("unchecked")
	public static String[] toStringArray(Collection collection) {
		if (collection == null) {
			return null;
		}
		return (String[]) collection.toArray(new String[collection.size()]);
	}

	/**
	 * Trim leading whitespace from the given String.
	 * 
	 * @param str the String to check
	 * @return the trimmed String
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimLeadingWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuffer buf = new StringBuffer(str);
		while (buf.length() > 0 && Character.isWhitespace(buf.charAt(0))) {
			buf.deleteCharAt(0);
		}
		return buf.toString();
	}

	/**
	 * Trim trailing whitespace from the given String.
	 * 
	 * @param str the String to check
	 * @return the trimmed String
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimTrailingWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuffer buf = new StringBuffer(str);
		while (buf.length() > 0 && Character.isWhitespace(buf.charAt(buf.length() - 1))) {
			buf.deleteCharAt(buf.length() - 1);
		}
		return buf.toString();
	}

	/**
	 * Trim leading and trailing whitespace from the given String.
	 * 
	 * @param str the String to check
	 * @return the trimmed String
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuffer buf = new StringBuffer(str);
		while (buf.length() > 0 && Character.isWhitespace(buf.charAt(0))) {
			buf.deleteCharAt(0);
		}
		while (buf.length() > 0 && Character.isWhitespace(buf.charAt(buf.length() - 1))) {
			buf.deleteCharAt(buf.length() - 1);
		}
		return buf.toString();
	}

	private static String TruncateUrlPage(String strURL) {
		String strAllParam = null;
		String[] arrSplit = null;
		strURL = strURL.trim().toLowerCase();
		arrSplit = strURL.split("[?]");
		if (strURL.length() > 1) {
			if (arrSplit.length > 1) {
				if (arrSplit[1] != null) {
					strAllParam = arrSplit[1];
				}
			}
		}
		return strAllParam;
	}

	/**
	 * Uncapitalize a <code>String</code>, changing the first letter to lower case
	 * as per {@link Character#toLowerCase(char)}. No other letters are changed.
	 * 
	 * @param str the String to uncapitalize, may be <code>null</code>
	 * @return the uncapitalized String, <code>null</code> if null
	 */
	public static String uncapitalize(String str) {
		return changeFirstCharacterCase(str, false);
	}

	/**
	 * Unqualify a string qualified by a '.' dot character. For example,
	 * "this.name.is.qualified", returns "qualified".
	 * 
	 * @param qualifiedName the qualified name
	 */
	public static String unqualify(String qualifiedName) {
		return unqualify(qualifiedName, '.');
	}

	/**
	 * Unqualify a string qualified by a separator character. For example,
	 * "this:name:is:qualified" returns "qualified" if using a ':' separator.
	 * 
	 * @param qualifiedName the qualified name
	 * @param separator     the separator
	 */
	public static String unqualify(String qualifiedName, char separator) {
		return qualifiedName.substring(qualifiedName.lastIndexOf(separator) + 1);
	}

	public static Map<String, String> URLRequest(String URL) {
		Map<String, String> mapRequest = new HashMap<String, String>();
		String[] arrSplit = null;
		String strUrlParam = TruncateUrlPage(URL);
		if (strUrlParam == null) {
			return mapRequest;
		}
		// 每个键值为一组
		arrSplit = strUrlParam.split("[&]");
		for (String strSplit : arrSplit) {
			String[] arrSplitEqual = null;
			arrSplitEqual = strSplit.split("[=]");
			// 解析出键值
			if (arrSplitEqual.length > 1) {
				// 正确解析
				mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
			} else {
				if (arrSplitEqual[0] != "") {
					// 只有参数没有值，不加入
					mapRequest.put(arrSplitEqual[0], "");
				}
			}
		}
		return mapRequest;
	}

	public static String nullToEmpty(String ret) {
		if (ret == null) {
			ret = "";
		}
		return ret;
	}

	public static Integer nullToEmpty(Integer ret) {
		if (ret == null) {
			ret = 0;
		}
		return ret;
	}

	public static Double nullToEmpty(Double ret) {
		if (ret == null) {
			ret = 0d;
		}
		return ret;
	}

	public static String asUrlParams(Map<String, String> source) {
		Map<String, String> tmp = Maps.newHashMap();
		// java8 语法
		source.forEach((k, v) -> {
			if (k != null) {
				try {
					tmp.put(k, URLEncoder.encode(v, "utf-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		});
		return Joiner.on("&").useForNull("").withKeyValueSeparator("=").join(tmp);
	}

	public static String[] getLocation(String address) {
		String url = "https://restapi.amap.com/v3/geocode/geo?key=3a0f30185a4911a6e129033821dff84c&address=" + address;
		String result = HttpKit.get(url);
		JSONObject res_json = JSONObject.parseObject(result);
		if (res_json != null) {
			String status = res_json.getString("status");
			if (status.equals("1")) {
				JSONArray geocodes = res_json.getJSONArray("geocodes");
				if (geocodes.size() == 1) {
					JSONObject geocode = geocodes.getJSONObject(0);
					if (geocode != null) {
						String location = geocode.getString("location");
						String[] lat_lon = location.split(",");
						return lat_lon;
					}
				}
			}
		}
		return null;
	}
}
