import java.util.ArrayList;

/**
 * Disjoint sets class, using union by size and path compression.
 * @author Sergio Delgado.
 *
 * @param <T> Generic type.
 */
public class DisjointSets<T>
{
	private int[] s; //array that will contain the roots of the sets
	private ArrayList<Set<T>> sets; //the actual data for the sets

	/**
	 * Constructor of DisjointSets
	 * @param data ArrayList containing all elements to be added to the sets (ArrayList of sets).
	 */
	public DisjointSets(ArrayList<T> data)
	{
		s = new int[data.size()];
		sets = new ArrayList<Set<T>>();
		
		for (int i=0; i<data.size(); i++)	// initializing array of roots
			s[i]= -1;
		
		for (int i=0; i<data.size(); i++)	// inserting each element from data into sets.
		{
			Set<T> newSet = new Set<T>();   // creating a new set, which will be a new element to add to sets (ArrayList of Set<T>)
			newSet.add(data.get(i));		// adding the corresponding element to that new set, which is data(i) (ArrayList of pixel<A,B>)
			
			sets.add(newSet);				// adding each new set (with one element in it) to the sets ArrayList 
		}
	}

	/**
	 * Compute the union of two sets using rank union by size.
	 * if two sets are equal, root1 is the new root
	 * @param root1 int value representing root of set to be unioned.
	 * @param root2 int value representing root of the other set to be unioned.
	 * @return int value that represents new root of the unioned set
	 */
   public int union( int root1, int root2 )
   {
       assertIsRoot( root1 );
       assertIsRoot( root2 );
	   int newRoot;   
	        
       if( root1 == root2 )
    	   return root1;
 
       if( s[ root2 ] < s[ root1 ] )  // if root2 is deeper
       {	   
    	   s[root2] = s[root2] + s[root1];  // root2 grows by adding size of root1;
    	   s[root1] = root2;				// root1 takes root2 as its parent;
           newRoot = root2;        			// Make root2 new root
           
           sets.get(root2).addAll(sets.get(root1));
           sets.get(root1).clear();
       }    
       
       else                         		// if root1 is deeper or as deep as root2
       {
    	   s[root1] = s[root1] + s[root2];  // root1 grows by adding size of root1;
    	   s[root2] = root1;				// root2 takes root1 as its parent;
           newRoot = root1;        			// Make root2 new root
           
           sets.get(root1).addAll(sets.get(root2));
           sets.get(root2).clear();
       }
       
       return newRoot;
   }	
	
	/**
	 * Find and return the root. Implements path compression.
	 * @param x int value representing the element of the set whose root will be found.
	 * @return int value representing the root of the set containing x.
	 */
	public int find(int x)
	{
        assertIsItem( x );
        if( s[ x ] < 0 )
            return x;
        else
            return s[ x ] = find( s[ x ] );

	}

	/**
	 * Checks whether or not a value represents the root of a set
	 * @param root set's root to be analyzed.
 	 * @throw IllegalArgumentException() if non-roots provided
	 */
    private void assertIsRoot( int root )
    {
        assertIsItem( root );    //   Making sure the root is within boundaries
        if( s[ root ] >= 0 )
        {   
        	throw new IllegalArgumentException( "Union: " + root + " not a root" );
        }
    }
	   
	/**
	 * Checks whether or not a value represents an element of a set.
	 * @param root set's root to be analyzed.
	 * @throw IllegalArgumentException() if element x is out of bounds for the set.
	 */    
    private void assertIsItem( int x )
    {
        if( x < 0 || x >= s.length )
            throw new IllegalArgumentException( "Disjoint sets: " + x + " not an item" );       
    }	
	
    
    /**
     * Get all the data in the same set. O(1).
     * @param root int value representing root of set to be retrieved.
     * @return Set<T> the set whose root is represented by parameter (root).
     */
	public Set<T> get(int root) 
	{
		int r = find(root);
		
		return sets.get(r); 
	}
	
	/**
	 * main method just for your testing
	 */
	public static void main(String[] args) {
		ArrayList<Integer> arr = new ArrayList<>();
		for(int i = 0; i < 10; i++)
			arr.add(i);
		
		DisjointSets<Integer> ds = new DisjointSets<>(arr);
		
		System.out.println(ds.find(0)); //should be 0
		System.out.println(ds.find(1)); //should be 1
		System.out.println(ds.union(0, 1)); //should be 0
		System.out.println(ds.find(0)); //should be 0
		System.out.println(ds.find(1)); //should be 0
		System.out.println("-----");
		System.out.println(ds.find(0)); //should be 0
		System.out.println(ds.find(2)); //should be 2
		System.out.println(ds.union(0, 2)); //should be 0
		System.out.println(ds.find(0)); //should be 0
		System.out.println(ds.find(2)); //should be 0
		System.out.println("-----");
		//Note: AbstractCollection provides toString() method using the iterator
		//see: https://docs.oracle.com/javase/8/docs/api/java/util/AbstractCollection.html#toString--
		//so your iterator in Set needs to work for this to print out correctly
		System.out.println(ds.get(0)); //should be [0, 1, 2]
//		System.out.println(ds.get(1)); //should be []   //This is an error from staff. 
														//to get [] you need the statement ds.sets.get(1).
														//ds.get(1) will print the elements of sets(0) because ds.gets
														//calls find(root) and returns the sets( find(root) ) not sets(root)
		System.out.println(ds.sets.get(1));
		System.out.println(ds.get(3)); //should be [3]
	}
}
