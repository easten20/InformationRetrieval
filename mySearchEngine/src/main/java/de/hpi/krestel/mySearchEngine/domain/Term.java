package de.hpi.krestel.mySearchEngine.domain;

public class Term {
	
	private String text;
	private StarOp starOp;	
	
	public Term(String text)
	{
		this.text = text;
		this.processInputText();
	}	
			
	public StarOp getStarOp()
	{
		return this.starOp;
	}
	
	public String getText()
	{
		return this.text;
	}	

	//[*high] -> [high] + [FRONTSTAR]
 	public void processInputText(){
 		if(this.text.contains("*")){ 			
 			char firstChar = this.text.charAt(0);
 			char lastChar = this.text.charAt(this.text.length()-1);
 			
 			if(firstChar == '*' && lastChar == '*'){
 				this.starOp = StarOp.BOTHSTAR;
 				this.text = this.text.substring(1, this.text.length()-1);
 			}else if(firstChar == '*'){
 				this.starOp = StarOp.FRONTSTAR;
 				this.text = this.text.substring(1, this.text.length());	 			
 			}else{
 				this.starOp = StarOp.ENDSTAR;
 				this.text = this.text.substring(0, this.text.length()-1); 				
 			}
 					
 		}else{
 			this.starOp = StarOp.NOSTAR; 			 		
 		}
 	}
 	
 	public boolean isRegularExpression() {
 		return this.starOp != StarOp.NOSTAR;
 	}
	
	private String getRegexRep()
	{
		String str;
		switch(this.starOp){
			case ENDSTAR:
				str = this.text + ".*";
				break;
			case FRONTSTAR:
				str = ".*"+ this.text;
				break;
			case BOTHSTAR:
				str = ".*"+ this.text + ".*";
				break;			
			default:
				str = this.text;
				break;
		}
		return str;
	}	
	
	public boolean isRegexMatch(String word){
		return word.matches(this.getRegexRep());
	}

	public boolean matches(String token) {
		if (this.isRegularExpression()) {
			return isRegexMatch(token);
		}
		return this.text.equals(token);
	}
		
}
