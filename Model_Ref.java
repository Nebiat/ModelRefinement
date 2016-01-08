import java.util.*;

//import fi.helsinki.ltdk.csbl.anduril.component.CommandFile;
//import fi.helsinki.ltdk.csbl.anduril.component.ErrorCode;
//import fi.helsinki.ltdk.csbl.anduril.component.SkeletonComponent;
//import fi.helsinki.ltdk.csbl.asser.io.CSVParser;

public class Model_Ref
{
  public static void main(String[] args)
  {
	  
	  String sbmlfile="";
	  String csvFile="";
	  String outputfile="";
      Scanner input = new Scanner(System.in);
	/*  System.out.println("Enter the original model name: ");
	  sbmlfile = input.nextLine();
      
	  System.out.println("Enter the Refinement relation file name (csv format): ");
	  csvFile = input.nextLine();
      
	  System.out.println("Enter the output file name for the refined model: ");
	  outputfile = input.nextLine();
      
	  */

	  sbmlfile = "D:\\LotkaVolterra.cps";
	  csvFile = "D:\\Ref_Relations_LV.csv";
	  outputfile = "Refined_LV";
	  
	//  sbmlfile = "D:\\HSRbasic.cps";
	//  csvFile = "D:\\Ref_Relations_HSR.csv";
	//  outputfile = "Refined_HSR";
	  
	//  sbmlfile = "D:\\ErbBinitial.cps";
	//  csvFile = "D:\\Ref_Relations_ErbB.csv";
	//  outputfile = "Refined_ErbB";

	  
	  if (args.length>0)
	  {
	     sbmlfile = args[0];
	     csvFile = args[1];
	     outputfile=args[2];
	  }
	  
	  //... Read model
	  //  input.close();
	  //model=sb.ReadModel(sbmlfile + ".xml");
	  
	  //... Read species from the model
	  
	  //species = sb.Read_Metabolites(model);
	  
	  //... Create a Hash Map with the species as keys
	  
	 RefinementClass rf = new RefinementClass();
	 	      
	  //... Assign refinement parameters      
	  //  rf.Assign_RefParameters(csvFile + ".csv", map);   
	  //... Create refined model
      
      rf.RefineModel(sbmlfile,csvFile, outputfile);
      input.close();
  }
}
