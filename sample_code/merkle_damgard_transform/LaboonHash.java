/**
 * LaboonHash - A simple cryptographic hash function using
 * Merkle-Damgard transforms and Merkle-Damgard strengthening.
 * Blocks are eight bytes and output is two bytes (16 bit).
 * Final blocks are strengthened (padded), if necessary, by length
 * of original string, modulo (2^8 * num_characters_to_pad).
 * Initialization value = AACC
 */

import java.util.Arrays;
import java.util.Math;

public class LaboonHash {


    // Initialization Value
    public static final byte[] INITIAL_VALUE = { (byte) 0xAA, (byte) 0xCC };

    // Size of blocks (in bytes)
    public static final int BLOCK_SIZE = 8;

    // Size of results of compressin function (in bytes)
    public static final int RESULT_SIZE = 2;

    /**
     * Given some arbitrary byte array bytes, convert it to a hex string.
     * Example: [0xFF, 0xA0, 0x01] -> "FFA001"
     * @param bytes arbitrary-length array of bytes
     * @return String hex string version of byte array
     */

    public static String convertBytesToHexString(byte[] bytes) {
	StringBuffer toReturn = new StringBuffer();
	for (int j = 0; j < bytes.length; j++) {
	    String hexit = String.format("%02x", bytes[j]);
	    toReturn.append(hexit);
	}
	return toReturn.toString();
    }

    /**
     * Given a string, calculate how many blocks of size BLOCK_SIZE
     * it should be split into.
     * @param s String to checl
     * @return int number of blocks
     */

    public static int calculateNumBlocks(String s) {
	int numBlocks = -1;
	int len = s.length();
	if (len % BLOCK_SIZE == 0) {
	    numBlocks = len / BLOCK_SIZE;
	} else {
	    numBlocks = (len / BLOCK_SIZE) + 1;
	}
	return numBlocks;
    }

    /**
     * Generate initial (empty) byte blocks given some string
     * @param s String to generate blocks from
     * @return byte[][] empty blocks (will be filled up later)
     */

    public static byte[][] generateInitialBlocks(String s) {
	int numBlocks = calculateNumBlocks(s);
	byte[][] toReturn = new byte[numBlocks][1];
	return toReturn;
    }

    /**
     * Given a String s and an original length, will fill up the remaining
     * space in a block with the original length of the string converted
     * to standard ASCII numerals modulo the size of the remaining space.
     * For example, given an original string 1234567890A (length = 11),
     * and a block size of eight, the blocks will start as
     * ["12345678", "90A"].  We want to pad the final block "09A" so that
     * it is eight chars/bytes long.  So we take 11 % 10^5, i.e.,
     * 11 % 10000, i.e., 11, and zero-pad that value.  So the padded block
     * should be "90A00011"
     * Assume a new string 1234567890ABCDE, length = 15, same block size
     * of 8, so original string blocks = ["12345678", "90ABCDE"].  Now
     * we take 15 % 10 ^ 1, i.e. 15 % 10, i.e. 5, and add that to the
     * end of the block, so the final padded block is "90ABCDE5".
     * This is known as Merkle-Damgard strengthening or padding.
     * NOTE: Padding can have other meanings in cryptography.
     * @param s string block to pad
     * @param len length of original string
     * @return String padded version of block
     */
    public static String pad(String s, int len) {
	int sizeToPad = BLOCK_SIZE - s.length();
	int modValue = Math.pow(10, sizeToPad);
	int moddedLen = len % modValue;
	String padded = String.format("%0" + sizeToPad + "d", moddedLen);
	return padded;
    }

    /**
     * Pad the final block of the array if necessary.
     * @see pad() method for algorithm
     * @param String[] initial blocks
     * @return String[] padded (if necessary)
     */

    public static String[] strengthenIfNecessary(int origLength,
						 String[] stringBlocks) {
	// Theoretically, we could not pass in origLength, and
	// re-calculate length of original string by adding up all
	// of the strings in stringBlocks.  This seemed more straightforward
	// despite the extra argument, however.
	String finalBlock = stringBlocks[stringBlocks.length - 1];
	int finalBlockLength = finalBlock.length();
	if (finalBlockLength < BLOCK_SIZE) {
	    String paddedBlock = finalBlock + pad(finalBlock, origLength);
	    stringBlocks[stringBlocks.length - 1] = paddedBlock;
	}
	return stringBlocks;
    }

    /**
     * Given a String s, split into blocks of at most size BLOCK_SIZE.
     * The following examples assume a BLOCK_SIZE of 8.
     * Example: "AAA" -> ["AAA"]
     * Example: "AAAAAAAABBBBBBBB" -> ["AAAAAAAA", "BBBBBBBB"]
     * Example: "FOOFOOFOO" -> ["FOOFOOFO", "O"]
     * Example: "FOOFOOBARBARQUUXQUUX" -> ["FOOFOOBA", "RBARQUUX", "QUUX"]
     * @param s String to split
     * @return String[] split string
     */

    public static String[] splitBlocks(String s) {
	return s.split("(?<=\\G.{" + BLOCK_SIZE + "})");
    }

    /**
     * Given an array of String blocks, convert them into byte blocks

     */

    public static byte[][] stringBlocksToByteBlocks(String[] stringBlocks) {
	byte[][] toReturn = new byte[stringBlocks.length][BLOCK_SIZE];
	for (int j = 0; j < stringBlocks.length; j++) {
	    // System.out.println("(" + j + ") Converting " + stringBlocks[j]);
	    byte[] bytes = stringBlocks[j].getBytes();
	    for (int k = 0; k < BLOCK_SIZE; k++) {
		// System.out.println("\t(" + k + ") Converting " + bytes[k]);
		toReturn[j][k] = bytes[k];
	    }
	}
	return toReturn;
    }

    public static byte[][] stringToByteBlocks(String s) {
	int origLength = s.length();
	String[] stringBlocks = splitBlocks(s);
	stringBlocks = strengthenIfNecessary(origLength, stringBlocks);

	// Uncomment to see string blocks
	System.out.println("String Blocks:");
	for (String block : stringBlocks) {
	    System.out.println(block);
	}

	byte[][] toReturn = stringBlocksToByteBlocks(stringBlocks);

	// Uncomment to see byte blocks
	System.out.println("Byte Blocks:");
	for (byte[] b : toReturn) {
	    System.out.println(convertBytesToHexString(b));
	}

	return toReturn;
    }

    public static byte[] concatArrays(byte[] a1, byte[] a2) {
	int len1 = a1.length;
	int len2 = a2.length;
	int c = 0;
	byte[] toReturn = new byte[a1.length + a2.length];
	for (int j = 0; j < len1; j++) {
	    toReturn[c++] = a1[j];
	}
	for (int j = 0; j < len2; j++) {
	    toReturn[c++] = a2[j];
	}
	return toReturn;

    }

    public static byte[] compress(byte[] oldResult, byte[] block) {
	byte[] combined = concatArrays(oldResult, block);
	byte[] result = { 65, 63 };
	for (byte b : combined) {
	    result[0] = (byte) ((byte) (result[1] ^ b) + (byte) b);
	    result[1] = (byte) ((byte) (result[0] ^ b) - (byte) b);
	}
	return result;

    }

    public static byte[] compressAll(byte[][] byteBlocks) {
	byte[] result = INITIAL_VALUE;
	int numRounds = 0;
	for (byte[] b : byteBlocks) {
	    System.out.print("Round + " + numRounds++ + ": prev res = "
			     + convertBytesToHexString(result)
			     + ", block = " + convertBytesToHexString(b)
			     + " --> ");
	    result = compress(result, b);
	    System.out.println(convertBytesToHexString(result));
	}
	return result;

    }

    public static byte[] laboonHash(String toHash) {
	byte[][] blocks = stringToByteBlocks(toHash);
	byte[] toReturn = compressAll(blocks);
	return toReturn;
    }

    /**
     * Print usage information and exit program with exit code 1.
     */
    public static void printUsageAndExit() {
	System.err.println("Enter a single string to hash");
	System.exit(1);
    }

    public static void main(String[] args) {
	if (args.length != 1) {
	    printUsageAndExit();
	}
	byte[] result = laboonHash(args[0]);
	System.out.println("Hash: " + convertBytesToHexString(result));

    }
}
