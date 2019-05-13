//1. I used breadth-first to explore the web because this would allow the user to do a tighter search because it processes
//everything immediately to the center before going to the next layer & there is too much info on the internet
//2. I calculated relevance by totaling the number of query word stems in text and dividing that by the total number of words 
//in the text. However, when the entire query word appears, it has a higher relevance value (+1) than just part of the query word 
//appearing (+0.95). I "normalized" text by making the query word lowercase and stemming it, then comparing that to the text, 
//which is also made lowercase.
//3. Relevance is stored as one of the variables in the EPIC object, which can be used by quicksort to rank the EPIC pages by
//their relevance value.

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.Iterator;

public class EPIC implements Comparable<EPIC>{
    private static Matcher matcher;
    private static final String DOMAIN_NAME_PATTERN
	= "([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,15}";
    private static Pattern patrn = Pattern.compile(DOMAIN_NAME_PATTERN);

    private String URL;
    private String theTitle;    
    private Document doc; 
    private String theText;
    private double relevance; 

    public EPIC(String url) throws IOException {
		URL = url;
		doc = Jsoup
		    .connect(URL)
		    .userAgent("Jsoup client")
		    .timeout(3000).get();

		theText = doc.select("body").text();
		theTitle = doc.title();
		relevance = 0;
    }

    public static void setRel(EPIC p, String [] query) { //set relevance value to each EPIC page
    	if(query.length==1)
    		p.relevance = relev2(p, query);
    	else
    		p.relevance = relev(p, query);
    }

    public String getTitle() { return theTitle; }
    public String getText() { return theText; }
    public String getURL() { return URL; }

    public Iterable<String> adjacentURL() {
		LinkedList<String> domains = new LinkedList<String>();
		Elements links = doc.select("a[href]");
		
		for (Element link : links) {
		    String attr = link.attr("href");
		    String domainName;
		    // System.out.println("trying " + attr);
		    if (attr.startsWith("http") || attr.startsWith("https"))
			domainName = attr;
		    else
			if (attr.startsWith("/"))
			    domainName = URL + attr;
			else
			    domainName = URL + "/" + attr;
		    if (domains.exists(domainName) || domainName.equals(URL))
			continue;
		    domains.addLast(domainName);
		}
		return domains;
    }

    public Iterable<EPIC> adjacentTo() {
		// This retrieves all the EPIC pages that can be accessed
		// through the links of the current Page.
		LinkedList<EPIC> theLinks = new LinkedList<EPIC>();
		LinkedList<String> domains = new LinkedList<String>();
		Elements links = doc.select("a[href]");
		
		for (Element link : links) {
		    String attr = link.attr("href");
		    String domainName;
		    // System.out.println("trying " + attr);
		    if (attr.startsWith("http") || attr.startsWith("https"))
			domainName = attr;
		    else
			if (attr.startsWith("/"))
			    domainName = URL + attr;
			else
			    domainName = URL + "/" + attr;
		    try {
			if (domains.exists(domainName) || domainName.equals(URL))
			    continue;
			domains.addLast(domainName);
			theLinks.addLast(new EPIC(domainName));
		    }
		    catch (IOException | IllegalArgumentException e) {
			System.out.println("unretievable: " + domainName + "; " + e.getMessage());
		    }
		}
		return theLinks;
    }
    
    public static String getDomainName(String url) {
		String domainName = "";
		matcher = patrn.matcher(url);
		if (matcher.find()) 
		    domainName = matcher.group(0).toLowerCase().trim();
		return domainName;
    }

    public int compareTo(EPIC other) { //compares the relevance of one EPIC page to another
        if(this.relevance > other.relevance)
        	return -1;
        else if(this.relevance < other.relevance)
        	return 1;
        else 
        	return 0;
    }

    public static String[] convert(String query) { //converts inputed query into an array, needed especially when query is multiple words
    	Stemmer st = new Stemmer();
    	for(int i=0; i<query.length(); i++)
    		st.add(query.toLowerCase().charAt(i)); //converts query into lowercase
    	st.stem(); //removes stem words from query
    	String input = st.toString();
    	String [] convert = input.split(" ");
    	return convert;
    }

    public static boolean remove(LinkedList<EPIC> l, EPIC p){ //removes pages in collected linked list that have the same length of text (same pages)
    	if(l.isEmpty())
    		return false;
    	Iterator<EPIC> remove = l.iterator(); 
		while(remove.hasNext()){
			EPIC next = remove.next();
			if(p.getText().length() == next.getText().length())
				return true;
		}
		return false;
    }

    public static double relev(EPIC p, String [] query){ //calculating relevance value when there are more than one query word
    	int count = 0; //counts how many times query words appear in text
    	double relev = 0;
    	double total = p.getText().length();
    	String txt = p.getText().toLowerCase();
    	String strQuery = "";
	    for(int i=0; i<query.length; i++){
	    	if(i == query.length-1)
	    		strQuery+= query[i];
	    	else
	    		strQuery+= query[i] + " ";
	    }
	    //if query is more than one word, the relevance value increases more if text includes the entire query, not just part of it
	    int index = txt.indexOf(strQuery);
	    while(index!= -1){
	    	count++; //count increases by 1 when it finds entire query 
	    	txt = txt.substring(index+1);
	    	index = txt.indexOf(strQuery);
	    }
    	for(int i=0; i<query.length; i++){ 
    		int index2 = txt.indexOf(query[i]); //finds the first index of each query word in the text
	    	while(index2!= -1) {
	    		count+=0.95; //count increases by 0.95 when it finds only part of the query (one of the words)
	    		txt = txt.substring(index2 + 1); 
	    		index2 = txt.indexOf(query[i]); //finds index of where query word next appears
	    	}
	 	}
	    relev = count / total; //divides number of times all query words appear in text and divide it by the total amount of text on page
	    return relev * 100;
    }

    public static double relev2(EPIC p, String [] query){ //calculating relevance value when there is only one query word (if we use same relev() method as that of when there are multiple query words => read timed out error)
    	int count = 0; 
    	double relev = 0;
    	double total = p.getText().length();
    	String txt = p.getText().toLowerCase();
       	for(int i=0; i<query.length; i++){ 
    		int index = txt.indexOf(query[i]); 
	    	while(index!= -1) {
	    		count++; 
	    		txt = txt.substring(index + 1); 
	    		index = txt.indexOf(query[i]); 
	    	}
	    }
	    relev = count / total; 
	    return relev * 100;
	}

   	public static int getQIndex(EPIC p, String [] query) {	
    	String input = p.getText().toLowerCase(); //check in all lowercase 
    	int index = 0;
    	for(int i=0; i<query.length; i++){
    		index = input.indexOf(query[i]); //find first time query appears in text
    		if(index != -1)
    			break;
    	}
    	return index;
    }

    public static boolean isSpace(EPIC p, int index){ //to make sure that when printing snippet of text, it does not start/end in the middle of a word
    	String input = p.getText();
    	if(input.charAt(index)==(' ')){
    		return true;
    	}
    	return false;
    }

    public static boolean isVisit(LinkedList<String> visited, String url){ //checks if it is a url that has already been visited, if so, do not need to iterate the adjacent pages to it
    	if(visited.exists(url)==true){
    		return true;
    	}
    	else
    		return false;
    }

    public static void finalResult(EPIC [] result, String [] query){
		boolean print = true;
		int begin = 0; int end = 0; 
		for(int i= 0; i<result.length; i++){
			print = true;
			int qIndex = getQIndex(result[i], query);
			System.out.println(""); 
			System.out.println("\033[34;47;1m" + result[i].getTitle() +  "\033[0m");
			System.out.println(result[i].getURL()); //prints URL of page
			begin = qIndex - 100;
			end = qIndex + 100;
			while(begin < 0)
				begin++;
			while(print == true){
				if(isSpace(result[i], begin)==true || begin==0){
					if(isSpace(result[i], end)==true){
						System.out.println("..." + result[i].getText().substring(begin, end) + "..."); //prints snippet of text that includes query terms
						print = false;
					}
					else
						end++;
				}
				else
					begin--;
			}
		}
    }

    public static void main(String[] args) throws IOException {
    	LinkedList<String> frontier = new LinkedList<String>(); //linkedlist of all the EPIC pages 
    	LinkedList<EPIC> collected = new LinkedList<EPIC>(); //linkedlist of all the collected EPIC pages that have query words 
		LinkedList<String> visited = new LinkedList<String>(); //linkedlist of all the EPIC pages that have been visited / iterated through their adjacent pages

		try{
			if(args.length==3){
				if(! args[0].substring(0,2).equals("-u"))
					throw new RuntimeException("Missing -u");
				if(! args[1].substring(0,2).equals("-q"))
					throw new RuntimeException("Missing -q");
				if(! args[2].substring(0,2).equals("-m"))
					throw new RuntimeException("Missing -m");
			}
			else
				throw new RuntimeException("Incorrect number of arguments");
		}
		catch(Exception e){
			System.out.println(e);
			System.exit(0);
		}		

		EPIC p = new EPIC(args[0].substring(2, args[0].length()));
		frontier.addFirst(p.getURL()); //seed URL added to frontier linkedlist
		String [] query = convert(args[1].substring(2, args[1].length()));
		int num = Integer.parseInt(args[2].substring(2, args[2].length()));

		while(true){
			String v = frontier.removeFirst(); //first time this is removed, should be the seed URL because that was the first one added to the frontier linkedlist
			if(visited.exists(v)) //if this URL (and the pages adjacent to it) has already been visited, continue to next URL in frontier linkedlist
				continue;
			try {	    	
				p = new EPIC(v); 
				visited.addFirst(v);
			    setRel(p, query); 
			    if(p.relevance > 0){ //if it is a relevant page, relevance value will be > 0 (it has query word at least once in text)
			    	if (remove(collected, p)==false) //checks if it is a page that is already in collected linkedlist
			    		collected.addFirst(p);
				}
				for (String w: p.adjacentURL()) 
			    	frontier.addLast(w);
			    if(collected.size()==num){ //once we have enough EPIC pages in collected linkedlist 
			    	EPIC[] find = new EPIC[num];
			    	for(int i=0; i<num; i++)
			    		find[i]= collected.removeFirst();
			    	Quick.sort(find); //use quicksort to sort pages by their relevance values (how relevant they are, which is determined by # of query words/# total words in text)
			    	finalResult(find, query); 
				   	System.exit(0);
				}	    
			}
			catch (IOException | IllegalArgumentException e) {
				System.out.println("unretievable: " + v + "; " + e.getMessage());
				continue; //this continues to the next URL in frontier linkedlist
			}	
		}
    }
}