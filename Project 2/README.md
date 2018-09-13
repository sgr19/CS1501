#Project 2
Note: No files are present in example_files because they were .gitignore'd from the repository. This program should be able to compress any file regardless.

#Goal: To understand the innerworkings and implementation of the LZW compression algorithm, and to gain a better understanding of the performance it offers.

#Description: 
  *Modify the current LZW code provided by authors of the textbook to use variable-width codewords and rest codebook under certain conditions:
  *   myLZW.java will be a copy of LZW.java that will be modified
  *   myLZW.java will require BinaryStdIn.java, BinaryStdOut.java, TST.java, Queue.java, StdIn.java, and StdOut.java which are in the repository.
  *Changes:
  *   myLZW.java will vary the size of the codewords from 9 to 16 bits
  *   when the codebook is filled there will be 3 options (Do Nothing, Monitor and Reset)
  *   Do Nothing will continue to use the full codebook (same as LZW.java)
  *   Monitor will keep the full codebook and monitor the compression ratio whenever you fill the codebook. The ratio of the codebook will be the size of uncompressed data/ size of compressed data (if compression ratio degrades by more than a set of threshold, it will reset back to its initial state.
  *    Reset will reset the dictionary back to its initial state so new codewords can be added
