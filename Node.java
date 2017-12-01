
public class Node implements Comparable<Node>{
	
	private char key;
	private int freq;
	private Node left;
	private Node right;
	private String encoding;
	
	Node(char key, int freq, Node left, Node right)
	{
		this.key = key;
		this.freq = freq;
		this.left = left;
		this.right = right;
		this.encoding = null;
	}
	
	public boolean isLeaf()
	{
		if(left == null && right == null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public int getFreq()
	{
		return this.freq;
	}
	
	public char getCh()
	{
		return this.key;
	}
	
	public Node getLeft()
	{
		return this.left;
	}
	
	public Node getRight()
	{
		return this.right;
	}
	
	public void setEncoding(String code)
	{
		this.encoding = code;
	}
	
	public String getEncoding()
	{
		return this.encoding;
	}
	
	public int compareTo(Node that)
	{
        return this.freq - that.freq;
    }
}
