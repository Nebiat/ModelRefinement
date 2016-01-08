import org.COPASI.CCopasiDataModel;
import org.COPASI.CCopasiRootContainer;
import org.COPASI.CMetab;
import org.COPASI.CModel;
import org.COPASI.CReaction;

public class SBMLClass {

	public CModel ReadModel(String filename)
	{
		assert CCopasiRootContainer.getRoot() != null;
	      // create a new datamodel
	      CCopasiDataModel dataModel = CCopasiRootContainer.addDatamodel();
	      assert CCopasiRootContainer.getDatamodelList().size() == 1;
	      try
	          {
	    	  // load the model
	    	      if (filename.matches(".*\\.sbml"))
	                  dataModel.importSBML(filename);
	    	      else if (filename.matches(".*\\.cps"))
	    	    		  dataModel.loadModel(filename);
	          }
	          catch (java.lang.Exception ex)
	          {
	              System.err.println( "Error while importing the model from file named \"" + filename + "\"." );
	              System.exit(1);
	          }
	          CModel model = dataModel.getModel();
	          assert model != null;
	          return model;
	}
	
	public CMetab[] Read_Metabolites(CModel model)
	{
	      int iMax = (int) model.getMetabolites().size();
         // System.out.println("Metabolites: ");
          CMetab[] metab=new CMetab[iMax];
          for (int i = 0;i < iMax;++i)
          {
              metab[i] = model.getMetabolite(i);
              assert metab != null;
           //   System.out.println("\t" + metab.getObjectName());
              
          }
          return metab;  
	}
		
	public void Show_Metabolites(CModel model)
	{
		int iMax = (int) model.getMetabolites().size();
	    System.out.println("Number of Metabolites: " + (new Integer(iMax)).toString());
	    System.out.println("Metabolites: ");
	    for (int i = 0;i < iMax;++i)
	    {
	      //  metab = model.getMetabolite(i);
	       // assert metab != null;
	        System.out.println("\t" + model.getMetabolite(i).getObjectName());
	        
	    }
	}
	
	public String Get_MetabKey(CMetab[] m, String spname)
	{
		String key="";
	      for (int i=0;i<m.length;++i)
	          {
	              if (m[i].getObjectName().equals(spname))
	            	  key=m[i].getKey();  	  
	          }
	      return key;
	}
	    
	public void Read_Reactions(CModel model)
	{
		 //  reactions
	     int iMax = (int)model.getReactions().size();
	     System.out.println("Number of Reactions: " + (new Integer(iMax)).toString());
	     System.out.println("Reactions: ");
	     for (int i = 0;i < iMax;++i)
	     {
	         CReaction reaction = model.getReaction(i);
	         assert reaction != null;
	         System.out.println("\t" + reaction.getObjectName());
	       //  System.out.println(reaction.getFunction());
	        		 //.getProducts().size());
	     }	

	}
	
/*	
	 public static void main(String[] args)
	 {     
	     // now we create a reaction
	     CReaction reaction = model.createReaction("hexokinase");
	     assert reaction != null;
	     assert model.getReactions().size() == 1;
	     // hexokinase converts glucose and ATP to glucose-6-phosphate and ADP
	     // we can set these on the chemical equation of the reaction
	     CChemEq chemEq = reaction.getChemEq();
	     // glucose is a substrate with stoichiometry 1
	     chemEq.addMetabolite(glucose.getKey(), 1.0, CChemEq.SUBSTRATE);
	     // ATP is a substrate with stoichiometry 1
	     chemEq.addMetabolite(atp.getKey(), 1.0, CChemEq.SUBSTRATE);
	     // glucose-6-phosphate is a product with stoichiometry 1
	     chemEq.addMetabolite(g6p.getKey(), 1.0, CChemEq.PRODUCT);
	     // ADP is a product with stoichiometry 1
	     chemEq.addMetabolite(adp.getKey(), 1.0, CChemEq.PRODUCT);
	     assert chemEq.getSubstrates().size() == 2;
	     assert chemEq.getProducts().size() == 2;
	     // this reaction is to be irreversible
	     reaction.setReversible(false);
	     assert reaction.isReversible() == false;
	     // now we ned to set a kinetic law on the reaction
	     // maybe constant flux would be OK
	     // we need to get the function from the function database
	     CFunctionDB funDB = CCopasiRootContainer.getFunctionList();
	     assert funDB != null;
	     // it should be in the list of suitable functions
	     // lets get all suitable functions for an irreversible reaction with  2 substrates
	     // and 2 products
	     CFunctionStdVector suitableFunctions = funDB.suitableFunctions(2, 2, COPASI.TriFalse);
	     assert suitableFunctions.size() > 0;
	     int i,iMax=(int)suitableFunctions.size();
	     for (i=0;i<iMax;++i)
	     {
	         // we just assume that the only suitable function with Constant in
	         // it's name is the one we want
	         if (suitableFunctions.get(i).getObjectName().indexOf("Constant") != -1)
	         {
	             break;
	         }
	     }
	     if (i != iMax)
	     {
	         // we set the function
	         // the method should be smart enough to associate the reaction entities
	         // with the correct function parameters
	         reaction.setFunction(suitableFunctions.get(i));
	         assert reaction.getFunction() != null;
	         // constant flux has only one function parameter
	         assert reaction.getFunctionParameters().size() == 1;
	         // so there should be only one entry in the parameter mapping as well
	         assert reaction.getParameterMappings().size() == 1;
	         CCopasiParameterGroup parameterGroup = reaction.getParameters();
	         assert parameterGroup.size() == 1;
	         CCopasiParameter parameter = parameterGroup.getParameter(0);
	         // make sure the parameter is a local parameter
	         assert reaction.isLocalParameter(parameter.getObjectName());
	         // now we set the value of the parameter to 0.5
	         assert parameter.getType() == CCopasiParameter.DOUBLE;
	         parameter.setDblValue(0.5);
	         object = parameter.getObject(new CCopasiObjectName("Reference=Value"));
	         assert object != null;
	         changedObjects.add(object);
	     }
	     else
	     {
	         System.err.println("Error. Could not find a kinetic law that conatins the term \"Constant\".");
	         System.exit(1);
	     }
	     // now we also create a separate reaction for the backwards reaction and
	     // set the kinetic law to irreversible mass action
	     // now we create a reaction
	     reaction = model.createReaction("hexokinase-backwards");
	     assert reaction != null;
	     assert model.getReactions().size() == 2;
	     chemEq = reaction.getChemEq();
	     // glucose is a product with stoichiometry 1
	     chemEq.addMetabolite(glucose.getKey(), 1.0, CChemEq.PRODUCT);
	     // ATP is a product with stoichiometry 1
	     chemEq.addMetabolite(atp.getKey(), 1.0, CChemEq.PRODUCT);
	     // glucose-6-phosphate is a substrate with stoichiometry 1
	     chemEq.addMetabolite(g6p.getKey(), 1.0, CChemEq.SUBSTRATE);
	     // ADP is a substrate with stoichiometry 1
	     chemEq.addMetabolite(adp.getKey(), 1.0, CChemEq.SUBSTRATE);
	     assert chemEq.getSubstrates().size() == 2;
	     assert chemEq.getProducts().size() == 2;
	     // this reaction is to be irreversible
	     reaction.setReversible(false);
	     assert reaction.isReversible() == false;
	     // now we ned to set a kinetic law on the reaction
	     CFunction massAction = (CFunction)funDB.findFunction("Mass action (irreversible)");
	     assert massAction != null;
	     // we set the function
	     // the method should be smart enough to associate the reaction entities
	     // with the correct function parameters
	     reaction.setFunction(massAction);
	     assert reaction.getFunction() != null;

	     assert reaction.getFunctionParameters().size() == 2;
	     // so there should be two entries in the parameter mapping as well
	     assert reaction.getParameterMappings().size() == 2;
	     // mass action is a special case since the parameter mappings for the
	     // substrates (and products) are in a vector

	     // Let us create a global parameter that is determined by an assignment
	     // and that is used as the rate constant of the mass action kinetics
	     // it gets the name rateConstant and an initial value of 1.56
	     CModelValue modelValue = model.createModelValue("rateConstant", 1.56);
	     assert modelValue != null;
	     object = modelValue.getObject(new CCopasiObjectName("Reference=InitialValue"));
	     assert object != null;
	     changedObjects.add(object);
	     assert model.getModelValues().size() == 1;
	     // set the status to assignment
	     modelValue.setStatus(CModelValue.ASSIGNMENT);
	     // the assignment does not have to make sense
	     modelValue.setExpression("1.0 / 4.0 + 2.0");

	     // now we have to adjust the parameter mapping in the reaction so
	     // that the kinetic law uses the global parameter we just created instead
	     // of the local one that is created by default
	     // The first parameter is the one for the rate constant, so we point it to
	     // the key of out model value
	     reaction.setParameterMapping(0, modelValue.getKey());
	     // now we have to set the parameter mapping for the substrates
	     reaction.addParameterMapping("substrate", g6p.getKey());
	     reaction.addParameterMapping("substrate", adp.getKey());

	     // finally compile the model
	     // compile needs to be done before updating all initial values for
	     // the model with the refresh sequence
	     model.compileIfNecessary();

	     // now that we are done building the model, we have to make sure all
	     // initial values are updated according to their dependencies
	     model.updateInitialValues(changedObjects);

	     // save the model to a COPASI file
	     // we save to a file named example1.cps 
	     // and we want to overwrite any existing file with the same name
	     // Default tasks are automatically generated and will always appear in cps
	     // file unless they are explicitley deleted before saving.
	     dataModel.saveModel("example1.cps", true);

	     // export the model to an SBML file
	     // we save to a file named example1.xml, we want to overwrite any
	     // existing file with the same name and we want SBML L2V3
	     try
	     {
	       dataModel.exportSBML("example1.xml", true, 2, 3);
	     }
	     catch(java.lang.Exception ex)
	     {
	        System.err.println("Error. Exporting the model to SBML failed.");
	     }
	 }
	
	*/
	
}	          
	               
	  /*        // compartments
	          int i, iMax = (int)model.getCompartments().size();
	          System.out.println("Number of Compartments: " + (new Integer(iMax)).toString());
	          System.out.println("Compartments: ");
	          for (i = 0;i < iMax;++i)
	          {
	              CCompartment compartment = model.getCompartment(i);
	              assert compartment != null;
	              System.out.println("\t" + compartment.getObjectName());
	          }
	    */      
	
		/*
			// metabolites
	          int iMax = (int) model.getMetabolites().size();
	          System.out.println("Number of Metabolites: " + (new Integer(iMax)).toString());
	          System.out.println("Metabolites: ");
	          CMetab metab=new CMetab();
	          for (int i = 0;i < iMax;++i)
	          {
	              metab = model.getMetabolite(i);
	              assert metab != null;
	              System.out.println("\t" + metab.getObjectName());
	              
	          }
		 */         
	//}
	
	

