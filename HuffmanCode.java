import java.io.*;
import java.util.Scanner;
import java.util.PriorityQueue;

public class HuffmanCode {

	public static int R = 256;		//size of extended ascii library
	public static Scanner kbd = new Scanner(System.in);
	
	public static void main(String[] args)
	{
		int choice;		
		
		System.out.println("Huffman Code Compressor");
		System.out.println("---------------------------------");
		
		do
		{
			System.out.println("1 - Compress a file");
			System.out.println("2 - Decompress a file");
			choice = kbd.nextInt();
			switch(choice)
			{
				case 1:
					
					compress();	
					System.out.println("Compression is finished. Check project folder for resulting files.");
					break;
				
				case 2:
					
					decompress();
					System.out.println("Decompression is finished. Check project folder for resulting files.");
					break;
					
				default:
					
					System.out.println("Invalid option. Try again.");
			}
			
		}while(choice != 1 && choice != 2);

		kbd.close();
	}
	
	//method for compressing a file
	public static void compress()
	{		
		//initialize all file readers and writers
		BufferedReader inputStream = null;
		PrintWriter statStream = null;
		FileOutputStream encodedStream = null;
		
		//get name of file
		System.out.println("Input name of human readable file (Include any extensions)");
		String fileName = kbd.next();
		
		File file = new File(fileName);		//create object of file to get length
		
		int fileLength = (int)file.length();
		
		try
		{
			//set values for readers and writers
			inputStream = new BufferedReader(new FileReader(file));
			statStream = new PrintWriter(new FileWriter("stats"+ fileName));
			encodedStream = new FileOutputStream("encoded"+fileName);
			
			char[] input = new char[fileLength+1];	//char array for input file
			
			inputStream.read(input, 0, fileLength);		//transfers file into input array
			
			input[fileLength] = (char)3;	//end of text marker
			
			inputStream.close();
			
			int[] freq = new int[R];	//freq holds the frequency of each character by ascii code
			
			//find all character frequencies
			for(int i = 0; i < input.length; i++)
			{
				freq[input[i]]++;
			}
			
			Node root = buildTree(freq);	//creates Huffman tree
			
			String[] encodings = new String[R];		//array for holding the encodings of each character

			buildLookupTable(encodings, root, "", statStream);		//builds lookup table that outputs to stats file
			
			
			//converting binary strings to unintelligible binary characters
			int compressedBitLength = 0;
			for(int i = 0; i < input.length; i++)
			{
				compressedBitLength += encodings[input[i]].length();
			}
			byte[] outputBytes = new byte[compressedBitLength/8+1];
			
			int bitCounter = 0;
			int byteCounter = 0;
			int offset = 0;
			byte mask = 1;
			for(int i = 0; i < input.length; i++)
			{
				// j goes from msb to lsb
				for(int j = 0; j < encodings[input[i]].length(); j++)
				{
					// mask needs to also go from msb to lsb
					mask = (byte)(1 << (7-offset));

					if(encodings[input[i]].charAt(j) == '1')
					{
						outputBytes[byteCounter] = (byte)(outputBytes[byteCounter] | mask);
					}
					else
					{
						mask = (byte)~mask;
						outputBytes[byteCounter] = (byte)(outputBytes[byteCounter] & mask);
					}
					
					bitCounter++;
					byteCounter = bitCounter/8;
					offset = bitCounter % 8;
				}
			}
			//end of bit conversion
			
			encodedStream.write(outputBytes);	//output bytes to encoded file
			
			inputStream.close();
			statStream.close();
			encodedStream.close();
			
		}
		catch(IOException e)
		{
			System.out.println("File not found");
			return;
		}
	}
	
	//method for decompressing a file
	public static void decompress()
	{
		BufferedReader statistics = null;
		FileInputStream encodedStream = null;
		int[] freq = new int[R];
		
		System.out.println("Please input the name of the file you would like decompressed (include all extentions)");
		String fileName = kbd.next();
		System.out.println("Please input the name of the statistics file (include all extentions)");
		String stats = kbd.next();
		
		File file = new File(fileName);
		int fileLength = (int)file.length();
		byte[] contents = new byte[fileLength];
		String[] bits = new String[fileLength*8];
		try
		{
			encodedStream = new FileInputStream(fileName);
			encodedStream.read(contents);
			encodedStream.close();
		}
		catch(IOException e)
		{
			System.err.println("Caught Exception: " + e.getMessage());
			return;
		}
		
		
		for(int i = 0; i < bits.length; i++)
		{
			int offset = i/8;
			byte mask = (byte)(1<<(7-(i%8)));
			if((contents[offset] & (int)mask) == 0)
			{
				bits[i] = "0";
			}
			else
			{
				bits[i] = "1";
			}
		}
		
		try
		{
			statistics = new BufferedReader(new FileReader(stats));
			
			String line;
			while((line = statistics.readLine()) != null)
			{
				String[] tokens = line.split("\t");
				if(tokens[0].charAt(0) != '\'')
				{
					int charIndex = Integer.parseInt(tokens[0]);
					int fr = Integer.parseInt(tokens[1]);
					freq[charIndex] = fr;
				}
				else
				{
					int charIndex = (int)tokens[0].charAt(1);
					int fr = Integer.parseInt(tokens[1]);
					freq[charIndex] = fr;
				}
			}
			statistics.close();

		}
		catch(IOException e)
		{
			System.out.println("File not found");
			return;
		}
		
		Node root = buildTree(freq);
		
		Node node = root;
		String stream = "";
		for(int i = 0; i < bits.length; i++)
		{
			if(bits[i] == "0")
			{
				node = node.getLeft();
			}
			else
			{
				node = node.getRight();
			}
			
			if(node.isLeaf())
			{
				if(node.getCh() == (char)3)
				{
					break;
				}
				stream = stream.concat(Character.toString(node.getCh()));
				node = root;
			}
		}
		
		try
		{
			FileWriter output = new FileWriter("decoded"+fileName);
			output.write(stream);
			output.close();
		}
		catch(IOException e)
		{
			System.err.println("Caught Exception: " + e.getMessage());
			return;
		}
		
	}

	//builds Huffman binary tree
	public static Node buildTree(int[] freq)
	{
		PriorityQueue<Node> pq = new PriorityQueue<Node>();
		for (char i = 0; i < R; i++)
		{
			if(freq[i] > 0)
			{
				pq.offer(new Node(i, freq[i], null, null));
			}
		}
		
		while(pq.size() > 1)
		{
			Node left = pq.poll();
			Node right = pq.poll();
			Node parent = new Node('\0', left.getFreq() + right.getFreq(), left, right);
			pq.offer(parent);
		}
		return pq.peek();
	}
	
	//builds encoding lookup table that outputs to the stats file
	public static void buildLookupTable(String[] strEncoding, Node n, String s, PrintWriter output)
	{
		if(!n.isLeaf())
		{
			buildLookupTable(strEncoding, n.getLeft(), s + '0', output);
			buildLookupTable(strEncoding, n.getRight(), s + '1', output);
		}
		else
		{
			strEncoding[n.getCh()] = s;
			n.setEncoding(s);
			if(n.getCh() > 32 && n.getCh() < 127)
			{
				output.println("'" + n.getCh() + "'" + "\t" + n.getFreq() + "\t" + n.getEncoding());
			}
			else
			{
				output.println((int)n.getCh() + "\t" + n.getFreq() + "\t" + n.getEncoding());
			}
		}
	}
}