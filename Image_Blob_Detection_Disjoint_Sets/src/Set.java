import java.util.AbstractCollection;
import java.util.Iterator;

public class Set<T> extends AbstractCollection<T> {
	//O(1)

	/**
	 * Declaration of private class Node
	 */	
	private class Node<T>
	{
		private T value;
		private Node<T> next;
		
		/**
		 * Constructor of Node 
		 * @param v, Generic value, stores node's data.
		 */
		private Node(T v)
		{
			value = v;
			next = null;
		}
	}
	
	Node<T> head;	//  Head of Node
	Node<T> tail;	//	Tail of Node
	int size;
	
	/**
	 * Constructor of Set.
	 * Sets the head, tail and size to an initial state of null, null and 0 respectively
	 */
	public Set() 
	{
		head = null;
		tail = null;
		size = 0;
	}
	
	/**
	 * Adds an item (Node) at the end of the Set. O(1).
	 * @param item Genetic value to be stored by the Node about to be added.
	 * @return boolean type value, indicating whether insertion was successful.
	 */
	public boolean add(T item)
	{
		Node<T> elem = new Node<>(item);
		if (head == null)
			head = tail = elem;
		else
			tail.next = elem;
		
		tail = elem;
		size++;
		
		return true;
	}
	
	/**
	 * Adds all elements of one set to the end of this set. O(1).
	 * @param other Set to be added at end of this set.
	 * @return Boolean which indicates whether addition was successful.
	 */
	public boolean addAll(Set<T> other) 
	{
		boolean success = true;
		if (other != null)	// if set to insert is not empty
		{
			if (head != null)  // if this set is not empty either
			{
				tail.next = other.head;		// add all elements to this set
				tail = other.tail;
				size = size + other.size;
			}
			else				// if this set is empty
				head = other.head;	// make head of this set refer to head of received set
		}
		
		else	// if both sets are empty...
			if (head == null)
				success = false;
		
		return success;
	}
	
	//O(1)
	/**
	 * Clears the set of all its elements. O(1).
	 */
	public void clear() 
	{
		head = tail = null;
		size = 0;
	}
	
	/**
	 * @return Integer value indicating number of elements in the set. O(1).
	 */
	public int size() 
	{
		return size;
	}
	
	//O(1) for next() and hasNext()
	/**
	 * Declaration of the set's iterator.
	 */
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			//O(1)
			Node<T> current = head;
			
			/**
			 * Retrieves the next element of the set. O(1).
			 * @return Generic value with the data from the next element of the set.
			 */
			public T next() 
			{
				if (hasNext())
				{
					T value = current.value;
					current = current.next;
					return value; 
				}
				
				else
					return null;
				
			}
			
			/**
			 * Expresses whether or not there is a next element in the set.
			 * @return Boolean value indicating whether or not there is a next element.
			 */
			public boolean hasNext()
			{
				if (current == null)
					return false;
				else
					return true; 
			}
		};
	}

	/**
	 * main method just for your testing.
	 */
	public static void main(String[] args)
	{
		
	}
}
