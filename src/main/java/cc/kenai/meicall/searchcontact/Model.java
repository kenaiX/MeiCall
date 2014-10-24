package cc.kenai.meicall.searchcontact;

public class Model{
	
	public String name;
	public String telnum;
	public String pyname;
	public String group;
	
	public Model(){
		
	}
	
	public Model(String name, String telnum){
		this.name = name;
		this.telnum = telnum;
		this.group = "";
		pyname = BaseUtil.getPingYin(name);
	}
}