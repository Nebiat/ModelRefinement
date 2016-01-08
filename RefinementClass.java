import java.util.*;

import org.COPASI.*;

import java.io.*;

//import com.opencsv.CSVReader;


public class RefinementClass {

	SBMLClass sb= new SBMLClass();
	GeneralClass gn=new GeneralClass();
	
	public Map<String, List<String>> Create_Hashmap(Map<String, List<String>> map, CMetab[] species)
	{	  
		 for (int i = 0;i < species.length;++i)
         {
			 map.put(species[i].getObjectName(), null);
			 
         }
		 return map;
	}
	
	public void Assign_RefParameters(String csvFile, Map<String, List<String>> map)//,String sp, List<String> subsp)
	{
		
		BufferedReader br = null;
		String line = "";
		String splitBy = ",";
		
		
	try {
	 /*
			CSVReader reader = new CSVReader(new FileReader(csvFile));
			String [] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				System.out.println("oo" + nextLine[1]);
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
			/*CSVParser reader = new CSVParser();
			String[] nextline= reader.parseLine(csvFile);
			
			
			 for (CSVRecord record : parser) {
				     System.out.printf("%s\t%s\n",
				       record.get("COL1"),
				       record.get("COL2"));

				   }
*/
			br = new BufferedReader(new FileReader(csvFile));
			
			while ((line = br.readLine()) != null) 
				{
					// use comma as separator
				String[] sp = line.split(splitBy);
				    
					// copy sub species
					List<String> subsp_Set = new ArrayList<String>();			
					for (int i = 1 ; i < sp.length; i++ ) {
				    	  subsp_Set.add(sp[i]);
				      }	
					if (!map.isEmpty())
					{
						if (map.containsKey(sp[0]))
						{
							map.put(sp[0], subsp_Set);
						}
						
					}	 
				}
			
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
	}
	
	public int Return_MetSize(Map<String, List<String>> map)
	{
		int metsize=0;
		for (Map.Entry<String, List<String>> entry : map.entrySet()) 
		{
            List<String> values = entry.getValue();
            if (values!=null)
            	metsize=metsize + values.size();
            else
            	metsize=metsize+1;
            
        }
		return metsize;
	}


	public int[] Assign_NewRx_Coeff(CChemEq chemEq)
	{
		int[] newrxsubmulp={};
		if (chemEq.getSubstrates().size()>0)
	    { 
			newrxsubmulp=new int[(int)chemEq.getSubstrates().size()];				    
			for(int x=0; x<chemEq.getSubstrates().size();x++) // loop thru the substrates for one specific reaction
			{
				newrxsubmulp[x]= (int) chemEq.getSubstrate(x).getMultiplicity();
			}
	    }
		return newrxsubmulp;
	}
	
	public void Assign_Parameters(Vector<CReaction> newrx,int rxnum, CModel refmodel,double[][] sub_rates_each)
	{
  	  int rxncount=0;	  
  	  for (int i=0; i<sub_rates_each.length;i++)  
      {
  		  for (int k=0;k<sub_rates_each[i].length;k++)
  		  {
  			//System.out.print(newrx.get(rxncount).getObjectDisplayName());
  			CModelValue cm = refmodel.createModelValue("k" + rxnum + i + k,sub_rates_each[i][k]);
  			CReaction new_reaction = newrx.get(rxncount); //refmodel.getReaction(rxncount); 
  			//System.out.print(new_reaction.getObjectDisplayName());
//  			System.out.print(refmodel.getNumReactions());
  			//System.out.print(new_reaction.getParameterMapping("k1"));
  			new_reaction.setParameterValue(new_reaction.getParameterMapping("k1").toString(), sub_rates_each[i][k]);
  			new_reaction.setParameterMapping(0, cm.getKey());
  			rxncount++;
  		  }
      }
  	}
	
	public void RefineModel(String filename,String csvFile,String outputfile)
	{

		  CModel oldmodel;
		  CMetab[] species;
		  
		  oldmodel=sb.ReadModel(filename);
		  
		  //... Read species from the model
		  
		  species = sb.Read_Metabolites(oldmodel);
		  
		//... Create a Hash Map with the species as keys
		  
		  Map<String, List<String>> map =new HashMap<String, List<String>>(); 
		  Create_Hashmap(map,species); 

		  //... Assign refinement parameters      
		  
		  Assign_RefParameters(csvFile , map);
		  
		  assert CCopasiRootContainer.getRoot() != null;
	      // create a new datamodel
	      CCopasiDataModel refinedmodel = CCopasiRootContainer.addDatamodel();
	      assert CCopasiRootContainer.getDatamodelList().size() == 1;
	      
	      CModel refmodel= refinedmodel.getModel();
	      assert refmodel != null;
	          
	      // Assign the model details to the new refined model
	          
	      refmodel.setTimeUnit(oldmodel.getTimeUnitName());
	      refmodel.setQuantityUnit(oldmodel.getQuantityUnitName());
	      refmodel.setVolumeUnit(oldmodel.getVolumeUnitName());
	      
	      // Vector for keeping set of changed initial values
	      ObjectStdVector changedObjects=new ObjectStdVector();

	      // Assign compartments to the refined model
	                   
	      CCompartment new_compartment= null;
	      int compsize = (int)oldmodel.getNumCompartments();
	      for (int compindex = 0;compindex < compsize;++compindex)
	      {
	       	  CCompartment old_compartment = oldmodel.getCompartment(compindex);
	       	  new_compartment = refmodel.createCompartment(old_compartment.getObjectName(), oldmodel.getCompartments().size()); 	
	       	  CCopasiObject object = new_compartment.getObject(new CCopasiObjectName("Reference=InitialVolume"));
	       	  assert object != null;
	       	  changedObjects.add(object);
	          assert new_compartment != null;
	      }
	      assert refmodel.getCompartments().size()==compsize;
	      
	      // Assign species and sub species from the old model and the refinement relation to the refined model
	          
	      int oldmetsize = (int) oldmodel.getNumMetabs();
	      CMetab[] met = new CMetab[Return_MetSize(map)]; // the new list of metabolites including the sub species
	      List <String> submet;
	      CCopasiObject object;
	      int j=0;
	          
	          for (int metindex = 0;metindex < oldmetsize;++metindex)
	          {
	        	  CMetab oldmet=oldmodel.getMetabolite(metindex);
	        	  submet=map.get(oldmet.getObjectName());
	        	  if (submet!=null)
	        	  {
	        		 for (int k=0; k<submet.size();k++)
	        		 {   
	        			 met[j]=refmodel.createMetabolite(submet.get(k),new_compartment.getObjectName(),(oldmet.getInitialConcentration()/submet.size()));
	        			 object = met[j].getObject(new CCopasiObjectName("Reference=InitialConcentration"));
	        			 assert object != null;
	        			 changedObjects.add(object);
	        			 assert (new_compartment!=null);
	        			 assert met[j] != null;
	        			 j++;
	        		 }
	        	  }
	        	  else
	        	  {
	        		  met[j]=refmodel.createMetabolite(oldmet.getObjectName(),new_compartment.getObjectName(),oldmet.getInitialConcentration());
	        		  object = met[j].getObject(new CCopasiObjectName("Reference=InitialConcentration"));
        			  assert object != null;
        			  changedObjects.add(object);
        			  assert met[j] != null;
        			  j++;
	        	  }
	          }
	          assert refmodel.getMetabolites().size() == j;
	          
	          
	         ///////  ------- get the list of reactions from the old model -------///////
	          for (int rxnum=0; rxnum<oldmodel.getReactions().size();rxnum++)
	          {
	        	  Vector<CReaction> newrx = new Vector<CReaction>();
	        	  CReaction old_reaction = oldmodel.getReaction(rxnum);
	        	  CChemEq oldchemEq = old_reaction.getChemEq();
        		  int oldSubmulp=1;
        		  int oldProdmulp=1;
        		  int subrxnum=1;
        		  int prorxnum=1;
        		  
        		  List <String> submets = null;
        		  List <String> submetp=null;
        		  GeneralClass gn = new GeneralClass();
        		  if (oldchemEq.getSubstrates().size()>0)
        		  {
        			  for(int s=0; s<oldchemEq.getSubstrates().size();s++) // loop thru the substrates for one specific reaction
 	        	      {
        			    submets = map.get(oldchemEq.getSubstrate(s).getMetabolite().getObjectName());
        			    oldSubmulp= (int) oldchemEq.getSubstrate(s).getMultiplicity();
       				    if (submets!=null) 
           				 // subrxnum=1; 
           			    //else 
           				  subrxnum = subrxnum * gn.Calculate_Factorial(submets.size()+oldSubmulp-1)/(gn.Calculate_Factorial(oldSubmulp) * gn.Calculate_Factorial(submets.size()-1));
 	        	      }
        		  }
        		  if (oldchemEq.getProducts().size()>0)
        		  {
        			  for(int p=0; p<oldchemEq.getProducts().size();p++)
  	        	      {
        			     submetp=map.get(oldchemEq.getProduct(p).getMetabolite().getObjectName());
        			     oldProdmulp= (int) oldchemEq.getProduct(p).getMultiplicity();
            		     if (submetp!=null) 
        				   //prorxnum=1; 
        			     //else 
        			       prorxnum = prorxnum * gn.Calculate_Factorial(submetp.size()+oldProdmulp-1)/(gn.Calculate_Factorial(oldProdmulp) * gn.Calculate_Factorial(submetp.size()-1));
  	        	      }
        		  }
    			  if (subrxnum==0)
    				  subrxnum=1;
    			  if (prorxnum==0)
    				  prorxnum=1;
    			//  int newrxnum=0;
    			//  newrxnum=subrxnum*prorxnum;
    			  
				  String[] sublist={};
				  String[] prodlist = {};
				  int[] submulp={}; ///// ---- Left and Right st. coefficients of the original reations
				  int[] prodmulp={};
				  
				  if (oldchemEq.getSubstrates().size()>0)
				  {
					  sublist=new String[(int) oldchemEq.getSubstrates().size()];
					  submulp=new int[(int) oldchemEq.getSubstrates().size()];
				      for(int s=0; s<oldchemEq.getSubstrates().size();s++) // loop thru the substrates for one specific reaction
	        	      {
					     sublist[s]=oldchemEq.getSubstrate(s).getMetabolite().getObjectName();
					     submulp[s]= (int) oldchemEq.getSubstrate(s).getMultiplicity();
					  }
				  }
				  if (oldchemEq.getProducts().size()>0)
				  {
					  prodlist=new String[(int)oldchemEq.getProducts().size()];
					  prodmulp=new int[(int)oldchemEq.getProducts().size()];
				      for(int p=0; p<oldchemEq.getProducts().size();p++)
	        	      {
					      prodlist[p]=oldchemEq.getProduct(p).getMetabolite().getObjectName();
					      prodmulp[p]=(int) oldchemEq.getProduct(p).getMultiplicity();
	        	      }
				  }
				  int submsindex=0;
				  int submpindex=0;
				  int srow=0; 
				  int prow=0;
				  int added=0; //int added2=0;
				  double[] sub_rates;
				  //double sub_rates_sum=0.0;
				 // int glbindex=0; //////....... index for the model values ......./////
				  int[][] subcoflist;
				  int[][] prodcoflist;
				  int[] newrxsubmulp={}; // ---- Left and Right st. coefficients of the refined reactions
				//  int[] newrxprodmulp={};
				  //int newstindex=0; // ---- loop index for st. coef of refined reactions
				  
				  
				  /* 
				  List <String> sub_subsp=map.get(sublist[0]);
		          CReaction new_reaction= refmodel.createReaction(old_reaction.getObjectName());
		          CChemEq chemEq = new_reaction.getChemEq();
	        	  if(sub_subsp!=null)
		             new_reaction.addSubstrate(sb.Get_MetabKey(met,sub_subsp.get(0)), 1);
	        	  else
	        		  new_reaction.addSubstrate(sb.Get_MetabKey(met,sublist[0]), 1);
		    	  new_reaction.addProduct(met[1].getKey(),2);
		          }*/ 
				  
				  
				  double sub_rates_each[][]=new double[subrxnum][prorxnum];
				  for (int sr=0; sr<subrxnum;sr++) // loop for new substrate reaction
				  {
					  prow=0;
					  added=0; //added2=0;
					  sub_rates=new double[prorxnum];
					  for(int pr=0; pr<prorxnum;pr++) //loop for new product reaction
					  {
						
					    CReaction new_reaction= refmodel.createReaction(old_reaction.getObjectName()+rxnum+sr+pr);
					    
					    //newrx[newrx.length+1]=new_reaction;
					    //System.out.print(newrx.length+1);
					    CChemEq chemEq = new_reaction.getChemEq();
					    /////---- Assigning functions to reactions -----///////
					    new_reaction.setFunction(old_reaction.getFunction().getObjectName());
					    new_reaction.setReversible(old_reaction.isReversible());
					    /////-----Assigning parameters----/////
					  					    
					    assert new_reaction != null;
					  
					    newrx.addElement(new_reaction);
					    
					    int flag=0;
					    int prodadded=0;
					    List <String> sub_subsp;
					    List <String> prod_subsp;
					  
					    for(int s=0;s<sublist.length;s++)
	        	        {
					      added=0; 
	        	          if (s>0)prodadded=1;       //s>0 means list of other products are already added 
					      sub_subsp=map.get(sublist[s]);					         
					      if (sub_subsp!=null )
					      {
					    	  	  if (submsindex==sub_subsp.size()) submsindex=0;
						          if (submulp[s]==1)
						          {
						        	  
						        	//  chemEq.addMetabolite(sb.Get_MetabKey(met,sub_subsp.get(submsindex)),submulp[s],CChemEq.SUBSTRATE); 
						        	  new_reaction.addSubstrate(sb.Get_MetabKey(met,sub_subsp.get(submsindex)), submulp[s]);
						          }
						          else // if substrate st. coeff is greater than 1
						          {
						        	     subcoflist=gn.Assign_Coeffcients(submulp[s],sub_subsp.size());
						        		 if(srow<subcoflist.length)
							        	 {
						        			 for(int col=0;col<subcoflist[srow].length;col++)
							        			{
							        				 if (subcoflist[srow][col]>0)
									        		 {
							        					 //chemEq.addMetabolite(sb.Get_MetabKey(met,sub_subsp.get(col)),subcoflist[srow][col],CChemEq.SUBSTRATE);
							        					 new_reaction.addSubstrate(sb.Get_MetabKey(met,sub_subsp.get(col)),subcoflist[srow][col]);
									        		 }
					        		            } 
						                 }
					              }
					      }
					      else
					      {
					     	  //chemEq.addMetabolite(sb.Get_MetabKey(met,sublist[s]),submulp[s],CChemEq.SUBSTRATE);
					    	  new_reaction.addSubstrate(sb.Get_MetabKey(met,sublist[s]),submulp[s]);
					      } 
					      if (s>0 && prodlist.length== 1) break;
					      if (s>0 && flag==1) break;
					      //if (p==prodlist.length && s>0) break;
					      prodmulp=gn.ReArrangeMulp(prodlist, map,prodmulp);
					      prodlist=gn.ReArrange(prodlist, map);
					     
					      /////prodmulp=prodmulp;
						  for (int p=0; p<prodlist.length;p++)
						  { // submpindex=0; //????? check why you needed it here  ???????
							  prod_subsp=map.get(prodlist[p]);
							  
							
							  if (prod_subsp!=null && prodadded==0)
							  { 
								  if (submpindex==prod_subsp.size()) submpindex=0;	
								
							      if (prodmulp[p]==1 && added==0)
							      {
									 	// chemEq.addMetabolite(sb.Get_MetabKey(met,prod_subsp.get(submpindex)),prodmulp[p],CChemEq.PRODUCT);
							    	  new_reaction.addProduct(sb.Get_MetabKey(met,prod_subsp.get(submpindex)),prodmulp[p]);
									 	 added=1;
									 	// f=1;
							      }
							      //else if (prodmulp[p]==1) // if product has sub species but the st. coeff is one
							      //{
							    	//	  chemEq.addMetabolite(sb.Get_MetabKey(met,prod_subsp.get(submpindex)),prodmulp[p],CChemEq.PRODUCT);
							      //}
							      else  // if product st. coeff is greater than 1
							      {
							    	  if (flag==0)
							    	  {                  
							    	     prodcoflist=gn.Assign_Coeffcients(prodmulp[p],prod_subsp.size());
						        		   if(prow<prodcoflist.length)
							        	   { 
						        			  for(int col=0;col<prodcoflist[prow].length;col++)
							        			 {
							        				 if (prodcoflist[prow][col]>0)
									        		 {
							        					 //chemEq.addMetabolite(sb.Get_MetabKey(met,prod_subsp.get(col)),prodcoflist[prow][col],CChemEq.PRODUCT);
							        					 new_reaction.addProduct(sb.Get_MetabKey(met,prod_subsp.get(col)),prodcoflist[prow][col]);
									        		 }
							        			  } 
						                   }
							            flag=1;
							          }
							      }
							      submpindex++;
							 }
							 else
							 {								
								 if (added==0 && prodadded==0)
								 {
								   //chemEq.addMetabolite(sb.Get_MetabKey(met,prodlist[p]),prodmulp[p],CChemEq.PRODUCT);
									 new_reaction.addProduct(sb.Get_MetabKey(met,prodlist[p]),prodmulp[p]);

									 //  added=1;
								   //f=-1;
								   //prodmulp[p]=0;
								 }
								 if (sublist.length>1 && prodlist.length==1) added=1;
								 
							 }
						  }						
					  }
					    
					    
					  prow++;					  			
					  newrxsubmulp=Assign_NewRx_Coeff(chemEq);
				  
					  }   /////////////..... end of loop for new product reaction .......////////////////// 
		
					  submsindex++;   
					  srow++;
					  
					//----- calculate fit preserving criteria------//

				     	double bin_coef=gn.Calculate_Binomial_Coefficient(submulp, newrxsubmulp);
				     	for (int ind=0; ind<sub_rates.length;ind++)
				     		sub_rates_each[sr][ind]= (bin_coef * old_reaction.getParameterValue("k1"))/sub_rates.length;
				     	
				     	// Assign for each refined reaction...
				     //	new_reaction.setParameterMapping(0, modelValue[glbindex].getKey());
				     	
				  } //////////...... end of loop for new substrate reaction ....... ///////
				  
				      Assign_Parameters(newrx,rxnum,refmodel,sub_rates_each);
	          } // end of the old reaction loop
	          
	         
	          refmodel.compileIfNecessary();
	          refmodel.updateInitialValues(changedObjects);
	       //   CCopasiDataModel newdatamodel = CCopasiRootContainer.addDatamodel();
           //  String modname="ZTestModel";
	          try
	          {
/*
	              CModel newmodel= newdatamodel.getModel();
	              CCompartment compartment = newmodel.createCompartment("cell");
	        	  CMetab met1=newmodel.createMetabolite("sp1", compartment.getObjectName(),10,CMetab.REACTIONS);
	        	  CMetab met2=newmodel.createMetabolite("sp2", compartment.getObjectName(),20,CMetab.REACTIONS);
	        	  CReaction cr=newmodel.createReaction("reaction1");
	        	  CChemEq cq=cr.getChemEq();
	        	  cq.addMetabolite(met1.getKey(),1.0, CChemEq.SUBSTRATE);
	        	  cq.addMetabolite(met2.getKey(),1.0, CChemEq.PRODUCT);
	        	  
	        	  //cr.setSBMLId("reaction_sbml");
	        	  //cr.addSubstrate(sp1.getKey());
	        	  //cr.addProduct(sp2.getKey());
	        	  newdatamodel.saveModel("D:" + "\\" + modname + ".cps", true);
	*/	          
	        	  refinedmodel.saveModel("D:" + "\\" + outputfile + ".cps", true);
		          System.out.println("Refinement completed.");
	          }
	          catch(java.lang.Exception ex)
		      {
	        	  System.err.println("Error. Exporting the model to COPASI failed.");
	          }

	          try
	          {
	//      	  newdatamodel.exportSBML("D:" + "\\" + modname + ".xml",true, 2, 3);
	        	  refinedmodel.exportSBML("D:" + "\\" + outputfile + ".xml", true);
	          }
	          catch(java.lang.Exception ex)
	          {
	             System.err.println("Error. Exporting the model to SBML failed.");
	          }
	}
	
	
	public void Read_Hashmap(Map<String, List<String>> map)
	{
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				String key = entry.getKey();
	            List<String> values = entry.getValue();
	            System.out.println("Key = " + key);
	            System.out.println("Values = " + values);
	            if (values!=null)
	            {
	            	System.out.println(values.size());
	            }
	            
	        }
	}

}
