package p2;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class LZWIOStream {
	
	private static InputStream inStream	=	System.in;
	private static OutputStream outStream	=	System.out;
	
	// don't instantiate
    private LZWIOStream() { }
    
	public static InputStream getInputStream() { return inStream; }
	public static OutputStream getOutputStream() { return outStream; }
	
	/**
	 * 
	 * @param inputStream
	 */
    public static void setInputStream(InputStream inputStream) {
    	if (inputStream == null) {
    		inStream	= System.in;
    	} else {
    		inStream = inputStream;
    	}
    }

    /**
     * 
     * @param ostream
     */
    public static void setOutputStream(OutputStream ostream) {
    	if (ostream == null) {
    		outStream	= System.out;
    	} else {
    		outStream = ostream;
    	}
    }

    /**
     * 
     * @param args
     */
    public static void handleIOStream(String[] args) {
    	
    	String inputFileName=null, outputFileName=null;
    	if (args.length == 0) {
    		throw new IllegalArgumentException("Illegal command line argument");
    	} else if (args.length > 1) {
    		inputFileName = args[1];
    		if (args.length == 3) {
    			outputFileName = args[2];
    		}
    	}
    	
    	FileInputStream fis=null;
    	FileOutputStream fos=null;
		try {
			File inputFile = new File(inputFileName);
			fis = new FileInputStream(inputFile);
			setInputStream(fis);
			
			if (outputFileName != null && outputFileName.length() > 0) {
				File outputFile = new File(outputFileName);
				fos = new FileOutputStream(outputFile);
				setOutputStream(fos);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}
