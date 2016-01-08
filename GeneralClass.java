import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.COPASI.CModelValue;

public class GeneralClass {

	public int Calculate_Factorial(int n) {
        int fact = 1; 
        for (int i = 1; i <= n; i++) {
            fact *= i;
        }
        return fact;
    }
	
	public double Calculate_Binomial_Coefficient(int[] x, int[] y)
	{
		int index;
		double x_fac=1;
		double y_fac=1;
		for (index=0;index<x.length;index++)
		{
			x_fac=x_fac * Calculate_Factorial(x[index]);
		}
		for (index=0;index<y.length;index++)
		{
		y_fac=y_fac * Calculate_Factorial(y[index]);
		}
		return x_fac/y_fac;
		
	}
	
	public double Sum_of_rates(double[] rates)
	{
		double sum=0;
		for (int m=0;m<rates.length;m++)
     	{
     		sum=sum+rates[m];
     	}
		return sum;
	}

	public int SearchKeyIndex(CModelValue[] cmod, String par)
	{
		int index=0;
		int ind=par.indexOf('_');
		String s=par.substring(ind+1,par.length()); 
		for (int glbq=0;glbq<cmod.length;glbq++)
  	    {
		    if (cmod[glbq].getKey()==par)
			     index= Integer.parseInt(s);
  	    }
		 index= Integer.parseInt(s);
		 return index;
	}
	
	public int[] ReArrangeMulp(String[] prod, Map<String, List<String>> map,int[] mulp)
	 {
		List <String> prod_subsp;
		List<Integer> list1 = new ArrayList<Integer>();
		List<Integer> list2 = new ArrayList<Integer>();
	                 	
		for (int p=0;  p<prod.length;p++)
		{ 
			prod_subsp=map.get(prod[p]);
			if (prod_subsp!=null)
			  list1.add(mulp[p]);
			  
			else
			 list2.add(mulp[p]);
		}
		
		list2.addAll(list1);
		
		int[] ret = new int[list2.size()];       
		Iterator<Integer> iter = list2.iterator();
		for (int i=0; iter.hasNext(); i++) {       
		    ret[i] = iter.next();                
		}                                        
		return ret;  	      
	}
	
  	public String[] ReArrange(String[] prod, Map<String, List<String>> map)
 	 {
 		List <String> prod_subsp;
 		List<String> list1 = new ArrayList<String>();
		List<String> list2 = new ArrayList<String>();
	                 	
		for (int p=0;  p<prod.length;p++)
		{ 
			prod_subsp=map.get(prod[p]);
			if (prod_subsp!=null)
			{  
				  list1.add(prod[p]);
			}	  
			else
			{ 
 				 list2.add(prod[p]);
			}
		}
		
		list2.addAll(list1);
		String[] p = new String[list2.size()];	
		return list2.toArray(p);      
	}
	
  	
	public int[][] Assign_Coeffcients(int r,int c)
	{
     	 //int [][] coflist = new int [(r+1 )* c][c];
     	int [][] coflist = new int [(r * c)][c];
    	 int value=r;
    	 int rowsum=r;
    	 int prevsum=0;
    	 /*if (r==1)
    	 {
    		 for(int col=0;col<coflist[0].length;col++)
        	 {
        		 for(int row=0;row<coflist.length;row++)
        			    coflist[row][col]=value;
        	 }
         }
    	 else
    	 {
    	 */
    	 for(int col=0;col<coflist[0].length;col++)
    	 {
    		 if (col>0)
    		 {
    			 for(int row=0;row<coflist.length;row++) 
        	        {
    				 prevsum=0;
    				     for(int colindex=col;colindex>=0;colindex--)
    					 {
    					      prevsum=prevsum+coflist[row][colindex];
    					 } 
    				     
    				   //  if (prevsum<0 && rowsum==1) coflist[row][col]=1; 
    				   //  else if (prevsum<0) coflist[row][col]=rowsum+prevsum;
    				   //  else
    				    	 
    				    	 if (prevsum<0 && coflist[row][col-1]<=0) coflist[row][col]=coflist[row][col-1]+1;
    				    	 else if (prevsum<=0 && coflist[row][col-1]==rowsum) coflist[row][col]=0; 
    				    	 else coflist[row][col]=rowsum - prevsum;
        	        }
    		 }
    		 else
    		 {
    	        for(int row=0;row<coflist.length;row++)
    	        {
    			  coflist[row][col]=value;
    			//  if (value!=1) 
    				  value=value-1;
    				  //if (value<0) value=0;
    	        }
    		 }		 
    	 }
    	 
         return coflist;
	}	
}
