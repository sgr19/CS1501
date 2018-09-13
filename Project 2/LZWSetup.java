package p2;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class LZWSetup {
	
	private static InputStream inStream		=	System.in;
	private static OutputStream outStream	=	System.out;
	private static CompressMode compressMode = CompressMode.DO_NOTHING_MODE;

	// don't instantiate
    private LZWSetup() { }
    
	public static InputStream getInStream() {
		return inStream;
	}

	public static void setInStream(InputStream inStream) {
		LZWSetup.inStream = inStream;
	}

	public static OutputStream getOutStream() {
		return outStream;
	}

	public static void setOutStream(OutputStream outStream) {
		LZWSetup.outStream = outStream;
	}

	public static CompressMode getCompressMode() {
		return compressMode;
	}

	public static void setCompressMode(CompressMode compressMode) {
		LZWSetup.compressMode = compressMode;
	}

	/**
	 * 
	 * @return
	 */
	public static char getCompressModeChar() {
		char mode = 'n';
		switch (compressMode) {
		case MONITOR_MODE:
			mode = 'm';
			break;
		case DO_NOTHING_MODE:
			mode = 'n';
			break;
		case RESET_MODE:
			mode = 'r';
			break;
		default:
			mode = 'n';
			break;
		}
		return mode;
	}

	/**
	 * 
	 * @param mode
	 */
	public static void setDynamicMode(char mode) {
		switch (mode) {
		case 'm':
			setCompressMode(CompressMode.MONITOR_MODE);
			break;
		case 'n':
			setCompressMode(CompressMode.DO_NOTHING_MODE);
			break;
		case 'r':
			setCompressMode(CompressMode.RESET_MODE);
			break;
		default:
			throw new IllegalArgumentException("Invalid Compression Mode: " + mode);
		}
	}
	
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
    public static void handleCmdArg(String[] args) {
    	
    	String inputFileName=null, outputFileName=null;
    	int argPos=0;
    	boolean fileFlag=false;
    	if (args.length < 2) {
    		throw new IllegalArgumentException("Illegal command line argument");
    	} else {
    		String action=args[argPos];
    		if ("-".equals(action))  {// compress
    			String mode=args[++argPos];
				switch (mode) {
				case "m":
					compressMode = CompressMode.MONITOR_MODE;
					break;
				case "n":
					compressMode = CompressMode.DO_NOTHING_MODE;
					break;
				case "r":
					compressMode = CompressMode.RESET_MODE;
					break;
				}
			}
    		
			if (args.length > argPos) {
				inputFileName = args[++argPos];
				if (args.length > argPos) {
					outputFileName = args[++argPos];
					fileFlag = true;
				} 
			}
    	}
    	
    	if (!fileFlag) {
    		return;
    	}
    	
    	FileInputStream fis=null;
    	FileOutputStream fos=null;
		try {
			File inputFile = new File(inputFileName);
			fis = new FileInputStream(inputFile);
			setInStream(fis);
			
			if (outputFileName != null && outputFileName.length() > 0) {
				File outputFile = new File(outputFileName);
				fos = new FileOutputStream(outputFile);
				setOutStream(fos);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
