package dis08;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Implementation of the A-Priori-Algorithm.
 * 
 * @author marius
 * 
 */
public class Apriori
{
	private String _dataFile;

	private List<int[]> _itemsets;
	private int _itemsCount;
	private int _transactionsCount;

	private double _minsup;

	public Apriori(String dataFile) throws Exception
	{
		_dataFile = dataFile;
		_minsup = 0.01;
		
		init();
	}
	
	public Apriori(String dataFile, double minsup) throws Exception
	{
		_dataFile = dataFile;
		_minsup = minsup;
		
		init();
	}
	
	private void init() throws Exception
	{
		if (_minsup > 1 || _minsup < 0) throw new Exception("Unsupported value for minsup");
		
		// compute itemsCount and transactionsCount
		_itemsCount = 0;
		_transactionsCount = 0;
               
		BufferedReader inputData = new BufferedReader(new FileReader(_dataFile));
		
		while (inputData.ready()) {
			String line = inputData.readLine();
			
			// ignore empty lines
			if (line.matches("\\s*")) continue;
			
			_transactionsCount++;
			
			StringTokenizer tokenizer = new StringTokenizer(line, " ");
			while (tokenizer.hasMoreTokens()) {
				int x = Integer.parseInt(tokenizer.nextToken());
				
				if (x + 1 > _itemsCount) {
					_itemsCount = x + 1;
				}
			}
			
		}
	}
	
	public void execute() throws Exception
	{
		long start = System.currentTimeMillis();
		
		createItemsetsOfSize1();
		
		// the current itemset
		int itemsetNumber = 1;
		int frequentSets = 0;
		
		while (_itemsets.size() > 0) {
			calculateFrequentItemsets();
			
			if (_itemsets.size() != 0) {
				frequentSets += _itemsets.size();
				System.out.println("Found " + _itemsets.size() + " Frequent-Item-Sets of size " + itemsetNumber +  " (minsup = " + _minsup + ")");
				
				createNewItemsetsFromPrevious();
			}
			
			itemsetNumber++;
		}
		
		long end = System.currentTimeMillis();
		
		System.err.println("Needed " + (end-start) + " miliseconds.");
		
		System.err.println("Found " + frequentSets + " Frequent-Item-Sets (minsup = " + _minsup + ")");
		
	}
	
	/**
	 * Print frequent itemset to console
	 * 
	 * @param itemset
	 * @param count
	 */
	private void printFrequentItemSet(int[] itemset, int count) 
	{
		System.out.println(Arrays.toString(itemset) + " (" + count + " times = " + (count / (double) _transactionsCount) + "%)");
	}

	/**
	 * Save all different itemsets of size 1
	 */
	private void createItemsetsOfSize1()
	{
		_itemsets = new ArrayList<int[]>();

		for (int i = 0; i < _itemsCount; i++) {
			int[] cand = { i };
			_itemsets.add(cand);
		}
	}

	/**
	 * Create all possible itemsets of size n+1 from current itemsets with size
	 * n and replace old itemsets with new ones
	 */
	private void createNewItemsetsFromPrevious()
	{
		// initially all itemsets have the same size
		int currentItemsetsSize = _itemsets.get(0).length;
		
		System.err.println("Creating itemsets of size " + (currentItemsetsSize+1) + " based on " + _itemsets.size() + " itemsets of size " + currentItemsetsSize);
		
		// temporary candidates
		Map<String, int[]> tempCandidates = new HashMap<String, int[]>();

		// compare each pair of itemsets of size n-1
		for (int i = 0; i < _itemsets.size(); i++) {
			for (int j = i + 1; j < _itemsets.size(); j++) {
				int[] X = _itemsets.get(i);
				int[] Y = _itemsets.get(j);

				assert (X.length == Y.length);

				// make a string of the first n-2 tokens of the strings
				int[] newCand = new int[currentItemsetsSize + 1];
				for (int s = 0; s < newCand.length - 1; s++) {
					newCand[s] = X[s];
				}

				int ndifferent = 0;
				// find the missing value
				for (int s1 = 0; s1 < Y.length; s1++) {
					boolean found = false;
					// is Y[s1] in X?
					for (int s2 = 0; s2 < X.length; s2++) {
						if (X[s2] == Y[s1]) {
							found = true;
							break;
						}
					}

					// Y[s1] is not in X
					if (!found) {
						ndifferent++;
						// put the missing value at the end of newCand
						newCand[newCand.length - 1] = Y[s1];
					}
				}

				// find at least 1 different, otherwise the same set is in
				// existing candidates twice
				assert (ndifferent > 0);

				if (ndifferent == 1) {
					Arrays.sort(newCand);
					tempCandidates.put(Arrays.toString(newCand), newCand);
				}
			}
		}

		// set the new itemsets
		_itemsets = new ArrayList<int[]>(tempCandidates.values());
		
		System.err.println("Created " + _itemsets.size() + " unique itemsets of size " + (currentItemsetsSize+1));
	}

	/**
	 * Set trans[i] = true if i exists in the given line
	 * 
	 * @param line
	 * @param trans
	 */
	private void lineToBooleanArray(String line, boolean[] trans)
	{
		Arrays.fill(trans, false);
		// tokenize line by spaces
		StringTokenizer tokenizer = new StringTokenizer(line, " ");

		// put items of that line into transaction array
		while (tokenizer.hasMoreTokens()) {
			int parsedToken = Integer.parseInt(tokenizer.nextToken());
			trans[parsedToken] = true;
		}
	}

	/**
	 * Calculates the Frequent-Item-Sets from the input data
	 */
	private void calculateFrequentItemsets() throws Exception
	{
		
		System.err.println("Looping through the data to compute the frequency of " + _itemsets.size() + " itemsets of size " + _itemsets.get(0).length);
		
		// frequent candidates for the current itemset
		List<int[]> frequentCandidates = new ArrayList<int[]>();
		// whether the transaction has all items in an itemset
		boolean match;
		// number of successful matches
		int count[] = new int[_itemsets.size()];
		// load transaction data
		BufferedReader inputData = new BufferedReader(new InputStreamReader(
				new FileInputStream(_dataFile)));

		boolean[] trans = new boolean[_itemsCount];

		// loop transactions
		for (int i = 0; i < _transactionsCount; i++) {
			String line = inputData.readLine();
			lineToBooleanArray(line, trans);

			// check each candidate
			for (int c = 0; c < _itemsets.size(); c++) {
				match = true;
				// tokenize candidate to know what items need to be present for
				// a match
				int[] cand = _itemsets.get(c);
				// check each item in the itemset to see if it is present in the
				// transaction
				for (int x : cand) {
					if (trans[x] == false) {
						match = false;
						break;
					}
				}

				// if items match, increase count
				if (match) {
					count[c]++;
				}
			}
		}

		inputData.close();

		for (int i = 0; i < _itemsets.size(); i++) {
			// if there are relatively more items than minsup, add the
			// candidates to frequent candidates
			if ((count[i] / (double) _transactionsCount) >= _minsup) {
				printFrequentItemSet(_itemsets.get(i), count[i]);
				frequentCandidates.add(_itemsets.get(i));
			}
		}
		
		// update itemsets, only frequent candidates can be new candidates
		_itemsets = frequentCandidates;
	}
}
