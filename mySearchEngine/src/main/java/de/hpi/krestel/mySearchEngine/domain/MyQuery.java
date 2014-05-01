package de.hpi.krestel.mySearchEngine.domain;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class MyQuery {

	private String booleanOp; //[And] or [OR] or [BUTNOT]
	private ArrayList<Term> termArrayList; //[Term1, Term2, ...]
	private String inputQuery; //[fish* and *tropical]
	
	//constructor
	public MyQuery(String inputQuery){
		this.booleanOp = "";
		this.termArrayList = new ArrayList<Term>();
		this.inputQuery = inputQuery;
	}
	
	//set booleanOP
	public void setBooleanOp(String booleanOp){
		this.booleanOp = booleanOp;
	}
	
	//get BooleanOp
	public String getBooleanOp(){
		return booleanOp;
	}
	
	//add Term to termArrayList
	public void addToTermArrayList(Term term){
		termArrayList.add(term);
	}
	
	//get termArrayList
	public ArrayList<Term> getTermArrayList(){
		return termArrayList;
	}
	
	public void processQuery(){

		ArrayList<String> stringList = new ArrayList<String>();
        StringTokenizer queryTokenizer = new StringTokenizer(this.inputQuery," ");
        
        while (queryTokenizer.hasMoreTokens()){
            String token = queryTokenizer.nextToken();
            stringList.add(token);
        }
        
        //in case of "but not", [fish, but, not, tropical] -> [fish, butnot, tropical]
        for(int i = 0;i<stringList.size()-1;i++){
        	String currString = stringList.get(i); //[BUT]
        	String nextString = stringList.get(i+1); //[NOT]
        	
        	if(currString.toLowerCase().equals("but") && nextString.toLowerCase().equals("not")){
        		stringList.remove(i+1);
        		stringList.set(i, "butnot");
        	}
        }
        
        //now stringList = [fish*, and, *tropical] or [fish*, butnot, *tropical]
        
        String andOp = "and";
        String orOp = "or";
        String butnotOP = "butnot";
        
        String operatorString = stringList.get(1); //[and]
        
        if(operatorString.toLowerCase().equals(andOp)){
        	this.setBooleanOp("AND");
        }else if(operatorString.toLowerCase().equals(orOp)){
        	this.setBooleanOp("OR");
        }else if(operatorString.toLowerCase().equals(butnotOP)){
        	this.setBooleanOp("BUTNOT");
        }
        
        Term term1 = new Term(stringList.get(0)); //[fish*]
        term1.processInputText();
        Term term2 = new Term(stringList.get(2)); //[*tropical]
        term2.processInputText();
        
        this.addToTermArrayList(term1);
        this.addToTermArrayList(term2);
        
        //now termArrayList = [ term1, term2 ]

	}
	
}
