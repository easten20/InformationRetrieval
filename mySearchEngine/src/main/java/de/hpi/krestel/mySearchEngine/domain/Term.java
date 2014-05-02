package de.hpi.krestel.mySearchEngine.domain;

public class Term {
	
	private String text; //[high]
	//private StarOp starOp; //[NOSTAR] or [FRONTSTAR] or [BOTHSTAR] or [ENDSTAR]
	private String starOp; //[NOSTAR] or [FRONTSTAR] or [BOTHSTAR] or [ENDSTAR]
	private String inputText; //[high] or [*high] or [high*] or [*high*]
	
	//constructor
	public Term(String inputText)
	{
		this.text = "";
		this.starOp = "";
		this.inputText = inputText;
	}
		
	//get starOp
	public String getStarOp()
	{
		return this.starOp;
	}
	
	//set starOp
	public void setStarOp(String starOp){
		this.starOp = starOp;
	}
	
	//get text
	public String getText(){
		return this.text;
	}
	
	//set text
	public void setText(String text){
		this.text = text;
	}
	
	//[*high] -> [high] + [FRONTSTAR]
	public void processInputText(){
		if(inputText.contains("*")){
			
			char firstChar = this.inputText.charAt(0);
			char lastChar = this.inputText.charAt(this.inputText.length()-1);
			
			if(firstChar == '*' && lastChar == '*'){
				this.setStarOp("BOTHSTAR");
				this.setText(this.inputText.substring(1, this.inputText.length()-1));
			}else if(firstChar == '*'){
				this.setStarOp("FRONTSTAR");
				this.setText(this.inputText.substring(1, this.inputText.length()));
			}else{
				this.setStarOp("ENDSTAR");
				this.setText(this.inputText.substring(0, this.inputText.length()-1));
			}
					
		}else{
			this.setStarOp("NOSTAR");
			this.setText(this.inputText);
		}
	}
	
}
