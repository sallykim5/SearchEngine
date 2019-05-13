import java.util.Scanner;
 
public class Quick {
 
    public static <T extends Comparable<T>> void sort(T[] a) {
        // might shuffle elements to makes sure T is not sorted                                 
        sort(a, 0, a.length - 1);
    }
 
    // quicksort the subarray from a[lo] to a[hi]                                               
    private static <T extends Comparable<T>> void sort(T[] a, int lo, int hi) {
        if (hi <= lo) return;
        int j = partition(a, lo, hi);
        sort(a, lo, j-1);
        sort(a, j+1, hi);
    }
 
    // partition the subarray a[lo..hi] so that a[lo..j-1] <= a[j] <= a[j+1..hi]                
    // and return the index j.                                                                  
    private static <T extends Comparable<T>> int partition(T[] a, int lo, int hi) {
	  int i = lo;
        int j = hi + 1;
        T v = a[lo]; // use the leftmost element for the pivot
	 while (true) {
            // find item on lo to swap                                                          
            while (a[++i].compareTo(v)<0) // search lo to hi 
                if (i == hi) break;
            // find item on hi to swap    // search hi to lo
            while (v.compareTo(a[--j])<0) 
                if (j == lo) break;      // redundant; a[lo] acts as a sentinel              
            // check if pointers cross                                                          
            if (i >= j) break;
            exch(a, i, j);
        }
        // put partitioning item v at a[j]                                                      
        exch(a, lo, j);
        // now, a[lo .. j-1] <= a[j] <= a[j+1 .. hi]                                            
        return j;
    }
 
   /***************************************************************************                 
    *  Helper functions.                                                                        
    ***************************************************************************/
 
    // exchange a[i] and a[j]                                                                   
    private static <T> void exch(T[] a, int i, int j) {
        T swap = a[i];
        a[i] = a[j];
        a[j] = swap;
    }
 
    // print array to standard output                                                           
    private static <T> void show(T[] a) {
        for (int i = 0; i < a.length; i++)
            System.out.println(a[i]);
    }
 
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        String content = input.useDelimiter("\\Z").next();
        String[] words = content.replaceAll("[^a-zA-Z ]", " ").toLowerCase().split("\\s+");
        Quick.sort(words);
        show(words);
    }
}
