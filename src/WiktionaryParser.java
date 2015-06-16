import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Short program to extract definitions from a wiktionary page
 **/
public class WiktionaryParser {
	
	private Scanner sc;
	private String currentWord;
	private String[] defTypes = {"en-noun", "en-verb", "en|adjective", "en-adj", "en-interj", "en-adv"};
	
	public WiktionaryParser() {}
	
	private Iterable<String> getPageDefinitions(String pageTitle) {
		currentWord = pageTitle;
		BufferedReader br = null;
		// read in file
		try {
			br = new BufferedReader(new InputStreamReader
					(new URL("https://en.wiktionary.org/wiki/" + pageTitle + "?action=raw").openStream()));
			sc = new Scanner(br);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("No definitions found for " + pageTitle);
		}
		
		ArrayList<String> defs = new ArrayList<>();
		String nextLine;
		while (sc.hasNext()) {
			nextLine = sc.nextLine();
			for (String t : defTypes) {
				if (nextLine.contains(t)){
					nextLine = sc.nextLine();
					while (!nextLine.startsWith("#"))
						if (sc.hasNext()) {
							nextLine = sc.nextLine();								
						}
						else {
							break;
						}
					while (!nextLine.isEmpty()) {
						//if (nextLine.startsWith("#:") || nextLine.startsWith("#*") 
							//	|| nextLine.startsWith("##*") || nextLine.startsWith("|")
								//|| nextLine.startsWith("##:") || nextLine.startsWith("{")
								//|| nextLine.startsWith(" ")) {
						Pattern pattern = Pattern.compile("#?#[ {a-zA-z]");
						Matcher matcher = pattern.matcher(nextLine);
						if (!matcher.lookingAt()) {
							if (sc.hasNext()) {
								nextLine = sc.nextLine();
								continue;								
							}
							else {
								break;
							}
						}
						String def = nextLine;
						while (def.contains("{{"))
							//TODO: find innermost occurrence of "{{", remove and work out to rest
							def = def.substring(0, def.indexOf("{{")) + def.substring(def.indexOf("}}") + 2);
						//while (def.contains("<")) 
						//	def = def.substring(0, def.indexOf("<")) + def.substring(def.indexOf(">") + 1);
						while (def.contains("|")) {
							int index = def.indexOf("|");
							for (int i = index; i >= 0; i--) {
								if (def.charAt(i) == '[') {
									def = def.substring(0, i) + def.substring(index + 1);
									break;
								}
							}
						}
						def = def.replaceAll("[\\[\\]#{}]", "");
						def = def.replaceAll("''", "'");
						def = def.replaceAll("&nbsp;", "");
						def = def.trim();
						if (!def.equals("")) {
							def = t.replaceAll("en-", "") + ": " + def;
							defs.add(def);
						}
						if (sc.hasNext()) {
							nextLine = sc.nextLine();							
						}
						else {
							break;
						}
					}
				}
			}
		}
		try {
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		return defs;
	}
	
	public void definitionsToConsole(String pageTitle) {
		ArrayList<String> defs = (ArrayList<String>)getPageDefinitions(pageTitle);
		if (defs != null) {
			System.out.println(currentWord);
			for (String s : defs) {
				System.out.println(s);
			}
		}
		else {
			System.out.println("No definitions currently stored.");
		}
	}
	
	public void definitionsToFile(String pageTitle) {
		ArrayList<String> defs = (ArrayList<String>)getPageDefinitions(pageTitle);
		if (defs != null) {
			Writer writer = null;
			try {
				writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(currentWord + ".txt"), "utf-8"));
				writer.write("* " + currentWord + "\n");
				for (String s : defs)
					writer.write(s + "\n");
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				try {
					writer.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void allDefinitionsToFile(Iterable<String> words, String title) {
		ArrayList<String> defs;
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(title + "-defs.txt"), "utf-8"));
			for (String s : words) {
				defs = (ArrayList<String>)getPageDefinitions(s);
				System.out.println("Defs found - " + s);
				writer.append("* " + s + "\n");
				if (defs != null) {
					for (String d : defs) {
						writer.write(d + "\n");
					}
				}	
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				writer.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		
		WiktionaryParser p = new WiktionaryParser();
		p.definitionsToConsole("apple");
		//p.allDefinitionsToFile(samples, "sowpods");
	}

}
