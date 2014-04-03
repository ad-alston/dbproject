public class HTMLFormatter{

	public static final String NEWLINE = "<br/>";

	/**
	 * Formats an HTML element with a given set of
	 * attributes.
	 */
	public static String formatElement(String tag, 
		String[] args, String content)
	{
		String lead = "<"+tag;
		if( args != null ){
			for(int i = 0; i < args.length; i+=2){
				lead = lead + " " + args[i] + "="
					+ args[i+1];
			}
		}
		lead = lead + ">";
		
		return lead + content + "</"+tag+">";
	}
	
}