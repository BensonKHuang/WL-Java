/* WORD LADDER assignment3.Main.java
 * EE422C Project 3 submission by 2/27/2018
 *
 * Benson Huang
 * bkh642
 * Class ID: 15470
 * Spring 2018
 */

package assignment3;

import java.util.*;
import java.io.*;

public class Main {

	static Map<String,ArrayList<String>> wordMap = new HashMap<>();

	public static void main(String[] args) throws Exception {

		Scanner kb;	// input Scanner for commands
		PrintStream ps;	// output file, for student testing and grading only
		// If arguments are specified, read/write from/to files instead of Std IO.
		if (args.length != 0) {
			kb = new Scanner(new File(args[0]));
			ps = new PrintStream(new File(args[1]));
			System.setOut(ps);			// redirect output to ps
		} else {
			kb = new Scanner(System.in);// default input from Stdin
			ps = System.out;			// default output to Stdout
		}
		initialize();
		ArrayList<String> wordLadder;
		ArrayList<String> words;

		while(true) {
			words = parse(kb);
			if (words.size() == 0) {
				break; // '/quit' returns empty arrayList
			}
			wordLadder = getWordLadderBFS(words.get(0), words.get(1));
			printLadder(wordLadder);
			wordLadder = getWordLadderDFS(words.get(0), words.get(1));
			printLadder(wordLadder);
		}
	}

	/**
	 * Initializes the static map,
	 * key = each String from dictionary file
	 * value = a Set of each String that is one letter off from key
	 */

	public static void initialize() {

		Set<String> dict = makeDictionary();

		for(String word : dict){

			ArrayList<String> DiffList = new ArrayList<String>();

			for(int i = 0; i < word.length(); ++i){

		   		char[] chars = word.toCharArray();
				char[] modifyChar = word.toCharArray();
				for(char ch = 'A'; ch <= 'Z'; ch++){

					if(chars[i] == ch) {
						continue;
					}

					modifyChar[i] = ch;
					String str = new String(modifyChar);

					if(dict.contains(str)){
						DiffList.add(str);
					}
				}
			}
			wordMap.put(word, DiffList);
		}
	}

	/**
	 * Takes in user input for two words or quit
	 * @param keyboard Scanner connected to System.in
	 * @return ArrayList of Strings containing start word and end word.
	 * If command is /quit, return empty ArrayList.
	 */
	public static ArrayList<String> parse(Scanner keyboard) {

		String temp;
		String[] words;
		ArrayList<String> wordsArrayList = new ArrayList<String>();

		do{
			System.out.println("Enter two words or '/quit' : ");
			temp = keyboard.nextLine();

			if(temp.contains("/quit")){
				return wordsArrayList;
			}
			words = temp.split("\\W+");
		}
		while(words.length != 2 || words[0].length() != words[1].length());

		wordsArrayList.add(words[0].toUpperCase());
		wordsArrayList.add(words[1].toUpperCase());
		return wordsArrayList;
	}

	/**
	 * Back tracks through a Map to find the valid Word Ladder
	 * @param found boolean value if word Ladder is found in map
	 * @param startWord start of word Ladder
	 * @param endWord end of word Ladder
	 * @param path map of path
	 * @return ArrayList of a wordLadder
	 */
	private static ArrayList<String> getListFromMap(boolean found, String startWord, String endWord, Map<String, String> path){
		ArrayList<String> list  = new ArrayList<String>();
		if(found){
			String node = endWord;
			while(node != null){
				list.add(node);
				node = path.get(node);
			}
			Collections.reverse(list);
		}
		else{
			list.add(startWord);
			list.add(endWord);
		}
		return list;
	}

	/**
	 * Uses Depth First Search to find word Ladder
	 * @param startWord start of the word Ladder
	 * @param endWord finish of the word Ladder
	 * @return ArrayList of a wordLadder between startWord and endWord using DFS
	 */
	public static ArrayList<String> getWordLadderDFS(String startWord, String endWord) {

		startWord = startWord.toUpperCase();
		endWord = endWord.toUpperCase();
		Set<String> visited = new HashSet<String>();
		ArrayList<String> wordLadder  = new ArrayList<String>();
		Map<String, String> path = new HashMap<String, String>();
		boolean found = false;

		if(wordMap.containsKey(startWord) && wordMap.containsKey(endWord)){

			try{
				found = getWordLadderDFSHelper(startWord, endWord, visited, path); // Returned list should be ordered start to end.  Include start and end.
			}
			catch(StackOverflowError ignored){
			}
		}

		wordLadder = getListFromMap(found, startWord, endWord, path);
		return wordLadder;
	}

	/**
	 * getWordLadderDFSHelper is a recursive function helper
	 * @param currentWord word that needs to be checked for the ladder
	 * @param endWord is target end word for the word ladder
	 * @param visited a set that contains every vertex that has been reached
	 * @param path is the current path that has lead to the currentWord
	 * @return ArrayList with valid path
	 */
	private static boolean getWordLadderDFSHelper(String currentWord, String endWord, Set<String> visited, Map<String, String> path){
		visited.add(currentWord);
		if(currentWord.equals(endWord)){
			return true;
		}

		ArrayList<String> sortedDiffList = getBestList(endWord, wordMap.get(currentWord));

		for(String word : sortedDiffList){

			if(!visited.contains(word)){
				if(getWordLadderDFSHelper(word, endWord,  visited, path)){
					path.put(word, currentWord);
					return true;
				}
			}
		}
		return false; //return empty list when edge is found
	}

	/**
	 * Sorts the ArrayList with end word as comparator
	 * @param target is end word to use for comparison
	 * @param list is the list of words for comparison
	 * @return sorted ArrayList from closest matching to target to least matching
	 */
	private static ArrayList<String> getBestList(String target, ArrayList<String> list){
		if(list.isEmpty()) return list;
		ArrayList<String> sortedList = new ArrayList<String>(list);
		Map<String, Integer> differenceMap = new HashMap<>();

		char[] targetChars = target.toCharArray();

		for(String word : list){
			int diffCounter = 0;
			int valueComp = 0;
			char[] wordChars = word.toCharArray();
			for(int i = 0; i < wordChars.length;  ++i){
				if(targetChars[i] != wordChars[i]){
					diffCounter++;
				}
				valueComp = valueComp + Math.abs((int)targetChars[i] - (int) wordChars[i]);
			}

			differenceMap.put(word, diffCounter*25*wordChars.length + valueComp);
		}
		//insertion sort used because list size is small with approx. size = 20 per method call
		for(int i = 1; i < sortedList.size(); ++i){
			String key = sortedList.get(i);
			int j = i - 1;

			while(j >= 0 && differenceMap.get(sortedList.get(j)) > differenceMap.get(key)){
				sortedList.set(j + 1, sortedList.get(j));
				j--;
			}
			sortedList.set(j + 1, key);
		}
		return sortedList;
	}

	/**
	 * Uses Breadth First Search to find a word Ladder
	 * @param startWord start of the word Ladder
	 * @param endWord finish of the word Ladder
	 * @return ArrayList of a wordLadder between startWord and endWord using BFS
	 */
    public static ArrayList<String> getWordLadderBFS(String startWord, String endWord) {

    	startWord = startWord.toUpperCase(); //dictionary only contains uppercase
		endWord = endWord.toUpperCase();

		ArrayList<String> wordLadder = new ArrayList<String>();
		Queue<String> queue = new LinkedList<>();
		Set<String> visited = new HashSet<String>();
		Map<String, String> pathMap = new HashMap<String, String>();
		boolean found = false;

		if(wordMap.containsKey(startWord) && wordMap.containsKey(endWord)){
			queue.add(startWord);
			visited.add(startWord);
			pathMap.put(startWord, null);
		}

		while(!queue.isEmpty() && !found){
			String checkWord = queue.poll();
			if(endWord.equals(checkWord)){
				found = true;
				break;
			}
			for(String word : wordMap.get(checkWord)){
				if(!visited.contains(word)){
					queue.add(word);
					visited.add(word);
					pathMap.put(word, checkWord);
				}
			}
		}
		wordLadder = getListFromMap(found, startWord, endWord, pathMap);
		return wordLadder;
	}

	/**
	 * Outputs the ladder based on ArrayList
	 * @param ladder is ArrayList to be printed
	 */
	public static void printLadder(ArrayList<String> ladder) {

		if(ladder.size() <= 2){
			System.out.println("no word ladder can be found between " + ladder.get(0).toLowerCase() + " and " + ladder.get(ladder.size() - 1).toLowerCase() + ".");
		}
		else{
			System.out.println("a " + (ladder.size() - 2) + "-rung ladder exists between " + ladder.get(0).toLowerCase() + " and " + ladder.get(ladder.size() - 1).toLowerCase() + ".");

			for(String ss : ladder){
				System.out.println(ss.toLowerCase());
			}
		}
		System.out.println();
	}
	// TODO


	/* Do not modify makeDictionary */
	public static Set<String>  makeDictionary () {
		Set<String> words = new HashSet<String>();
		Scanner infile = null;
		try {
			infile = new Scanner (new File("five_letter_words.txt"));
		} catch (FileNotFoundException e) {
			System.out.println("Dictionary File not Found!");
			e.printStackTrace();
			System.exit(1);
		}
		while (infile.hasNext()) {
			words.add(infile.next().toUpperCase());
		}
		return words;
	}
}
