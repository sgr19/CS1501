import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 
 * CS 1501 Project 1 DLB Implementation of Dictionary Trie
 *
 */
class DlbTrieNode {
	public static final int MAX_WORD_COUNT = 5;
	public static final char END_OF_WORD = '.';

	DlbTrieNode(char alphabet, DlbTrieNode parent) {
		this.charValue = alphabet;
		this.parent = parent;
		childNode = null;
	}

	public char getCharValue() {
		return charValue;
	}

	public void setCharValue(char charValue) {
		this.charValue = charValue;
	}

	public DlbTrieNode getParent() {
		return parent;
	}

	public void setParent(DlbTrieNode parent) {
		this.parent = parent;
	}
	
	public SortedMap<Character, DlbTrieNode> getChildNode() {
		return childNode;
	}

	public void setChildNode(SortedMap<Character, DlbTrieNode> childNode) {
		this.childNode = childNode;
	}

	public DlbTrieNode insert(Character c) {
		if (childNode == null) {
			childNode = new TreeMap<Character, DlbTrieNode>();
		}
		// System.out.println("this.charValue=" + this.getCharValue());
		DlbTrieNode node = new DlbTrieNode(c, this);
		childNode.put(c, node);
		return node;
	}

	public DlbTrieNode insertEndOfWord() {
		return (insert(END_OF_WORD));
	}

	public DlbTrieNode search(Character c) {
		if (childNode == null) {
			return null;
		}
		DlbTrieNode node = childNode.get(c);
		return node;
	}

	public void composeAutoComplStr(StringBuilder prefix, DlbTrieNode node, List<String> autoCompleteList) {
		// System.out.println("composeAutoComplStr(): prefix=" +
		// prefix.toString());
		// char lastChar=prefix.charAt(prefix.length()-1);
		SortedMap<Character, DlbTrieNode> childMap = node.getChildNode();
		/*
		 * DlbTrieNode childNode=null; if (childMap == null) {
		 * autoCompleteList.add(prefix.toString()); return; } else { childNode =
		 * childMap.get(lastChar); }
		 */
		StringBuilder sb = null;
		for (Character key : childMap.keySet()) {
			// System.out.println("child_key=" + key);
			if (key.equals(END_OF_WORD)) {
				if (autoCompleteList.size() < MAX_WORD_COUNT) {
					autoCompleteList.add(prefix.toString());
				}
			} else {
				DlbTrieNode autoCompleteNode = childMap.get(key);
				sb = new StringBuilder(prefix).append(autoCompleteNode.getCharValue());
				// System.out.println("sb=" + sb);
				if (autoCompleteList.size() < MAX_WORD_COUNT) {
					composeAutoComplStr(sb, autoCompleteNode, autoCompleteList);
				}
			}
		}
	}

	// data members
	char charValue;
	DlbTrieNode parent;
	SortedMap<Character, DlbTrieNode> childNode;
}

class AutoCompleteEngine {
	public AutoCompleteEngine() {
		rootNode = searchNode = new DlbTrieNode('^', null);
	}

	public void reset() {
		searchNode = rootNode;
	}

	/**
	 * 
	 * @param dictTextFile
	 * @return
	 */
	public boolean establish(File dictTextFile) {
		Scanner dictScanner = null;
		try {
			dictScanner = new Scanner(dictTextFile);
		} catch (FileNotFoundException e) {
			System.err.println("Error: " + e.getMessage());
			return false;
		}
		String word = null;
		// Reading each word of dictionary file
		while (dictScanner.hasNextLine()) {
			word = dictScanner.nextLine();
			// System.out.println("line:" + word);
			if (word != null && word.trim().length() > 0) {
				insert(word);
			}
		}
		dictScanner.close();
		return true;
	}

	/**
	 * 
	 * @param inputWord
	 * @param userHistory
	 * @return
	 */
	public List<String> predict(String inputWord) {
		// System.out.println("inputWord=" + inputWord + ", node=" +
		// searchNode.getCharValue());
		DlbTrieNode node = searchNode;
		String dictWord = inputWord.toLowerCase();
		char searchChar = dictWord.charAt(dictWord.length() - 1);
		node = node.search(searchChar);
		if (node == null) {
			return null;
		} else {
			searchNode = node;
		}
		List<String> autoCompleteList = new ArrayList<String>();
		StringBuilder prefix = new StringBuilder(dictWord);
		searchNode.composeAutoComplStr(prefix, searchNode, autoCompleteList);
		Collections.sort(autoCompleteList);
		return autoCompleteList;
	}

	public void insert(String word) {
		// System.out.println("insert:" + word);
		DlbTrieNode node = searchNode;
		char[] charArray = word.toLowerCase().toCharArray();
		for (char c : charArray) {
			DlbTrieNode searchNode = node.search(c);
			if (searchNode == null) {
				node = node.insert(c);
			} else {
				node = searchNode;
			}
		}
		node.insertEndOfWord();
		// System.out.println("****");
	}

	// data members
	DlbTrieNode searchNode;
	DlbTrieNode rootNode;
}

/**
 * 
 * Auto-Complete App
 *
 */
class AutoCompleteApp {
	
	public static final String DICT_FILE = "dictionary.txt";
	public static final String USER_HISTORY_FILE = "user_history.txt";
	public static final String END_OF_AUTO_COMPL = "$";
	public static final String END_OF_SESSION = "!";
	
	public static boolean isNumeric(String s) {
		return s != null && s.matches("[-+]?\\d*\\.?\\d+");
	}

	/**
	 * 
	 * @param inputStr
	 * @param isFirst
	 * @return
	 */
	public boolean validateUserInput(String inputStr, boolean isFirst) {
		if (inputStr.length() != 1 || " ".equals(inputStr)) {
			return (false);
		} else if (END_OF_AUTO_COMPL.equals(inputStr) || END_OF_SESSION.equals(inputStr) || "'".equals(inputStr)) {
			return (true);
		}
		
		boolean validFlag = true;
		char[] charArray = inputStr.toCharArray();
		char ch = charArray[0];
		if (Character.isDigit(ch)) {
			int acceptNum = Integer.valueOf(inputStr), predSize=mergedPredictList.size();
			if (predSize == 0 || acceptNum <= 0 || acceptNum > predSize) {
				validFlag = false;
			}
		} else if (!Character.isAlphabetic(ch)) {
			validFlag = false;
		}

		return validFlag;
	}

	/**
	 * 
	 */
	public AutoCompleteApp() {
		dictAutoEngine = new AutoCompleteEngine();
		histAutoEngine = new AutoCompleteEngine();
		userHistory = new LinkedHashSet<String>();
		mergedPredictList = new ArrayList<String>();
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	/**
	 * 
	 * @param histPredictList
	 * @param beginIndex
	 */
	public void printPredictList(List<String> histPredictList, List<String> dictPredictList) {
		Set<String> mergedPredictSet = new LinkedHashSet<String>();
		if (histPredictList != null) {
			mergedPredictSet.addAll(histPredictList);
		}
		if (dictPredictList != null) {
			mergedPredictSet.addAll(dictPredictList);
		}
		int predictCount = 0;
		mergedPredictList = new ArrayList<String>();
		for (String str : mergedPredictSet) {
			System.out.print(
					new StringBuilder("(").append(++predictCount).append(") ").append(str).append(" ").toString());
			mergedPredictList.add(str);
			if (predictCount == 5) {
				break;
			}
		}
		System.out.println();
		/*
		 * System.out.println("##################"); if (histPredictList !=
		 * null) { for (String str : histPredictList) { System.out.print(new
		 * StringBuilder(str).append(" + ").toString()); } }
		 * System.out.println(); if (dictPredictList != null) { for (String str
		 * : dictPredictList) { System.out.print(new StringBuilder(str).append(
		 * " * ").toString()); } } System.out.println();
		 */
	}

	/**
	 * 
	 * @param dictFile
	 * @return
	 */
	public boolean loadDictionary(String dictFileDir) {
		boolean stat = true;
		File dictDir=new File(dictFileDir);
		if (!dictDir.exists() || !dictDir.isDirectory()) {
			System.err.println("Error: unable to access directory [" + dictFileDir + "]");
			return false;
		}
		
		// System.out.println("dictDir=" + dictDir.getAbsolutePath());
		StringBuilder dictFilePath=new StringBuilder(dictFileDir).append(File.separator)
			.append(DICT_FILE);
		File dictFile = new File(dictFilePath.toString());
		if (dictFile.canRead()) {
			dictAutoEngine.establish(dictFile);
			dirPath = dictFile.getParent();
		} else {
			System.out.println("Error: unable to access dictionary file [" +
				dictFile.getAbsolutePath() + "]");
			stat = false;
		}
		
		return stat;
	}

	/**
	 * 
	 * @param wordSelected
	 */
	private void onSelectWord(String wordSelected) {
		if (!userHistory.contains(wordSelected)) {
			userHistory.add(wordSelected);
			histAutoEngine.reset();
			histAutoEngine.insert(wordSelected);
		}
		dictAutoEngine.reset();
		histAutoEngine.reset();
	}

	/**
	 * 
	 */
	public void persistUserSelection() {
		String outFile = new StringBuilder(dirPath).append(File.separator).append(USER_HISTORY_FILE).toString();
		FileOutputStream fos = null;
		BufferedWriter bw = null;
		try {
			fos = new FileOutputStream(outFile);
			bw = new BufferedWriter(new OutputStreamWriter(fos));
			for (String word : userHistory) {
				bw.write(word);
				bw.newLine();
			}
		} catch (IOException e) {
			System.out.println(new StringBuilder("Error in writing user history file [").append(outFile).append("] - ")
					.append(e.getMessage()).toString());
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					System.out.println(new StringBuilder("Error in closing user history file [").append(outFile)
							.append("] - ").append(e.getMessage()).toString());
				}
			}
		}
	}

	/**
	 * 
	 * @param scanner
	 * @param firstFlag
	 * @param autoCompleteSB
	 * @return
	 */
	private String readUserInput(Scanner scanner, boolean firstFlag, StringBuilder autoCompleteSB) {
		boolean validInput=false;
		String userInput=null;
		while (!validInput) {
			System.out.print(new StringBuilder("Enter the ")
				.append(firstFlag ? "first" : "next")
				.append(" character (").append(autoCompleteSB)
				.append("): ").toString());
			userInput = scanner.nextLine();
			if (!validateUserInput(userInput, false)) {
				System.out.println("Invalid User Input: " + userInput);
				System.out.println();
				userInput = null;
			} else {
				validInput = true;
			}
		}
			
		return userInput;
	}
	
	public void startApp() {
		// 1. Create a user input scanner
		Scanner scanner = new Scanner(System.in);
		String userInput = null;
		StringBuilder autoCompleteSB = new StringBuilder();
		boolean exitFlag = false, doneFlag = false, wordComplFlag = false;
		List<Double> predictTimeList = new ArrayList<Double>();
		List<String> dictPredictList = new ArrayList<String>(), histPredictList = new ArrayList<String>();
		int acceptNum = 0;
		do {
			// prompt the user
			userInput = readUserInput(scanner, true, autoCompleteSB);
			
			doneFlag = wordComplFlag = false;
			while (!doneFlag) {
				if (isNumeric(userInput)) {
					wordComplFlag = true;
				} else {
					switch (userInput) {
					case END_OF_AUTO_COMPL:
						doneFlag = true;
						break;
					case END_OF_SESSION:
						doneFlag = exitFlag = true;
						break;
					}
				}
				
				if (wordComplFlag) {
					acceptNum = Integer.valueOf(userInput);
					if (dictPredictList != null) {
						System.out.println();
						System.out.println();
						if (acceptNum > 0 && acceptNum < mergedPredictList.size() + 1) {
							String wordSelected = mergedPredictList.get(acceptNum - 1);
							System.out.println("WORD COMPLETED:  " + wordSelected);
							onSelectWord(wordSelected);
							autoCompleteSB = new StringBuilder("");
							doneFlag = true;
						} else {
							doneFlag = true;
							System.out.println("ERROR: Invalid Word Selection - #" + acceptNum);
							autoCompleteSB = new StringBuilder("");
							dictAutoEngine.reset();
							histAutoEngine.reset();
							autoCompleteSB.setLength(0);
							System.out.println();
							System.out.println();
						}
						System.out.println();
						System.out.println();
					}
				} else if (doneFlag) {
					autoCompleteSB = new StringBuilder("");
					dictAutoEngine.reset();
					histAutoEngine.reset();
					autoCompleteSB.setLength(0);
					System.out.println();
					System.out.println();
				} else if (exitFlag) {
				} else if (!doneFlag || !exitFlag) {
					autoCompleteSB.append(userInput.toLowerCase());
					long startTime = System.nanoTime();
					dictPredictList = dictAutoEngine.predict(autoCompleteSB.toString());
					histPredictList = histAutoEngine.predict(autoCompleteSB.toString());
					double predictTime = (double) ((System.nanoTime() - startTime) / 1000000000.0);
					DecimalFormat df = new DecimalFormat("0.000000");
					System.out.println("(" + df.format(predictTime) + " s)");
					predictTimeList.add(predictTime);
					System.out.println(new StringBuilder("Predictions for ").append(autoCompleteSB).append(":"));
					printPredictList(histPredictList, dictPredictList);

					System.out.println(); System.out.println();
					userInput = readUserInput(scanner, false, autoCompleteSB);
				}
			}
		} while (!exitFlag);
		double totalSec = 0.0;
		System.out.println(); System.out.println();
		for (double sec : predictTimeList) {
			totalSec += sec;
		}
		if (predictTimeList.size() > 0) {
			DecimalFormat df = new DecimalFormat("0.000000");
			System.out.println("Average time: " + 
					df.format(totalSec / predictTimeList.size()) + " s");
		}
		scanner.close();
		persistUserSelection();
	}

	AutoCompleteEngine dictAutoEngine;
	AutoCompleteEngine histAutoEngine;
	Set<String> userHistory;
	List<String> mergedPredictList;
	String dirPath;
}

public class ac_test {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Please include dictionary_file_path as argument!");
			System.exit(1);
		}
		AutoCompleteApp app = new AutoCompleteApp();
		if (app.loadDictionary(args[0])) {
			app.startApp();
		}
		System.out.println("Bye!");
		System.exit(0);
	}
}
