import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;


public class ExportLang {
	static Properties propHU=new Properties();
	static Properties propEN=new Properties();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Random random=new Random(System.currentTimeMillis());
		System.out.println(random.nextDouble());
		/*
		importFile();
		//exportFile();
		LinkedHashMap<String, String> values=new LinkedHashMap<String, String>();
		
		Properties props=new Properties();
		
		try {
			Scanner sc=new Scanner(new File("D:\\java\\install\\c2\\photospot\\WebContent\\WEB-INF\\i3-label.properties"));
			sc.useDelimiter("[=]");
			while(sc.hasNext()){
				String token=sc.next();
				System.out.print("|"+token+"|");
				try{
					sc.skip(".*");
				}catch(NoSuchElementException e){}
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub

		 */

	}
	public static void importFile(){
		try {
			BufferedReader read=new BufferedReader(new InputStreamReader(new FileInputStream("D:\\java\\install\\c2\\photospot\\WebContent\\WEB-INF\\texts.csv"),"ISO-8859-2"));
			String input=null;
			while((input=read.readLine())!=null){
				String [] result=input.split(";");
				System.out.println(result[0]+"|"+result[1]+"|"+result[2]);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void exportFile(){
		FileOutputStream os=null;
		try {
			os=new FileOutputStream("D:\\java\\install\\c2\\photospot\\WebContent\\WEB-INF\\texts.csv");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			propEN.load(new InputStreamReader(new FileInputStream("D:\\java\\install\\c2\\photospot\\WebContent\\WEB-INF\\i3-label.properties"),"UTF-8"));
			propHU.load(new InputStreamReader(new FileInputStream("D:\\java\\install\\c2\\photospot\\WebContent\\WEB-INF\\i3-label_hu.properties"),"UTF-8"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Set<String> keys=(Set)propHU.keySet();
		for (String key : keys) {
			try {
				os.write((key+";").getBytes());
				System.out.println(propHU.getProperty(key));
				os.write(propHU.getProperty(key).getBytes());
				os.write(";".getBytes());
				os.write((propEN.getProperty(key)==null?"":propEN.getProperty(key)+";\r\n").getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		try {
			os.flush();
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
