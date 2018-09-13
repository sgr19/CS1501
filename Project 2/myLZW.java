
/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/
package p2;
public class AudreyLZW {
	
	// constants
    public static final int MAX_CHAR_ASCII 			= 256; // number of input chars
	public static final int MIN_CODEWORD_BITLEN		= 9;
	public static final int MAX_CODEWORD_BITLEN		= 16;
	 
	// in monitor mode, don't let the ratio of compression ratios exceed this value
	private static final double RATIO_OF_RATIOS_THRESHOLD = 1.1;

	
    // static data members
    private static int codewordBitLen = MIN_CODEWORD_BITLEN; // codeword bit length
    // number of codewords = 2^codewordWidth
    private static int codewordSize = calcCodewordSize(codewordBitLen);
    
    // monitor mode compression ratio
    private static double mntOldRatio = 0.0; // ratio recorded when last filled the codebook
    private static double mntNewRatio = 0.0; // current compression ratio
    private static int mntUncmprSize = 0; // size of the uncompressed data generated/processed so far
    private static int mntCmprSize = 0; // size of the compressed data generated/processed so far
    private static boolean isMonitorMode=false;
    
    
    /**
     * update the codewordBitLen and codewordSize accordingly
     */
    private static void updateCodeword(int bitLen) {
    	codewordBitLen = bitLen;
    	codewordSize = calcCodewordSize(codewordBitLen);
    }
    
    /**
     * calculate the codeword size
     * @param bitWidth
     * @return
     */
    private static int calcCodewordSize(int bitWidth) {
    	return (int) Math.pow(2, bitWidth);
    }
    
    /**
     * build the compression dictionary
     * @return TST<Integer> dictTable
     */
    private static TST<Integer> buildCompressionDictTable() {
    	TST<Integer> dictTable = new TST<Integer>();
        for (int i = 0; i < MAX_CHAR_ASCII; i++) {
        	dictTable.put("" + (char) i, i);
        }
        	
        return dictTable;
    }

    /**
     * build the expansion dictionary
     * @return String[] dictTable
     */
    private static String[] buildExpansionDictTable() {
    	int tableSize=calcCodewordSize(MAX_CODEWORD_BITLEN);
        String[] dictTable = new String[tableSize];
        int i; // next available codeword value

        // initialize dictionary with all 1-character strings
        for (i = 0; i < MAX_CHAR_ASCII; i++)
        	dictTable[i] = "" + (char) i;
        dictTable[i++] = ""; 
        
        return (dictTable);
    }
    
    /**
     * update the size of the compressed and uncompressed data that 
     * has been processed/generated so far
     * @param str
     * @param bitLen
     */
    private static void updateMonitorDataSize(String str, int bitLen) {
    	if (!isMonitorMode) return;
        mntUncmprSize += (str.length() * 16);
        mntCmprSize += bitLen;
    }
    
    private static void updateMonitorOldRatios () {
    	if (!isMonitorMode) return;
    	mntOldRatio = (double)(mntUncmprSize / mntCmprSize);
    }
    
    private static void updateMonitorNewdRatios () {
    	if (!isMonitorMode) return;
    	mntNewRatio = (double)(mntUncmprSize / mntCmprSize);
    }
    
    /**
     * check if "ratio of ratios" exceeds threshold
     * @return boolean
     */
    private static boolean isMonitorRatioOfRatiosExceeds() {
    	if (!isMonitorMode) return false;
    	double ratioOfCompressionRatios = mntOldRatio/mntNewRatio;
        return (ratioOfCompressionRatios > RATIO_OF_RATIOS_THRESHOLD);
    }
    
    /**
     * data compression for dynamic codeword bit length
     * @param dictTable
     * @param code
     * @return code
     */
    private static int compressDynamic(TST<Integer> dictTable, int code) {
    	
    	if (codewordBitLen < MAX_CODEWORD_BITLEN) {
    		// update codewordBitlen and codewordSize
    		updateCodeword(codewordBitLen+1);
    	} else { // dictionary reaches MAX_BIT_SIZE threshold
    		CompressMode mode = LZWSetup.getCompressMode();
    		switch (mode) {
    			case RESET_MODE:
    				dictTable = buildCompressionDictTable();
    				code = MAX_CHAR_ASCII + 1;
    				updateCodeword(MIN_CODEWORD_BITLEN);
    				break;
    				
    			case MONITOR_MODE:

                    // If the compression ratio degrades by more than a set threshold 
                    // from the point when the last codeword was added, 
                    // then reset the dictionary back to its initial state.
                    updateMonitorNewdRatios();
                    if(isMonitorRatioOfRatiosExceeds()) {
                    	dictTable = buildCompressionDictTable();
                    	updateCodeword(MIN_CODEWORD_BITLEN);
                    	code = MAX_CHAR_ASCII + 1;
                    }
    				break;
    				
    			case DO_NOTHING_MODE:
    				// do nothing
    				break;
    		}
    	}
    	
    	return code;
    }

    /**
     * LZW Data Compression
     */
    public static void compress() { 
    	
    	isMonitorMode = (LZWSetup.getCompressMode() == CompressMode.MONITOR_MODE);

    	// write the compression mode
        BinaryStdOut.write((byte) LZWSetup.getCompressModeChar());

    	TST<Integer> st = buildCompressionDictTable();
        int code = MAX_CHAR_ASCII+1;  // inputCharSize is codeword for EOF
        String input = BinaryStdIn.readString();
        while (input.length() > 0) {
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), codewordBitLen);      // Print s's encoding.

            if (isMonitorMode) {
            	updateMonitorDataSize(s, codewordBitLen);
            }

            int t = s.length();
            if (t < input.length()) {
            	if (code < codewordSize) {   
            		st.put(input.substring(0, t + 1), code++); // Add s to dictionary.
            		if (isMonitorMode) {
            			updateMonitorOldRatios();
            		}
            	} else {
            		// The codeword size should be increased when 
            		// all of the codewords of a previous size have been used
            		code = compressDynamic(st, code);
            		st.put(input.substring(0, t + 1), code++); // Add s to dictionary.
            	}
            }
            input = input.substring(t);            // Scan past s in input.
        }
        
        BinaryStdOut.write(MAX_CHAR_ASCII, codewordBitLen);
        BinaryStdOut.close();
    } 

    /**
     * data expansion for dynamic codeword bit length
     * @param dictTable
     * @param code
     * @return code
     */
    private static int expandDynamic(String[] dictTable, int code) {
    	
    	if (codewordBitLen < MAX_CODEWORD_BITLEN) { 
    		// update codewordBitlen and codewordSize
    		updateCodeword(codewordBitLen+1);
    		if (isMonitorMode) {
    			updateMonitorOldRatios();
    		}
    	} else if (codewordBitLen == MAX_CODEWORD_BITLEN) { // codewordWidth reaches threshold 
    		CompressMode mode = LZWSetup.getCompressMode();
    		switch (mode) {
    			case RESET_MODE:
    				dictTable = buildExpansionDictTable();
    				updateCodeword(MIN_CODEWORD_BITLEN);
    				code = MAX_CHAR_ASCII + 1;
    				break;
    				
    			case MONITOR_MODE:
    				updateMonitorNewdRatios();
    				
    				// check if ratio exceeds threshold for reset
    				if (isMonitorRatioOfRatiosExceeds()) {
        				dictTable = buildExpansionDictTable();
        				updateCodeword(MIN_CODEWORD_BITLEN);
        				code = MAX_CHAR_ASCII + 1;
        				mntOldRatio = 0.0;
                    }
    				break;
    				
    			case DO_NOTHING_MODE:
    				// do nothing
    				break;
    		}
    	}
    	
    	return code;
    }
    
	/**
     * LZW Data Expansion
     */
    public static void expand() {
    	
    	// read the first byte for the mode
    	char dynamicMode = BinaryStdIn.readChar(); 
    	LZWSetup.setDynamicMode(dynamicMode);
    	isMonitorMode = (LZWSetup.getCompressMode() == CompressMode.MONITOR_MODE);
    	
    	// read the second byte for the codeword bit length
        int codeword = BinaryStdIn.readInt(codewordBitLen);
        if (codeword == MAX_CHAR_ASCII)
        	return; // expanded message is empty string
        
    	String[] st = buildExpansionDictTable();
        String val = st[codeword];
    	int i=MAX_CHAR_ASCII+1; // next available codeword value

        while (true) {
        	
            BinaryStdOut.write(val);
            
            if (isMonitorMode) {
            	updateMonitorDataSize(val, codewordBitLen);
            }
            
            if (i >= codewordSize) { // codeword size threshold is reached
            		i = expandDynamic(st, i);
            }
            
            codeword = BinaryStdIn.readInt(codewordBitLen);
            if (codeword == MAX_CHAR_ASCII) break;
            String s = st[codeword];
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            if (i < codewordSize) {
            	st[i++] = val + s.charAt(0);
            	updateMonitorOldRatios();
            }
            val = s;
        }
            
        BinaryStdOut.close();
    }

    /**
     * main program
     * @param args
     */
    public static void main(String[] args) {
    	// handle command arguments and enhance input and output file specification
    	// to enable the output trace shown on console
    	LZWSetup.handleCmdArg(args); 
    	
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
        
        System.exit(0);
    }

}
