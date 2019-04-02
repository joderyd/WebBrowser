// Jonathan Oderyd, oderyd@kth.se, CMETE3, Programutvecklingsteknik
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.html.*;
import javax.swing.text.*;


public class WebBrowser extends JFrame implements ActionListener{
	private WebReader webReader;
	private JTable table = new JTable(50,2);
	private String[] header = {"WEBBADRESS", "BENAMNING"};
	private JTextField text = new JTextField();
		WebBrowser(String name){
			super(name);
			setSize(900, 600);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			setLayout(new BorderLayout());
			Container container = getContentPane();
			JScrollPane scrollTable = new JScrollPane(table);
			webReader = new WebReader();
			text.addActionListener(this);
  			JScrollPane webReaderPane = new JScrollPane(webReader);
			container.add(text, BorderLayout.NORTH);
			container.add(webReaderPane, BorderLayout.CENTER);
			container.add(scrollTable, BorderLayout.EAST);
			setVisible(true);
		}
		  
		WebBrowser(String name, String startPage){
			this(name);
			text.setText(startPage); //does not load page, only puts the adress in textfield
		}
		  
		
	public void actionPerformed(ActionEvent e) {
		if(webReader.showPage(text.getText())){
			getLinks(text.getText());
			System.out.println("current webpage= " + text.getText());
		}
	}

	public void getLinks(String webpage) {
		String adress = webpage;
		String[][] matrix = new String[50][2];
		try {
			InputStream in = new URL(adress).openConnection().getInputStream();
			InputStreamReader reader = new InputStreamReader(in);
			HTMLEditorKit htmlKit = new HTMLEditorKit();
			HTMLDocument doc = (HTMLDocument)htmlKit.createDefaultDocument();
			doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
			htmlKit.read(reader, doc, 0); //reads html code to a HTMLdocument
			int counter = 0;
				
			for(HTMLDocument.Iterator iterator = doc.getIterator(HTML.Tag.A);
					iterator.isValid() && counter<50;  //isValid checks if there's another tag, not null
					iterator.next()) {
				AttributeSet attributes = iterator.getAttributes();
				String attributeString = (String)attributes.getAttribute(HTML.Attribute.HREF);
				if(!attributeString.startsWith("http://")) {
					attributeString = webpage.concat("/"+attributeString+"/");
				}
				matrix[counter][0] = attributeString;
				int startOffset = iterator.getStartOffset();
				int endOffset = iterator.getEndOffset();
				int length = endOffset - startOffset;
				String textString = doc.getText(startOffset, length);
				matrix[counter][1] = textString;
				counter = counter +1;
			}
		}
		catch(Throwable t) {
			JOptionPane.showMessageDialog(this, "Invalid Webadress!\n\n"+webpage+"\n\n Try to begin the adress with:\nhttp://www");
			t.printStackTrace();
		}
			
		table.setModel(new DefaultTableModel(matrix, header));
	}
		
		
	public static void main(String[] args) {
		String startPage = "http://www.nada.kth.se/~henrik"; //I assume this is a valid adress
		//String startPage = "http://www.google.com"; //I assume this is a valid adress
		new WebBrowser("Jonathan's WebBrowser", startPage);
	}

}



class WebReader extends JEditorPane{
	protected WebReader(){
		setEditable(false);
		setText("\n\nWelcome to Jonathan's WebBrowser!\n\nThis is the Default Home Screen");
	}

	public boolean showPage(String webaddr){  // returns true if webbpage can be found
		try{
			URL u = new URL(webaddr);
			HttpURLConnection huc =  (HttpURLConnection)u.openConnection(); 
			huc.setRequestMethod("GET"); 
			huc.connect(); 
			if(huc.getResponseMessage().equals("OK")) {
				setPage(webaddr);
			}
			else {
				setText("ERROR: " + huc.getResponseCode() + " " + huc.getResponseMessage());
			    JOptionPane.showMessageDialog(this,"Webpage can't be found!\n\nERROR: " + huc.getResponseCode() + " " + huc.getResponseMessage());
			    return false;
			}
			System.out.println("\nResponse Message: " + huc.getResponseMessage() + "\nResponse Code: " + huc.getResponseCode());
			return true;
		}
		catch(IOException e) {  // wrong with "http" or "www"
			if(webaddr.startsWith("http://www.")) {
				JOptionPane.showMessageDialog(this, "Could not find webpage!\n\nHost could not be found!");
			}
			else {
				JOptionPane.showMessageDialog(this, "Invalid Webadress:\n"+webaddr+"\n\n Try to begin the adress with:\nhttp://www.");
			}
			System.out.print(e);
			return false;
		}
		catch(Throwable t) {  // wrong with "//"
			JOptionPane.showMessageDialog(this, "Invalid Webadress:\n"+webaddr+"\n\n Try to begin the adress with:\nhttp://www.\n\n" 
					+ "tip: probably missing // between http: and www");
			//t.printStackTrace();
			return false;
		}
	}
}
